# MobileOtpLoginFlowIT

## 测试目标

验证手机号首次登录在真实基础设施上的完整链路：

1. 通过公开接口 `POST /api/user-auth/challenges` 发布手机验证码。
2. 通过公开接口 `POST /api/user-auth/login/mobile-otp` 提交本次验证码。
3. 验证 `user-auth` 使用 RestClient 自调用本地 SAS `/oauth2/token`，完成认证和
   Access Token 签发。
4. 验证 MySQL 中 Challenge、AuthAccount、Credential、RegistrationProcess、
   LoginSession、RegisteredClient 和领域事件的最终状态。
5. 验证 Redis 中保存 Authorization 与 Access Token 哈希索引，且不保存可直接使用的
   Token 明文。
6. 以 `REFERENCE` 格式重启完整应用，验证不透明 Bearer Access Token、标准 introspection、
   user-auth 本地在线认证、handoff 创建，以及 logout 后 introspection 立即返回 inactive。
7. 验证 OAuth2 Authorization、Session Handoff 和分布式锁的 namespace 均只注入一次。

## 运行条件

- JDK 17 或更高版本。
- 本机 Docker 可用。
- 测试使用 Testcontainers 自动启动隔离的 MySQL 8.4 和 Redis 8.4，测试结束后自动清理。
- 使用完整 `UserAuthApplication`、真实 JPA Repository、真实 Redis Authorization
  Store、真实 SAS Token Endpoint 和真实 RestClient 本地自调用。
- 短信发送和 user 初始化属于系统外部边界，测试使用可观测探针，不访问外部服务。
- 测试日志不输出验证码或 Access Token 明文。

## 执行方式

在 `user-auth` 根目录执行：

```bash
mvn -pl user-auth-integration-tests -am verify -Dit.test=MobileOtpLoginFlowIT
```

必须保留 `-am`，确保集成测试使用当前工作区内各模块的最新产物，而不是本机 Maven
仓库中可能存在的旧 Snapshot。

## 手工观测步骤

### IDE 断点检查容器

1. 使用 IDE 以 Debug 模式运行 `MobileOtpLoginFlowIT`。
2. 在 `manualInspectionBreakpoint(ManualInspection inspection)` 方法内设置断点。
3. 测试到达断点时，手机号登录事务已经提交，Spring Boot、MySQL 和 Redis 容器仍在运行。
4. 展开方法参数 `inspection`，可以直接看到：
   - MySQL 容器 ID、JDBC URL、用户名、测试密码和数据库名。
   - Redis 容器 ID、映射主机、端口和 Key 匹配模式。
   - 本次登录的 ChallengeId、UserId、AuthAccountId、SessionId。
   - Authorization、Access Token 索引的完整 Redis Key。
5. 保持断点暂停，使用 IDE Database、DataGrip、DBeaver 或命令行连接映射端口。
6. 检查完成后恢复程序，测试会继续断言并自动删除容器。

也可以通过 `inspection.mysqlContainerId` 和 `inspection.redisContainerId` 进入容器：

```bash
docker exec -it <mysqlContainerId> mysql -uuser_auth -puser_auth user_auth
docker exec -it <redisContainerId> redis-cli
```

MySQL 中建议执行：

```sql
SELECT * FROM ua_auth_challenge WHERE id = <challengeId>;
SELECT * FROM ua_auth_account WHERE id = <authAccountId>;
SELECT * FROM ua_credential WHERE auth_account_id = <authAccountId>;
SELECT * FROM ua_registration_process WHERE challenge_id = <challengeId>;
SELECT * FROM ua_login_session WHERE id = '<sessionId>';
SELECT id, client_id, authorization_grant_types, scopes
FROM oauth2_registered_client;
SELECT * FROM ua_domain_event ORDER BY occurred_at;
```

Redis 中建议执行；完整 Key 可以直接从 `inspection` 复制：

```text
SCAN 0 MATCH 'user-auth-it:oauth2:{authorization-state}:*' COUNT 100
SCAN 0 MATCH 'user-auth-session-it:session-handoff:*' COUNT 100
SCAN 0 MATCH 'user-auth-lock-it:mobile-login:*' COUNT 100
GET <authorizationKey>
TTL <authorizationKey>
GET <accessIndexKey>
TTL <accessIndexKey>
```

Redis 中的两个 Token 索引值应为 SessionId，Authorization 内容中的 Token 值是
SHA-256 摘要，不是前端可直接使用的 Token 明文。

### 日志与报告检查

1. 执行 Maven 命令，等待 MySQL、Redis 容器和 `UserAuthApplication` 启动。
2. 查找日志 `Mobile challenge observed`，观察 Challenge ID、类型、标准化手机号和过期时间。
3. 查找日志 `MySQL business observation`，观察下列业务数据：
   - Challenge 为 `CONSUMED`，消费方为 `REGISTRATION_PROCESS`。
   - AuthAccount 和手机号 Credential 为 `ACTIVE`。
   - RegistrationProcess 为 `COMPLETED`。
   - LoginSession 为 `ACTIVE`、场景为 `MOBILE_LOGIN`、有效期为 30 天。
   - RegisteredClient 包含 `mobile_otp` 和配置的 scopes。
   - 领域事件数量大于 0。
4. 查找日志 `Redis token observation`，观察：
   - Authorization Key、Access Token 哈希索引 Key。
   - 两个 Key 的 TTL 均大于 0。
   - Access Token 索引指向登录返回的 SessionId。
   - `plaintextTokenStored=false`。
5. 查找日志 `Reference access token observed`，观察 Reference Token 长度、SessionId 和
   handoffId；日志不会输出 Token 明文。
6. 打开 Failsafe 报告，确认执行数为 1，失败数和错误数均为 0。

测试还会在锁持有期间检查 `user-auth-lock-it:mobile-login:<mobile>`，并在 handoff 创建后检查
`user-auth-session-it:session-handoff:ticket:<ticket>` 与
`user-auth-session-it:session-handoff:login-session:<sessionId>`。这些断言同时排除 namespace
重复拼接。

## 观察入口

- Maven 控制台中的 `Mobile challenge observed`、`MySQL business observation` 和
  `Redis token observation`、`Reference access token observed`。
- `user-auth-integration-tests/target/failsafe-reports/com.cloud.userauth.MobileOtpLoginFlowIT.txt`
- `user-auth-integration-tests/target/failsafe-reports/TEST-com.cloud.userauth.MobileOtpLoginFlowIT.xml`

Testcontainers 在测试结束后删除容器，因此数据观察以测试断言和上述结构化日志为稳定入口，
不依赖临时映射端口，也不会在测试结束后残留数据库或 Redis 数据。

## 预期结果

- 两个公开 HTTP 接口调用成功，登录 scope 为 `app`。
- 登录结果为首次注册流程：`fromRegistrationFlow=true`、`replayed=false`。
- user 初始化探针收到与登录结果一致的 UserId。
- MySQL 中关键业务实体、登录会话、RegisteredClient 和领域事件均符合最终状态。
- Redis 中恰好存在本次 Authorization 及 Access Token 哈希索引，索引值为 SessionId。
- Redis Authorization 只含 Token 的 SHA-256 摘要，不含 Token 明文。
- OAuth2、Session Handoff 和分布式锁 Redis Key 的 namespace 均且仅出现一次。
- Reference Access Token 可通过标准 introspection 取得 active 状态，可认证宿主级 handoff
  请求，并在 logout 后立即变为 inactive。
- Failsafe 报告显示测试通过，无失败或错误。
