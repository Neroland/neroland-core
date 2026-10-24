# 太空标签与实体注册

> 本页为机器翻译草稿，仅供参考，以英文版本为准：[English](../../Space-Tags-and-Entities.md)。

**Core 1.10.0** 中为在其他世界添加生物和内容的模组加入的两个 API。两者本身都不会向游戏添加任何东西，也都不储存玩家数据。

## 太空标签

Core 拥有一套小型共享词汇表，用来回答“这是什么样的地方？”——`neroland:space/dark_biomes`、`moon_biomes`、`crystalline_biomes`、`asteroid_biomes`、总括性的 `planet_biomes`，以及维度类型标签 `neroland:space/dimensions`。

为什么重要：添加太空生物、遗迹或外星作物的模组可以说“在卫星生物群系生成”，而无需知道是哪个模组提供了卫星。Core 以**可选**条目的形式提供成员关系，因此这些标签会解析为你实际安装的行星。

- 安装了 **Nerospace** 时：Cindara 是黑暗世界，Glacira 是低重力水晶卫星，Greenxertz 是水晶原野，而每个 Nerospace 生物群系（包括经过地形改造的生物群系）都算作行星生物群系。
- 安装了 **Ad Astra** 时（待其移植到 26.1+ 之后）：它的月球、火星、金星、水星、Glacio 和轨道会归入相同的标签。
- **两者都没有**时：每个标签都为空，这也没关系——依赖这些标签的内容就不会出现。**地球按设计保持安静。**

整合包作者可以用普通数据包重新调整每个标签——将任意模组的生物群系加入 `data/neroland/tags/worldgen/biome/space/moon_biomes.json`，任何在卫星上生成的东西现在也会在那里生成。

## 实体注册

一个面向开发者的接缝。Minecraft 的三个模组加载器各自以不同方式注册生物的默认属性和自然生成位置；Core 将其隐藏在一次调用之后，使模组可以在共享代码中一次性声明两者，并在 NeoForge、Forge 和 Fabric 上都能运行。

这里没有任何玩家可见的内容——它的存在是为了让基于 Core 的生物模组在三个加载器上的行为完全一致。

参见开发者文档：[`../docs/SPACE-TAGS-AND-ENTITIES.md`](../../../docs/SPACE-TAGS-AND-ENTITIES.md)。

## 另请参阅

- [标签与数据包](Tags-and-Datapacks.md)
- [开发者指南](For-Developers.md)
- [首页](Home.md)
