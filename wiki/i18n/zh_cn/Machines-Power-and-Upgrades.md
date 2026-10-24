# 机器、电力与升级

> 本页为机器翻译草稿，仅供参考，以英文版本为准：[English](../../Machines-Power-and-Upgrades.md)。

Neroland Core 提供一个供其他 Neroland 模组使用的**机器框架**。Core 本身**不提供任何机器、发电机或升级物品**——它定义电力单位、能量契约、升级解析器以及跨加载器的能量互通，使所有 Nero 机器都使用同一种语言。不过，它确实自带了几个基于这些契约的**被动存储方块**（见下文）。

## Nero Flux (NF)

面向玩家的共享电力单位是 **Nero Flux (NF)**。为保持兼容，现有的 API 类名和 `neroEnergyToForgeEnergyRatio` 配置键保持不变，NF 到 FE 的换算也使用与之前完全相同的配置比例。

| 组件 | 作用 |
| --- | --- |
| `NeroEnergyStorage` | 与加载器无关的能量契约 |
| `EnergyBuffer` | 现成的有界实现：产生、消耗、输入／输出，受 `maxIO` 限制 |
| `EnergyConversions` | 通过 `neroEnergyToForgeEnergyRatio` 配置比例与 Forge Energy 互相换算 |

`AbstractMachineBlockEntity` 将一个 `EnergyBuffer` 和一个 `UpgradeContainer` 打包在一起并持久化两者。下游机器继承它即可。

## 升级模块

机器可以接受改变其运行方式的升级模块。模块**类型**由 Core 固定；**物品**由下游模组提供。

| 升级类型 | 效果 |
| --- | --- |
| SPEED | 运行更快（`speedMultiplier`） |
| EFFICIENCY | 能耗更低（`energyMultiplier`） |
| RANGE | 工作范围更大（`rangeBonus`） |
| CAPACITY | 能量缓存更大（`capacityMultiplier`） |

它们如何协同工作：

- 机器拥有一个由槽位组成的 `UpgradeContainer`
- 分类器将物品堆叠映射为某种类型，通常通过标签检查
- `UpgradeModifiers` 将模块数量换算为倍率

由于所有 Nero 模组中的每台机器都使用**同一个**解析器，模块可以互换，平衡也只需在一处——配置中——进行调整：

- `upgradeStackingDiminish` 设定叠加模块的收益递减曲线
- `upgradeModuleSlotCap` 限制槽位数量
- 硬上限约束最终倍率

Core **不**提供模块物品；由下游模组定义物品以及堆叠到类型的映射。

## 流体与气体存储

Core 为另外两种资源类型复刻了能量接缝，使模组能像传输 NF 一样传输流体和气体。两者都是**通用的**——Core 不自带具体的流体或气体，只提供契约及其跨加载器能力。

| 组件 | 作用 |
| --- | --- |
| `NeroFluidStorage` | 与加载器无关的流体契约 |
| `FluidBuffer` | 现成的有界单一流体实现（以 mB 计量） |
| `NeroGasStorage` | 与加载器无关的气体契约 |
| `GasBuffer` | 现成的有界单一气体实现 |
| `NeroGases` | 用于命名／解析气体的辅助工具（每种气体以 ID 标识） |

Core 在每个加载器上拥有共享的 `nerolandcore:fluid` 和 `nerolandcore:gas` 能力，与 `nerolandcore:energy` 完全一样。

## Core 自带的存储方块

Core 自带五个**被动存储端点**（它的第一批方块实体）；前四个还有创造变体。它们容纳（或销毁）某种资源并将其提供给管道和漏斗，但不执行任何工作——它们不是机器：

| 方块 | 容纳 | 页面 |
| --- | --- | --- |
| **电池** | Nero Flux (NF) | [电池](Battery.md) |
| **流体储罐** | 一种流体 | [流体储罐](Fluid-Tank.md) |
| **气体储罐** | 一种气体 | [气体储罐](Gas-Tank.md) |
| **物品存储器** | 物品（原版容器） | [物品存储器](Item-Store.md) |
| **垃圾桶** | 无——销毁物品、流体和气体 | [垃圾桶](Trash-Can.md) |

电池暴露 `nerolandcore:energy`，流体储罐暴露 `nerolandcore:fluid`，气体储罐暴露 `nerolandcore:gas`，物品存储器暴露标准的原版物品处理器（它打开原版箱子界面）。**垃圾桶**在每一个面上同时接受 `nerolandcore:fluid`、`nerolandcore:gas` 和物品处理器，并丢弃任何输入的东西；它打开一个只有单个投放槽位的原版箱子式界面（Core 的第一个菜单类型 + 客户端界面）。五者都可用镐／铁级工具挖掘，并自带合成配方（参见[配方](Recipes.md)）。[创造源方块](Creative-Source-Blocks.md)是前四者用于测试的无限源／汇变体。

## 跨加载器能量互通

Core 通过 `EnergyLookup` 接缝在每个加载器上拥有共享的 `nerolandcore:energy` 能力／查找。下游机器将其方块实体注册到 Core 的能力上，因此任何 Nero 机器（来自任意模组）都能找到其他任何 Nero 机器。同样的模式也适用于 `nerolandcore:fluid` 和 `nerolandcore:gas`。与外部 Forge Energy 或 Fabric 能量库的桥接将推迟到这些库移植到 MC 26.x 之后。

## 开发者信息

完整的机器、电力与升级 API 文档见 [../docs/MACHINES-POWER-UPGRADES.md](../../../docs/MACHINES-POWER-UPGRADES.md)。

## 另请参阅

- [配置](Configuration.md)
- [标签与数据包](Tags-and-Datapacks.md)
- [开发者指南](For-Developers.md)
- [首页](Home.md)
