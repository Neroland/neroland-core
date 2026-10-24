# 常见问题

> 本页为机器翻译草稿，仅供参考，以英文版本为准：[English](../../FAQ.md)。

## Neroland Core 本身会添加玩法吗？

不多。Core 是一个**库**：它自带一些合成材料、标签以及一系列供其他 Neroland 模组使用的 API。你安装它是因为你想玩的某个模组需要它。参见[入门指南](Getting-Started.md)。

## 支持哪些 Minecraft 版本和加载器？

Minecraft **26.1.2 和 26.2**，支持 **NeoForge、Forge 和 Fabric**。在 Fabric 上还需要安装 Fabric API。

## Core 添加了哪些材料？

Nero合金、星钢、虚空水晶和等离子玻璃，以及它们的各种形态（锭、粒、粉、板、碎片、宝石、方块、玻璃板）。参见[材料](Materials.md)。Core 有意**不提供任何矿石**——引入某种材料矿石的模组会将其冶炼成这些 Core 物品。

## 为什么纹理看起来像占位图？

1.0.0 的纹理是程序生成的占位纹理，计划日后重新绘制。功能和标签已经定型，美术还没有。

## 服务器整合包能否不写代码就调整 Core？

可以。配方、战利品表、标签、挖掘要求和门槛定义都是普通数据，可以用数据包覆盖，数值平衡则在配置中。参见[标签与数据包](Tags-and-Datapacks.md)和[配置](Configuration.md)。

## Core 有机器或电力吗？

Core 提供**框架**（Nero Flux、机器方块实体基类、升级模块系统），但不包含具体的机器或发电机——这些来自 Nerotech 和 NeroPower 等模组。参见[机器、电力与升级](Machines-Power-and-Upgrades.md)。

## Core 处理金钱或阵营吗？

Core 定义了**货币**和**声望**的契约，并自带一个非持久化的内存后备实现，但不会永久储存任何数据。真正的存储由 NeroEconomy 和 NeroFactions 提供。参见[经济与声望](Economy-and-Reputation.md)。

## 如何删除我的数据？

运行 `/neroland data eraseme` 即可删除你在所有系统中的 Neroland 数据。管理员和定期保留清理使用其他 `/neroland data` 命令。参见[隐私与数据](Privacy-and-Data.md)和[命令](Commands.md)。

## 我是模组开发者——如何基于 Core 进行开发？

参见[开发者指南](For-Developers.md)以及更深入的 [`../docs/USING-CORE.md`](../../../docs/USING-CORE.md)。

## 另请参阅

- [首页](Home.md)
- [入门指南](Getting-Started.md)
- [命令](Commands.md)
