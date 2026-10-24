# 装饰契约

> 本页为机器翻译草稿，仅供参考，以英文版本为准：[English](../../Decor-Contracts.md)。

Core 为整个生态的装饰层提供的共享**纯装饰性**契约（在 Core 1.9.0 中加入）。NeroDecor 是第一个使用者，但任何模组或数据包都可以使用它们。这些契约都不涉及玩家数据，因此都不受数据删除的约束。

## 调色板导出

一个只读的命名**饰面**（颜色 + 发光元数据）注册表，使装饰方块、机器饰边和纹理与 Core 的材料色调完全一致。Core 自带四种材料（`neroland:nero_alloy`、`neroland:starsteel`、`neroland:void_crystal`、`neroland:plasma_glass`）以及一套 16 色强调色（`neroland:accent/<dye-name>`）。通过 `PaletteRegistry.getFinish(id)` 读取。

## 仪表盘表面

全息投影和控制面板提供一个很小的内容 API——文本、图标、状态颜色，按维度 + 方块坐标寻址——NeroSecurity 和 NeroLogistics 等模组可以向其推送读数。Core 只定义接口和一个空操作注册表；渲染由提供方模组完成。没有驱动时，表面就保持空白。

## 装饰标签

`neroland:decor/*` 方块标签（`hull`、`panel`、`glass`、`neon`、`trim`、`dashboard_surface`、`planet` + 各行星）对装饰表面进行分组，使其他模组（以及仅通过标签的外部模组）能够与 NeroDecor 的建筑搭配使用。成员关系由所属模组通过数据包 JSON 声明。

参见开发者文档：[`../docs/DECOR-CONTRACTS.md`](../../../docs/DECOR-CONTRACTS.md)。
