# ExternalLoginFlowIT

## 测试目标

验证第三方身份首次登录的两种跨模块流程：

1. 第三方手机号可信时，无需验证码 Challenge，直接创建手机号 Credential、外部身份
   Credential 和 `EXTERNAL_LOGIN` Session。
2. 第三方手机号不可信时，预登录要求补充手机号验证；完成
   `COMPLETE_EXTERNAL_LOGIN` 短信 Challenge 后，再创建两个 Credential 和 Session。

## 运行条件

- JDK 17。
- 不需要数据库、Redis、微信接口、短信服务。
- Repository、ExternalIdentityVerifier、事件存储和外部协作使用测试内存适配器。
- 测试不会输出第三方 proof、验证码或 Token 明文。

## 执行方式

在 `user-auth` 根目录执行全部测试：

```bash
mvn verify
```

只执行本集成测试：

```bash
mvn -pl user-auth-integration-tests -am verify -Dit.test=ExternalLoginFlowIT
```

## 手工观测步骤

1. 执行上述命令。
2. 在 Maven 控制台查找两条 `external login completed` 记录。
3. 对可信手机号场景，观察 `trustedMobile=true` 且
   `mobileVerificationRequired=false`。
4. 对不可信手机号场景，观察 `trustedMobile=false`、
   `mobileVerificationRequired=true` 以及用于完成登录的 Challenge ID。
5. 确认两条记录均包含 UserId、AuthAccountId、SessionId，且 Credential 数量为 2。
6. 打开观察入口中的 Failsafe 报告，确认测试执行数为 2，失败数和错误数均为 0。

## 观察入口

- Maven 控制台。
- `user-auth-integration-tests/target/failsafe-reports/com.cloud.userauth.ExternalLoginFlowIT.txt`
- `user-auth-integration-tests/target/failsafe-reports/TEST-com.cloud.userauth.ExternalLoginFlowIT.xml`

## 预期结果

- 可信手机号直接完成登录，不要求短信验证。
- 不可信手机号只有在完成 `COMPLETE_EXTERNAL_LOGIN` Challenge 后才能登录。
- 两种场景都创建一个 `MOBILE` Credential 和一个 `EXTERNAL` Credential。
- 两种场景创建的 Session 均使用 `EXTERNAL_LOGIN`。
- Failsafe 报告显示两个测试均通过，无失败或错误。
