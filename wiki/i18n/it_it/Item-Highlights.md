# Evidenziazione degli oggetti

> Questa pagina è una bozza tradotta automaticamente; fa fede la versione inglese: [English](../../Item-Highlights.md).

Core disegna un **leggero bordo colorato** all'interno degli slot dell'inventario che contengono oggetti dell'ecosistema Nero, così risaltano a colpo d'occhio in qualsiasi schermata di contenitore, in modo simile alla classica mod *Item Borders* ma consapevole dell'ecosistema. Il bordo si trova sotto l'oggetto ed è disegnato come un bagliore morbido: tre pixel per impostazione predefinita, sfumato verso l'interno e verso la parte superiore dello slot.

## Colori per categoria

Il colore indica **che cos'è** un oggetto, non da quale mod proviene. La categoria viene letta dai tag oggetto `neroland:highlight/*`; vince il tag più specifico:

| Categoria | Tag | Colore |
| --- | --- | --- |
| Macchine e blocchi funzionali | `neroland:highlight/machines` | Ambra |
| Attrezzi, armi ed equipaggiamento | `neroland:highlight/tools` | Viola |
| Moduli di potenziamento e migliorie | `neroland:highlight/upgrades` | Verde |
| Materiali di fabbricazione | `neroland:highlight/materials` | Verde acqua |

Core assegna i tag ai propri contenuti già di serie: i quattro materiali e tutte le loro forme (verde acqua) e i blocchi di stoccaggio passivi (Batteria, Serbatoio di fluidi, Serbatoio di gas, Deposito di oggetti, Cestino e le loro varianti creative, in ambra). Le mod Nero a valle aggiungono i propri oggetti agli stessi tag, così l'evidenziazione funziona in modo uniforme in tutto l'ecosistema.

## Configurazione

Entrambe le impostazioni sono lato client e solo locali, in `nerolandcore.properties` (ricaricabili a caldo con `/neroland config reload`):

| Chiave | Predefinito | Scopo |
| --- | --- | --- |
| `itemHighlightsEnabled` | `true` | Attiva o disattiva i bordi |
| `itemHighlightOpacity` | `65` | Opacità del bordo in percentuale (0-100) |
| `itemHighlightThickness` | `3` | Spessore del bordo in pixel (1-4, sfumato verso l'interno) |

## Per i creatori di modpack

L'appartenenza è un semplice JSON di tag in un pacchetto dati, senza codice. Aggiungi o rimuovi voci in `data/neroland/tags/item/highlight/` in un pacchetto dati per decidere quali oggetti brillano e in quale categoria, esattamente come per qualsiasi altro tag (vedi [Tag e pacchetti dati](Tags-and-Datapacks.md)). Le voci dovrebbero usare `"required": false` così il tag si carica anche quando una mod a cui fa riferimento è assente.

## Per gli sviluppatori

Aggiungi gli oggetti della tua mod agli stessi tag dal tuo jar, per esempio `data/neroland/tags/item/highlight/machines.json` con `"replace": false`. Il rendering è interamente compito di Core: basta dipendere da Core.

## Vedi anche

- [Configurazione](Configuration.md)
- [Tag e pacchetti dati](Tags-and-Datapacks.md)
- [Materiali](Materials.md)
