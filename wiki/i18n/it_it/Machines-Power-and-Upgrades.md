# Macchine, energia e potenziamenti

> Questa pagina è una bozza tradotta automaticamente; fa fede la versione inglese: [English](../../Machines-Power-and-Upgrades.md).

Neroland Core fornisce un **framework per le macchine** su cui si basano le altre mod Neroland. Core da sola **non include macchine, generatori né oggetti di potenziamento**: definisce l'unità di energia, i contratti energetici, il risolutore dei potenziamenti e l'interoperabilità energetica tra loader, così ogni macchina Nero parla la stessa lingua. Include però alcuni **blocchi di stoccaggio passivi** (vedi sotto) basati su questi contratti.

## Nero Flux (NF)

L'unità di energia condivisa mostrata ai giocatori è il **Nero Flux (NF)**. I nomi delle classi API esistenti e la chiave di configurazione `neroEnergyToForgeEnergyRatio` restano invariati per compatibilità, e la conversione da NF a FE usa esattamente lo stesso rapporto configurato di prima.

| Componente | Ruolo |
| --- | --- |
| `NeroEnergyStorage` | Il contratto energetico indipendente dal loader |
| `EnergyBuffer` | Implementazione limitata pronta all'uso: genera, consuma, immette/estrae, limitata da `maxIO` |
| `EnergyConversions` | Converte da/verso Forge Energy tramite il rapporto di configurazione `neroEnergyToForgeEnergyRatio` |

`AbstractMachineBlockEntity` riunisce un `EnergyBuffer` e un `UpgradeContainer` e li rende persistenti entrambi. Le macchine a valle lo estendono.

## Moduli di potenziamento

Le macchine accettano moduli di potenziamento che cambiano il loro funzionamento. I **tipi** di modulo sono fissati da Core; gli **oggetti** sono forniti dalle mod a valle.

| Tipo di potenziamento | Effetto |
| --- | --- |
| SPEED | Funzionamento più rapido (`speedMultiplier`) |
| EFFICIENCY | Consumo di energia ridotto (`energyMultiplier`) |
| RANGE | Area di lavoro più ampia (`rangeBonus`) |
| CAPACITY | Riserva di energia più grande (`capacityMultiplier`) |

Come si combinano:

- una macchina ha un `UpgradeContainer` di slot
- un classificatore associa una pila di oggetti a un tipo, di solito tramite un controllo dei tag
- `UpgradeModifiers` trasforma il numero di moduli in moltiplicatori

Poiché ogni macchina di ogni mod Nero usa **un unico** risolutore, i moduli sono intercambiabili e il bilanciamento si regola in un solo posto, la configurazione:

- `upgradeStackingDiminish` imposta la curva dei rendimenti decrescenti per i moduli sommati
- `upgradeModuleSlotCap` limita il numero di slot
- limiti rigidi vincolano i moltiplicatori risultanti

Core **non** include oggetti modulo; una mod a valle definisce gli oggetti e l'associazione tra pila e tipo.

## Stoccaggio di fluidi e gas

Core replica il punto di aggancio energetico per altri due tipi di risorsa, così le mod possono convogliare fluidi e gas come convogliano gli NF. Entrambi sono **generici**: Core non include fluidi o gas specifici, solo i contratti e le relative capability multi-loader.

| Componente | Ruolo |
| --- | --- |
| `NeroFluidStorage` | Il contratto per fluidi indipendente dal loader |
| `FluidBuffer` | Implementazione limitata pronta all'uso per un singolo fluido (misurata in mB) |
| `NeroGasStorage` | Il contratto per gas indipendente dal loader |
| `GasBuffer` | Implementazione limitata pronta all'uso per un singolo gas |
| `NeroGases` | Strumento per denominare/risolvere i gas (ognuno identificato da un id) |

Core possiede le capability condivise `nerolandcore:fluid` e `nerolandcore:gas` su ogni loader, esattamente come `nerolandcore:energy`.

## Blocchi di stoccaggio inclusi in Core

Core include cinque **punti di stoccaggio passivi** (i suoi primi block entity); i primi quattro hanno anche una variante creativa. Contengono (o eliminano) una risorsa e la offrono a tubi e tramogge, ma non svolgono lavoro: non sono macchine.

| Blocco | Contiene | Pagina |
| --- | --- | --- |
| **Batteria** | Nero Flux (NF) | [Batteria](Battery.md) |
| **Serbatoio di fluidi** | un fluido | [Serbatoio di fluidi](Fluid-Tank.md) |
| **Serbatoio di gas** | un gas | [Serbatoio di gas](Gas-Tank.md) |
| **Deposito di oggetti** | oggetti (contenitore del gioco base) | [Deposito di oggetti](Item-Store.md) |
| **Cestino** | nulla: elimina oggetti, fluidi e gas | [Cestino](Trash-Can.md) |

La Batteria espone `nerolandcore:energy`, il Serbatoio di fluidi `nerolandcore:fluid`, il Serbatoio di gas `nerolandcore:gas` e il Deposito di oggetti il gestore di oggetti standard del gioco base (apre l'interfaccia del baule). Il **Cestino** accetta su ogni lato `nerolandcore:fluid`, `nerolandcore:gas` e il gestore di oggetti, e scarta tutto ciò che vi viene immesso; apre un'interfaccia simile al baule con un solo slot di scarico (il primo tipo di menu di Core + schermata client). Tutti e cinque si estraggono con piccone di livello ferro e hanno ricette di fabbricazione (vedi [Ricette](Recipes.md)). I [Blocchi sorgente creativi](Creative-Source-Blocks.md) sono varianti sorgente/pozzo infinite dei primi quattro, per i test.

## Interoperabilità energetica tra loader

Core possiede la capability/lookup condivisa `nerolandcore:energy` su ogni loader tramite il punto di aggancio `EnergyLookup`. Le macchine a valle registrano il proprio block entity sulla capability di Core, così qualsiasi macchina Nero (di qualsiasi mod) può trovare qualsiasi altra. Lo stesso schema vale per `nerolandcore:fluid` e `nerolandcore:gas`. Il collegamento con librerie esterne Forge Energy o di energia per Fabric è rimandato finché quelle librerie non saranno portate su MC 26.x.

## Per gli sviluppatori

L'API completa di macchine, energia e potenziamenti è documentata in [../docs/MACHINES-POWER-UPGRADES.md](../../../docs/MACHINES-POWER-UPGRADES.md).

## Vedi anche

- [Configurazione](Configuration.md)
- [Tag e pacchetti dati](Tags-and-Datapacks.md)
- [Per gli sviluppatori](For-Developers.md)
- [Home](Home.md)
