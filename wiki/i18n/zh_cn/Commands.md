# 命令

> 本页为机器翻译草稿，仅供参考，以英文版本为准：[English](../../Commands.md)。

所有 Neroland Core 命令都位于 `/neroland` 根命令之下。会更改服务器状态或影响其他玩家的命令需要 **2 级 OP 权限**（游戏管理员）；个人数据命令对任何玩家开放，但仅限其本人的数据。

## 配置

| 命令 | 权限 | 效果 |
| ------- | ---------- | ------ |
| `/neroland config reload` | op 2 | 从磁盘重新读取所有已注册的配置结构，并将服务器权威值重新同步到在线客户端。 |
| `/neroland config list` | op 2 | 打印每个配置结构的值，并标记出服务器权威值。 |

参见[配置](Configuration.md)。

## 进度门槛

| 命令 | 权限 | 效果 |
| ------- | ---------- | ------ |
| `/neroland gate list` | 任何人 | 列出所有已定义的门槛、其作用范围，以及对你是否已开启。 |
| `/neroland gate open <id>` | op 2 | 为你强制开启一个门槛。 |
| `/neroland gate close <id>` | op 2 | 为你强制关闭一个门槛。 |

像 `reached_orbit` 这样不带命名空间的 ID 会被视为 `nerolandcore:reached_orbit`。参见[进度门槛](Progression-Gates.md)。

## 数据与隐私

| 命令 | 权限 | 效果 |
| ------- | ---------- | ------ |
| `/neroland data eraseme` | 任何人 | 删除**你自己**在所有系统中的 Neroland 数据（选择退出／重置）。 |
| `/neroland data erase <uuid>` | op 2 | 删除指定玩家的 Neroland 数据。 |
| `/neroland data purge-inactive` | op 2 | 删除所有不活跃时间超过 `dataRetentionDays` 阈值的玩家数据。 |

参见[隐私与数据](Privacy-and-Data.md)。

## 另请参阅

- [配置](Configuration.md)
- [进度门槛](Progression-Gates.md)
- [隐私与数据](Privacy-and-Data.md)
