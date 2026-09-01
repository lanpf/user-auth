# user-auth integration tests

## 覆盖范围

覆盖手机号验证码发布与 SAS 登录、第三方身份登录、固定验证码动态刷新、
Challenge 策略刷新、首次登录注册、跨服务失败恢复、Challenge 恢复期限和过期 Session
重放拒绝等跨模块场景。

## 环境要求

需要 JDK 17。`MobileOtpLoginFlowIT` 需要本机 Docker，并通过 Testcontainers 启动隔离的
MySQL 和 Redis。其他场景是否使用内存适配器或外部基础设施，以各测试的同名文档为准。
`WechatMiniProgramApiClientAdapterLiveIT` 仅在显式提供真实微信小程序环境变量时访问微信服务；
未提供时自动跳过，不影响统一验证。

## 统一执行方式

在 `user-auth` 根目录执行：

```bash
mvn verify
```

## 报告位置

Failsafe 报告位于 `user-auth-integration-tests/target/failsafe-reports`。

## 文档索引

- [MobileOtpLoginFlowIT](docs/MobileOtpLoginFlowIT.md)
- [ExternalLoginFlowIT](docs/ExternalLoginFlowIT.md)
- [MobileRegistrationRetryIT](docs/MobileRegistrationRetryIT.md)
- [WechatMiniProgramApiClientAdapterLiveIT](docs/WechatMiniProgramApiClientAdapterLiveIT.md)
