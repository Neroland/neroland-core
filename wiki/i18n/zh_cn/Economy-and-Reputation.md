# 经济与声望

> 本页为机器翻译草稿，仅供参考，以英文版本为准：[English](../../Economy-and-Reputation.md)。

Neroland Core 定义了货币和声望的**契约**，但自身不储存任何数据。NeroEconomy 实现货币存储，NeroFactions 实现声望。在真正的提供方注册之前，Core 自带一个非持久化的内存参考实现来支撑每个 API，使整个生态开箱即用。

## 货币

Core 从一开始就支持多种命名货币。每个余额都以一个 `Currency` 为键。内置默认货币是 `nerolandcore:credits`（“信用点”）。

| API | 行为 |
| --- | --- |
| `getBalance` | 读取玩家某种货币的余额 |
| `deposit` | 增加资金 |
| `withdraw` | 扣除资金；余额不足时返回 false |
| `transfer` | 在玩家之间转移资金（默认实现） |

成功的操作前后会触发变更事件。提供方需实现 `getBalance`、`deposit` 和 `withdraw`；`transfer` 和 `forgetPlayer` 有默认实现。

模组在转移资金时还可以注明自己的名称并给出简短的原因代码（例如 `neroquests` / `quest_reward`），以便经济模组记录谁因何支付了什么。模组 ID 和原因代码只接受小写字母、数字和少数分隔符，因此绝不会携带个人数据。

## 声望

声望是以（玩家 UUID，阵营 ID）为键的整数值。数值范围和上下限由提供方决定。

| API | 行为 |
| --- | --- |
| `getReputation` | 读取与某阵营的声望 |
| `adjust` | 按 +/- 调整声望 |
| `setReputation` | 直接设定声望 |

可以订阅变更事件。

## 隐私

两个 API 都以玩家 UUID 为键。提供方暴露一个 `forgetPlayer(UUID)` 删除钩子，由 Core 的共享删除机制驱动。Core 自身不储存余额或声望。参见[隐私与数据](Privacy-and-Data.md)。

## 开发者信息

完整的经济与声望 API 文档见 [../docs/ECONOMY-REPUTATION.md](../../../docs/ECONOMY-REPUTATION.md)。

## 另请参阅

- [进度门槛](Progression-Gates.md)
- [隐私与数据](Privacy-and-Data.md)
- [配置](Configuration.md)
- [首页](Home.md)
