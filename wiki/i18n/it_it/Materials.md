# Materiali

> Questa pagina è una bozza tradotta automaticamente; fa fede la versione inglese: [English](../../Materials.md).

Neroland Core include quattro materiali portanti per l'ecosistema Neroland. Core fornisce oggetti, blocchi, tag e ricette di compattazione, ma **nessun minerale**. Le mod che aggiungono il minerale di un materiale (per esempio i minerali planetari di Nerospace) lo fondono in questi oggetti di Core tramite tag, così tutte le mod condividono un unico insieme di materiali.

Le texture incluse nella 1.0.0 sono segnaposto procedurali 16x16 destinati a essere ridisegnati in seguito.

## I quattro materiali

| Materiale | Tema | Forme | Blocco di stoccaggio |
| --- | --- | --- | --- |
| Lega Nero | Industriale | pepita, lingotto, polvere, piastra | Blocco di Lega Nero |
| Acciaio stellare | Era spaziale | pepita, lingotto, polvere, piastra | Blocco di acciaio stellare |
| Cristallo del vuoto | Alieno | scheggia, gemma («Cristallo del vuoto»), polvere | Blocco di cristallo del vuoto (leggero bagliore) |
| Vetro al plasma | Trasparente | scheggia | blocco di Vetro al plasma + pannello |

## Forme e id degli oggetti

### Lega Nero e Acciaio stellare

Entrambi sono metalli e condividono le stesse quattro forme più un blocco di stoccaggio.

- pepita
- lingotto
- polvere
- piastra
- blocco di stoccaggio («Blocco di Lega Nero» / «Blocco di acciaio stellare»)

### Cristallo del vuoto

È un cristallo, quindi **non ha piastra**.

- scheggia (`void_crystal_shard`)
- oggetto gemma mostrato come «Cristallo del vuoto» (id oggetto `void_crystal`)
- polvere
- blocco di stoccaggio («Blocco di cristallo del vuoto», leggero bagliore)

### Vetro al plasma

- oggetto scheggia mostrato come «Scheggia di vetro al plasma» (id oggetto `plasma_glass`)
- blocco trasparente «Vetro al plasma» (`plasma_glass_block`)
- pannello «Pannello di vetro al plasma» (`plasma_glass_pane`)

## Riferisciti sempre tramite tag

Non dipendere mai dall'id oggetto di un materiale. Riferisciti ai materiali tramite tag, così modpack e altre mod possono sostituirli, regolarli o estenderli:

- le ricette tra mod e l'ingresso/uscita delle macchine usano la famiglia `c:` (per esempio `c:ingots/nero_alloy`)
- i controlli «è uno dei nostri materiali?» usano `neroland:materials/<material>`

Vedi [Tag e pacchetti dati](Tags-and-Datapacks.md) per la mappa completa dei tag.

## Per gli sviluppatori

Dettagli più approfonditi su materiali e tag per gli autori di mod si trovano in [../docs/TAGS-AND-DATAPACKS.md](../../../docs/TAGS-AND-DATAPACKS.md).

## Vedi anche

- [Ricette](Recipes.md)
- [Tag e pacchetti dati](Tags-and-Datapacks.md)
- [Per iniziare](Getting-Started.md)
- [Home](Home.md)
