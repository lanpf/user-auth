# user-auth 服务职责与协作契约

本文声明 `user-auth` 对外承担的职责、明确不承担的职责，以及与同级 `gateway` 的协作契约（被调方视角）。领域模型与业务规则见 [DOMAIN.md](DOMAIN.md)，OAuth2/OIDC 协议实现见 [OAUTH2.md](OAUTH2.md)；gateway 的职责与入口契约见 gateway 工程 [RESPONSIBILITIES.md](../../gateway/docs/RESPONSIBILITIES.md)。

## 服务定位

`user-auth` 是负责账户认证与协议令牌生命周期的业务服务，同时拥有功能授权的目录与策略管理。它不站在流量入口上，只消费入口层写入的可信请求上下文。

## 职责清单

### 本服务负责

- 账户、AuthAccount、Credential、认证证明与登录路径；首次登录建账与初始化。
- LoginSession 的创建、状态、绝对生命周期与级联撤销（Browser Session、handoff ticket、协议授权随宿主登出一起失效）。
- 通过 Spring Authorization Server（SAS）签发与撤销 OAuth2 Access/Refresh Token，含 Refresh Token rotation 与重放撤销。
- 全局 Permission/Role 目录、渠道授权策略（草稿、激活、停用、重放）及用户有效权限查询。
- Browser Session 的状态、初始签发、TTL（idle/absolute）与父 LoginSession 级联撤销；handoff ticket 的签发与一次性消费。
- 浏览器载体的受限 Browser Session 端点（`/api/browser-sessions/**`），由 gateway 的 `USER_AUTH.BROWSER_SESSION_API` 入口类别（`BROWSER_SESSION_REQUIRED`）放行并覆盖写受限 Subject Type。
- 供 gateway 逐请求调用的 Browser Session 内网在线验证端点（见下文协作契约）。

### 本服务不负责

| 不属于本服务的职责 | 所有者 |
| --- | --- |
| 入口安全：TLS、请求签名验签、timestamp/nonce 防重放、限流 | `gateway`（或其前置受控入口层） |
| ClientApp 可用性、platform 推导、渠道组合与端点入口策略校验 | `gateway` |
| OAuth2 Access Token 校验（JWT/introspection）与 `SCOPE_admin` 管理入口授权 | `gateway` |
| Browser Session 的逐请求验证触发与 Subject Type 传播 | `gateway` |
| 渠道主数据目录 | 渠道/网关边界 |
| `order:refund` 等功能权限的最终业务执行与资源归属判断 | 拥有该资源的业务服务 |

## 与 gateway 的协作契约

### 路径命名空间

- 本服务自身路径以 `/api/**`（业务）、`/admin/**`（管理）、`/internal/**`（服务内网）表达。
- 外部统一以 `/api/user-auth/**`、`/admin/user-auth/**` 访问；gateway 按 EndpointCatalog 校验入口后剥离 `user-auth` 服务段转发。`/oauth2/**`、`/.well-known/**` 协议端点原样透传。
- `/internal/**` 不在 gateway 登记任何 Endpoint，外部流量无法经网关到达；仅可信内网内的服务间调用可直达。

### 可信请求上下文消费

业务 REST 接口不重复解析 Bearer Token 或 Browser Cookie，只消费 gateway 覆盖写入的 `X-Subject-Type`、`X-User-Id`、`X-Session-Id`（Header 常量由共享 framework `RequestHeader` 承载）：

- `X-Subject-Type=HOST_SESSION` 授予宿主会话 authority；`BROWSER_SESSION` 只授予受限的浏览器会话 authority，不能调用宿主级命令（绑定 Credential、登出父 LoginSession、创建 handoff ticket、管理接口）。
- 缺失或未知类型不授予任何 authority，不得默认解释为宿主会话。
- 应用层对高价值认证命令仍以 `userId + sessionId` 回查 LoginSession 验证归属并恢复认证账户，不只相信 UserId；调用方不能指定其他用户、账户或会话。
- 有业务请求体的 Controller 参数收敛为 `ClientRequest` 子类型并标注 `@Valid`，由框架 RequestBodyAdvice 用受保护 Header 覆盖注入；管理接口显式使用请求体中的 `targetChannelCode`/`targetUserId` 声明操作目标，Header Binder 不会用 `X-Channel-Code`/`X-User-Id` 覆盖这些目标字段。

### Browser Session 结束端点契约

- 端点：`POST /api/browser-sessions/end`；要求受限浏览器会话 authority（`X-Subject-Type=BROWSER_SESSION`），宿主 token 不可调用。
- gateway 对 `USER_AUTH.BROWSER_SESSION_API` 入口类别按 Cookie 白名单回传原始 `BROWSER_SESSION` Cookie；本服务以 Cookie 精确定位当前浏览器会话并按凭据单独结束，不影响同一父 LoginSession 的其他浏览器会话；结束成功后由本服务响应同名、`Max-Age=0` 的安全 Cookie。gateway 不写 Browser Session Cookie。
- Browser Session 的 Origin 白名单属于 gateway 的 `IngressClientAppPolicy`；gateway 在转发非安全 Cookie 请求前完成精确校验并随入口策略快照热加载。user-auth 不重复维护 Origin 配置，只校验 gateway 写入的受限 Subject Context、会话归属与原始 Cookie 凭据；归属失败或会话无效使用错误 `617`。

### Browser Session 在线验证端点

Browser Session 的逐请求验证由 gateway 触发、本服务执行；状态、TTL 与父 LoginSession 校验的所有权在本服务：

- 端点：`POST /internal/browser-sessions/verify`，请求体 `{"credential":"<BROWSER_SESSION Cookie 值>"}`。
- 稳定的服务契约是 `BrowserSessionCommandFacade.verify(VerifyBrowserSessionApiCommand)`；Internal Controller 只负责把 HTTP 请求转换为该 API Command。后续改用 Dubbo 等 RPC 时直接发布同一 Facade，无需复制业务载荷。
- 语义：校验会话存在、absolute TTL 未过期、父 LoginSession `ACTIVE` 且归属一致；按 idle 窗口执行服务端滑动续期。
- 应用用例以 `VerifyBrowserSessionOutput.Verified` / `Unverified` 显式表达业务验证结果；存储或其他依赖异常不降级成 `Unverified`，而是作为调用失败向外传播，使 gateway 保持 fail-closed。
- 响应：`{"verified":true,"userId":...,"sessionId":"...","remainingIdleTtlSeconds":...}`；该剩余时间是本次验证及按需滑动续期完成后的服务端有效期边界，会话无效时返回 `verified=false`。是否实际执行 Redis 续期写入属于本服务内部实现，不进入跨服务契约。
- 可达性：仅可信内网；本服务以 `spring.application.name=user-auth` 注册到 Nacos，gateway 通过逻辑 `serviceId` 和固定内部路径调用。服务无可用实例或调用失败时 gateway 保持 fail-closed。

## 能力交付状态

### 当前已交付

- 创建认证挑战并完成手机号验证码登录；外部身份登录尝试与确认；已由网关验签的合作方可信手机号直连登录。
- 首次登录创建 `User` 并初始化；LoginSession 与 OAuth2 Bearer Access Token 交付（`SELF_CONTAINED` JWT 或 `REFERENCE` 不透明令牌可配）；Refresh Token rotation 与重放撤销。
- 已绑定外部 Credential 的通用登录入口（微信小程序 `wx.login` 为一例）。
- 基于当前 Bearer Access Token 的登出与 LoginSession 级联撤销；Browser Session 与 handoff ticket 及其 Redis 反向索引清理。
- Browser Session 内网在线验证端点；本服务在验证过程中管理 Redis idle TTL 滑动续期，gateway 负责逐请求触发验证和覆盖写入 `X-Subject-Type`（见 gateway RESPONSIBILITIES.md）。
- Browser Session 结束端点 `POST /api/browser-sessions/end`（受限 `BROWSER_SESSION` authority；Origin 白名单由 gateway 入口策略校验）；gateway 的 `USER_AUTH.BROWSER_SESSION_API` 入口类别已登记。
- 管理 Permission/Role 目录与渠道授权策略；登录时按可信 `X-Channel-Code` 幂等同步渠道来源授权。

### 尚未交付

- 人工直接授权 API，以及供资源服务器逐请求使用的在线功能鉴权接口。
- 面向最终用户的 OIDC Authorization Code 登录产品能力（浏览器登录页、同意页、受管 OIDC 客户端）。
- `SessionFamily`（一次初始登录及其重新认证代际的统一撤销）与全账户跨设备会话撤销。
- Refresh Token 的 DPoP/硬件密钥持有证明。
