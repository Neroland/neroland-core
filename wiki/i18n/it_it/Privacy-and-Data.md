# Privacy e dati (sintesi)

> Questa pagina è una bozza tradotta automaticamente; fa fede la versione inglese: [English](../../Privacy-and-Data.md).

Questa è una sintesi tradotta per comodità; il testo vincolante è la versione inglese.

Neroland Core gestisce il contratto di protezione dei dati per l'intero ecosistema, secondo i principi di POPIA e GDPR. Core memorizza solo lo stato di gioco minimo necessario, indicizzato dall'UUID del giocatore, e fornisce un unico hook di cancellazione condiviso a cui si registrano tutti i sistemi Neroland e le mod a valle.

## Cosa memorizza Core

Solo stato di gioco indicizzato dall'UUID del giocatore: flag dei requisiti di progressione, tappe dei materiali, data e ora dell'ultimo accesso (per la conservazione) e avvisi NeroLink (id della mod, gravità, un breve messaggio non personale). Core **non** memorizza saldi o reputazione (vedi [Economia e reputazione](Economy-and-Reputation.md)) e non memorizza mai nomi, IP, chat o cronologia delle posizioni oltre quanto serve al gioco.

## Minimizzazione dei dati

I record dei giocatori contengono solo un UUID e il valore di gioco; nessun dato dei giocatori viene registrato nei log di livello info; la cancellazione registra una conferma anonima, non chi è stato cancellato.

## Diritto alla cancellazione

| Comando | Chi | Effetto |
| --- | --- | --- |
| `/neroland data eraseme` | Qualsiasi giocatore | Cancella i tuoi dati Neroland (rinuncia / azzeramento) |
| `/neroland data erase <uuid>` | Amministratore (livello op 2) | Cancella un giocatore specifico |

**Limite noto:** i requisiti di progressione e le tappe dei materiali dichiarati con `scope: team` sono memorizzati in base al nome della squadra del tabellone, quindi la cancellazione di un giocatore non li rimuove. Dai alle squadre nomi di ruoli o colori, mai di singoli giocatori; per la spiegazione completa e i passaggi da seguire consulta la versione inglese.

## Conservazione

La configurazione `dataRetentionDays` (0 = mai, attivazione esplicita) stabilisce per quanto tempo vengono conservati i dati di un giocatore inattivo. `/neroland data purge-inactive` cancella tutti quelli oltre la soglia. Vedi [Configurazione](Configuration.md).

## Segnalazioni di arresto anomalo (Sentry)

Le segnalazioni di arresto anomalo sono **attive per impostazione predefinita** e si possono **disattivare**. Quando si verifica un errore causato dal codice di Neroland Core, viene inviata una segnalazione anonima a Sentry, ospitato nell'UE (`ingest.de.sentry.io`). Non contiene mai il tuo indirizzo IP, nome utente, UUID del giocatore, nome del computer, dati del mondo, chat, coordinate o inventari, e non esiste un identificatore persistente che colleghi le segnalazioni. Le segnalazioni sono limitate a 10 per sessione ed eliminate dopo 90 giorni. Per l'elenco completo dei contenuti consulta la versione inglese.

Per disattivarle, imposta quanto segue in `config/nerolandcore.properties` (creato al primo avvio) e riavvia:

```properties
telemetryEnabled=false
```

È un'impostazione locale: il server non la sincronizza con i client. Domande o richieste sui dati: [info@neroland.co.za](mailto:info@neroland.co.za).

## Mod a valle e sviluppatori

Se la tua mod memorizza dati dei giocatori: usa l'UUID come chiave, memorizza solo ciò che serve al gioco, tieni i dati dei giocatori fuori dai log di livello info e registra un eraser. Le linee guida complete sulla conformità sono documentate in [../docs/COMPLIANCE.md](../../../docs/COMPLIANCE.md).

## Vedi anche

- [Configurazione](Configuration.md)
- [Requisiti di progressione](Progression-Gates.md)
- [Economia e reputazione](Economy-and-Reputation.md)
- [Comandi](Commands.md)
- [Home](Home.md)
