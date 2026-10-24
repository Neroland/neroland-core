# Ricette

> Questa pagina è una bozza tradotta automaticamente; fa fede la versione inglese: [English](../../Recipes.md).

Neroland Core include le ricette di compattazione standard per i suoi [materiali](Materials.md). Ogni ricetta è un semplice dato in `data/nerolandcore/recipe/*.json`, quindi un pacchetto dati può sostituire, rimuovere o aggiungere ricette liberamente. Nessuna parte del codice fissa un oggetto materiale, e le ricette fanno riferimento ai tag `c:`/`neroland:` invece che agli id degli oggetti.

## Set di compattazione

| Materiale | Ricette (in entrambe le direzioni) |
| --- | --- |
| Lega Nero | blocco <-> lingotto (9), lingotto <-> pepita (9) |
| Acciaio stellare | blocco <-> lingotto (9), lingotto <-> pepita (9) |
| Cristallo del vuoto | blocco <-> gemma (9), gemma <-> scheggia (9) |
| Vetro al plasma | blocco di vetro -> pannello |

Il Cristallo del vuoto si compatta lungo la catena blocco <-> gemma <-> scheggia.

## Blocchi di stoccaggio

Core include le ricette di fabbricazione per i suoi [blocchi di stoccaggio](Machines-Power-and-Upgrades.md) passivi: Batteria, Serbatoio di fluidi, Serbatoio di gas, Deposito di oggetti e Cestino. Sono le stesse ricette con cui i blocchi venivano forniti in precedenza; come ogni ricetta di Core fanno riferimento ai tag di materiale `#c:` invece che agli id degli oggetti, così un modpack può sostituire il metallo o regolarne il costo. (Le varianti creative non hanno ricetta: sono solo nella scheda creativa.)

| Blocco | Forma | Ingredienti |
| --- | --- | --- |
| **Batteria** | involucro di metallo attorno a una cella di redstone e metallo | metallo `#c:ingots/*` + redstone |
| **Serbatoio di fluidi** | guscio di metallo con finestre di vetro | metallo `#c:ingots/*` + vetro |
| **Serbatoio di gas** | un Serbatoio di fluidi sigillato nel metallo | metallo `#c:ingots/*` + un Serbatoio di fluidi |
| **Deposito di oggetti** | telaio di metallo attorno a un baule | metallo `#c:ingots/*` + un baule |
| **Cestino** | un anello di ferro attorno a un cactus | `#c:ingots/iron` + cactus |

Poiché gli ingredienti sono basati sui tag, il metallo esatto è quello che un modpack associa ai tag dei lingotti `#c:`; fai riferimento al tag, mai a un id oggetto specifico.

## Sovrascrivere ed estendere

Poiché le ricette sono semplici dati e gli ingredienti sono basati sui tag, un modpack può:

- sostituire qualsiasi ricetta di Core fornendo lo stesso id di ricetta
- rimuovere una ricetta
- aggiungere nuove ricette che consumano i tag di materiale `c:` o `neroland:`

Riferisciti ai materiali tramite tag (per esempio `c:ingots/nero_alloy`), mai tramite id oggetto. Vedi [Tag e pacchetti dati](Tags-and-Datapacks.md).

## Tabelle del bottino ed estrazione

Le tabelle del bottino dei blocchi di materiale si trovano in `data/nerolandcore/loot_table/blocks/*.json`. Per impostazione predefinita ogni blocco di materiale lascia cadere se stesso; i modpack possono sovrascriverle per Tocco di velluto o drop bonus.

I tag di estrazione elencano i blocchi solidi, così i requisiti degli attrezzi sono regolabili dal modpack:

- `minecraft:mineable/pickaxe`
- `needs_iron_tool`

## Per gli sviluppatori

I dettagli su tag e pacchetti dati per gli autori di mod si trovano in [../docs/TAGS-AND-DATAPACKS.md](../../../docs/TAGS-AND-DATAPACKS.md).

## Vedi anche

- [Materiali](Materials.md)
- [Tag e pacchetti dati](Tags-and-Datapacks.md)
- [Per iniziare](Getting-Started.md)
- [Home](Home.md)
