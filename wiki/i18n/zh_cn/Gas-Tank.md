# 气体储罐

> 本页为机器翻译草稿，仅供参考，以英文版本为准：[English](../../Gas-Tank.md)。

一种用于储存**单一气体**的加压存储方块。气体储罐是 Neroland Core 自带的四种存储方块之一（参见[机器、电力与升级](Machines-Power-and-Upgrades.md)）；它容纳一种气体，并通过气体能力进行交换。

> 这些方块在 `1.1.0` 中从 Nerospace 迁移到了 Core。已放置的 `nerospace:gas_tank` 不会自动迁移——详情参见 [Nerospace 更新日志](https://github.com/Neroland/nerospace)。

## 工作原理

- 一次只容纳**一种**气体，以毫桶（mB）计量。
- 通过 Core 的 `nerolandcore:gas` 能力在**每一个面**上注入和抽出，因此任何 Neroland 模组的气体管道都可以输入或输出气体。
- 空手右键点击可读取内容物。

Core 不自带任何具体的气体——储罐容纳下游模组通过管道输入的任何气体（气体在内部以 ID 标识；储罐会采用它收到的第一种气体）。

**创造气体储罐**变体可无限供应气体——参见[创造源方块](Creative-Source-Blocks.md)。

## 详细信息

- ID：`nerolandcore:gas_tank` · 工具：镐，铁级 · 掉落：自身
- 配方：参见[配方](Recipes.md)。

## 另请参阅

- [机器、电力与升级](Machines-Power-and-Upgrades.md)
- [电池](Battery.md)
- [流体储罐](Fluid-Tank.md)
- [物品存储器](Item-Store.md)
- [配方](Recipes.md)
- [首页](Home.md)
