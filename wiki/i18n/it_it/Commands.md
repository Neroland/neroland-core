# Comandi

> Questa pagina è una bozza tradotta automaticamente; fa fede la versione inglese: [English](../../Commands.md).

Tutti i comandi di Neroland Core si trovano sotto la radice `/neroland`. I comandi che modificano lo stato del server o riguardano altri giocatori richiedono il **livello op 2** (gamemaster); il comando sui dati personali è disponibile a qualsiasi giocatore per i propri dati.

## Configurazione

| Comando | Permesso | Effetto |
| ------- | ---------- | ------ |
| `/neroland config reload` | op 2 | Rilegge dal disco ogni schema di configurazione registrato e risincronizza i valori decisi dal server con i client online. |
| `/neroland config list` | op 2 | Stampa i valori di ogni schema, segnalando quelli decisi dal server. |

Vedi [Configurazione](Configuration.md).

## Requisiti di progressione

| Comando | Permesso | Effetto |
| ------- | ---------- | ------ |
| `/neroland gate list` | chiunque | Elenca ogni requisito definito, il suo ambito e se è aperto per te. |
| `/neroland gate open <id>` | op 2 | Forza l'apertura di un requisito per te. |
| `/neroland gate close <id>` | op 2 | Forza la chiusura di un requisito per te. |

Un id senza namespace come `reached_orbit` viene interpretato come `nerolandcore:reached_orbit`. Vedi [Requisiti di progressione](Progression-Gates.md).

## Dati e privacy

| Comando | Permesso | Effetto |
| ------- | ---------- | ------ |
| `/neroland data eraseme` | chiunque | Cancella i **tuoi** dati Neroland in tutti i sistemi (rinuncia / azzeramento). |
| `/neroland data erase <uuid>` | op 2 | Cancella i dati Neroland di un giocatore specifico. |
| `/neroland data purge-inactive` | op 2 | Cancella i dati di tutti i giocatori inattivi oltre la soglia `dataRetentionDays`. |

Vedi [Privacy e dati](Privacy-and-Data.md).

## Vedi anche

- [Configurazione](Configuration.md)
- [Requisiti di progressione](Progression-Gates.md)
- [Privacy e dati](Privacy-and-Data.md)
