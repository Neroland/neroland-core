# 垃圾桶

> 本页为机器翻译草稿，仅供参考，以英文版本为准：[English](../../Trash-Can.md)。

一个用于处理多余资源的无底虚空。垃圾桶是 Neroland Core 自带的存储方块之一（参见[机器、电力与升级](Machines-Power-and-Upgrades.md)）；用管道或漏斗输入的任何东西都会被销毁。

> 该方块在 `1.1.0` 中从 Nerospace 迁移到了 Core。已放置的 `nerospace:trash_can` 会在加载时通过 Core 的方块 ID 别名机制重新映射为 `nerolandcore:trash_can`——详情参见 [Nerospace 更新日志](https://github.com/Neroland/nerospace)。

## 工作原理

- **销毁所有类型：**从任意面接受**物品、流体和气体**并将其丢弃——没有任何输出接口，因此任何东西都无法被取回。
- **永不堵塞：**其内部接收槽会被清空，因此总有空间，从不拒绝或退回任何东西。
- **仅输入：**无法从中提取任何东西；只有明确通过管道或漏斗*输入*垃圾桶的东西才会丢失。
- **不接受能量：**它不接受电力（能量不是“垃圾”）——只接受物品、流体和气体。

## 界面

右键点击打开一个**原版箱子式界面**，其中只有**一个投放槽位**：放入一组物品，它会在下一次输入时被销毁。这是 Core 的第一个菜单类型，由 Core 的客户端界面基础设施支持。

## 详细信息

- ID：`nerolandcore:trash_can` · 工具：镐，铁级 · 掉落：自身
- 配方：一圈铁锭包围一株仙人掌（参见[配方](Recipes.md)）。

## 另请参阅

- [机器、电力与升级](Machines-Power-and-Upgrades.md)
- [电池](Battery.md)
- [流体储罐](Fluid-Tank.md)
- [气体储罐](Gas-Tank.md)
- [物品存储器](Item-Store.md)
- [配方](Recipes.md)
- [首页](Home.md)
