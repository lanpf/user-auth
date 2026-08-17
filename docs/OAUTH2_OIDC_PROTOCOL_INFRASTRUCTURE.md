# OAuth2/OIDC 协议基础设施

本文定义 `user-auth` 的 OAuth2/OIDC 协议基础设施边界和当前 SAS 实现。它不是功能授权领域的实现文档：Role、Permission、`order:refund` 等功能资格属于 `domain.authorization`；其渠道策略和查询能力见 [DOMAIN.md](DOMAIN.md)。

文件名使用 **OAuth2/OIDC 协议基础设施**，表示此模块是承载标准认证授权协议的技术边界；这不表示当前已交付完整 OIDC。

## 当前能力与范围

| 范围 | 当前状态 |
| --- | --- |
| OAuth2 Authorization Server | 已通过 Spring Authorization Server（SAS）提供。 |
| Token endpoint | 已提供，支持内部客户端使用的手机号验证码、外部身份和 refresh token grant。 |
| Access Token | 已提供，格式为 RS256 签名 JWT。 |
| Refresh Token | 按 ClientApp 的 `renewalPolicy` 选择性签发；支持 rotation、旧 token 重放检测和 token-family 撤销。 |
| JWK Set | 已提供，由 SAS 端点发布签名公钥。 |
| OIDC 协议端点 | 可用 `user-auth.authentication.oauth2.authorization-server.sas.oidc.enabled=true` 显式启用。默认关闭。 |
| 可投入使用的 OIDC 登录产品能力 | 未交付。尚无浏览器认证入口、授权同意策略、受管 OIDC 客户端及身份声明披露策略。 |
| 功能权限声明 | 未写入 JWT；当前 JWT 不携带角色或 PermissionCode，管理查询能力与协议 Token 签发保持分离。 |

实现模块为 `user-auth-infrastructure-oauth2-sas`，Java 包为 `com.cloud.userauth.infrastructure.oauth2.sas`。该命名刻意避免把 SAS 误解成 `authorization`（功能授权）子域的一种实现。

## 架构职责

```text
interfaces REST -> application login/logout use cases -> protocol ports
                                                   <- SAS OAuth2 adapter
SAS endpoint  -> custom grant provider -> application authentication process
```

- `application` 定义 `LoginTokenIssuer`、`SessionAuthorizationRevoker` 等端口，不依赖 SAS 类型。
- `LoginTokenIssuer` 表达“认证成功后的结果交付”；SAS module 的 `SasLoginTokenIssuer` 交付 OAuth2 Access Token，Session Token 实现交付不透明会话凭据。
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

`POST /api/user-auth/login/external/trusted-mobile` 同样复用 `external_identity` grant，但仅由服务端外部授权码登录用例在内部增加 `bind_external_identity=false` 参数。该参数不得向外部 OAuth2 client 开放；它使一次性合作方授权码证明不被写为长期外部 Credential。

`scope` 是 OAuth2 客户端获得的协议 scope，按注册客户端配置解析。当前业务登录的 `clientAppId` 通过 `user-auth.authentication.client-apps.<clientAppId>.oauth2-scopes` 选择服务端允许的 scope；SAS 的 `scopes` 则登记全局允许集合，ClientApp 配置必须是其非空子集。这只是 access-token 登录交付的调用方策略，不是 OAuth2 `RegisteredClient` 的替代物。它不是 PermissionCode，也不等价于角色或业务数据范围。新增协议 scope 不会自动授予任何 `order:*` 功能权限。

## Token、JWT 与登出

### 签发

SAS 使用 `DelegatingOAuth2TokenGenerator`：

- Access Token 使用 `JwtGenerator`，并由 RSA 私钥按 `RS256` 签名。
- Access Token 受配置的 issuer 和 audiences 约束；资源服务器使用相同 issuer 验证。

Access Token 中的服务声明由 `user-auth-api` 的 `JwtApiConstants` 定义：

- `user_id`
- `auth_account_id`
- `session_id`

其中 `session_id` 是跨认证领域与协议基础设施的 JWT 声明，不属于 SAS 私有命名。它使当前令牌登出能定位到相应的 `LoginSession`/协议授权记录。Token 不包含权限、角色 信息。这些服务私有声明只写入 Access Token，不能因开启 OIDC 而进入 ID Token。

### 续期与撤销

续期策略位于共享配置 `user-auth.authentication.client-apps.<clientAppId>.renewal-policy`，由受信任的 `X-Client-App-Id` 解析，调用方不能在请求体中选择。相同 ClientApp 下的 `oauth2-scopes` 只管理 OAuth2 协议 scope，不承担通用的宿主访问凭据续期策略：

- `REFRESH_TOKEN_ROTATION`：自定义登录 grant 同时生成 Refresh Token；`POST /api/user-auth/login/refresh` 可用。
- `EXTERNAL_AUTHORIZATION_CODE`：不生成 Refresh Token，使用外部一次性授权码重新登录。
- `NONE`：不提供续期。

上述三个值都可用于 Bearer Access Token 交付。Session Token 交付只允许 `EXTERNAL_AUTHORIZATION_CODE` 或 `NONE`；由于 Session Token 不是 OAuth2 Token，任一 ClientApp 配置 `REFRESH_TOKEN_ROTATION` 时 Session Token 运行模式会拒绝启动。

公开 refresh 接口不会把 public client 直接注册为持有 secret 的 OAuth2 client，而是经应用用例调用本机 Token endpoint 的内部 confidential client。调用前会以原始 Refresh Token 查询协议授权记录并校验其中的 `client_app_id`，再校验关联 LoginSession 的 ClientApp、ACTIVE 状态和绝对有效期。轮换使用 refresh token 哈希作为分布式锁 key，避免并发请求同时消费同一 token；Redis 中 Access Token/Refresh Token 均以 SHA-256 哈希保存，旧 Refresh Token 哈希进入 family 历史索引。旧 token 再次出现时，协议授权和 LoginSession 会一起撤销。

当 `user-auth.authentication.access-token.provider=session-token` 时，认证流程仍创建同一 LoginSession，但不会调用 SAS Token endpoint。登录响应统一使用 `tokenType + accessToken`：客户端在 `tokenType=SESSION_TOKEN` 时从响应体取得 `accessToken`，并在后续请求使用 `X-Session-Token`。这里的响应字段是宿主访问令牌的统一名称，不会赋予 Session Token OAuth2 语义。服务不签发也不接受 `SESSION_TOKEN` Cookie。该凭据保存在 Redis，解析时必须验证关联 LoginSession 仍为 ACTIVE 且未过期。Session Token ClientApp 采用 `EXTERNAL_AUTHORIZATION_CODE` 时，可通过通用外部授权码登录入口重新取得新的 Session Token credential；采用 `NONE` 时不提供续期。Session Token 不面向 OAuth2/OIDC 客户端或合作方服务端；这些场景可独立启用 SAS 协议能力。

单个 LoginSession 是当前派生凭据的生命周期根。logout 通过 application `LoginSessionArtifactRevoker` 统一调用 SAS 授权撤销器及 Session Token/H5/handoff Store：SAS 授权和 Refresh Token 状态失效，Session Token/H5 credential、未消费 ticket 及其 Redis 反向索引被删除；这些 Redis 凭据的有效期也不超过父 LoginSession。短期 JWT Access Token 仍按自然失效策略处理。当前没有 SessionFamily，因此外部授权码重新认证产生的新 LoginSession 不会与初始 LoginSession 一起撤销。

登录结果交付方式与 OAuth2 Authorization Server 是两个正交的运行时维度：

| `authentication.access-token.provider` | `authentication.oauth2.authorization-server.enabled` | 运行模式 |
| --- | ---: | --- |
| `sas` | `true` | 业务登录交付 SAS Access Token，并提供 OAuth2/OIDC 协议能力。 |
| `session-token` | `true` | 业务登录交付 Session Token，同时保留独立的 OAuth2/OIDC 协议能力。 |
| `session-token` | `false` | Session-Token-only，不装配 SAS、OAuth2 Authorization Redis Store 或 JWT Resource Server。 |
| `sas` | `false` | 非法组合，启动配置校验失败。 |

宿主 App 打开 WebView 时，无论宿主当前使用 SAS Access Token 还是 Session Token，均可创建一次性 WebView handoff ticket。SAS Access Token 只用于认证该创建请求，绝不写入 H5 Cookie。H5 以 ticket 兑换独立的 `H5_SESSION` HttpOnly Cookie；该 Cookie 按 idle window 滑动且受 absolute TTL 限制，父 LoginSession 登出时级联失效。ticket 默认 60 秒且一次性消费。

Redis 适配器只持久化并原子消费通用的 Session handoff ticket；票据包含目标会话类型绑定。应用层 `SessionHandoffTicketService` 不依赖 H5，`H5SessionHandoffCommandService` 才负责消费目标为 `H5_SESSION` 的 ticket 并创建 H5 Session。新增其他目标会话时复用前者并新增独立目标用例，不复用 H5 Store 或 Cookie 交付逻辑。

- `EXTERNAL_AUTHORIZATION_CODE` ClientApp 的 Access Token 到期后，已绑定微信 Credential 的小程序重新执行 `wx.login → code → POST /api/user-auth/login/external/bound`，请求体提交 `issuer=WECHAT_MINI_PROGRAM`、`authorizationCode` 和可选设备字段；user-auth 校验新的外部一次性 code 后签发新 Token。
- 通用外部授权码登录只允许使用已绑定到 AuthAccount、且对应账户已有有效 LoginMobile 的 Credential；它不接受手机号、不建账，也不在登录中绑定 Credential。首次建立账户和绑定微信 Credential 分别走已有的手机号登录及认证后的 Credential 绑定接口。网关注入 `X-Client-App-Id`、`X-Client-Platform`、`X-Client-Version`、`X-Channel-Code`；Token 响应是否含 `refreshToken` 完全由对应 ClientApp 的 renewal policy 决定。
- `POST /api/user-auth/logout`、`POST /api/user-auth/credentials/external/bind` 与 `POST /api/user-auth/web-view-handoffs` 要求宿主登录态。Bearer JWT 认证阶段校验 `user_id`、`auth_account_id`、`session_id` 必备声明，Session Token Header 认证解析对应服务端会话，两者都转换为统一的 `AuthenticatedSession` Principal 并获得宿主会话 authority。H5 Session 虽转换为相同 Principal 形态，但只获得受限 H5 authority，不能调用上述宿主级命令。
- `SasSessionAuthorizationRevoker` 根据 session id 查找 SAS 的 `OAuth2Authorization` 并使关联 Access Token 在协议状态中失效。
- Access Token 的撤销策略是短期 JWT 自然失效。它已签发后的签名 JWT 在过期前不会由资源服务器逐请求查询 SAS 撤销状态；需要更强的即时失效时，必须另行设计 introspection 或 deny-list，而不能误以为当前 logout 提供此保证。

## 端点与安全边界

SAS 以 `OAuth2AuthorizationServerConfigurer` 挂载其标准协议端点，并将它们置于独立的最高优先级 `SecurityFilterChain`。协议端点要求已认证的 OAuth2 client，CSRF 仅对协议端点忽略。

应用 REST 链开放认证挑战和登录接口；绑定外部 Credential、登出父 LoginSession 与创建 handoff ticket 必须具有宿主会话 authority，该 authority 仅由有效 Bearer Access Token 或 `X-Session-Token` 提供。`H5_SESSION` 只能建立受限 H5 认证，不能调用这些宿主级写接口；其余未明确允许的请求默认拒绝。SAS endpoint 不应被当作面向最终用户的业务 API。

开发或运维配置必须保证：

- `user-auth.authentication.oauth2.authorization-server.enabled=true` 时才装配 SAS 及 OAuth2 Authorization Redis Store；应用默认配置为启用。
- `user-auth.authentication.oauth2.authorization-server.sas.oidc.enabled=true` 时才调用 SAS 的 OIDC 配置；它只装配协议端点，客户端是否可使用 `openid` 仍由其 `RegisteredClient` 决定。
- issuer 为稳定且可被资源服务器校验的值，audiences 与资源服务器约定一致。
- 内部 OAuth2 client id/secret 只存在于服务端配置；它登记 `refresh_token` grant，但该能力仅由受 ClientApp 策略约束的公开 REST 用例在服务端使用。
- 生产环境使用稳定的 RSA KeyStore、key alias、密码与 key id；非生产 profile 会临时生成 RSA key，重启后不保证 JWT 可继续验证。

## 协议状态与持久化

- 注册客户端由 SAS 的 JDBC `RegisteredClientRepository` 保存。
- OAuth2 授权记录与 Token 状态由 SAS 的 `OAuth2AuthorizationService` 保存到 Redis。
- Redis authorization store 的 namespace 与 key scene 由 `user-auth.authentication.oauth2.authorization-store` 配置；该配置属于 Redis 技术模块，不属于功能授权领域或 SAS 协议行为配置。
- Session Token、Session handoff ticket 与 H5 Session 由独立的 `user-auth-infrastructure-session-redis` 模块实现；它们不是 OAuth2 授权记录持久化的一部分。Bearer Access Token 与 Session Token 共享 `user-auth.authentication.access-token.ttl`，交付实现由同级 `provider` 选择，从配置层保证两种宿主访问令牌都是短时令牌；H5 与 handoff 生命周期分别位于 `user-auth.authentication.h5-session.idle-ttl/absolute-ttl/renewal-threshold` 和 `user-auth.authentication.session-handoff.ttl`。
- Session Token、H5 Session 与 handoff ticket 使用独立的应用端口和 Redis Store，并共同实现 `LoginSessionArtifactRevoker`。三个 Store 分别维护 loginSessionId 反向索引，以支持 logout 的物理级联删除；Session Token/H5 解析和 ticket 兑换还会验证父 LoginSession。接口层对应使用独立认证 Filter，并保证 Session Token Header 认证先于 H5 Cookie 认证。
- 登录会话是认证域记录；协议授权记录保存其 `session_id` 属性以支持刷新和登出撤销。

协议存储 schema、Redis 可用性、密钥轮换和客户端密钥轮换属于部署安全要求。领域模型不应直接依赖 SAS 的授权对象或存储对象。

## OIDC 的交付边界

OIDC 开关不是全局登录模式，也不应和微信登录或 Session Token 互斥。它仅表示此 SAS 部署是否提供 OIDC 协议端点；标准 OAuth2/OIDC 客户端由 `RegisteredClientRepository` 管理，不能复用内部 token client，也不能由外部请求临时声明。

当前开启开关后，仍必须完成以下工作才可对外承诺 OIDC 登录：

- 为浏览器授权请求提供认证入口和会话安全策略；现有手机号/外部身份 REST 登录接口不是 `/oauth2/authorize` 的浏览器认证页。
- 为 OIDC 客户端登记 `authorization_code`、PKCE、合法 redirect URI 与 `openid` scope；内部 token client 只允许自定义登录 grant。
- 确定 ID Token 的 `sub` 与可披露身份声明，补充 UserInfo、同意策略和端到端协议测试。

在这些条件落实前，不能仅因 Discovery 或 ID Token endpoint 已出现，就向客户端宣称 OIDC 登录已可用。
