# 流体储罐

> 本页为机器翻译草稿，仅供参考，以英文版本为准：[English](../../Fluid-Tank.md)。

一种被动的单一流体存储方块。流体储罐是 Neroland Core 自带的四种存储方块之一（参见[机器、电力与升级](Machines-Power-and-Upgrades.md)）；它容纳一种流体，并提供给管道和桶使用。

> 这些方块在 `1.1.0` 中从 Nerospace 迁移到了 Core。已放置的 `nerospace:fluid_tank` 不会自动迁移——详情参见 [Nerospace 更新日志](https://github.com/Neroland/nerospace)。

## 工作原理

- 一次只容纳**一种**流体，以毫桶（mB）计量。
- **桶：**手持装满的桶右键点击可倒入，手持空桶右键点击可取出。
- **管道：**在**每一个面**上都可以注入和抽出，同时支持 Core 的 `nerolandcore:fluid` 能力和加载器的**标准**流体能力（NeoForge `Capabilities.Fluid`、Forge `FLUID_HANDLER`、Fabric `FluidStorage.SIDED`）——因此任何 Neroland 模组的流体管道*以及*任何其他模组的流体管道都可以输入或输出流体。储罐还会从相邻的第三方储罐中抽取流体，或向其注入流体。
- 空手右键点击可读取内容物。

Core 不自带任何具体的流体——储罐可以容纳下游模组或原版提供的任何流体。

**创造流体储罐**变体可无限供应你所选择的流体——参见[创造源方块](Creative-Source-Blocks.md)。

## 详细信息

- ID：`nerolandcore:fluid_tank` · 工具：镐，铁级 · 掉落：自身
- 配方：参见[配方](Recipes.md)。

## 另请参阅

- [机器、电力与升级](Machines-Power-and-Upgrades.md)
- [电池](Battery.md)
- [气体储罐](Gas-Tank.md)
- [物品存储器](Item-Store.md)
- [配方](Recipes.md)
- [首页](Home.md)
