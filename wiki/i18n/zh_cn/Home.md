# Neroland Core 维基

> 本页为机器翻译草稿，仅供参考，以英文版本为准：[English](../../Home.md)。

面向玩家和贡献者的 **Neroland Core** 文档。Neroland Core 是 Neroland 生态的基础库。

> **状态：**V1 基础已完成——版本 `1.3.0`。Core 是一个**库，而不是内容模组**：它提供材料、标签和一系列 API，供其他 Neroland 模组（Nerospace、Nerotech、NeroPower、NeroEconomy、NeroFactions、NeroQuests、NeroEvents）在其之上构建。它本身只添加少量合成材料、一个创造模式物品栏标签页，以及一组被动存储方块（电池、流体储罐、气体储罐、物品存储器、垃圾桶）；玩法——机器、发电机、行星——都在依赖它的模组中。

为 **Minecraft 26.1.2 和 26.2** 构建，支持 **NeoForge、Forge 和 Fabric**。

## 从这里开始

- [入门指南](Getting-Started.md)——Core 是什么、适合谁以及如何安装。
- [命令](Commands.md)——所有 `/neroland …` 命令汇总。
- [常见问题](FAQ.md)——快速解答。

## 内容与系统

- [材料](Materials.md)——Nero合金、星钢、虚空水晶、等离子玻璃及其形态。
- [配方](Recipes.md)——Core 自带的压缩配方。
- [机器、电力与升级](Machines-Power-and-Upgrades.md)——Nero Flux + 机器框架。
- [面配置](Side-Config.md)——所有机器共享的逐面输入／输出／电力／禁用控制。
- 存储方块——Core 自带的被动端点：[电池](Battery.md)、[流体储罐](Fluid-Tank.md)、[气体储罐](Gas-Tank.md)、[物品存储器](Item-Store.md)、[垃圾桶](Trash-Can.md)，以及[创造源方块](Creative-Source-Blocks.md)。
- [进度门槛](Progression-Gates.md)——共享的里程碑系统。
- [陨石材料注册表](Meteor-Material-Registry.md)——共享的可研磨随机产出材料列表。
- [经济与声望](Economy-and-Reputation.md)——货币和阵营声望契约。
- [物品高亮](Item-Highlights.md)——在物品栏中标记 Nero 物品的彩色槽位边框。
- [配置](Configuration.md)——配置文件和重载命令。
- [标签与数据包](Tags-and-Datapacks.md)——`c:`/`neroland:` 标签与整合包调整。
- [装饰契约](Decor-Contracts.md)——调色板导出、仪表盘表面以及 `neroland:decor/*` 标签。
- [太空标签与实体注册](Space-Tags-and-Entities.md)——`neroland:space/*` 生物群系词汇表以及跨加载器的生物注册接缝。
- [隐私与数据](Privacy-and-Data.md)——POPIA/GDPR、数据删除与保留。

## 开发者信息

- [开发者指南](For-Developers.md)——如何依赖 Core 并使用各个系统。
- 更深入的 API 文档位于 [`../docs/`](../../../docs/USING-CORE.md)。

## 另请参阅

- [构建与贡献者背景信息](../../../AGENTS.md)
