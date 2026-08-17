# MobileRegistrationRetryIT

## 测试目标

验证同一个固定验证码生成器能够读取刷新后的配置值，Challenge 策略变化只影响新签发记录；手机号首次登录在 user 初始化暂时失败后，能够使用原 Challenge 恢复同一注册流程；同时验证已消费 Challenge 的恢复期限和过期 Session 的重放保护。

## 运行条件与执行方式

需要 JDK 17，不依赖外部数据库、Redis。在 `user-auth` 根目录执行：

```bash
mvn verify
```

## 手工观测

观察 Maven 控制台或 `target/failsafe-reports`：

- 注册恢复场景输出同一 `userId`、`authAccountId`、最终 `sessionId` 和失败重试次数。
- 固定验证码场景输出刷新后的六位验证码。
- 策略刷新场景输出同一 CommandService 实例刷新前后新 Challenge 的 TTL。
- Challenge 过期场景输出 `EXPIRED` 状态。
- Session 重放场景输出被拒绝的 `sessionId`。

## 预期结果

五个场景全部通过；固定验证码生成器无需重建即可读取更新后的配置；策略变化只影响新签发 Challenge；首次远程初始化失败不创建第二个账号，恢复结果标记 `fromRegistrationFlow=true`，相同 Challenge 的响应重放复用原 Session；过期 Challenge 和过期 Session 均被拒绝。
