# OAuth2/OIDC 协议基础设施

本文定义 `user-auth` 的 OAuth2/OIDC 协议基础设施边界和当前 SAS 实现。它不是功能授权领域的实现文档：Role、Permission、`order:refund` 等功能资格属于 `domain.authorization`；其渠道策略和查询能力见 [DOMAIN.md](DOMAIN.md)。

文件名使用 **OAuth2/OIDC 协议基础设施**，表示此模块是承载标准认证授权协议的技术边界；这不表示当前已交付完整 OIDC。

## 当前能力与范围

| 范围 | 当前状态 |
| --- | --- |
| OAuth2 Authorization Server | 已通过 Spring Authorization Server（SAS）提供。 |
| Token endpoint | 已提供，支持内部客户端使用的手机号验证码、外部身份和 refresh token grant。 |
| Access Token | 已提供，可全局配置为 RS256 签名 JWT（`SELF_CONTAINED`）或 SAS Reference Token（`REFERENCE`）；两者都使用 Bearer 语义。 |
| Refresh Token | 按 ClientApp 的 `renewalPolicy` 选择性签发；支持 rotation、旧 token 重放检测和 token-family 撤销。 |
| JWK Set | 已提供，由 SAS 端点发布签名公钥。 |
| OIDC 协议端点 | 可用 `user-auth.authentication.oauth2.authorization-server.sas.oidc.enabled=true` 显式启用。默认关闭。 |
| 可投入使用的 OIDC 登录产品能力 | 未交付。尚无浏览器认证入口、授权同意策略、受管 OIDC 客户端及身份声明披露策略。 |
| 功能权限声明 | 未写入 Access Token；当前 Token 不携带角色或 PermissionCode，管理查询能力与协议 Token 签发保持分离。 |

实现模块为 `user-auth-infrastructure-oauth2-sas`，Java 包为 `com.cloud.userauth.infrastructure.oauth2.sas`。该命名刻意避免把 SAS 误解成 `authorization`（功能授权）子域的一种实现。

## 架构职责

```text
interfaces REST -> application login/logout use cases -> protocol ports
                                                   <- SAS OAuth2 adapter
SAS endpoint  -> custom grant provider -> application authentication process
```

- `application` 定义 `LoginTokenIssuer`、`LoginTokenRefresher`、`LoginSessionRevoker` 等端口，不依赖 SAS 类型。
- `LoginTokenIssuer` 表达“认证成功后的结果交付”；当前唯一实现 `SasLoginTokenIssuer` 交付标准 OAuth2 Bearer Access Token，Token 的自包含或 Reference 格式不会改变应用用例。
- SAS module 实现登录结果交付、刷新和协议撤销 Port，并将认证成功的 `UserId`、认证账户和 `LoginSession` 转换为协议授权记录与 Token；OAuth2 Redis module 实现 SAS `OAuth2AuthorizationService`，负责授权记录和哈希化 Token 状态的持久化。
- `interfaces` 的业务登录接口不直接暴露 SAS grant；它通过应用用例调用 SAS module 内部的 Token endpoint 客户端。
- SAS、JWK、client、scope、协议授权记录和 Token 生命周期都是技术协议职责，不承担功能权限判定或业务资源数据范围判定。

## OAuth2 grant 与登录流程

当前 SAS Token endpoint 注册以下 grant：

- `urn:ietf:params:oauth:grant-type:mobile_otp`：手机号验证码登录。SAS converter/provider 将请求转给 `MobileOtpAuthenticationProcess`，认证成功后签发 Token。
- `urn:ietf:params:oauth:grant-type:external_identity`：外部身份登录。SAS converter/provider 将请求转给 `ExternalAuthenticationProcess`。
- `refresh_token`：仅由公开 REST refresh 用例通过内部 confidential client 调用；是否允许由原登录 ClientApp 的服务端配置决定。

两种登录 grant 仅由服务内部注册客户端调用。客户端调用公开 REST 登录接口，而 REST 应用层再以配置的内部 client 调用 Token endpoint；不得把内部 client secret 下发给浏览器或移动端。

两个自定义登录 grant 都会在内部请求中携带经网关注入并由 REST 层消费的 `channel_code`。认证流程在 Token 生成前调用渠道授权应用端口；只有 ACTIVE 渠道策略会新增授权，策略不存在时保持兼容并不授予任何内容。`channel_code` 是授权来源标识，不会改变 Role、Permission 或 OAuth2 scope 的定义。

`POST /api/user-auth/login/partner/trusted-mobile` 同样复用 `external_identity` grant，但仅由服务端合作方可信手机号用例在内部增加 `bind_external_identity=false` 参数。该参数不得向外部 OAuth2 client 开放；它使临时 LoginAttempt 中的合作方业务追踪标识不被写为长期外部 Credential。

`POST /api/user-auth/login/external/proof` 先通过 issuer 对应的 `ExternalIdentityVerifier` 同时验证稳定外部身份与手机号，再复用 `external_identity` grant，并保持 `bind_external_identity=true`。该入口只接受已被 issuer policy 信任的验证手机号；不满足时在进入 Token endpoint 前拒绝，不退化为两步流程。

`scope` 是 OAuth2 客户端获得的协议 scope，按注册客户端配置解析。当前业务登录的 `clientAppId` 通过 `user-auth.authentication.client-apps.<clientAppId>.oauth2-scopes` 选择服务端允许的 scope；SAS 的 `scopes` 则登记全局允许集合，ClientApp 配置必须是其非空子集。这只是 access-token 登录交付的调用方策略，不是 OAuth2 `RegisteredClient` 的替代物。它不是 PermissionCode，也不等价于角色或业务数据范围。新增协议 scope 不会自动授予任何 `order:*` 功能权限。

当前服务支持的 scope 是 API 契约 `OAuth2Scope` 定义的封闭目录：`APP=app`、`ADMIN=admin`。配置只负责从目录中启用 SAS 全局允许集合并为 ClientApp 分配子集，不能声明任意字符串。gateway 是业务 API 的入口认证执行点：对 JWT 本地验证，使用 SAS 标准 introspection endpoint 在线校验 Reference Token，并在线验证 Browser Session；`admin` scope 在网关映射为 `SCOPE_admin` 并完成管理入口授权。user-auth 的业务安全链只消费 gateway 写入的 `X-Subject-Type + X-User-Id + X-Session-Id` 可信上下文，不重复验证 Bearer 或 Browser Cookie；SAS OAuth2/OIDC 协议端点继续由独立 SAS 安全链处理。Browser Session 由 gateway 通过 user-auth 内网验证端点 `POST /internal/browser-sessions/verify` 逐请求在线验证，`X-Subject-Type` 常量由共享 framework 承载。

## Access Token 与登出

### 签发

SAS 使用 `DelegatingOAuth2TokenGenerator`，并由 `user-auth.authentication.oauth2.access-token.format` 全局选择格式：

- `SELF_CONTAINED`：使用 `JwtGenerator`，由 RSA 私钥按 `RS256` 签名；网关使用 issuer、audience、签名与过期时间本地验证。
- `REFERENCE`：使用 `OAuth2AccessTokenGenerator` 生成高熵不透明值；Token 状态和声明保存在 OAuth2 Authorization Redis Store，网关使用专用 introspection client 调用 SAS 标准 introspection endpoint。

两种格式共享 `user-auth.authentication.oauth2.access-token.ttl`，默认 15 分钟；登录响应和请求携带方式均为 `tokenType=Bearer + accessToken` / `Authorization: Bearer <token>`。格式是服务端部署策略，不允许客户端在登录请求中选择。Access Token 中的服务声明由 `user-auth-api` 的 `AccessTokenClaimApiConstants` 定义：

- `sub`（UserId 的字符串形式，不再重复签发 `user_id`）
- `auth_account_id`
- `session_id`

其中 `session_id` 是跨认证领域与协议基础设施的 Access Token 声明，不属于 SAS 私有命名。它使当前令牌登出能定位到相应的 `LoginSession`/协议授权记录。Token 不包含权限、角色信息。这些服务声明同时进入 JWT claims 或 Reference Token 的受保护授权元数据，只写入 Access Token，不能因开启 OIDC 而进入 ID Token。

### 续期与撤销

续期策略位于共享配置 `user-auth.authentication.client-apps.<clientAppId>.renewal-policy`，由受信任的 `X-Client-App-Id` 解析，调用方不能在请求体中选择。相同 ClientApp 下的 `oauth2-scopes` 只管理 OAuth2 协议 scope，不承担通用的宿主访问凭据续期策略：

- `REFRESH_TOKEN_ROTATION`：自定义登录 grant 同时生成 Refresh Token；`POST /api/user-auth/login/refresh` 可用。
- `EXTERNAL_AUTHORIZATION_CODE`：不生成 Refresh Token，使用外部一次性授权码重新登录。
- `NONE`：不提供续期。

上述三个值都可用于两种格式的 Bearer Access Token；是否签发 Refresh Token 只由 ClientApp 策略决定，与 Access Token 格式无关。

公开 refresh 接口不会把 public client 直接注册为持有 secret 的 OAuth2 client，而是经应用用例调用本机 Token endpoint 的内部 confidential client。调用前会以原始 Refresh Token 查询协议授权记录并校验其中的 `client_app_id`，再校验关联 LoginSession 的 ClientApp、ACTIVE 状态和绝对有效期。轮换使用 refresh token 哈希作为分布式锁 key，避免并发请求同时消费同一 token；Redis 中 Access Token/Refresh Token 均以 SHA-256 哈希保存，旧 Refresh Token 哈希进入 family 历史索引。旧 token 再次出现时，协议授权和 LoginSession 会一起撤销。

单个 LoginSession 是当前派生凭据的生命周期根。logout 通过 application `LoginSessionRevoker.revoke` 集合统一调用 SAS 授权撤销器、Browser Session Store 及 handoff Store：SAS Access Token 与 Refresh Token 状态失效，Browser Session credential、未消费 ticket 及其 Redis 反向索引被删除。短期 JWT 在其他离线校验节点继续自然失效；Reference Token 的 introspection 会立即返回 inactive。当前没有 SessionFamily，因此外部授权码重新认证产生的新 LoginSession 不会与初始 LoginSession 一起撤销。

SAS 与 OAuth2 Authorization Redis Store 是宿主访问令牌的必需基础设施，不再提供 Session-Token-only 或关闭 Authorization Server 后继续运行登录交付的组合。`SELF_CONTAINED` 与 `REFERENCE` 只改变 Access Token 的表示和验证方式，不改变 REST 登录 API、`LoginTokenIssuer`、ClientApp 续期策略或 LoginSession 语义。

宿主 App 打开 WebView 时，以当前 Bearer Access Token 创建一次性 WebView handoff ticket。Access Token 只用于认证该创建请求，绝不写入 Browser Cookie。H5 以 ticket 兑换独立的 `BROWSER_SESSION` HttpOnly Cookie；Cookie `Max-Age` 取 Browser Session absolute TTL 与父 LoginSession 剩余时间的较小值。gateway 逐请求经 user-auth 内网端点在线验证该 Cookie，user-auth 在验证过程中按需滑动 Redis idle TTL，gateway 不刷新 Cookie。absolute TTL 不可滑动，父 LoginSession 登出时级联失效。ticket 默认 60 秒且一次性消费。

Redis 适配器只持久化并原子消费通用的 Session handoff ticket；票据包含目标会话类型绑定。`SessionHandoffCommandFacade` 的创建和兑换命令由外部显式传入目标类型，应用层 `SessionHandoffService` 不依赖具体目标会话，并在兑换时校验声明目标与 ticket 绑定一致；当前会话创建实现仅开放 `BROWSER_SESSION`。新增其他目标会话时复用通用 Facade 与 ticket 服务，并增加对应的目标会话创建及协议交付实现，不复用 Browser Session Store 或 Cookie 交付逻辑。

- `EXTERNAL_AUTHORIZATION_CODE` ClientApp 的 Access Token 到期后，已绑定微信 Credential 的小程序重新执行 `wx.login → code → POST /api/user-auth/login/external/bound-credential`，请求体提交 `issuer=WECHAT_MINI_PROGRAM`、`authorizationCode` 和可选设备字段；user-auth 校验新的外部一次性 code 后签发新 Token。
- 通用外部授权码登录只允许使用已绑定到 AuthAccount、且对应账户已有有效 LoginMobile 的 Credential；它不接受手机号、不建账，也不在登录中绑定 Credential。首次建立账户和绑定微信 Credential 分别走已有的手机号登录及认证后的 Credential 绑定接口。网关注入 `X-Client-App-Id`、`X-Client-Platform`、`X-Client-Version`、`X-Channel-Code`；Token 响应是否含 `PARENT` 完全由对应 ClientApp 的 renewal policy 决定。
- `POST /api/user-auth/logout`、`POST /api/user-auth/credentials/bind` 与 `POST /api/user-auth/handoffs` 要求宿主登录态。gateway 校验 JWT 或 Reference Token 后，从 `sub` 与 `session_id` 写入 `X-Subject-Type=HOST_SESSION`、`X-User-Id`、`X-Session-Id`；user-auth 使用该组合回查 LoginSession，并从领域会话恢复 AuthAccount。客户端不能指定其他用户、账户或会话。Browser Session 由 gateway 写入 `X-Subject-Type=BROWSER_SESSION`，只获得受限浏览器会话 authority，不能调用上述宿主级命令。
- `SasLoginSessionRevoker` 根据 session id 查找 SAS 的 `OAuth2Authorization` 并使关联 Access/Refresh Token 在协议状态中失效。
- `SELF_CONTAINED` JWT 的撤销策略仍是短期自然失效：已签发 JWT 在过期前不会由离线资源服务器逐请求查询 SAS。`REFERENCE` Token 必须每次 introspect，logout 后可即时失效，但其可用性和延迟依赖授权存储与 introspection endpoint。

## 端点与安全边界

SAS 以 `OAuth2AuthorizationServerConfigurer` 挂载其标准协议端点，并将它们置于独立的最高优先级 `SecurityFilterChain`。协议端点要求已认证的 OAuth2 client，CSRF 仅对协议端点忽略。

应用 REST 链开放认证挑战和登录接口；绑定外部 Credential、登出父 LoginSession 与创建 handoff ticket 必须具有可信 gateway Header 转换出的 HOST_SESSION authority。Bearer、Browser Session 和 `admin` scope 校验属于 gateway；`X-Subject-Type=BROWSER_SESSION` 只能建立受限浏览器会话认证，不能调用这些宿主级写接口；其余未明确允许的请求默认拒绝。SAS endpoint 不应被当作面向最终用户的业务 API。

开发或运维配置必须保证：

- `user-auth.authentication.oauth2.access-token.format` 只能为 `SELF_CONTAINED` 或 `REFERENCE`；SAS 与 OAuth2 Authorization Redis Store 始终装配。
- `user-auth.authentication.oauth2.authorization-server.sas.oidc.enabled=true` 时才调用 SAS 的 OIDC 配置；它只装配协议端点，客户端是否可使用 `openid` 仍由其 `RegisteredClient` 决定。
- issuer 为稳定且可被资源服务器校验的值，audiences 与资源服务器约定一致。
- 内部 OAuth2 client id/secret 只存在于服务端配置；它登记 `refresh_token` grant，但该能力仅由受 ClientApp 策略约束的公开 REST 用例在服务端使用。
- 生产环境使用稳定的 RSA KeyStore、key alias、密码与 key id；非生产 profile 会临时生成 RSA key，重启后不保证 JWT 可继续验证。

## 协议状态与持久化

- 注册客户端由 SAS 的 JDBC `RegisteredClientRepository` 保存。
- OAuth2 授权记录与 Token 状态由 SAS 的 `OAuth2AuthorizationService` 保存到 Redis。
- Redis authorization store 的 namespace 与 key scene 由 `user-auth.authentication.oauth2.authorization-store` 配置；该配置属于 Redis 技术模块，不属于功能授权领域或 SAS 协议行为配置。
- Session handoff ticket 与 Browser Session 由独立的 `user-auth-infrastructure-session-redis` 模块实现；它们不是 OAuth2 授权记录持久化的一部分。Browser Session 与 handoff 生命周期分别位于 `user-auth.authentication.browser-session.idle-ttl/absolute-ttl/renewal-threshold` 和 `user-auth.authentication.session-handoff.ttl`。
- SAS 授权撤销器、Browser Session Store 与 handoff Store 共同实现 `LoginSessionRevoker`。Browser Session 和 handoff Store 分别维护 loginSessionId 反向索引，以支持 logout 的物理级联删除；gateway 的 BrowserSessionAuthenticator 通过 `HttpBrowserSessionVerifier` 在线调用 user-auth 内网验证端点校验会话与父 LoginSession，ticket 兑换仍由 user-auth 校验父会话。
- 登录会话是认证域记录；协议授权记录保存其 `session_id` 属性以支持刷新和登出撤销。

协议存储 schema、Redis 可用性、密钥轮换和客户端密钥轮换属于部署安全要求。领域模型不应直接依赖 SAS 的授权对象或存储对象。

## OIDC 的交付边界

OIDC 开关不是全局登录模式，也不应和微信登录或 Access Token 格式互斥。它仅表示此 SAS 部署是否提供 OIDC 协议端点；标准 OAuth2/OIDC 客户端由 `RegisteredClientRepository` 管理，不能复用内部 token client，也不能由外部请求临时声明。

当前开启开关后，仍必须完成以下工作才可对外承诺 OIDC 登录：

- 为浏览器授权请求提供认证入口和会话安全策略；现有手机号/外部身份 REST 登录接口不是 `/oauth2/authorize` 的浏览器认证页。
- 为 OIDC 客户端登记 `authorization_code`、PKCE、合法 redirect URI 与 `openid` scope；内部 token client 只允许自定义登录 grant。
- 确定 ID Token 的 `sub` 与可披露身份声明，补充 UserInfo、同意策略和端到端协议测试。

在这些条件落实前，不能仅因 Discovery 或 ID Token endpoint 已出现，就向客户端宣称 OIDC 登录已可用。
