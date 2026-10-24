# Requisiti di progressione

> Questa pagina è una bozza tradotta automaticamente; fa fede la versione inglese: [English](../../Progression-Gates.md).

Un requisito di progressione è una tappa con nome (per esempio `nerolandcore:reached_orbit`). NeroQuests apre i requisiti man mano che i giocatori completano gli obiettivi, e ogni altra mod Neroland si limita a chiedere «questo requisito è aperto?» prima di sbloccare contenuti. Lo stato dei requisiti è **di proprietà del server** e sincronizzato con i client; i client non lo modificano mai.

## Ambiti

Ogni requisito dichiara un ambito. La risoluzione è automatica.

| Ambito | Significato |
| --- | --- |
| player (predefinito) | Percorso per singolo giocatore |
| team | Condiviso da una squadra del tabellone del gioco base (cooperativa) |
| server | Un unico flag condiviso per tutto il mondo |

Un requisito di squadra torna a essere per singolo giocatore se il giocatore non ha una squadra.

## I requisiti sono dati

Ogni requisito è un file JSON in `data/<namespace>/neroland_gates/<path>.json`. L'id del requisito è dato da namespace più percorso del file.

| Campo | Significato |
| --- | --- |
| `scope` | player, team o server |
| `requires` | Elenco degli id dei requisiti che devono già essere aperti |
| `title` | Titolo mostrato |

Un pacchetto sovrascrive un requisito fornendo lo stesso id, oppure ne aggiunge uno proprio con un nuovo id. Vincono i pacchetti caricati dopo.

## Il percorso canonico

Core include questa catena predefinita:

- `industrial_power`
- `reached_orbit`
- `first_colony`
- `deep_space`

`tryOpen` apre un requisito solo quando **ogni** requisito del suo elenco `requires` è già aperto. Le operazioni esplicite di apertura/chiusura forzano lo stato in ogni caso. Ogni modifica emette un evento e risincronizza i giocatori interessati.

## Tappe di scoperta dei materiali

Core fornisce anche tappe tipizzate per singolo materiale. Invece di aprire semplicemente un requisito con nome, registrano che un determinato identificatore di materiale è stato incontrato legittimamente tramite un'azione della mod proprietaria, una visita a un pianeta, una raccolta da parte del giocatore osservata dal server o un'azione di un amministratore. L'automazione da sola non può generare una scoperta.

Le definizioni sono guidate da pacchetti dati in `data/<namespace>/neroland_material_milestones/<path>.json` e usano gli stessi ambiti player/team/server. La definizione integrata `nerolandcore:material_discovered` è quella predefinita condivisa dall'ecosistema.

## Privacy

La memorizzazione della progressione è minima secondo POPIA/GDPR: un record del giocatore contiene solo un UUID più gli id dei requisiti o dei materiali. Non ci sono nomi, posizioni o date di scoperta. I record dei materiali dei giocatori possono essere esportati e tutti i record di progressione vengono cancellati tramite l'hook di cancellazione condiviso di Core. Vedi [Privacy e dati](Privacy-and-Data.md).

## Comandi

Servono principalmente per i test (livello op 2 per aprire/chiudere):

| Comando | Effetto |
| --- | --- |
| `/neroland gate list` | Elenca i requisiti |
| `/neroland gate open <id>` | Forza l'apertura di un requisito |
| `/neroland gate close <id>` | Forza la chiusura di un requisito |

Un id senza namespace come `reached_orbit` viene interpretato come `nerolandcore:reached_orbit`.

## Per gli sviluppatori

I requisiti possono essere controllati e aperti anche per giocatori offline. Un requisito di squadra si risolve tramite la squadra del tabellone del giocatore, come quando è online.

L'API completa della progressione è documentata in [../docs/PROGRESSION.md](../../../docs/PROGRESSION.md).

## Vedi anche

- [Economia e reputazione](Economy-and-Reputation.md)
- [Comandi](Commands.md)
- [Privacy e dati](Privacy-and-Data.md)
- [Home](Home.md)
