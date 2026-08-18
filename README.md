# user-auth

`user-auth` 是 cloud 中负责账户认证与协议令牌生命周期的服务。它提供手机号验证码登录、外部身份登录、已绑定外部 Credential 的授权码登录、登录会话和当前令牌登出；通过 OAuth2 协议基础设施签发短期 Access Token。

功能授权（角色、权限、直接授权）已具备关系型持久化、来源追溯、渠道授权策略管理及用户有效权限查询。角色与权限是全局目录，不带渠道作用域；渠道只作为授权来源参与叠加和独立撤销。当前仍未把 Permission 写入 Access Token，也未交付资源服务器的在线鉴权端点。

## 当前已交付能力

- 创建认证挑战并完成手机号验证码登录。
- 通过外部身份登录尝试和确认登录。
- 供已由网关验签的合作方使用的外部直连登录；外部订单证明不会绑定为长期第三方凭据。
- 首次登录时创建 `User`，并通过应用端口初始化。
- 创建登录会话并通过 SAS 交付 OAuth2 Bearer Access Token；`user-auth.authentication.oauth2.access-token.format` 可选择 `SELF_CONTAINED` JWT 或 `REFERENCE` 不透明令牌。共享的 ClientApp `renewalPolicy` 控制 Refresh Token rotation、外部一次性授权码重新认证或不续期。
- 为 `REFRESH_TOKEN_ROTATION` ClientApp 提供 `POST /api/user-auth/login/refresh`；每次刷新返回新的 Access Token/Refresh Token，旧 Refresh Token 重放会撤销整个 token family 和关联 LoginSession。
- 已绑定外部 Credential 的 public client 可用外部一次性授权码调用通用登录入口换取新的 Bearer Access Token。微信小程序的 `wx.login` 是该策略的一个实例。
- 基于当前 Bearer Access Token 的登出：JWT 或 Reference Token 均归一为当前 LoginSession，并按 `session_id` 撤销 SAS 协议授权。`H5_SESSION` 不允许登出父 LoginSession；JWT 在其他离线资源服务器上自然失效，Reference Token 可通过 introspection 立即反映撤销状态。
- 以单个 LoginSession 为生命周期根，宿主 logout 时通过 `LoginSessionRevoker.revoke` 级联撤销 SAS 授权状态，并删除关联 H5 Session、未消费 handoff ticket 及其 Redis 反向索引；H5/ticket 的有效期均受父 LoginSession 到期时间约束。
- 使用 Spring Authorization Server（SAS）实现 OAuth2 token endpoint、自定义登录 grant 和 JWK 发布。
- 管理全局 Permission/Role 目录及其启用状态，并管理渠道授权策略的草稿、激活、停用和重放；手机号及外部身份登录会在访问凭据签发前按可信 `X-Channel-Code` 幂等同步该渠道来源的角色和直接权限。
- 查询用户的有效角色、有效权限以及各项授予的来源。渠道策略和授权查询接口要求 OAuth2 `admin` scope；H5 Session 不能访问。

## 尚未交付的能力

- 人工直接授权 API，以及供资源服务器逐请求使用的在线鉴权接口。渠道策略只能引用数据库中已登记且启用的全局 Role/Permission。
- 面向最终用户的 OIDC Authorization Code 登录体验（浏览器登录页、同意页、OIDC 客户端管理与隐私声明策略）。SAS 的 OIDC 协议端点可由配置启用，但这不等同于上述端到端能力已交付。
- `SessionFamily` 尚未建模：外部一次性授权码重新认证会产生新的 LoginSession，因此尚未完成“一次初始登录及其所有重新认证续期代际一起撤销”；全账户跨设备会话统一撤销也未交付。
- Refresh Token 的 DPoP/硬件密钥持有证明；当前 family 绑定 ClientApp 与 LoginSession，并在 refresh token 哈希维度串行轮换，但没有独立的设备密钥证明。

## 模块地图

| 模块 | 职责                                                                                                                                                                                                                  |
| --- |-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `user-auth-api` | 对内 Facade、命令/查询/响应 DTO，以及跨服务可用的 Access Token 声明名称；认证命令、功能授权命令和功能授权查询分别由 `UserAuthenticationCommandFacade`、`UserAuthorizationCommandFacade`、`UserAuthorizationQueryFacade` 表达。 |
| `user-auth-domain` | 认证域、功能授权领域模型，以及共享 `User` 模型。                                                                                                                                                                      |
| `user-auth-application` | 登录、登出与注册流程编排；Permission/Role 目录管理、渠道授权策略同步与查询；定义 challenge、外部身份验证、登录结果交付、续期、会话存储/交接、协议撤销及 user 协作 Port。                                              |
| `user-auth-infrastructure` | 技术适配器、持久化 capability 抽象及技术无关转换；不包含具体持久化技术的 DO 或 DO Mapper。                                                                                                                            |
| `user-auth-infrastructure-persistence-jpa` | JPA 持久化实现：JPA DO、DO Mapper、Spring Data repository 与 JPA 装配。                                                                                                                                               |
| `user-auth-infrastructure-oauth2-sas` | OAuth2/OIDC 协议基础设施的 SAS 实现；OIDC 端点按显式配置启用。                                                                                                                                                        |
| `user-auth-infrastructure-oauth2-redis` | OAuth2 授权记录与 Token 状态的 Redis 持久化。                                                                                                                                                                         |
| `user-auth-infrastructure-session-redis` | Session handoff ticket 与 H5 Session 的 Redis 实现。                                                                                                                                                                  |
| `user-auth-interfaces` | REST 接口和资源服务器安全配置。                                                                                                                                                                                       |
| `user-auth-boot` | 运行时组装与启动。                                                                                                                                                                                                    |

依赖方向为：`api/domain` ← `application` ← `infrastructure/interfaces` ← `boot`。协议基础设施是 `application` 端口的外层实现，不能反向依赖领域模型以外的外层模块。

## 文档入口

- [领域文档](docs/DOMAIN.md)：认证域、功能授权领域模型、边界、当前交付状态和业务 API 语义。涉及领域规则、错误或 API 业务语义时必须阅读。
- [Client 与 Channel 上下文设计](docs/CLIENT_AND_CHANNEL_CONTEXT.md)：ClientApp、platform、channelCode、入口信任边界和渠道功能授权策略。涉及登录入口、调用方身份或渠道授权时必须阅读。
- [OAuth2/OIDC 协议基础设施](docs/OAUTH2_OIDC_PROTOCOL_INFRASTRUCTURE.md)：SAS Provider、grant、Token、scope、JWK、协议存储与端点安全。涉及 OAuth2、OIDC、SAS、Token 生命周期或协议配置时必须阅读。

## 本地验证

使用 Java 17 和 Maven：

```bash
mvn test
mvn verify
```

`verify` 包含基础设施集成验证，运行环境需要满足 Testcontainers 所需的 Docker 条件。

## 外置配置

可独立更新的运行配置与数据库初始化脚本位于工程根目录 `config/`。从工程根目录使用
Maven 或运行可执行 jar 时，Spring 会从 `./config/` 加载 `application.yml`；其他工作目录或
部署环境须显式提供 `--spring.config.additional-location=optional:file:<config-dir>/`，并设置
`USER_AUTH_CONFIG_DIR=<config-dir>`，供 SQL 初始化定位 `db/schema.sql`。

## 运行配置提示

生产环境必须提供数据库凭据、内部 OAuth2 Client Secret、稳定的 RSA KeyStore、Challenge pepper，以及 `UserGateway` 的实际运行时实现；该端口没有无操作兜底实现，缺失时服务装配失败。OAuth2 协议端点、签名密钥、客户端、scope 和 Token 生命周期的配置约束见 [OAuth2/OIDC 协议基础设施](docs/OAUTH2_OIDC_PROTOCOL_INFRASTRUCTURE.md)。

`user-auth` 的服务配置按子域隔离：认证、登录会话、宿主访问令牌、外部身份及 OAuth2/OIDC 协议基础设施统一位于 `user-auth.authentication`；`user-auth.authorization` 专门保留给功能授权子域。当前 Permission、Role、渠道授权策略均由关系型数据管理，功能授权子域没有独立运行时 Properties，因此配置文件不声明空的 `authorization` 节点。

OAuth2 Access Token 的格式与有效期统一配置在 `user-auth.authentication.oauth2.access-token.format/ttl`，默认 `SELF_CONTAINED`、TTL 15 分钟；选择 `REFERENCE` 时仍以标准 `Authorization: Bearer` 携带，但由授权存储和 introspection 在线校验。ClientApp 的续期策略和 OAuth2 scope 分别使用 `renewal-policy` 与 `oauth2-scopes`；后者只是协议 scope，不是功能授权 Permission。OAuth2 Authorization Server 是唯一的宿主访问令牌交付实现，不再存在 Session-Token-only 运行模式。
