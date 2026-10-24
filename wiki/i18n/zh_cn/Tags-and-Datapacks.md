# 标签与数据包

> 本页为机器翻译草稿，仅供参考，以英文版本为准：[English](../../Tags-and-Datapacks.md)。

Neroland Core 通过标签暴露其[材料](Materials.md)，并将每个钩子都保持为普通数据，因此整合包和其他模组无需改动代码就能调整整个生态。**请通过标签引用材料，切勿通过物品 ID。**

## 两个标签系列

Core 提供两个并行的标签系列。

### `c:`——跨模组约定

`c:` 命名空间与 Create、AE2、Mekanism 以及 Fabric/NeoForge 平台使用的约定命名空间相同。请在配方和机器输入／输出中使用 `c:`——它是互通的接口，也是最稳定的契约。

| 形态 | 单材料标签 | 汇总标签 |
| --- | --- | --- |
| 锭 | `c:ingots/<m>` | `c:ingots` |
| 粒 | `c:nuggets/<m>` | `c:nuggets` |
| 粉 | `c:dusts/<m>` | `c:dusts` |
| 板 | `c:plates/<m>` | `c:plates` |
| 宝石 | `c:gems/<m>` | `c:gems` |
| 存储方块（物品 + 方块） | `c:storage_blocks/<m>` | `c:storage_blocks` |
| 玻璃方块（物品 + 方块） | — | `c:glass_blocks` |
| 玻璃板（物品 + 方块） | — | `c:glass_panes` |

汇总标签引用各单材料子标签，因此添加一种材料会自动扩展汇总标签。

### `neroland:`——生态命名空间

`neroland:` 命名空间归 Core 所有，并在次版本之间保持稳定。标签 `neroland:materials/<material>` 表示“任何形态的该材料的全部物品”。将其用于生态内部的“这是否是我们的材料？”检查。

### 经验法则

- 配方材料和外部互通 -> `c:`
- “这是我们的材料吗？”检查 -> `neroland:`

## 数据包钩子

所有钩子都是普通数据，可以完全覆盖；后加载的整合包优先。标签以累加方式扩展。

| 钩子 | 位置 |
| --- | --- |
| 配方 | `data/nerolandcore/recipe/*.json` |
| 战利品表 | `data/nerolandcore/loot_table/blocks/*.json` |
| 标签（`c:`） | `data/c/tags/**` |
| 标签（`neroland:`） | `data/nerolandcore/tags/**` |
| 挖掘（镐） | `data/minecraft/tags/block/mineable/pickaxe` |
| 挖掘（工具等级） | `needs_iron_tool` |

## 开发者信息

面向模组作者的完整标签映射和数据包参考见 [../docs/TAGS-AND-DATAPACKS.md](../../../docs/TAGS-AND-DATAPACKS.md)。

## 另请参阅

- [材料](Materials.md)
- [配方](Recipes.md)
- [进度门槛](Progression-Gates.md)
- [首页](Home.md)
