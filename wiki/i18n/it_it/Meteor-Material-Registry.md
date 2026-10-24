# Registro dei materiali delle meteore

> Questa pagina è una bozza tradotta automaticamente; fa fede la versione inglese: [English](../../Meteor-Material-Registry.md).

Il Registro dei materiali delle meteore è un elenco condiviso di **materiali macinabili** che Neroland Core mantiene per l'intero ecosistema. Le macchine che trasformano qualcosa in un materiale Nero *casuale* (prima fra tutte il **macinatore di meteore** di Nerospace) leggono questo elenco invece di definirne uno proprio. Qualsiasi mod (o tu, con un pacchetto dati) può aggiungervi un materiale, e le probabilità si ribilanciano automaticamente.

> Core da sola non aggiunge un macinatore: possiede l'elenco. Lo vedrai all'opera tramite le mod che lo usano, come Nerospace.

## Contenuto predefinito dell'elenco

Core include i suoi quattro materiali di base:

| Materiale | Prodotto | Rarità | Sbloccato da |
| -------- | ------ | ------ | ----------- |
| Lega Nero | Polvere di Lega Nero | comune | sempre |
| Vetro al plasma | Vetro al plasma | non comune | sempre |
| Cristallo del vuoto | Polvere di cristallo del vuoto | non comune | raggiungere l'orbita |
| Acciaio stellare | Polvere di acciaio stellare | raro | raggiungere l'orbita |

Altre mod aggiungono i propri (minerali planetari, drop esotici, …). Se una mod non è installata, le sue voci semplicemente non ci sono: non si rompe nulla.

## Come funzionano le probabilità

Ogni materiale ha un **livello di rarità** che ne stabilisce il peso base, e il macinatore ne sceglie uno a caso in proporzione a quei pesi, ma solo tra i materiali che hai effettivamente sbloccato. Man mano che progredisci e più materiali diventano idonei, quelli comuni diventano naturalmente più rari, senza tabelle fisse da nessuna parte.

Un esempio con i materiali di Core:

- **Prima di raggiungere l'orbita:** Polvere di Lega Nero ~70,6%, Vetro al plasma ~29,4%.
- **Dopo aver raggiunto l'orbita:** ~49,2% / 20,5% / 20,5% / 9,8% tra i quattro.
- **Su un pianeta colonia (con un minerale legato al pianeta):** il minerale locale entra nel gruppo con un vantaggio, diluendo ulteriormente gli altri; lontano da quel pianeta scompare di nuovo.

C'è anche una piccola probabilità separata (8% per impostazione predefinita) di un drop **esotico** bonus dalle mod che registrano materiali esotici.

## Regolazione (amministratori di server)

Le leve si trovano nella configurazione di Core (`config/nerolandcore.properties`) e si applicano dopo `/neroland config reload`:

- `meteorTierBaseWeightCommon` / `…Uncommon` / `…Rare`: i pesi base (predefiniti 60 / 25 / 12).
- `meteorPlanetBias`: quanto un pianeta favorisce il proprio minerale (predefinito 2.0).
- `meteorExoticChance`: la probabilità di bonus esotico per macinazione (predefinita 0.08).

Controlla l'elenco attivo nel gioco con `/neroland meteor list` e rileggi i file dei materiali con `/neroland meteor reload` (solo operatori). Vedi [Comandi](Commands.md) e [Configurazione](Configuration.md).

## Aggiungere o modificare materiali con un pacchetto dati

Inserisci un JSON per materiale in un pacchetto dati in `data/<your_namespace>/neroland/meteor_materials/<id>.json`:

```json
{
  "item": "yourmod:cobalt_dust",
  "tier": "uncommon",
  "min_gate": "nerolandcore:reached_orbit",
  "planet": null,
  "weight_override": null,
  "enabled": true
}
```

- `tier` è uno tra `common`, `uncommon`, `rare`, `exotic` (le voci `exotic` devono impostare `weight_override`).
- `min_gate` è l'id di un [requisito di progressione](Progression-Gates.md), oppure `null` per «sempre».
- `planet` lega il materiale a una dimensione (l'id di un pianeta Nerospace), oppure `null` per ovunque.
- Per **regolare** un materiale esistente, inserisci un file nello stesso percorso in un pacchetto dati con priorità più alta: devi includere solo i campi che vuoi cambiare, gli altri vengono mantenuti. Un `null` esplicito azzera un campo.

L'appartenenza per i visualizzatori di ricette è il tag `neroland:meteor/grindable`; aggiungi lì anche il tuo oggetto se vuoi che JEI/REI e i livelli di compatibilità lo riconoscano come macinabile.

Gli autori di mod possono invece annotare gli oggetti nel codice con `@GrindableMaterial`: vedi la [documentazione per sviluppatori](../../../docs/METEOR-MATERIAL-REGISTRY.md).

## Privacy

Il registro contiene solo metadati degli oggetti: non memorizza nulla sui giocatori, quindi non c'è niente da esportare o cancellare. Vedi [Privacy e dati](Privacy-and-Data.md).
