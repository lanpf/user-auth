# user-auth

`user-auth` 是 cloud 中负责账户认证与协议令牌生命周期的服务。它提供手机号验证码登录、外部身份登录、已绑定外部 Credential 的授权码登录、登录会话和当前令牌登出；通过 OAuth2 协议基础设施签发短期 Access Token。

功能授权（角色、权限、直接授权）已具备关系型持久化、来源追溯、渠道授权策略管理及用户有效权限查询。角色与权限是全局目录，不带渠道作用域；渠道只作为授权来源参与叠加和独立撤销。当前仍未把 Permission 写入 Access Token，也未交付资源服务器的在线鉴权端点。

## 能力交付状态

服务的职责边界与已交付/尚未交付能力清单见 [docs/RESPONSIBILITIES.md](docs/RESPONSIBILITIES.md)。

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
| `user-auth-infrastructure-session-redis` | Session handoff ticket 与 Browser Session 的 Redis 实现。                                                                                                                                                                  |
| `user-auth-interfaces` | REST 接口与可信网关会话 Header 接入；按 `X-Subject-Type` 区分宿主与受限浏览器 authority，并提供供 gateway 逐请求调用的浏览器会话内网验证端点。                                                                                                  |
| `user-auth-boot` | 运行时组装与启动。                                                                                                                                                                                                    |

依赖方向为：`api/domain` ← `application` ← `infrastructure/interfaces` ← `boot`。协议基础设施是 `application` 端口的外层实现，不能反向依赖领域模型以外的外层模块。

## 文档入口

- [服务职责与协作契约](docs/RESPONSIBILITIES.md)：本服务负责/不负责的职责清单、与 gateway 的协作契约和能力交付状态。涉及职责边界、跨服务协作或交付状态时必须阅读。
- [领域文档](docs/DOMAIN.md)：认证域、功能授权领域模型、边界和业务 API 语义。涉及领域规则、错误或 API 业务语义时必须阅读。
- [网关职责与信任边界](../gateway/docs/RESPONSIBILITIES.md)：统一入口安全、Access Token 与 Browser Session 校验、可信 Header、内部调用、H5 流量及网关与服务的职责边界。该文档由 gateway 工程维护。
- [OAuth2/OIDC 协议基础设施](docs/OAUTH2.md)：SAS Provider、grant、Token、scope、JWK、协议存储与端点安全。涉及 OAuth2、OIDC、SAS、Token 生命周期或协议配置时必须阅读。

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
部署环境须显式提供 `--spring.config.additional-location=optional:file:<config-dir>/`，
并用部署配置覆盖 `spring.sql.init.schema-locations`，使其指向该目录下的 `db/schema.sql`。

`config/application.yml` 只承载与代码默认值不同的本地开发显式决策，不引用环境变量占位符；
`*Properties` 已提供的默认值不在配置文件中重复。数据库凭据、协议密钥等部署差异由部署环境
通过自己的配置文件提供。

## 运行配置提示

生产环境必须由部署配置提供数据库凭据、内部 OAuth2 Client Secret、稳定的 RSA KeyStore、Challenge pepper，以及 `UserGateway` 的实际运行时实现；该端口没有无操作兜底实现，缺失时服务装配失败。OAuth2 协议端点、签名密钥、客户端、scope 和 Token 生命周期的配置约束见 [OAuth2/OIDC 协议基础设施](docs/OAUTH2.md)。

`user-auth` 的服务配置按子域隔离：认证、登录会话、宿主访问令牌、外部身份及 OAuth2/OIDC 协议基础设施统一位于 `user-auth.authentication`；`user-auth.authorization` 专门保留给功能授权子域。当前 Permission、Role、渠道授权策略均由关系型数据管理，功能授权子域没有独立运行时 Properties，因此配置文件不声明空的 `authorization` 节点。

运行时通过 Nacos 服务发现以 `spring.application.name=user-auth` 注册；gateway 使用逻辑服务名调用 Browser Session 内网验证端点并路由 user-auth 外部请求，实例扩缩容或地址变化不需要修改 gateway 的物理 URL。

OAuth2 Access Token 的格式与有效期统一配置在 `user-auth.authentication.oauth2.access-token.format/ttl`，默认 `SELF_CONTAINED`、TTL 15 分钟；选择 `REFERENCE` 时仍以标准 `Authorization: Bearer` 携带，但由授权存储和 introspection 在线校验。ClientApp 的续期策略和 OAuth2 scope 分别使用 `renewal-policy` 与 `oauth2-scopes`；后者只是协议 scope，不是功能授权 Permission。外部业务 Access Token 与 Browser Session 都由同级 gateway 验证；user-auth 的业务 REST 接口消费 gateway 覆盖写入的 `X-Subject-Type`、`X-User-Id`、`X-Session-Id` 等可信上下文，并通过 LoginSession 再校验命令归属。Browser Session 的逐请求验证能力由 `BrowserSessionCommandFacade.verify` 定义，当前 gateway 通过 `POST /internal/browser-sessions/verify` 的 REST 适配调用；该端点仅面向可信内网、不经 gateway 暴露。SAS 自身的 OAuth2/OIDC 协议端点仍由 SAS 安全链负责。
