# Client 与 Channel 上下文设计

本文定义 `user-auth` 登录、授权和商城协作中 Client、Channel 的概念、信任边界及后续实现方案。认证域与功能授权模型见 [DOMAIN.md](DOMAIN.md)；OAuth2/OIDC 协议实现见 [OAUTH2_OIDC_PROTOCOL_INFRASTRUCTURE.md](OAUTH2_OIDC_PROTOCOL_INFRASTRUCTURE.md)。

## 结论

`appId` 与 `channelCode` 是两个独立维度，不能相互替代：

| 概念 | 回答的问题 | 例子 | 所属边界 |
| --- | --- | --- | --- |
| appId | 哪个受管理的应用/调用方发起访问？ | `wx-mini-a`、`admin-console` | `user-auth` 认证接入管理 |
| platform | 该 ClientApp 的接入形态是什么？ | `WECHAT_MINI_PROGRAM`、`WEB` | ClientApp 的受管理属性 |
| channelCode | 此次业务访问归属哪个渠道？ | `DIRECT`、`PARTNER_A`、`MINI_PROGRAM_REVIEW` | 商城渠道目录由渠道/网关边界管理；user-auth 消费上下文并管理功能授权策略映射 |
| User | 谁在登录/被授权？ | `U100` | `user-auth` |

同一个 ClientApp 可以进入多个渠道；同一个渠道也可以由多个 ClientApp 进入。
```text
微信小程序 A
  ClientApp.appId = wx-mini-a
  ClientApp.platform = WECHAT_MINI_PROGRAM

同一个 appId 的不同业务进入上下文
  channelCode = DIRECT
  channelCode = PARTNER_A
  channelCode = PARTNER_B
  channelCode = MINI_PROGRAM_REVIEW
```

## ClientApp

### 模型与管理

ClientApp 是受管理的接入主体，建议至少包含：

```text
appId                 稳定业务标识，唯一
platform              接入形态
status                ENABLED / DISABLED
displayName           运维展示名称
```

`appId + platform` 必须在管理系统中登记；以全局唯一的 `appId` 为标识、将 `platform` 作为不可随意改写的属性。这样一次登录请求只需带 `appId` 与可选版本，服务端从 ClientApp 解析 `platform`，而非相信调用方提交的 platform。

`LoginSession.Client` 保存的是解析后的历史快照：`appId`、`platform`、`version`。ClientApp 后续被改名、停用或迁移时，历史会话仍能表达当时实际的接入形态。

### 内部服务

内部服务也分配 ClientApp，但 `platform` 表示调用载体而非操作系统：

| 调用方 | appId | platform |
| --- | --- | --- |
| 商城服务 | `mall-service` | `SERVICE` |
| 管理服务 | `admin-service` | `SERVICE` |
| 批处理任务 | `coupon-batch-job` | `JOB` |

服务的真实身份由 mTLS、workload identity、client credential 或服务 JWT 确认，再映射为 appId。若内部服务只是代理终端用户请求，`LoginSession.Client` 应保留最终用户客户端；中间服务身份进入网关与操作审计链路，不覆盖终端 Client。

## ChannelContext

### 信任边界

`channelCode` 来自调用方参数，但在到达 `user-auth` 之前，网关或入口协议切面必须完成签名、完整性、时效、重放保护、渠道状态和调用方绑定校验。校验通过后，传入的才是 `ChannelContext`。

```text
调用方请求(channelCode, signature, timestamp, nonce)
  -> 网关/入口协议校验
  -> 解析 ClientApp 与 ChannelContext
  -> user-auth 只消费已验证的 appId、channelCode、version
```

`user-auth` 不重复实现签名算法，但必须只接收受控网络/受保护调用链中产生的上下文，禁止公网调用方绕过网关直接伪造内部上下文。建议内部传递包含：

```text
clientAppId
clientVersion
channelCode
verifiedAt
requestId
```

当前 REST 入口使用以下网关注入 Header：`X-Client-App-Id`、`X-Client-Platform`、`X-Client-Version`、`X-Channel-Code`；认证后的请求还可由网关写入 `X-User-Id`。所有有业务参数的 user-auth Controller 接口必须将参数收敛为一个 `ClientRequest` 子类型；需要消费渠道上下文时继承 `ClientChannelRequest`，需要消费认证用户上下文时使用 `AuthenticatedRequest`。不得将业务字段拆为 path/query 参数后再额外声明客户端上下文。Web MVC starter 在反序列化或参数解析阶段以受保护 Header 填充这些上下文，Controller 自身不继承请求类型。

`ClientRequest`、`ClientChannelRequest` 与 `AuthenticatedRequest` 都是可实例化的具体类型：有业务请求体时继承相应类型，由 `RequestBodyAdvice` 注入上下文；Advice 只绑定 Header，随后由 Spring MVC 根据 `@Valid` 执行请求体 Bean Validation。无业务请求体但需要上下文时，Controller 直接声明不带 `@RequestBody` 的具体请求上下文参数，由统一的 Web MVC argument resolver 从 Header 创建，并仅在参数存在 `@Valid`/`@Validated` 时显式执行相同的 Bean Validation。所有 `ClientRequest` 及其子类型的 Controller 参数都必须标注 `@Valid`，以明确校验契约并使 `clientAppId`、`channelCode` 和 `userId` 的约束在两种入口中保持一致。无须上下文且没有业务参数的操作可以不声明请求对象。

继承关系表达接口的强制上下文需求：继承 `ClientRequest` 表示 `X-Client-App-Id` 必须存在；继承 `ClientChannelRequest` 表示该接口确实需要渠道，因此 `X-Channel-Code` 也必须存在。无需渠道的接口不得继承 `ClientChannelRequest`，不得通过把 `channelCode` 改为可选值来兼容两种语义。

业务入口与管理入口必须区分“当前请求上下文”和“管理员操作目标”。业务入口继承 `ClientChannelRequest` 时，`channelCode` 只来自网关注入的 `X-Channel-Code`；管理入口只继承 `ClientRequest`，调用上下文仅要求 `X-Client-App-Id`，并在 JSON 中使用 `targetChannelCode` 或 `targetUserId` 显式声明管理目标。即使网关额外携带 `X-Channel-Code` 或 `X-User-Id`，Header Binder 也不会覆盖这些目标字段。管理目标不得命名为 `channelCode` 或 `userId`，避免与上下文语义混淆。

`channelCode` 的存在、状态以及与 ClientApp 的合法组合由渠道/网关边界管理。`user-auth` 不维护渠道主数据；它只校验编码格式，并把该值用于查找自身的渠道功能授权策略。未配置策略时登录仍可继续，但不会产生任何渠道来源授权。

## 渠道功能授权策略

```text
ChannelAuthorizationPolicy
  channelCode
  roleCodes
  directPermissionCodes
  status = DRAFT / ACTIVE / DISABLED
  version
```

当用户以一个已验证的 ChannelContext 登录，或管理员执行策略重放时：

```text
GrantSource.sourceType = CHANNEL_AUTHORIZATION_POLICY
GrantSource.sourceId   = channelCode
```

策略中的角色和直接权限分别创建授权记录。相同的 `userId + targetCode + sourceType + sourceId` 幂等；不同渠道来源可以叠加。角色与 Permission 均为全局目录项，**不带渠道作用域**；渠道仅说明“这项授权由谁授予”。策略变更时，仅同步该 `channelCode` 来源：新增目标则授予、移除目标则撤销该来源的授予，绝不删除来自其他渠道、人工操作或将来其他来源的授予。

策略生命周期与同步规则如下：

- 新策略以 `DRAFT`、`version=1` 创建；草稿不会在登录时产生授权。
- 更新目标、激活和停用都必须提交 `expectedVersion`。变更事务以数据库写锁读取现有策略，版本不一致返回渠道策略版本冲突，防止管理端陈旧配置或并发请求相互覆盖。
- 激活后，后续登录在宿主访问凭据签发前同步授权；同一用户和渠道已应用相同版本时直接跳过。
- 更新 ACTIVE 策略、激活或停用只提交策略变更，不在该事务内重放用户。另提供显式 reconciliation 端点，供失败重试和运维按批重放。
- `UserChannelPolicyApplication(userId, channelCode)` 记录 `appliedVersion`、首次和最近应用时间。它是同步游标，不是新的权限来源。
- 策略只能引用已登记且启用的全局 Role/Permission；Role 关联的 Permission 也必须有效。

修改全局 Role 的 Permission 集合会影响所有持有该 Role 的用户，不属于某一渠道策略的局部变更。若只需改变某渠道的一项能力，应调整该渠道的直接 Permission 配置或建立更细的全局 Role。

## 管理与查询 API

以下接口均位于 `/admin/user-auth/authorization`，并要求 Bearer Access Token 具有 OAuth2 `admin` scope。JWT 与 Reference Token 使用相同 scope 语义；`H5_SESSION` 不具备管理资格。

| 接口 | 语义 |
| --- | --- |
| `PUT /channel-policies` | 按请求体 `targetChannelCode` 新建草稿或按 `expectedVersion` 更新角色与直接权限目标。 |
| `POST /channel-policies/activate` | 按请求体 `targetChannelCode` 和版本激活并同步既有应用用户。 |
| `POST /channel-policies/disable` | 按请求体 `targetChannelCode` 和版本停用并撤销该渠道来源的既有授权。 |
| `POST /channel-policies/reconciliation` | 按请求体 `targetChannelCode`、`batchSize` 重放一个有界批次，返回本批处理数、策略版本和 `hasPendingUsers`；重复调用从已提交的 `appliedVersion` 游标继续。 |
| `POST /channel-policies/query` | 按请求体 `targetChannelCode` 查询策略状态、版本和目标。 |
| `POST /users/query` | 按请求体 `targetUserId` 查询目标用户的有效角色、有效权限及角色/直接权限来源明细。 |
| `PUT /permissions` | 按请求体 `targetPermissionCode` 保存全局 Permission 目录项。 |
| `POST /permissions/activate`、`POST /permissions/disable` | 按请求体 `targetPermissionCode` 启用或停用 Permission。 |
| `POST /permissions/query`、`POST /permissions/query-page` | 查询单个 Permission，或按 `pageNo/pageSize` 分页查询目录。 |
| `PUT /roles` | 按请求体 `targetRoleCode` 保存全局 Role 及其 PermissionCode 集合。 |
| `POST /roles/activate`、`POST /roles/disable` | 按请求体 `targetRoleCode` 启用或停用 Role。 |
| `POST /roles/query`、`POST /roles/query-page` | 查询单个 Role，或按 `pageNo/pageSize` 分页查询目录。 |

所有管理请求都只消费网关注入的 ClientApp 上下文，不消费 Header 渠道或 Header 用户作为操作目标。`PUT` 请求体为 `targetChannelCode`、`expectedVersion`、`roleCodes`、`directPermissionCodes`。创建时 `expectedVersion` 为空，更新时必填；激活和停用请求体包含 `targetChannelCode` 与必填的 `expectedVersion`。查询使用 POST 是为了让显式目标与 `ClientRequest` 上下文收敛在同一个请求对象中，并避免 GET body 的代理和客户端兼容性问题。

## 分层实现边界

| 层 | 责任 |
| --- | --- |
| domain | Client 的历史快照、GrantSource、全局角色/权限、渠道策略及来源级授予规则。 |
| application | 消费受验证上下文、执行渠道策略同步、版本重放、查询与事务编排。 |
| infrastructure | 角色、权限、来源授权、渠道策略及应用游标的关系型持久化适配器。 |
| interfaces | REST/RPC 仅接收或读取受保护的入口上下文，不实现业务策略。 |
| boot | 选择配置和适配器并完成装配。 |

当前已交付 Permission/Role 目录管理、渠道策略持久化、管理查询 API、登录同步和显式批量重放。补偿使用 `(channelCode, appliedVersion, userId)` 索引按稳定 userId 顺序选择未达到目标版本的用户；每个用户同步与游标写入在同一本地事务内提交，因此失败重试从已提交边界继续。策略停用或用户游标落后于当前策略版本时，查询会 fail-closed，不把该渠道来源的旧授权视为有效。ClientApp/Channel 主数据目录、人工授权 API、异步批量触发器以及资源服务器运行时鉴权仍未交付。
