# 配置

> 本页为机器翻译草稿，仅供参考，以英文版本为准：[English](../../Configuration.md)。

Neroland Core 提供统一的配置服务，所有 Neroland 模组都向 Core 注册自己的配置，而不必各自手写文件读写。每个模组在加载器的配置目录中拥有一个 `<modId>.properties` 文件，具备类型化结构、默认值、校验、热重载以及可选的服务器到客户端同步。

## 配置文件的行为

- 首次运行时会创建带有说明注释和默认值的文件
- 数值会被限制在其 `[min, max]` 范围内
- 错误或空白的条目会回退为默认值
- 新的键会自动迁移到已有文件中
- 除此之外，你的修改和注释都会原样保留

## 类型化构建器

配置结构由类型化条目构建。每个条目接受一个键、一个默认值、（适用时的）范围上下限、一个 `serverAuthoritative` 标志以及一条注释。

- `bool`
- `intRange`
- `longRange`
- `doubleRange`
- `string`

## 服务器权威值

当某个值必须由服务器统一下发给所有客户端时（通常是游戏平衡相关的值），会将其标记为 `serverAuthoritative = true`。Core 会在玩家加入时以及重载后向每位玩家推送一份快照。仅本地的值（调试开关、客户端视觉效果）保持为 `false`，并从本地文件读取。

同步数据只包含配置键和值——绝不包含玩家身份信息。

## Core 自身的配置结构（`CoreConfig`）

| 键 | 用途 |
| --- | --- |
| `materialHardnessMultiplier` | 缩放材料方块的硬度 |
| `materialBlastResistanceMultiplier` | 缩放材料方块的爆炸抗性 |
| `upgradeModuleSlotCap` | 限制升级模块槽位数量 |
| `upgradeStackingDiminish` | 叠加升级的收益递减曲线 |
| `neroEnergyToForgeEnergyRatio` | NF <-> Forge Energy 的换算比例 |
| `itemHighlightsEnabled` | 在 Nero 物品上显示彩色槽位边框（参见物品高亮） |
| `itemHighlightOpacity` | 高亮边框不透明度，0-100 百分比 |
| `itemHighlightThickness` | 高亮边框粗细（像素），1-4 |
| `debugLogging` | 启用详细调试日志 |
| `telemetryEnabled` | 通过 Sentry（欧盟服务器）发送仅限 Neroland Core 的匿名崩溃报告；默认 `true`，设为 `false` 即可选择退出。仅客户端本地，不同步（参见隐私与数据） |
| `dataRetentionDays` | 不活跃玩家数据的保留天数；0 = 永不删除 |
| `linkAlertsMaxPerPlayer` | 每位玩家最多保留的 NeroLink 警报数（默认 50）；优先清除最早的已确认警报 |
| `linkAlertsMaxPerModule` | 每位玩家来自单个模组的最多 NeroLink 警报数（默认 25） |

## 命令

两者都需要 2 级 OP 权限。

| 命令 | 效果 |
| --- | --- |
| `/neroland config reload` | 重新读取所有配置结构并重新同步 |
| `/neroland config list` | 打印每个配置结构的值，并标记服务器权威值 |

## 开发者信息

完整的配置 API 文档见 [../docs/CONFIG.md](../../../docs/CONFIG.md)。

## 另请参阅

- [机器、电力与升级](Machines-Power-and-Upgrades.md)
- [隐私与数据](Privacy-and-Data.md)
- [命令](Commands.md)
- [首页](Home.md)
