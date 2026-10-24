# 隐私与数据（摘要）

> 本页为机器翻译草稿，仅供参考，以英文版本为准：[English](../../Privacy-and-Data.md)。

本页为便于理解而提供的翻译摘要，具有法律效力的是英文版本。

Neroland Core 为整个生态负责数据保护契约，遵循 POPIA 和 GDPR 的原则。Core 只储存所需的最少游戏状态，以玩家 UUID 为键，并提供一个所有 Neroland 系统和下游模组都会注册的共享删除钩子。

## Core 储存的内容

仅以玩家 UUID 为键的游戏状态：进度门槛标志、材料里程碑、最后登录时间戳（用于数据保留），以及 NeroLink 警报（模组 ID、严重程度、一条简短的非个人消息）。Core **不**储存余额或声望（参见[经济与声望](Economy-and-Reputation.md)），也从不储存名称、IP、聊天记录或超出游戏所需的位置历史。

## 数据最小化

玩家记录只包含 UUID 和游戏值；信息级日志中不记录任何玩家数据；删除操作只记录匿名确认，而不记录被删除的是谁。

## 删除权

| 命令 | 谁可使用 | 效果 |
| --- | --- | --- |
| `/neroland data eraseme` | 任何玩家 | 删除你自己的 Neroland 数据（选择退出／重置） |
| `/neroland data erase <uuid>` | 管理员（2 级 OP） | 删除指定玩家的数据 |

**已知缺口：**以 `scope: team` 声明的门槛和材料里程碑是按记分板队伍名称储存的，删除玩家时不会被移除。请以角色或颜色为队伍命名，切勿以单个玩家命名；完整说明和处理步骤请参阅英文版本。

## 数据保留

`dataRetentionDays` 配置（0 = 永不删除，需主动启用）设定不活跃玩家数据的保留时长。`/neroland data purge-inactive` 会删除所有超过阈值的玩家数据。参见[配置](Configuration.md)。

## 崩溃报告（Sentry）

崩溃报告**默认开启**，可**选择退出**。当 Neroland Core 自身代码出错时，会向托管在欧盟的 Sentry（`ingest.de.sentry.io`）发送一份匿名报告。报告绝不包含你的 IP 地址、用户名、玩家 UUID、机器名称、世界数据、聊天、坐标或物品栏，也没有将报告关联起来的持久标识符。每次会话最多 10 份报告，90 天后删除。包含内容的完整列表请参阅英文版本。

要选择退出，请在 `config/nerolandcore.properties`（首次启动时创建）中设置以下内容并重启：

```properties
telemetryEnabled=false
```

这是一个本地设置——服务器不会将其同步到客户端。问题或数据请求：[info@neroland.co.za](mailto:info@neroland.co.za)。

## 下游模组与开发者

如果你的模组储存玩家数据：以 UUID 为键、只储存游戏所需的数据、不在信息级日志中记录玩家数据，并注册一个删除器。完整的合规指南见 [../docs/COMPLIANCE.md](../../../docs/COMPLIANCE.md)。

## 另请参阅

- [配置](Configuration.md)
- [进度门槛](Progression-Gates.md)
- [经济与声望](Economy-and-Reputation.md)
- [命令](Commands.md)
- [首页](Home.md)
