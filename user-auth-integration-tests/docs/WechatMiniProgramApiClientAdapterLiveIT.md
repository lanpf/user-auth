# WechatMiniProgramApiClientAdapterLiveIT

## 测试目标

使用真实微信小程序 `appId`、`appSecret` 和 `wx.login` 产生的一次性 `loginCode`，调用微信
`jscode2session` 接口，验证 `WechatMiniProgramApiClientAdapter.exchangeLoginCode` 的真实网络连接、
查询参数、JSON 字段映射及成功响应契约。

该测试是外部系统 Live Integration Smoke Test，不替代使用 Mock Server、可重复执行的单元测试。

## 运行条件

- JDK 17，并且执行环境可以访问 `https://api.weixin.qq.com`。
- 设置 `WECHAT_MINI_PROGRAM_APP_ID`、`WECHAT_MINI_PROGRAM_APP_SECRET`、
  `WECHAT_MINI_PROGRAM_LOGIN_CODE` 三个环境变量。
- `loginCode` 必须由相同 `appId` 对应的小程序刚刚通过 `wx.login` 获取。它短时有效且只能使用一次，
  每次重新执行都应生成新 code。
- 未完整提供上述环境变量时，测试自动标记为 skipped；不会尝试真实网络调用。
- 测试不会输出 `appSecret`、`loginCode`、`openid` 或 `session_key` 明文。

## 执行方式

在 `user-auth` 根目录为当前 shell 设置环境变量后执行：

```bash
mvn -pl user-auth-integration-tests -am verify \
  -Dit.test=WechatMiniProgramApiClientAdapterLiveIT
```

不要把真实密钥或一次性 code 写进 Maven 命令、源码、测试资源、IDE 运行配置共享文件或版本库。

## 手工观测步骤

1. 在小程序中调用 `wx.login` 获取新的 `loginCode`。
2. 在本地 shell 中设置三个环境变量，执行上述命令。
3. 查看 Maven 控制台中的 `real WeChat code exchange completed` 摘要。
4. 确认 `openIdPresent=true`、`sessionKeyPresent=true`；`unionIdPresent` 是否为 true 取决于微信开放平台绑定条件。
5. 打开 Failsafe 报告，确认测试执行数为 1，失败数和错误数为 0，且不是 skipped。

## 观察入口

- Maven 控制台中的非敏感字段存在性摘要。
- `user-auth-integration-tests/target/failsafe-reports/com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.WechatMiniProgramApiClientAdapterLiveIT.txt`
- `user-auth-integration-tests/target/failsafe-reports/TEST-com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.WechatMiniProgramApiClientAdapterLiveIT.xml`

## 预期结果

- 微信接口接受 `appId`、`appSecret` 与一次性 `loginCode`。
- 客户端成功映射非空 `openid` 和 `session_key`。
- `unionid` 缺失不会导致失败。
- 环境变量缺失时测试安全跳过；真实凭据和响应中的敏感值不会出现在测试输出中。
