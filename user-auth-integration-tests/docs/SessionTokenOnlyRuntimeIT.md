# SessionTokenOnlyRuntimeIT

## 测试目标

验证 `user-auth` 可以在 `user-auth.authentication.access-token.provider=session-token` 且
`user-auth.authentication.oauth2.authorization-server.enabled=false` 时以 Session-Token-only 模式完整启动，并确认：

- 登录结果由 Session Token 实现交付。
- Session Token 与 H5 Session 分别装配独立的 Redis Store 和认证 Filter。
- Session Token 复用共享 ClientApp `renewalPolicy`：允许 `EXTERNAL_AUTHORIZATION_CODE` 或 `NONE`，拒绝 `REFRESH_TOKEN_ROTATION`。
- SAS Authorization Server、RegisteredClient、OAuth2 Authorization Store、JWT Decoder
  和协议授权撤销器均未装配。
- 应用 REST SecurityFilterChain 仍能正常创建。
- `X-Session-Token` 可以调用宿主级 handoff 创建接口；同一 LoginSession 派生的
  `H5_SESSION` 调用 handoff 创建、外部 Credential 绑定或父会话 logout 均返回 403。
- 宿主以 `X-Session-Token` logout 后，同一 LoginSession 派生的 Session Token credential、
  H5 Session、未消费 handoff ticket 及其 Redis 反向索引全部被级联删除。

## 运行条件

- JDK 17 或更高版本。
- 本机 Docker 可用。
- Testcontainers 自动启动隔离的 MySQL 8.4 和 Redis 8.4，并在测试结束后清理。
- 使用完整 `UserAuthApplication`；user 外部调用使用测试替身。

## 执行方式

在 `user-auth` 根目录执行：

```bash
mvn -pl user-auth-integration-tests -am verify -Dit.test=SessionTokenOnlyRuntimeIT
```

## 手工观测步骤

1. 以 Debug 模式运行 `SessionTokenOnlyRuntimeIT`。
2. 在 `manualInspectionBreakpoint` 设置断点。
3. 展开 `observation`，确认登录交付器为 `SessionTokenLoginTokenIssuer`，会话认证 Filter
   同时包含 `SessionTokenAuthenticationFilter` 与 `H5SessionAuthenticationFilter`，JWT Decoder
   和 AuthorizationServerSettings 数量均为 0，并查看实际 REST SecurityFilterChain 与随机端口。
4. 也可在控制台查找 `Session-Token-only runtime observation` 日志。
5. 在 `verifyHostSessionWriteBoundary` 设置断点，可观察测试创建的 Session Token/H5/ticket、
   宿主 logout 请求，以及 logout 后所有凭据和 Redis 反向索引的删除结果。

## 观察入口

- Maven 控制台中的 `Session-Token-only runtime observation`。
- `user-auth-integration-tests/target/failsafe-reports/com.cloud.userauth.SessionTokenOnlyRuntimeIT.txt`
- `user-auth-integration-tests/target/failsafe-reports/TEST-com.cloud.userauth.SessionTokenOnlyRuntimeIT.xml`

## 预期结果

- Spring Boot 应用成功启动。
- 只有应用 REST SecurityFilterChain，不存在 SAS 协议 SecurityFilterChain。
- `SessionTokenStore` 与 `H5SessionStore` 分别由对应的 Redis Store 实现，且各自认证 Filter 已装配。
- 不存在 `JwtDecoder`、`AuthorizationServerSettings`、`RegisteredClientRepository`、
  `OAuth2AuthorizationService` 或 `SessionAuthorizationRevoker` Bean。
- Session Token Header 创建 handoff 返回 200；H5 Cookie 调用三个宿主级写接口均返回 403。
- Session Token Header logout 返回 200；同一 LoginSession 下的 Session Token、H5、handoff ticket
  均无法再次解析，对应 Redis 主键和反向索引均不存在。
- `mini-program` 解析为 `EXTERNAL_AUTHORIZATION_CODE`，测试覆盖的 `app` 解析为 `NONE`；Session Token 启动校验器会拒绝任何 `REFRESH_TOKEN_ROTATION` 配置。
- Failsafe 报告无失败或错误。
