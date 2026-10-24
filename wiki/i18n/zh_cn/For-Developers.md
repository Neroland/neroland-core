# 开发者指南

> 本页为机器翻译草稿，仅供参考，以英文版本为准：[English](../../For-Developers.md)。

本页是面向贡献者和集成者的入口。深入的 API 参考位于仓库的 [`../docs/`](../../../docs/USING-CORE.md) 文件夹中；本页为你提供概览并链接到那里。

## 基于 Core 开发

Core 是所有 Neroland 模组依赖的唯一稳定基础。请将其声明为**必需**依赖，加载顺序在 Core **之后**，并锁定到一个主版本范围（`[1.0,2.0)`）：

- Fabric（`fabric.mod.json`）：`"depends": { "nerolandcore": ">=1.0.0" }`
- NeoForge（`neoforge.mods.toml`）：对 `nerolandcore` 声明一个必需的 `[[dependencies.<modid>]]`，`ordering = "AFTER"`。
- Forge（`mods.toml`）：等效的必需 `AFTER` 依赖。

完整说明：[`../docs/USING-CORE.md`](../../../docs/USING-CORE.md)。

## 各系统及其门面

每个系统都通过一个小型门面访问；各加载器的接线由 Core 负责。

| 你想要…… | 使用 | 维基 | 开发者文档 |
| ------------ | --- | ---- | ------------- |
| 按标签要求某种材料 | `c:ingots/<m>` / `neroland:materials/<m>` | [标签与数据包](Tags-and-Datapacks.md) | [TAGS-AND-DATAPACKS](../../../docs/TAGS-AND-DATAPACKS.md) |
| 注册你自己的配置 | `ConfigManager.register(schema)` | [配置](Configuration.md) | [CONFIG](../../../docs/CONFIG.md) |
| 按进度限制内容 | `ProgressionGates.isOpen(player, gate)` | [进度门槛](Progression-Gates.md) | [PROGRESSION](../../../docs/PROGRESSION.md) |
| 读取／转移金钱 | `CurrencyApi` | [经济与声望](Economy-and-Reputation.md) | [ECONOMY-REPUTATION](../../../docs/ECONOMY-REPUTATION.md) |
| 调整阵营声望 | `ReputationApi` | [经济与声望](Economy-and-Reputation.md) | [ECONOMY-REPUTATION](../../../docs/ECONOMY-REPUTATION.md) |
| 制作一台机器 | 继承 `AbstractMachineBlockEntity` | [机器、电力与升级](Machines-Power-and-Upgrades.md) | [MACHINES-POWER-UPGRADES](../../../docs/MACHINES-POWER-UPGRADES.md) |
| 暴露／查找能量 | `EnergyLookup` + `*EnergyLookup.ENERGY` | [机器、电力与升级](Machines-Power-and-Upgrades.md) | [MACHINES-POWER-UPGRADES](../../../docs/MACHINES-POWER-UPGRADES.md) |
| 储存／暴露流体 | `NeroFluidStorage` / `FluidBuffer` + `nerolandcore:fluid` | [机器、电力与升级](Machines-Power-and-Upgrades.md) | [MACHINES-POWER-UPGRADES](../../../docs/MACHINES-POWER-UPGRADES.md) |
| 储存／暴露气体 | `NeroGasStorage` / `GasBuffer` / `NeroGases` + `nerolandcore:gas` | [机器、电力与升级](Machines-Power-and-Upgrades.md) | [MACHINES-POWER-UPGRADES](../../../docs/MACHINES-POWER-UPGRADES.md) |
| 判断“这里是否在地球之外？” | `SpaceTags.isSpace(level)` / `SpaceTags.biomeIn(...)` | [太空标签与实体注册](Space-Tags-and-Entities.md) | [SPACE-TAGS-AND-ENTITIES](../../../docs/SPACE-TAGS-AND-ENTITIES.md) |
| 注册生物属性／生成位置 | `EntityRegistrationSupport.get(modId)` | [太空标签与实体注册](Space-Tags-and-Entities.md) | [SPACE-TAGS-AND-ENTITIES](../../../docs/SPACE-TAGS-AND-ENTITIES.md) |
| 储存玩家数据 | 注册一个 `PlayerDataEraser` | [隐私与数据](Privacy-and-Data.md) | [COMPLIANCE](../../../docs/COMPLIANCE.md) |
| 跨模组统计／提取玩家物品 | `ItemSources` + 一个 `ItemSource` | [经济与声望](Economy-and-Reputation.md) | [ECONOMY-REPUTATION](../../../docs/ECONOMY-REPUTATION.md) |

## 实现 Core 提供方

Core 定义契约，但不为经济／声望储存任何数据；由你的模组注册存储实现：

- `CurrencyApi.setProvider(new MyCurrencyStore())`——NeroEconomy。
- `ReputationApi.setProvider(new MyReputationStore())`——NeroFactions。

## API 稳定性

Core 的**公共 API** 遵循语义化版本控制，在主版本之间保持冻结。实现类型标注为 `@ApiStatus.Internal`，随时可能变化——请通过标签引用材料并使用门面，切勿使用内部实现。参见 [`../docs/API-STABILITY.md`](../../../docs/API-STABILITY.md)。

## 贡献

- 资源文件在 `common/src/main/resources` 中**手工编写**——编辑后请校验 JSON。
- 保持 `common/` 不含任何加载器专用的导入；使用平台接缝。
- 每当你添加、修改或移除某个功能时，**请在同一次变更中更新本维基**。
- 完整的构建与验证规则参见 [`../AGENTS.md`](../../../AGENTS.md) / [`../CLAUDE.md`](../../../CLAUDE.md)。

## 另请参阅

- [入门指南](Getting-Started.md)
- [首页](Home.md)
- [标签与数据包](Tags-and-Datapacks.md)
