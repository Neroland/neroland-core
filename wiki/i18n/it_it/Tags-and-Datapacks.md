# Tag e pacchetti dati

> Questa pagina è una bozza tradotta automaticamente; fa fede la versione inglese: [English](../../Tags-and-Datapacks.md).

Neroland Core espone i suoi [materiali](Materials.md) tramite tag e mantiene ogni punto di aggancio come semplice dato, così modpack e altre mod possono regolare l'ecosistema senza toccare il codice. **Riferisciti ai materiali tramite tag, mai tramite id oggetto.**

## Due famiglie di tag

Core include due famiglie di tag parallele.

### `c:`: convenzione tra mod

Il namespace `c:` è lo stesso namespace di convenzione usato da Create, AE2, Mekanism e dalle piattaforme Fabric/NeoForge. Usa `c:` nelle ricette e nell'ingresso/uscita delle macchine: è la superficie di interoperabilità e il contratto più stabile.

| Forma | Tag per materiale | Tag aggregato |
| --- | --- | --- |
| Lingotto | `c:ingots/<m>` | `c:ingots` |
| Pepita | `c:nuggets/<m>` | `c:nuggets` |
| Polvere | `c:dusts/<m>` | `c:dusts` |
| Piastra | `c:plates/<m>` | `c:plates` |
| Gemma | `c:gems/<m>` | `c:gems` |
| Blocco di stoccaggio (oggetto + blocco) | `c:storage_blocks/<m>` | `c:storage_blocks` |
| Blocco di vetro (oggetto + blocco) | — | `c:glass_blocks` |
| Pannello di vetro (oggetto + blocco) | — | `c:glass_panes` |

I tag aggregati fanno riferimento ai sotto-tag per materiale, quindi aggiungere un materiale estende automaticamente l'aggregato.

### `neroland:`: namespace dell'ecosistema

Il namespace `neroland:` è di proprietà di Core e stabile tra le versioni minori. Il tag `neroland:materials/<material>` significa «tutto ciò che è questo materiale, in qualsiasi forma». Usalo per i controlli interni all'ecosistema del tipo «è uno dei nostri materiali?».

### Regola pratica

- ingredienti delle ricette e interoperabilità esterna -> `c:`
- controlli «è un nostro materiale?» -> `neroland:`

## Punti di aggancio dei pacchetti dati

Tutti i punti di aggancio sono semplici dati e completamente sovrascrivibili; vincono i pacchetti caricati dopo. I tag si estendono in modo additivo.

| Punto di aggancio | Posizione |
| --- | --- |
| Ricette | `data/nerolandcore/recipe/*.json` |
| Tabelle del bottino | `data/nerolandcore/loot_table/blocks/*.json` |
| Tag (`c:`) | `data/c/tags/**` |
| Tag (`neroland:`) | `data/nerolandcore/tags/**` |
| Estrazione (piccone) | `data/minecraft/tags/block/mineable/pickaxe` |
| Estrazione (livello dell'attrezzo) | `needs_iron_tool` |

## Per gli sviluppatori

La mappa completa dei tag e il riferimento sui pacchetti dati per gli autori di mod si trovano in [../docs/TAGS-AND-DATAPACKS.md](../../../docs/TAGS-AND-DATAPACKS.md).

## Vedi anche

- [Materiali](Materials.md)
- [Ricette](Recipes.md)
- [Requisiti di progressione](Progression-Gates.md)
- [Home](Home.md)
