# 陨石材料注册表

> 本页为机器翻译草稿，仅供参考，以英文版本为准：[English](../../Meteor-Material-Registry.md)。

陨石材料注册表是 Neroland Core 为整个生态维护的一份共享的**可研磨材料**列表。将某物转化为*随机* Nero 材料的机器——首先是 Nerospace 的**陨石研磨机**——会读取这份列表，而不是各自写死。任何模组（或者你，通过数据包）都可以向其中添加材料，概率会自动重新平衡。

> Core 本身不添加研磨机——它只拥有这份列表。你会通过使用它的模组（例如 Nerospace）看到这份列表的实际效果。

## 默认列表内容

Core 自带四种基础材料：

| 材料 | 产出 | 稀有度 | 解锁条件 |
| -------- | ------ | ------ | ----------- |
| Nero合金 | Nero合金粉 | 常见 | 始终 |
| 等离子玻璃 | 等离子玻璃 | 少见 | 始终 |
| 虚空水晶 | 虚空水晶粉 | 少见 | 抵达轨道 |
| 星钢 | 星钢粉 | 稀有 | 抵达轨道 |

其他模组会添加它们自己的材料（行星矿石、奇异掉落物……）。如果某个模组未安装，它的条目就不会出现——不会出任何问题。

## 概率如何计算

每种材料都有一个**稀有度等级**，决定其基础权重，研磨机会按这些权重的比例随机挑选一种——但只从你实际已解锁的材料中挑选。随着你的进度推进、更多材料变得可选，常见材料自然会变得更稀少，任何地方都没有固定的概率表。

以 Core 的材料为例：

- **抵达轨道之前：**Nero合金粉约 70.6%，等离子玻璃约 29.4%。
- **抵达轨道之后：**四种材料分别约为 49.2% / 20.5% / 20.5% / 9.8%。
- **在殖民行星上（且有绑定该行星的矿石）：**当地矿石会带着偏向加入池中，进一步稀释其他材料；离开该行星后它又会消失。

此外，还有一个独立的小概率（默认 8%）从注册了奇异材料的模组中获得额外的**奇异**掉落。

## 调整（服务器管理员）

调节项位于 Core 的配置（`config/nerolandcore.properties`）中，在执行 `/neroland config reload` 后生效：

- `meteorTierBaseWeightCommon` / `…Uncommon` / `…Rare`——基础权重（默认 60 / 25 / 12）。
- `meteorPlanetBias`——行星对其自身矿石的偏向强度（默认 2.0）。
- `meteorExoticChance`——每次研磨获得奇异额外掉落的概率（默认 0.08）。

在游戏内用 `/neroland meteor list` 查看实时列表，用 `/neroland meteor reload` 重新读取材料文件（仅限管理员）。参见[命令](Commands.md)和[配置](Configuration.md)。

## 用数据包添加或修改材料

每种材料在数据包中放置一个 JSON 文件，路径为 `data/<your_namespace>/neroland/meteor_materials/<id>.json`：

```json
{
  "item": "yourmod:cobalt_dust",
  "tier": "uncommon",
  "min_gate": "nerolandcore:reached_orbit",
  "planet": null,
  "weight_override": null,
  "enabled": true
}
```

- `tier` 取值为 `common`、`uncommon`、`rare`、`exotic` 之一（`exotic` 条目必须设置 `weight_override`）。
- `min_gate` 是一个[进度门槛](Progression-Gates.md) ID，或者为 `null` 表示“始终”。
- `planet` 将材料绑定到某个维度（一个 Nerospace 行星 ID），或者为 `null` 表示任何地方。
- 要**调整**已有材料，请在更高优先级的数据包中于相同路径放置文件——只需包含你想修改的字段，其余字段会保留。显式的 `null` 会清除某个字段。

配方查看器使用的成员标签是 `neroland:meteor/grindable`；如果你希望 JEI/REI 和兼容层将你的物品识别为可研磨，也请将其加入该标签。

模组作者也可以在代码中用 `@GrindableMaterial` 注解物品——参见[开发者文档](../../../docs/METEOR-MATERIAL-REGISTRY.md)。

## 隐私

该注册表只是物品元数据——它不储存任何关于玩家的信息，因此没有需要导出或删除的内容。参见[隐私与数据](Privacy-and-Data.md)。
