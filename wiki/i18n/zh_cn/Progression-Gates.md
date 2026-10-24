# 进度门槛

> 本页为机器翻译草稿，仅供参考，以英文版本为准：[English](../../Progression-Gates.md)。

进度门槛是一个具名的里程碑（例如 `nerolandcore:reached_orbit`）。NeroQuests 会在玩家完成目标时开启门槛，而其他所有 Neroland 模组只需在解锁内容前询问“这个门槛是否已开启？”。门槛状态**由服务器拥有**并同步到客户端；客户端从不修改它。

## 作用范围

每个门槛声明一种作用范围。解析是自动进行的。

| 作用范围 | 含义 |
| --- | --- |
| player（默认） | 按玩家计算的进度线 |
| team | 由原版记分板队伍共享（合作） |
| server | 整个世界共享一个标志 |

如果玩家没有队伍，队伍门槛会回退为按玩家计算。

## 门槛即数据

每个门槛是 `data/<namespace>/neroland_gates/<path>.json` 下的一个 JSON 文件。门槛 ID 由文件的命名空间加路径组成。

| 字段 | 含义 |
| --- | --- |
| `scope` | player、team 或 server |
| `requires` | 必须已开启的门槛 ID 列表 |
| `title` | 显示标题 |

整合包可以通过提供相同 ID 来覆盖某个门槛，或以新 ID 添加自己的门槛。后加载的整合包优先。

## 标准进度线

Core 自带以下默认链：

- `industrial_power`
- `reached_orbit`
- `first_colony`
- `deep_space`

只有当 `requires` 列表中的**每一个**门槛都已开启时，`tryOpen` 才会开启该门槛。显式的开启／关闭操作则无视条件强制设定状态。每次变化都会触发变更事件并重新同步受影响的玩家。

## 材料发现里程碑

Core 还提供按材料类型化的里程碑。它们并非只是开启一个具名门槛，而是记录某个特定材料标识符已经通过合法途径被接触到：所属模组的操作、访问行星、服务器观察到的玩家拾取，或管理员操作。自动化本身无法产生发现。

定义由数据包驱动，位于 `data/<namespace>/neroland_material_milestones/<path>.json`，使用相同的 player/team/server 作用范围。内置的 `nerolandcore:material_discovered` 定义是共享的生态默认值。

## 隐私

进度存储符合 POPIA/GDPR 的最小化要求：玩家记录只包含 UUID 以及门槛或材料 ID。没有名称、位置或发现时间戳。玩家材料记录可以导出，所有玩家进度记录都会通过 Core 的共享删除钩子删除。参见[隐私与数据](Privacy-and-Data.md)。

## 命令

这些主要用于测试（开启／关闭需要 2 级 OP 权限）：

| 命令 | 效果 |
| --- | --- |
| `/neroland gate list` | 列出门槛 |
| `/neroland gate open <id>` | 强制开启门槛 |
| `/neroland gate close <id>` | 强制关闭门槛 |

像 `reached_orbit` 这样不带命名空间的 ID 会被视为 `nerolandcore:reached_orbit`。

## 开发者信息

可以为离线玩家检查和开启门槛。队伍门槛会通过玩家的记分板队伍解析，与玩家在线时的方式相同。

完整的进度 API 文档见 [../docs/PROGRESSION.md](../../../docs/PROGRESSION.md)。

## 另请参阅

- [经济与声望](Economy-and-Reputation.md)
- [命令](Commands.md)
- [隐私与数据](Privacy-and-Data.md)
- [首页](Home.md)
