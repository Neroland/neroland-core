# 入门指南

> 本页为机器翻译草稿，仅供参考，以英文版本为准：[English](../../Getting-Started.md)。

**Neroland Core** 是 Neroland 科幻模组生态的基础库。它为 **Minecraft 26.1.2 和 26.2** 构建，支持 **NeoForge、Forge 和 Fabric**，模组 ID 为 `nerolandcore`。

## Core 是什么（以及不是什么）

Core 是一个**库，而不是内容模组**。它提供其他 Neroland 模组所依赖的共享材料、标签和 API，使整个生态的行为保持一致。Core 本身只添加：

- 少量合成材料及其方块（参见[材料](Materials.md)），
- 一个存放 Core 自身物品的 **Neroland** 创造模式物品栏标签页（每个 Nero 模组都有自己的标签页），
- 一个 `/neroland …` 命令树（参见[命令](Commands.md)）。

真正的玩法——机器、行星、任务、经济——都在基于 Core 构建的模组中（Nerospace、Nerotech、NeroPower、NeroEconomy、NeroFactions、NeroQuests、NeroEvents）。

## 适用人群

- **玩家**——你安装 Core 是因为你想玩的某个模组需要它。它会在底层安静地运行。
- **整合包作者**——无需改动代码，就能通过数据包和配置对 Core 进行大量调整；参见[标签与数据包](Tags-and-Datapacks.md)和[配置](Configuration.md)。
- **模组开发者**——Core 是你进行开发时唯一稳定的依赖；参见[开发者指南](For-Developers.md)。

## 安装

1. 为 Minecraft 26.1.2 或 26.2 安装你选择的加载器（NeoForge、Forge 或 Fabric）。
2. 在 **Fabric** 上，还需安装 Fabric API。
3. 将 `nerolandcore` 以及所有依赖它的模组放入你的 `mods/` 文件夹。

Core 必须在依赖它的模组**之前**加载；加载器会根据依赖声明自动处理这一点。

## 可以先试试

- 打开创造模式物品栏，找到 **Neroland** 标签页，查看 Core 自带的材料。
- 运行 `/neroland gate list` 查看进度里程碑（参见[进度门槛](Progression-Gates.md)）。
- 运行 `/neroland config list` 查看 Core 的可调数值（参见[配置](Configuration.md)）。

## 另请参阅

- [命令](Commands.md)
- [材料](Materials.md)
- [开发者指南](For-Developers.md)
- [常见问题](FAQ.md)
