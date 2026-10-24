# Per gli sviluppatori

> Questa pagina è una bozza tradotta automaticamente; fa fede la versione inglese: [English](../../For-Developers.md).

Questa pagina è il punto d'ingresso per chi contribuisce o integra. Il riferimento API approfondito si trova nella cartella [`../docs/`](../../../docs/USING-CORE.md) del repository; questa pagina ti orienta e rimanda lì.

## Sviluppare su Core

Core è l'unica base stabile da cui dipende ogni mod Neroland. Dichiarala come dipendenza **obbligatoria**, caricata **dopo** Core, e fissala a un intervallo di versione principale (`[1.0,2.0)`):

- Fabric (`fabric.mod.json`): `"depends": { "nerolandcore": ">=1.0.0" }`
- NeoForge (`neoforge.mods.toml`): una `[[dependencies.<modid>]]` obbligatoria su `nerolandcore`, `ordering = "AFTER"`.
- Forge (`mods.toml`): l'equivalente dipendenza obbligatoria `AFTER`.

Istruzioni complete: [`../docs/USING-CORE.md`](../../../docs/USING-CORE.md).

## I sistemi e le loro facciate

Ogni sistema si raggiunge tramite una piccola facciata; il collegamento con ciascun loader è compito di Core.

| Vuoi… | Usa | Wiki | Documentazione per sviluppatori |
| ------------ | --- | ---- | ------------- |
| Richiedere un materiale tramite tag | `c:ingots/<m>` / `neroland:materials/<m>` | [Tag e pacchetti dati](Tags-and-Datapacks.md) | [TAGS-AND-DATAPACKS](../../../docs/TAGS-AND-DATAPACKS.md) |
| Registrare la tua configurazione | `ConfigManager.register(schema)` | [Configurazione](Configuration.md) | [CONFIG](../../../docs/CONFIG.md) |
| Vincolare contenuti alla progressione | `ProgressionGates.isOpen(player, gate)` | [Requisiti di progressione](Progression-Gates.md) | [PROGRESSION](../../../docs/PROGRESSION.md) |
| Leggere/spostare denaro | `CurrencyApi` | [Economia e reputazione](Economy-and-Reputation.md) | [ECONOMY-REPUTATION](../../../docs/ECONOMY-REPUTATION.md) |
| Modificare la reputazione presso una fazione | `ReputationApi` | [Economia e reputazione](Economy-and-Reputation.md) | [ECONOMY-REPUTATION](../../../docs/ECONOMY-REPUTATION.md) |
| Costruire una macchina | estendi `AbstractMachineBlockEntity` | [Macchine, energia e potenziamenti](Machines-Power-and-Upgrades.md) | [MACHINES-POWER-UPGRADES](../../../docs/MACHINES-POWER-UPGRADES.md) |
| Esporre/trovare energia | `EnergyLookup` + `*EnergyLookup.ENERGY` | [Macchine, energia e potenziamenti](Machines-Power-and-Upgrades.md) | [MACHINES-POWER-UPGRADES](../../../docs/MACHINES-POWER-UPGRADES.md) |
| Memorizzare/esporre un fluido | `NeroFluidStorage` / `FluidBuffer` + `nerolandcore:fluid` | [Macchine, energia e potenziamenti](Machines-Power-and-Upgrades.md) | [MACHINES-POWER-UPGRADES](../../../docs/MACHINES-POWER-UPGRADES.md) |
| Memorizzare/esporre un gas | `NeroGasStorage` / `GasBuffer` / `NeroGases` + `nerolandcore:gas` | [Macchine, energia e potenziamenti](Machines-Power-and-Upgrades.md) | [MACHINES-POWER-UPGRADES](../../../docs/MACHINES-POWER-UPGRADES.md) |
| Chiedere «questo luogo è fuori dalla Terra?» | `SpaceTags.isSpace(level)` / `SpaceTags.biomeIn(...)` | [Tag spaziali e registrazione delle entità](Space-Tags-and-Entities.md) | [SPACE-TAGS-AND-ENTITIES](../../../docs/SPACE-TAGS-AND-ENTITIES.md) |
| Registrare attributi delle creature / posizionamento di generazione | `EntityRegistrationSupport.get(modId)` | [Tag spaziali e registrazione delle entità](Space-Tags-and-Entities.md) | [SPACE-TAGS-AND-ENTITIES](../../../docs/SPACE-TAGS-AND-ENTITIES.md) |
| Memorizzare dati dei giocatori | registra un `PlayerDataEraser` | [Privacy e dati](Privacy-and-Data.md) | [COMPLIANCE](../../../docs/COMPLIANCE.md) |
| Contare/prelevare gli oggetti di un giocatore tra più mod | `ItemSources` + un `ItemSource` | [Economia e reputazione](Economy-and-Reputation.md) | [ECONOMY-REPUTATION](../../../docs/ECONOMY-REPUTATION.md) |

## Implementare un fornitore per Core

Core definisce i contratti e non memorizza nulla per economia e reputazione; è la tua mod a registrare la memorizzazione:

- `CurrencyApi.setProvider(new MyCurrencyStore())`: NeroEconomy.
- `ReputationApi.setProvider(new MyReputationStore())`: NeroFactions.

## Stabilità dell'API

Core segue il versionamento semantico per la sua **API pubblica**, congelata tra una versione principale e l'altra. I tipi di implementazione sono contrassegnati `@ApiStatus.Internal` e possono cambiare in qualsiasi momento: riferisciti ai materiali tramite tag e usa le facciate, mai le parti interne. Vedi [`../docs/API-STABILITY.md`](../../../docs/API-STABILITY.md).

## Contribuire

- Le risorse sono **scritte a mano** in `common/src/main/resources`: valida il JSON dopo ogni modifica.
- Mantieni `common/` libero da import specifici del loader; usa i punti di aggancio della piattaforma.
- Ogni volta che aggiungi, modifichi o rimuovi una funzionalità, **aggiorna questa wiki nella stessa modifica**.
- Vedi [`../AGENTS.md`](../../../AGENTS.md) / [`../CLAUDE.md`](../../../CLAUDE.md) per le regole complete di build e verifica.

## Vedi anche

- [Per iniziare](Getting-Started.md)
- [Home](Home.md)
- [Tag e pacchetti dati](Tags-and-Datapacks.md)
