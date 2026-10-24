# 物品高亮

> 本页为机器翻译草稿，仅供参考，以英文版本为准：[English](../../Item-Highlights.md)。

Core 会在存放 Nero 生态物品的物品栏槽位内绘制一圈**淡淡的彩色边框**，使它们在任何容器界面中都一目了然——类似经典的 *Item Borders* 模组，但能识别整个生态。边框位于物品下方，以柔和的光晕呈现——默认三像素宽，向内并朝槽位顶部渐隐。

## 按类别区分的颜色

颜色表示物品**是什么**，而不是它来自哪个模组。类别从 `neroland:highlight/*` 物品标签中读取；最具体的标签优先：

| 类别 | 标签 | 颜色 |
| --- | --- | --- |
| 机器与功能方块 | `neroland:highlight/machines` | 琥珀色 |
| 工具、武器与装备 | `neroland:highlight/tools` | 紫罗兰色 |
| 升级模块与增强件 | `neroland:highlight/upgrades` | 绿色 |
| 合成材料 | `neroland:highlight/materials` | 青色 |

Core 开箱即为自身内容添加了标签：四种材料及其所有形态（青色），以及被动存储方块——电池、流体储罐、气体储罐、物品存储器、垃圾桶及其创造变体（琥珀色）。下游 Nero 模组会将其物品加入相同的标签，因此高亮在整个生态中表现一致。

## 配置

两项设置都是客户端本地设置，位于 `nerolandcore.properties`（可通过 `/neroland config reload` 热重载）：

| 键 | 默认值 | 用途 |
| --- | --- | --- |
| `itemHighlightsEnabled` | `true` | 开启或关闭边框 |
| `itemHighlightOpacity` | `65` | 边框不透明度百分比（0-100） |
| `itemHighlightThickness` | `3` | 边框粗细像素（1-4，向内渐隐） |

## 整合包作者

成员关系只是普通的数据包标签 JSON——无需代码。在数据包的 `data/neroland/tags/item/highlight/` 下添加或删除条目，即可调整哪些物品发光以及属于哪个类别，与其他任何标签完全相同（参见[标签与数据包](Tags-and-Datapacks.md)）。条目应使用 `"required": false`，这样即使引用的模组不存在，标签也能正常加载。

## 开发者信息

从你自己的 jar 中将模组物品加入相同的标签，例如 `data/neroland/tags/item/highlight/machines.json`，并设置 `"replace": false`。渲染完全由 Core 负责——只需依赖 Core 即可。

## 另请参阅

- [配置](Configuration.md)
- [标签与数据包](Tags-and-Datapacks.md)
- [材料](Materials.md)
