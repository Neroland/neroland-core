# 电池

> 本页为机器翻译草稿，仅供参考，以英文版本为准：[English](../../Battery.md)。

一种被动的 **Nero Flux (NF)** 储能方块，为你的电网提供缓冲。电池是 Neroland Core 自带的四种存储方块之一（参见[机器、电力与升级](Machines-Power-and-Upgrades.md)）；它只储存能量，本身不执行任何工作。

> 这些方块在 `1.1.0` 中从 Nerospace 迁移到了 Core，因此所有 Neroland 模组共享同一套存储端点。已放置的 `nerospace:battery` 不会自动迁移——详情参见 [Nerospace 更新日志](https://github.com/Neroland/nerospace)。

## 工作原理

- 储存大量 **NF**，在**每一个面**上都可以输入和输出电力。
- 发电机通过网络为其充能，机器也以同样方式从中取电——因此生产出现波动时，你的基地也不会断电。
- 暴露 Core 的 `nerolandcore:energy` 能力，因此任何 Nero 机器、线缆或管道（来自任意 Neroland 模组）都可以为其充电或从中取电。

**创造电池**变体（仅限创造模式物品栏）是无限的能量源和能量汇，用于测试电网——参见[创造源方块](Creative-Source-Blocks.md)。

## 详细信息

- ID：`nerolandcore:battery` · 工具：镐，铁级 · 掉落：自身
- 配方：参见[配方](Recipes.md)。

## 另请参阅

- [机器、电力与升级](Machines-Power-and-Upgrades.md)
- [流体储罐](Fluid-Tank.md)
- [气体储罐](Gas-Tank.md)
- [物品存储器](Item-Store.md)
- [配方](Recipes.md)
- [首页](Home.md)
