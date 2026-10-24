# Tag spaziali e registrazione delle entità

> Questa pagina è una bozza tradotta automaticamente; fa fede la versione inglese: [English](../../Space-Tags-and-Entities.md).

Due API aggiunte in **Core 1.10.0** per le mod che portano creature e contenuti su altri mondi. Nessuna delle due aggiunge qualcosa al gioco da sola, e nessuna memorizza dati dei giocatori.

## Tag spaziali

Core possiede un piccolo vocabolario condiviso che risponde alla domanda «che tipo di luogo è questo?»: `neroland:space/dark_biomes`, `moon_biomes`, `crystalline_biomes`, `asteroid_biomes`, il tag generale `planet_biomes` e il tag di tipo di dimensione `neroland:space/dimensions`.

Perché è importante: una mod che aggiunge creature spaziali, rovine o colture aliene può dire «genera nei biomi lunari» senza sapere quale mod fornisce le lune. Core fornisce l'appartenenza come voci **opzionali**, quindi i tag si risolvono nei pianeti che hai effettivamente installato.

- Con **Nerospace** installata: Cindara è un mondo oscuro, Glacira è una luna cristallina a bassa gravità, Greenxertz è un campo di cristalli, e ogni bioma di Nerospace (compresi quelli terraformati) conta come bioma planetario.
- Con **Ad Astra** installata (una volta portata su 26.1+): la sua Luna, Marte, Venere, Mercurio, Glacio e l'Orbita rientrano negli stessi tag.
- Con **nessuna delle due**: ogni tag è vuoto, e va bene così: i contenuti legati a questi tag semplicemente non compaiono. **La Terra resta tranquilla per scelta.**

I creatori di modpack possono regolare ogni tag con un normale pacchetto dati: aggiungi un bioma di qualsiasi mod a `data/neroland/tags/worldgen/biome/space/moon_biomes.json` e tutto ciò che si genera sulle lune ora si genera anche lì.

## Registrazione delle entità

Un punto di aggancio per sviluppatori. I tre loader di mod di Minecraft registrano in modo diverso gli attributi predefiniti di una creatura e il suo posizionamento di generazione naturale; Core nasconde queste differenze dietro un'unica chiamata, così una mod può dichiarare entrambi una sola volta, nel codice condiviso, e farli funzionare su NeoForge, Forge e Fabric.

Niente di tutto ciò è visibile ai giocatori: esiste perché le mod di creature basate su Core si comportino in modo identico su tutti e tre i loader.

Vedi la documentazione per sviluppatori: [`../docs/SPACE-TAGS-AND-ENTITIES.md`](../../../docs/SPACE-TAGS-AND-ENTITIES.md).

## Vedi anche

- [Tag e pacchetti dati](Tags-and-Datapacks.md)
- [Per gli sviluppatori](For-Developers.md)
- [Home](Home.md)
