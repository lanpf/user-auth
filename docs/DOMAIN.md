# user-auth 领域文档

本文以当前源码为准，说明 `user-auth` 的领域边界、已交付业务语义和仍处于模型阶段的能力。项目入口见 [README.md](../README.md)；ClientApp、channelCode 与入口信任边界见 [CLIENT_AND_CHANNEL_CONTEXT.md](CLIENT_AND_CHANNEL_CONTEXT.md)；OAuth2/OIDC 的技术协议实现见 [OAUTH2_OIDC_PROTOCOL_INFRASTRUCTURE.md](OAUTH2_OIDC_PROTOCOL_INFRASTRUCTURE.md)。

## 子域与代码边界

| 子域/共享模型 | 职责 | 当前代码位置 | 当前状态 |
| --- | --- | --- | --- |
| 认证 | 账户、凭据、认证挑战、外部身份、登录尝试、登录会话与认证流程。 | `domain.authentication` | 已交付登录、会话和登出流程。 |
| 功能授权 | 功能权限目录、角色、用户角色与直接权限授予，以及渠道授权策略。 | `domain.authorization` | 已接入关系型持久化、渠道策略管理/同步和授权查询；未接入资源服务器鉴权。 |
| 共享用户 | `User` 的创建、状态和用户标识。 | `domain.user` | 被认证流程使用。 |
| 通用领域能力 | 领域事件、标识和值对象基类。 | `domain.common` | 共享。 |

`user-auth` 不拥有 user 相关资料，只保留。OAuth2/OIDC endpoint、SAS 持久化、JWK 和 Token 格式属于外层协议基础设施，不属于功能授权子域。

## 认证子域

### 统一语言

认证子域把“账户是谁”“本次拿什么证明”“以后可以用什么身份入口登录”和“认证成功后如何访问”分开表达。

#### 账户与持久身份入口

- **User**：平台账户主体，以 `UserId` 标识。User 表达平台用户身份，不维护手机号、第三方 subject 或协议 Token。
- **AuthAccount**：User 的可登录认证账户聚合，以 `AuthAccountId` 标识，维护账户状态和 Credential 集合。当前一个有效 AuthAccount 必须且只能有一个 ACTIVE Mobile Credential；External Credential 可以有多个。
- **Credential**：已经过验证、持久绑定在 AuthAccount 上、可作为后续登录身份入口的长期认证关联。当前只有 `MOBILE` 与 `EXTERNAL` 两类。Credential 不是验证码、授权码、Access Token 或 Session。
- **Mobile Credential**：`CredentialType.MOBILE + issuer=LOCAL + principal=规范化手机号`。它是当前账户的基础登录入口，也是新建 AuthAccount 的必要组成部分；更换手机号时旧 Mobile Credential 进入 REVOKED，新手机号产生新的 ACTIVE Credential。
- **External Credential**：`CredentialType.EXTERNAL + issuer + principal`。它表达公共三方平台等外部主体与 AuthAccount 的唯一绑定，例如 `WECHAT_MINI_PROGRAM + openid`。它只能在账户已有有效 Mobile Credential 后绑定，不能脱离 AuthAccount 独立存在。
- **Public Third-party External Credential**：不是新的 CredentialType，而是 `CredentialType.EXTERNAL + CredentialIssuerType.PUBLIC_THIRD_PARTY` 的业务称呼，用于区分公共身份平台与受信任合作方。当前微信小程序 Credential 属于这一类。
- **CredentialIssuer**：Credential 或外部身份的签发来源，由稳定的 issuer code 和 issuer type 组成。当前类型包括 `LOCAL`、`INTERNAL_SYSTEM`、`TRUSTED_PARTNER`、`PUBLIC_THIRD_PARTY`；issuer type 描述信任边界，issuer code 标识具体来源。
- **Principal**：某个 CredentialIssuer 命名空间内的稳定主体标识。Mobile Credential 的 Principal 是手机号；公共三方 External Credential 的 Principal 通常是 openid、partner user id 等经服务端验证后取得的标识。

#### 本次认证证明与过程状态

- **Proof**：调用方在本次认证中提交的短期证明，例如短信验证码、外部 `AUTHORIZATION_CODE` 或 `SIGNED_ASSERTION`。Proof 只回答“本次如何证明”，不是 Credential；不得把一次性授权码本身当作长期 External Credential Principal，除非外部协议明确把验证结果解析为另一个稳定 subject。
- **AuthChallenge**：本服务签发的一次性认证挑战，以 `AuthChallengeId` 标识，绑定 challenge type、scene、target、过期时间、尝试次数和消费方。手机号验证码是 `SMS_OTP` AuthChallenge 的 secret；服务只保存经 `ChallengeSecretHasher` 处理的值。
- **ExternalIdentity**：`ExternalIdentityVerifier` 使用外部 Proof 与外部服务端安全交换或验签后得到的已验证事实，包含 issuer、稳定 principal、可选手机号及手机号是否已由该 issuer 可靠验证。客户端直接提交 subject 不构成 ExternalIdentity。
- **IssuerMobileTrustPolicy**：决定某个 issuer 声明的已验证手机号能否直接成为本次登录的可信手机号。网关验签只证明请求来自约定调用方，并不自动等同于 user-auth 信任其手机号声明。
- **LoginAttempt**：外部身份登录的短期过程聚合。它保存已验证 ExternalIdentity 的 issuer/principal、手机号验证状态、`PENDING_MOBILE → READY → COMPLETED` 状态和最终 SessionId；用于跨越“外部身份已验证”和“手机号补充验证已完成”两个步骤，不是可访问业务接口的会话。
- **RegistrationProcess**：手机号首次注册的应用层过程状态，用于在本地账户创建、user 初始化与最终登录之间提供幂等恢复。它是跨系统注册编排记录，不是认证领域长期身份入口。

#### 客户端、登录会话与访问凭据

- **ClientApp**：受管理的接入应用或调用方；以 appId 标识，其 platform 和 renewalPolicy 由服务端注册配置解析，外部请求不能自行选择续期策略。
- **Client**：LoginSession 中的终端客户端快照，保存本次登录时解析出的 appId、platform 和版本；它不是 OAuth2 `RegisteredClient`。
- **ChannelContext**：经入口安全校验后的业务渠道上下文；独立于 ClientApp，可作为渠道功能授权策略的授权来源，不决定 Credential 归属或 Token scope。
- **Device**：登录时提交的设备上下文快照，用于会话记录和审计；当前普通 deviceId 不构成 DPoP 或设备私钥持有证明。
- **LoginSession**：一次成功登录形成的领域会话，以 SessionId 标识，关联 User、AuthAccount、本次实际使用的 Credential、Client、Device、登录场景、状态和绝对到期时间。协议授权、Bearer Access Token 和 Browser Session 都通过 SessionId 关联这个领域事实。
- **Host Access Credential**：认证成功后交付给宿主、用于后续恢复 LoginSession 的访问凭据。当前统一为 OAuth2 Bearer Access Token，可采用自包含 JWT 或不透明 Reference Token；它不是 AuthAccount Credential。
- **AuthenticatedSession**：应用内部使用的已认证会话快照，携带 `userId + authAccountId + sessionId`。gateway 向业务接口传递 `subjectType + userId + sessionId`；user-auth 必须区分 `HOST_SESSION` 与 `BROWSER_SESSION`，并从 LoginSession 恢复和校验 AuthAccount，不能信任调用方指定账户。
- **Session Handoff Ticket / Browser Session**：前者是宿主向另一载体交接当前认证结果的一次性短期票据；后者是 H5 兑换后取得的独立 Cookie 会话。二者都不是账户 Credential，也不能用于改变父 AuthAccount 的身份绑定。

### Credential 的建立与登录路径

Credential 的建立必须遵循“先完成本次证明，再创建或查找 AuthAccount，最后按规则持久绑定”的顺序。一次登录可以使用既有 Credential，但不会因为每次登录而重复创建 Credential。

| 场景 | 本次证明 | Mobile Credential | External Credential                                                                      | 最终用于创建 LoginSession 的 Credential |
| --- | --- | --- |------------------------------------------------------------------------------------------| --- |
| 手机验证码注册/登录 | `SMS_OTP` AuthChallenge 的 code | 手机号无账户时，注册事务创建 AuthAccount 并同时创建；已有账户时复用，不重复创建 | 不创建                                                                                   | Mobile Credential |
| 合作方可信手机号授权码直接注册/登录 | 网关已验签且 user-auth issuer policy 信任的 `issuer + authorizationCode + mobile` | 手机号无账户时创建；已有账户时复用 | 不创建。合作方 authorizationCode 只进入短期 LoginAttempt，使用 `DO_NOT_BIND` 完成登录    | Mobile Credential |
| 公共三方平台两步注册/登录 | 第一步验证外部 authorization code；第二步以 `COMPLETE_EXTERNAL_LOGIN` 手机验证码确认手机号 | 手机号无账户时先创建；已有手机号账户时复用 | Mobile Credential 已存在后，在同一外部登录流程中绑定 `issuer + stable principal` | 新绑定的 External Credential；之后可直接复用 |
| 先手机号注册/登录，再绑定公共三方平台 | 已认证宿主登录态 + 新的外部 authorization code | 已存在且保持 ACTIVE | `POST /api/user-auth/credentials/external/bind` 验证外部身份后绑定                       | 本次不创建 LoginSession；后续外部登录使用该 External Credential |
| 已绑定公共三方平台的无状态登录/续期 | 新的一次性外部 authorization code | 必须已经存在，不创建、不替换 | 必须已经绑定且 ACTIVE，不创建、不迁移                                                    | 既有 External Credential |

#### 手机验证码注册/登录

`POST /api/user-auth/auth-challenges` 创建面向手机号和登录场景的 SMS OTP AuthChallenge；`POST /api/user-auth/login/mobile-otp` 校验 challengeId + code。验证码只是 AuthChallenge secret，不会保存为 Credential。

- 手机号尚未关联账户：创建 UserId、AuthAccount 与唯一的 ACTIVE Mobile Credential，随后通过 `UserGateway` 初始化 user，再创建 LoginSession。
- 手机号已有账户：校验账户和 Mobile Credential 状态，直接使用该 Credential 创建新的 LoginSession。
- 同一手机号的并发首次登录由 `MobileOtpLoginLock` 串行化；RegistrationProcess 保证跨本地注册和 user 初始化的幂等恢复。

#### 合作方可信手机号授权码直接注册/登录

`POST /api/user-auth/login/external/trusted-mobile` 面向受保护的合作方链路。网关负责调用方验签、时效和授权码重放校验；user-auth 仍通过 `IssuerMobileTrustPolicy` 判断是否接受该 issuer 声明的已验证手机号。

服务把外部证明和可信手机号放入 READY LoginAttempt，随后按手机号查找或创建 AuthAccount。新账户只创建 Mobile Credential；已有账户复用其 Mobile Credential。合作方 authorizationCode 是外部单号式的一次性证明，不是稳定 partner subject，因此登录使用 `ExternalCredentialBinding.DO_NOT_BIND`：不会创建合作方 External Credential，也不能靠同一授权码再次登录。

#### 公共三方平台两步注册/登录

典型公共三方平台不能直接替代本服务的首次手机号确认：

1. `POST /api/user-auth/login/external/attempts` 提交 issuer 与 authorizationCode。`ExternalIdentityVerifier` 在服务端向第三方交换并验证 code，取得稳定 principal，创建短期 LoginAttempt。
2. 若该 External Credential 已绑定现有 AuthAccount，LoginAttempt 直接 READY；账户本身已经满足有效 Mobile Credential 不变量，不再要求补手机号。
3. 若尚未绑定且 issuer 的手机号不受信任，LoginAttempt 为 PENDING_MOBILE。客户端为目标手机号申请 scene=`COMPLETE_EXTERNAL_LOGIN` 的 SMS OTP AuthChallenge，再调用 `POST /api/user-auth/login/external` 提交 loginAttemptId + challengeId + code。
4. 手机号验证成功后，服务先按手机号查找 AuthAccount；不存在时创建 AuthAccount 和 Mobile Credential，存在时复用。随后才绑定公共三方 External Credential，并以该 External Credential 创建 LoginSession。

因此公共三方 External Credential 的首次产生有两条合法路径：外部两步登录在手机号确认后“创建/复用 Mobile Credential，再绑定 External Credential”；或者先完成独立手机号注册/登录，再由已认证宿主显式绑定。不存在“只有公共三方 External Credential、没有 Mobile Credential”的有效 AuthAccount。

#### 已认证宿主绑定与后续外部登录

`POST /api/user-auth/credentials/external/bind` 必须使用 Bearer Access Token 恢复的宿主 AuthenticatedSession。服务端从 Principal 决定 User/AuthAccount，客户端只能提交 issuer 与 authorizationCode。验证得到的 ExternalIdentity 已绑定当前账户时幂等成功；已绑定其他账户时拒绝，不能静默迁移或合并账户。

绑定完成后，`POST /api/user-auth/login/external/bound` 才能作为该公共三方的无状态登录/续期入口。该入口只查找既有绑定，要求 AuthAccount 和 Mobile Credential 有效；它不建账、不补手机号、不绑定 Credential，并由 ClientApp `renewalPolicy=EXTERNAL_AUTHORIZATION_CODE` 控制。

### 已实现的用例与 REST 接口

| 接口 | 语义 | 认证要求 |
| --- | --- | --- |
| `POST /api/user-auth/auth-challenges` | 创建认证挑战，例如发送手机号验证码。 | 无 |
| `POST /api/user-auth/login/mobile-otp` | 校验手机号验证码并完成登录。 | 无 |
| `POST /api/user-auth/login/external/attempts` | 建立外部身份登录尝试。 | 无 |
| `POST /api/user-auth/login/external` | 确认 LoginAttempt 并完成外部身份登录。 | 无 |
| `POST /api/user-auth/login/external/bound` | 使用已绑定外部 Credential 的授权码无状态登录或续期。 | 无 |
| `POST /api/user-auth/login/external/trusted-mobile` | 网关已验签的合作方以外部授权码和已核验手机号完成首次登录。 | 无（仅受保护网关链路） |
| `POST /api/user-auth/credentials/external/bind` | 将已验证的外部授权码身份绑定至当前宿主登录态所属认证账户。 | 宿主登录态（Bearer Access Token） |
| `POST /api/user-auth/logout` | 登出当前宿主登录态所属的 LoginSession。请求体不携带 session id。 | 宿主登录态（Bearer Access Token） |
| `POST /api/user-auth/web-view-handoffs` | 小程序或 App 以当前 Bearer Access Token 创建首次 WebView 一次性交接 ticket；请求显式声明 `target=BROWSER_SESSION`。 | 宿主登录态（Bearer Access Token） |
| `POST /api/user-auth/web-view-handoffs/exchange` | H5 以一次性 ticket 和 `target=BROWSER_SESSION` 换取 `BROWSER_SESSION` Cookie。 | 一次性 ticket |

登录成功后，应用层通过协议端口请求令牌，并返回 Token 响应。首次认证所需的 user 初始化通过 `UserGateway` 应用端口完成。

### Application 用例与 Port 边界

`user-auth-application` 是认证用例的编排层：它控制事务顺序，协调 AuthAccount、Credential、AuthChallenge、LoginAttempt、LoginSession 等领域对象，并通过 Port 请求外部能力。Application 不依赖 SAS、Redis、微信 SDK、短信实现、OpenFeign 或具体数据库类型；这些技术只能在外层实现 Port。领域 Repository 则定义在 domain，表达聚合持久化契约，不与 application Port 混为一类。

```text
REST / Facade
  → Application Command Service / Authentication Process
      → Domain aggregate、domain service、domain repository
      → Application Port
          ← infrastructure / oauth2-sas / oauth2-redis / session-redis adapter
```

当前 Port 按职责分为：

| Port 组 | 主要 Port | Application 需要的能力 | 当前外层实现或边界 |
| --- | --- | --- | --- |
| Challenge | `AuthChallengeDispatcher`、`OneTimeCodeGenerator`、`ChallengeSecretHasher`、`AuthChallengePolicyProvider` | 生成、哈希、派发并按配置约束一次性 challenge | 通用 infrastructure；派发适配器可替换为短信等实现，应用层不感知供应商 |
| 外部身份验证 | `ExternalIdentityVerifier`、`ExternalIdentityVerifierRegistry`、`IssuerMobileTrustPolicyProvider` | 按 issuer 和 proof type 验证一次性外部证明，取得可信 ExternalIdentity，并解析手机号信任策略 | 通用 infrastructure 注册表；微信小程序 verifier 是当前公共三方实例 |
| 并发与原子性 | `AuthChallengeIssueLock`、`MobileOtpLoginLock`、`RefreshTokenRotationLock` | 对 challenge 签发、同手机号首次注册、Refresh Token rotation 建立用例级串行边界 | Redis lock adapter；测试可使用 direct 实现 |
| 登录结果交付 | `LoginTokenIssuer` | 在认证流程完成后交付宿主访问凭据，同时保持手机号登录和外部登录相同的应用输入输出语义 | `SasLoginTokenIssuer`；JWT/Reference 格式由 SAS 配置决定 |
| 访问凭据续期 | `LoginTokenRefresher`、`ClientRenewalPolicyResolver` | 解析受管 ClientApp 策略，并在允许时取得新的宿主访问凭据 | SAS refresher；共享 ClientApp properties resolver |
| 会话存储与交接 | `BrowserSessionStore`、`SessionHandoffStore` | 创建/解析 Browser Session，以及原子签发/消费 handoff ticket | `user-auth-infrastructure-session-redis` |
| 统一生命周期撤销 | `LoginSessionRevoker` | LoginSession 撤销后，级联撤销协议授权、子会话和未消费 ticket | SAS revoker 以及 H5/handoff Redis Store |
| 跨服务协作 | `UserGateway` | 新 User 注册后初始化 | 由运行时显式提供的 infrastructure adapter；缺失时装配失败 |

这些 Port 是服务可替换性的正式边界，而不是为了隐藏任意代码而增加的接口。例如 `LoginTokenIssuer` 让认证用例不依赖 SAS grant 与 Token 表示；`ExternalIdentityVerifier` 允许增加新的公共三方 issuer，而不用把第三方 HTTP 响应模型带入 application/domain；`BrowserSessionStore` 和 `SessionHandoffStore` 分开，则防止目标会话生命周期与通用一次性交接票据被一个 Redis 接口混合。`SessionHandoffCommandFacade` 是通用会话交接能力，创建和兑换命令都必须显式携带目标会话类型；当前外部协议只开放 `BROWSER_SESSION`，ticket 会绑定该类型，兑换时必须再次匹配，不能把其他目标类型的 ticket 兑换成 Browser Session。

Challenge 交付尤其依赖外层装配：非生产 profile 使用固定验证码和 `NoopAuthChallengeDispatcher`，只记录派发请求；生产 profile 使用安全随机验证码，但必须由部署额外提供真实的 `AuthChallengeDispatcher`（例如短信 adapter），否则不能形成可用的手机号认证链路。领域和 application 不直接选择短信供应商。

Boot 只负责选择和装配具体 adapter。若某个实现模块没有进入运行时依赖，对应能力即不可用；application 不提供反射、静态工具或默认技术实现来绕过 Port。Interfaces 只负责 HTTP/Header/Cookie/Principal 的协议转换，再调用 Facade/Application，不承担 Credential 建立或会话续期规则。

### 认证成功后的宿主访问凭据

手机号验证码、外部身份和外部授权码回答“如何证明用户身份”；OAuth2 Bearer Access Token 回答“认证成功后，宿主以后使用什么凭据访问服务”。Access Token 可采用两种表示，但不是两种身份认证方法：

```text
手机号验证码 / 外部身份 / 外部授权码
  → 完成身份认证
  → 创建 LoginSession
  → SAS 根据 oauth2.access-token.format 签发 Bearer Access Token
```

| Access Token 格式 | 客户端携带 | 签发实现 | 服务端保存 | 访问验证 |
| --- | --- | --- | --- | --- |
| `SELF_CONTAINED` | `Authorization: Bearer <JWT>` | SAS `JwtGenerator` 与 RSA 私钥签发 RS256 JWT | OAuth2 Redis 保存 OAuth2Authorization 与 Token 状态；Token 原值只由客户端持有，Redis 保存 SHA-256 哈希 | 网关和 user-auth 本地校验签名、issuer、audience、有效期和必备声明 |
| `REFERENCE` | `Authorization: Bearer <opaque access token>` | SAS `OAuth2AccessTokenGenerator` 生成高熵不透明值 | OAuth2 Redis 保存授权状态、声明及 Token 哈希 | 网关通过专用 client 调用 SAS 标准 introspection endpoint，并向 user-auth 转发可信会话上下文 |

`user-auth.authentication.oauth2.access-token.format/ttl` 配置格式与有效期，默认 `SELF_CONTAINED`、15 分钟。JWT 由离线校验节点自然失效，Reference Token 由在线授权状态即时控制；两者的登录响应、Bearer 携带方式、scope、Refresh Token 能力和 LoginSession 语义相同。LoginSession 的绝对生命周期单独配置，不等同于 Access Token TTL。

#### OAuth2 Bearer Access Token 的签发与保存

`SasLoginTokenIssuer` 是 application `LoginTokenIssuer` 的 SAS adapter。它把 application 登录命令映射为服务内部的 OAuth2 自定义 grant，请求本机 SAS Token Endpoint。SAS 完成领域认证流程后创建 OAuth2Authorization，并签发包含标准 `sub`（UserId）、`auth_account_id`、`session_id` 的短期 JWT 或 Reference Access Token；不再重复签发 `user_id`。是否同时签发 Refresh Token 由 ClientApp renewalPolicy 决定。

协议职责由两个模块协作完成：

- `user-auth-infrastructure-oauth2-sas`：Token Endpoint、grant converter/provider、RegisteredClient、scope、JWT/JWK、Reference Token、签名与 Token 生成。
- `user-auth-infrastructure-oauth2-redis`：实现 SAS `OAuth2AuthorizationService`，保存授权记录、Token 状态、session_id 关联和 Refresh Token family 所需索引；持久化前以 SHA-256 替换 Access/Refresh Token 原值。

JWT 本身由客户端持有并通常本地验证，因此 Redis 中撤销协议状态不会让已发出的 JWT 在所有资源服务器上即时失效；Reference Token 每次在线查询授权状态，logout 后可即时失效，但依赖 Redis 和 introspection 的可用性。Redis 协议状态同时服务于刷新、重放检测、Token family 撤销、Reference Token introspection、协议查询和登出联动。

两种 Access Token 格式都由 gateway 归一为可信 `X-Subject-Type=HOST_SESSION + X-User-Id + X-Session-Id` 请求上下文。user-auth 接口层据此授予宿主会话入口权限，应用层再回查 LoginSession 验证归属并恢复认证账户。`X-Subject-Type` 尚待 gateway、framework 与 user-auth 完成代码迁移；迁移后缺失类型不得默认按 HOST_SESSION 处理。调用方不能在单次登录请求中选择格式；登录 API 始终返回 `tokenType + accessToken`，其中 `tokenType` 为 `Bearer`。访问凭据只解决当前请求的身份恢复，业务接口仍需继续执行功能权限和业务资源范围判断。

### 单个 LoginSession 的统一生命周期与级联撤销

当前以 `LoginSession.sessionId` 作为一次成功登录及其派生对象的统一关联根，不额外创建 SessionFamily。一个 LoginSession 可以关联 SAS OAuth2Authorization、Access/Refresh Token、Browser Session 和尚未消费的 handoff ticket。

`POST /api/user-auth/logout` 先把当前 LoginSession 变为 REVOKED，再通过 `LoginSessionRevoker.revoke` 集合执行幂等级联清理：

```text
LoginSession(REVOKED)
  → SAS OAuth2Authorization / Refresh Token 状态失效
  → 删除 Browser Session 与 loginSessionId 反向索引
  → 删除未消费 handoff ticket 与 loginSessionId 反向索引
```

Browser Session 在正常解析时仍会回查父 LoginSession，作为物理删除失败或并发窗口的安全兜底。Handoff ticket 在兑换后同样重新校验父 LoginSession；已登出、已过期或 User/AuthAccount 不匹配时，ticket 即使尚未被清理也不能创建 Browser Session。Browser Session 和 handoff ticket 创建时的实际 TTL 均取自身配置与父 LoginSession 剩余时间的较小值，不能越过父会话的绝对到期时间。

JWT Access Token 是明确例外：logout 会使 Redis 中的 SAS 协议授权状态失效，并阻止 Refresh Token 继续使用，但已经签发的 JWT 仍由资源服务器本地验证，在 `exp` 前不会查询 LoginSession 或 deny-list。Reference Token 则通过在线 introspection 立即看到 inactive 状态。若未来要求 JWT 立即失效，必须增加 deny-list 或资源服务器在线会话校验。

当前**尚未引入 SessionFamily**。策略二“外部一次性授权码无状态续期”本质上是重新认证，会创建新的 LoginSession；新旧 LoginSession 之间没有 familyId 或 lineage 关联。因此目前只完成“单个父 LoginSession 及其所有派生对象”的统一撤销，尚未完成“同一次初始登录及其所有重新认证续期代际一起撤销”。如果未来需要该能力，应新增 SessionFamilyId，把初始 LoginSession 与策略二产生的后续 LoginSession 纳入同一 family，再在 family 维度撤销；不能用 UserId 直接替代，否则会误伤该用户其他设备上的独立登录。

### 合作方外部直连登录 REST 契约

```http
POST /api/user-auth/login/external/trusted-mobile
Content-Type: application/json
X-Client-App-Id: partner-service
X-Client-Platform: SERVICE
X-Client-Version: 1.0
X-Channel-Code: PARTNER_A

{
  "issuer": "PARTNER_A",
  "authorizationCode": "partner one-time authorization code",
  "mobile": "13800138000"
}
```

该接口只接受网关验签后的受保护流量。网关必须校验合作方身份、签名覆盖的请求体、时效和 `authorizationCode` 重放，并注入 `X-*` 上下文；`user-auth` 不重复验签。只有 `user-auth.authentication.external-identity.issuer-policies.<issuer>.trusted-mobile=true` 的 issuer 才能把外部已核验手机号作为可信登录依据，未配置或关闭时不能绕过额外手机号验证。服务按 `mobile` 查找或创建账户，成功响应与普通外部登录相同。`issuer + authorizationCode` 只代表本次 `AUTHORIZATION_CODE` 证明，不建立外部 Credential，也不能用作后续登录标识。

`POST /api/user-auth/login/external/bound` 是通用的无状态登录/续期入口，请求体为 `issuer`、`authorizationCode` 和可选设备字段。它验证外部授权码后，只允许使用已绑定到 AuthAccount 的外部 Credential 登录；对应账户必须已有有效 LoginMobile。该接口不接受 `mobile`、不建账、不绑定 Credential，也不需要旧 Access Token。微信小程序只是 `issuer = WECHAT_MINI_PROGRAM` 的一个实现：Access Token 到期后重新执行 `wx.login` 并调用此接口。

已登录宿主可使用 `POST /api/user-auth/credentials/external/bind` 绑定外部 Credential。JWT 或 Reference Bearer Access Token 均转换为统一的 `AuthenticatedSession` Principal，接口从中取得 `userId` 与 `authAccountId`，客户端不得指定绑定账户；请求体只提交 `issuer` 和 `authorizationCode`。同一外部 Credential 已绑定当前账户时幂等成功，已绑定其他账户时拒绝，不能自动迁移或合并账户。`BROWSER_SESSION` 只代表受限的 WebView 会话，不允许绑定或修改账户 Credential。

### 宿主访问凭据的续期

面向移动端、小程序、桌面端等 public client，Bearer Access Token 应保持短期。“续期”的统一语义是：当前访问凭据即将或已经失效后，重新取得新的 Bearer Access Token。

续期能力由服务端 `user-auth.authentication.client-apps.<clientAppId>.renewal-policy` 配置，调用方不得在请求中提交或切换策略：

| Access Token 格式 | `REFRESH_TOKEN_ROTATION` | `EXTERNAL_AUTHORIZATION_CODE` | `NONE` |
| --- | --- | --- | --- |
| `SELF_CONTAINED` / `REFERENCE` | 支持；返回新的 Access Token 与 Refresh Token | 支持；验证外部一次性授权码后返回新的 Access Token | 不提供自动续期 |

续期不会绕过账户、Credential 与 ClientApp 校验。策略二会重新完成一次外部身份认证并创建新的 LoginSession；它是相对于 Refresh Token 协议状态而言的“无状态续期”，不表示 Credential、LoginSession 或访问凭据存储全部无状态。

#### 策略一：Refresh Token rotation

客户端持有 Access Token 和 Refresh Token，以 Refresh Token 换取新的令牌对。它的前提是客户端本地存储可被视为**相对受保护**的客户端存储：它不像 H5 `localStorage` 那样能被同源页面 JavaScript 直接读取，但仍可能因 root/越狱、运行时注入、篡改客户端、调试或日志泄漏而失窃。因此 Refresh Token 是“可能失窃的长期凭据”，不是绝对安全的密钥。

此策略必须同时具备：

- 每次刷新均 rotation：签发新 Refresh Token 后，旧 token 原子地立即失效；服务端仅保存其不可逆哈希。
- reuse detection：已失效的旧 token 再次被使用时，视为重放，撤销整个 token family，并要求重新认证。
- token family、绝对有效期、所属 ClientApp 与 LoginSession 绑定，以及刷新、重放和撤销审计；需要更强设备持有证明时应叠加 DPoP 或设备密钥签名。
- 短期、受众受限的 Access Token；高风险操作不可只依赖 Access Token。

rotation 与 reuse detection 只能缩短 Refresh Token 泄漏后的持续利用窗口并发现重放：攻击者若先使用被窃 token，仍可能先得到新的 token，直至合法客户端下一次刷新触发检测。

当前实现由受管理的 `ClientApp.renewalPolicy` 选择策略：

- `REFRESH_TOKEN_ROTATION`：登录响应同时返回 Access Token 与 Refresh Token；允许调用 `POST /api/user-auth/login/refresh`。该接口只接收 `PARENT`，ClientApp 从网关注入的 `X-Client-App-Id` 取得。
- `EXTERNAL_AUTHORIZATION_CODE`：登录响应不含 Refresh Token，只能使用下述外部一次性授权码入口续期；调用 refresh 接口会被拒绝。
- `NONE`：不开放任何自动续期能力。

Refresh Token 由 SAS 生成，Redis 仅保存不可逆哈希。每次刷新在 refresh token 哈希维度取得分布式锁并执行 rotation；旧 token 被记录为 token-family 历史，之后再次使用会撤销当前 Access Token、Refresh Token 及关联 LoginSession。刷新前还必须同时满足：请求 ClientApp 与授权记录及 LoginSession 的 ClientApp 一致、LoginSession 仍为 ACTIVE 且未超过其绝对到期时间。当前 LoginSession 记录设备快照，但 refresh 请求尚未提供 DPoP/设备私钥持有证明，因此不能把普通 `deviceId` 等同于抗窃取的设备绑定。

#### 策略二：外部一次性授权码无状态续期

客户端只持有短期 Bearer Access Token。凭据到期或即将到期时，客户端从已接入的外部身份提供方取得一次性授权码，再调用 `POST /api/user-auth/login/external/bound`。服务仅允许 `renewalPolicy=EXTERNAL_AUTHORIZATION_CODE` 的 ClientApp 使用该入口；验证成功后返回新的 Access Token，且不向 `NONE` ClientApp 签发新的凭据。

此策略有两个不可省略的前提：

1. 对应的外部 Credential 已存在且唯一绑定至 AuthAccount，例如 `issuer = WECHAT_MINI_PROGRAM`、`subject = openid`；静默续期只能查找该绑定，不能创建账户、绑定或迁移 Credential、修改手机号或合并账户。
2. 外部身份提供方可由服务端用一次性授权码安全取得可信的外部身份；服务端必须校验 issuer、客户端标识、授权码时效与单次消费语义，且不能相信客户端直接提交的外部 subject。

此策略必须同时具备：一次性 code 的短时和单次消费约束、issuer 与预期外部客户端标识校验、服务端交换而非前端交换、外部 Credential/账户状态校验、授权码重放防护、短期且受众受限的 Access Token，以及首次绑定 Credential 时更强的认证步骤。它降低了单独窃取 Access Token 后的长期接管风险；但设备或客户端运行时被完全控制时，攻击者仍可能重新取得外部授权码，这不是本策略能够消除的风险。

微信小程序是策略二的具体实例：小程序调用 `wx.login` 取得 code，`user-auth` 以服务端持有的微信 app secret 交换并验证 code，使用返回的 openid 查找 `WECHAT_MINI_PROGRAM` Credential。前端直接提交 openid 不构成可信登录证明。

当前服务已实现策略一及策略二的微信小程序实例；其他外部身份提供方只有在实现相同的一次性授权码验证契约后才能使用策略二。两种 Access Token 格式都支持全部 ClientApp renewalPolicy。

### 宿主 App 进入 WebView H5 的会话交接

本方案适用于已登录的宿主 App（包括小程序、原生 App 等）打开 WebView H5。宿主的 Access Token 不进入 H5；H5 也不持有宿主的 Refresh Token。H5 使用独立、服务端可查询和撤销的 `BROWSER_SESSION` HttpOnly Cookie。

一次性交接票据本身是与目标会话实现无关的通用应用能力：`SessionHandoffService` 只负责签发和原子消费票据，不创建 Browser Session，也不持有 H5 的 idle/absolute TTL。票据必须绑定 `SessionHandoffTarget`；当前仅登记 `BROWSER_SESSION`。`SessionHandoffCommandService` 才负责以 `BROWSER_SESSION` 目标消费票据并调用 `BrowserSessionStore` 创建具体会话。未来新增其他目标会话时，应新增对应的目标类型和专用交接用例，不能把目标会话创建逻辑重新放回通用票据服务。

```text
宿主当前 LoginSession + Bearer Access Token
  → 创建短时、一次性的 handoff ticket
  → 将 ticket 交给初始 H5 页面
  → H5 原子兑换 ticket
  → Set-Cookie: BROWSER_SESSION

H5 正常请求
  → gateway 校验 BROWSER_SESSION 与父 LoginSession
  → 写入 X-Subject-Type=BROWSER_SESSION + X-User-Id + X-Session-Id
  → 未超过 absolute TTL 时按 idle window 滑动续期

宿主再次进入 H5
  → 使用当前 Bearer Access Token 创建新的 handoff ticket
  → H5 创建或刷新独立 BROWSER_SESSION
```

初始 ticket 应仅放入 H5 URL fragment，H5 读取后立即调用 exchange endpoint，并使用 `history.replaceState` 清除 fragment；不得放入 query string、日志、埋点或 Referer。Browser Cookie 不是宿主 Access Token 的载体，不能把 JWT Access Token 直接写入 Cookie。

宿主 Bearer Access Token 可以创建目标为 `BROWSER_SESSION` 的 handoff ticket，H5 原子兑换后取得独立的 `BROWSER_SESSION` HttpOnly Cookie。handoff ticket 不是宿主续期凭据，Browser Session 也不是 Bearer Token 的另一种传输形式。Browser Session 在 absolute TTL 内按 idle window 滑动续期；达到 absolute TTL 后，宿主必须先确保自己的 Access Token 仍有效，必要时按 ClientApp 策略取得新凭据，再重新创建 handoff ticket。

该方案必须搭配以下安全设计：

- **原子单次兑换**：ticket 是随机高熵、短时凭据（建议 30–60 秒）。兑换必须使用 Redis `GETDEL` 或等价 Lua 脚本完成“读取并删除”；不得先查询再删除。并发兑换时仅一个请求可成功。服务端应以 ticket 哈希或 HMAC 派生值作为 Redis 索引，避免持久化原始 ticket。消费成功后创建 Browser Session 失败时，不恢复 ticket，宿主重新申请即可。
- **严格绑定**：ticket 当前绑定 `userId`、`authAccountId`、宿主 `parentLoginSessionId`、`handoffId`、目标会话类型与有效期；兑换入口必须以期望的目标会话类型消费，目标不一致时票据无效，不能把 H5 ticket 用于未来的其他会话。目标 H5 ClientApp/origin 的进一步绑定必须以受管理的目标应用与 origin 目录为前提，不能把客户端任意提交的字符串当成可信绑定；该目录尚未实现。
- **父会话关联与级联失效**：每个 Browser Session 记录 `parentLoginSessionId`。gateway 的 BrowserSessionAuthenticator 在线校验自身状态、idle TTL、absolute TTL，并确认父 LoginSession 为 `ACTIVE`。宿主主动登出或当前会话被强制下线时，通过已实现的 `parentLoginSessionId → Browser Session credential` Redis 反向索引立即删除全部关联 Browser Session；父会话状态校验是级联删除的兜底。全账户强制下线和跨重新认证代际撤销仍需要未来的 SessionFamily/账户会话能力。
- **会话职责隔离**：Bearer Access Token 是宿主访问凭据，Browser Session 是派生的浏览器 Cookie 会话。二者具有不同的传输、存储和生命周期，gateway 必须使用不同的 authenticator，并通过 `X-Subject-Type` 保留权限差异；不能把 Browser Session 转换成宿主 Header 权限。
- **宿主权限隔离**：Bearer Access Token 在接口层获得宿主会话权限；`BROWSER_SESSION` 只获得受限 H5 会话权限。Browser Cookie 不得调用绑定外部 Credential、登出父 LoginSession 或创建新 handoff ticket 等宿主级命令。H5 自身退出需要独立的 Browser Session 结束能力，不能复用宿主 `logout`；该独立端点当前尚未实现。
- **有限滑动窗口**：普通 H5 请求只在剩余 idle TTL 低于设定阈值时延长会话并刷新 Cookie，避免每次请求写存储。必须同时配置不可滑动的 absolute TTL，防止持续访问令会话无限存活。达到 absolute TTL 或父 LoginSession 失效时，必须由宿主重新创建 ticket。
- **Cookie 与 CSRF 防护**：`BROWSER_SESSION` 必须使用 `HttpOnly`、`Secure` 和恰当的 `SameSite`（优先 `Lax`，不依赖跨站跳转时可用 `Strict`）。Cookie 鉴权的写操作仍必须实施 Origin/Referer 校验及 CSRF token 或双提交 token，不能仅依赖 SameSite。
- **最小暴露与审计**：H5 永不读取宿主 Access Token；ticket 创建、兑换失败、重复兑换、会话滑动、宿主登出和级联撤销均应审计，并按 ClientApp、origin、账户和会话维度限流。

本方案不需要 H5 与宿主之间为 Cookie 续期建立 SSE、长轮询或 `renewalChannelKey` 通道。早期 WebView SSE handoff 实现已删除；当前采用“宿主再次进入时重新 ticket 交接 + Browser Cookie 滑动窗口”模型。

### 关键业务约束

- 认证挑战必须在有效期内、用途和目标匹配且未超过校验限制时才能消费。
- 外部登录必须先取得有效的 LoginAttempt，再完成确认；外部身份归属应保持唯一且可追溯。LoginAttempt 是多步骤登录流程聚合，AuthChallenge 仍是短信验证码等原子认证证明。
- 外部授权码登录仅适用于网关已经验证合作方签名、调用方绑定、时效和重放的受保护链路。请求中的 `issuer + authorizationCode` 是 `AUTHORIZATION_CODE` 型一次性证明：它只进入本次短期 LoginAttempt，绝不能绑定为长期 `Credential`；账户仅按已核验的手机号查找或创建。
- 登录会话是认证成功的领域事实；协议授权记录以 `session_id` 将 Token 与该会话关联。
- `logout` 只处理当前 Bearer Access Token 归一后的 `AuthenticatedSession.sessionId`，不会接受客户端指定任意会话，也不接受 `BROWSER_SESSION`。存在协议授权记录时会一并撤销；JWT Access Token 采用短期自然失效策略，Reference Token 在线 introspection 后即时失效。
- Session handoff ticket 为随机一次性凭据，默认 60 秒；它绑定 `userId`、`AuthAccountId`、`LoginSession`、服务端生成的 `handoffId` 与目标会话类型。签发 TTL 不超过父 LoginSession 剩余时间，兑换时重新校验父会话；logout 通过 loginSessionId 反向索引删除未消费 ticket。通用票据服务不创建任何具体会话；WebView H5 专用用例只接受目标为 `BROWSER_SESSION` 的 ticket，不能取得宿主 Access Token。

## 功能授权子域

功能授权回答“用户是否被授予执行某个功能的资格”，例如 `order:refund`；它不回答该用户能否操作某一条 `orderId`。后者需要业务服务按资源归属作数据访问判断。

当前存在以下领域表达：

- `Permission`：以 `PermissionCode` 为聚合标识的功能权限目录项。
- `Role`：以 `RoleCode` 为聚合标识的角色，关联 PermissionCode 集合。
- `UserRoleGrant`：用户被授予角色的记录，携带不可省略的 `sourceType` 与 `sourceId`。
- `UserPermissionGrant`：用户被直接授予权限的记录，携带不可省略的 `sourceType` 与 `sourceId`。
- `AuthorizationDomainService`：将角色权限和直接授予合并为有效功能权限的领域服务。
- `ChannelAuthorizationPolicy`：以 `ChannelCode` 标识、带版本的渠道授权策略，状态为 `DRAFT / ACTIVE / DISABLED`。
- `UserChannelPolicyApplication`：记录用户已应用某渠道策略的版本，是同步游标，不是授权本身。

上述类型的 `code` 是稳定的业务标识，未另外引入数据库代理 id。授权记录的来源用于追溯和独立撤销，而不是权限作用域：角色与 Permission 本身不带渠道范围。`CHANNEL_AUTHORIZATION_POLICY + channelCode` 是渠道策略授予的来源；`MANUAL + sourceId` 可用于人工或审批来源。同一用户、同一授权目标、同一来源的授予是幂等的；不同来源可以并存。一个来源撤销后，只要仍有其他有效来源，用户仍保有该角色或权限。

当前实现包括 Role、Permission、来源授权、渠道策略和策略应用游标的 JPA adapter，以及 Permission/Role 目录管理、渠道策略管理、批量重放和用户授权查询用例。Role 和渠道策略的多值目标以关系型主从记录持久化，避免把集合映射绑定到 JPA；策略更新使用 `channelCode + expectedVersion` 条件更新，未命中即返回既有版本冲突。策略写事务不会等待全量补偿；reconciliation 每次只处理稳定 userId 顺序的有限批次，并以 `appliedVersion` 作为持久化恢复检查点。策略停用或应用版本落后时，渠道来源授权在查询中 fail-closed。Permission 与 Role 的 `code` 创建后不可变；目录项不提供物理删除，使用 `ACTIVE / DISABLED` 状态维护引用稳定性。Permission 可修改名称与归属服务，Role 可修改名称并整体替换 PermissionCode 集合；保存或启用 Role 时，所有引用的 Permission 必须存在且启用。因此：

- 任何现有 Access Token 均不携带角色或 PermissionCode。
- 管理接口可以查询用户的有效角色、有效 PermissionCode 及每条授予来源，但当前 `user-auth` 不提供供业务资源服务器逐请求调用的功能鉴权接口。
- 渠道策略只能引用已经登记且启用的全局 Role/Permission；当前已经提供目录项管理 API，人工直接授权 API 尚未交付。

业务登录中的渠道策略同步必须由入口已校验的 `ChannelContext.channelCode` 触发，而不等同于 `clientAppId`。手机号和外部身份登录在访问凭据签发前同步 ACTIVE 策略；缺少策略时不影响登录，也不产生授权。管理接口不借用当前请求的 ChannelContext，而是由具备 `admin` scope 的调用方在请求体中显式提交 `targetChannelCode`；用户授权管理查询同样显式提交 `targetUserId`。更新 ACTIVE 策略、激活和停用只提交策略版本；显式 reconciliation 接口按 `batchSize` 重放一个批次并返回是否仍有待处理用户，用于失败重试。同一 ClientApp 可以进入多个渠道；签名与调用方绑定等入口安全规则、ClientApp 与渠道的完整设计见 [CLIENT_AND_CHANNEL_CONTEXT.md](CLIENT_AND_CHANNEL_CONTEXT.md)。

功能授权管理和用户授权查询位于 `/admin/user-auth/authorization/**`，要求 Bearer Access Token 具有 OAuth2 `admin` scope；H5 会话不能操作。管理调用只要求网关注入 `X-Client-App-Id`，目标 Permission、Role、渠道与用户分别通过请求体 `targetPermissionCode`、`targetRoleCode`、`targetChannelCode`、`targetUserId` 显式声明，不能由 `X-Channel-Code` 或 `X-User-Id` 代替。Permission 管理提供保存、启用、停用、按 code 查询和分页目录查询；Role 管理提供保存、启用、停用、按 code 查询和分页目录查询。分页默认每页 20 条，单页最多 200 条，按 code 升序返回。渠道策略更新、激活和停用必须携带 `expectedVersion`。角色编码非法使用错误 `304`；Permission 使用 `400-403`，其中不存在、已存在、停用和编码非法分别为 `400-403`；渠道策略不存在、已存在和版本冲突分别使用 `500`、`501`、`502`。

## 跨服务授权分工

一个业务请求的完整判断可分为：

1. 资源服务器按 Access Token 格式执行 JWT 本地验证，或 Reference Token introspection，并校验发行者、有效期和受众。
2. 功能授权能力根据 `UserId` 的有效角色和直接授权判断是否拥有所需的 PermissionCode；当前已有内部计算与管理查询，业务资源服务器的集成方式尚未交付。
3. 具体业务服务按自身领域规则判断该用户是否可访问目标资源。

目前第 1 步和功能授权的数据/查询能力已运行，但第 2 步尚未形成资源服务器可直接采用的统一鉴权契约。

## 领域事件与错误

认证域会以领域事件表达账户、挑战、外部身份、登录尝试和登录会话的状态变化，并由应用层在本地事务内编排持久化和外部协作。错误码与异常必须从所属子域的业务语义出发；已发布错误码不得改写、复用或重新分配。

服务错误码前缀为业务命名空间 `0`。共享领域错误范围为 `000-099`；认证账户、认证挑战、登录尝试和登录会话分别占用 `100-149`、`150-199`、`200-249`、`250-299`。角色、角色授权、权限、直接权限授权和渠道策略分别使用 `300-349`、`350-399`、`400-449`、`450-499`、`500-549`。应用错误范围为 `600-699`。

Session handoff 使用应用错误 `613`：一次性交接 ticket 无效或过期。ClientApp 不允许 Refresh Token 续期使用 `615`；Refresh Token 无效、跨 ClientApp、关联会话失效或协议刷新失败使用 `616`。这些都属于认证交付编排错误，不表示功能授权错误。

功能授权管理 API 是内部管理契约，不等同于资源服务器鉴权协议。新增在线鉴权、Token 权限声明或缓存传播机制前，应先补充本文件及对应实现和测试。
