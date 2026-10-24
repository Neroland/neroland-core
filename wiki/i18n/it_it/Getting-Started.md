# Per iniziare

> Questa pagina è una bozza tradotta automaticamente; fa fede la versione inglese: [English](../../Getting-Started.md).

**Neroland Core** è la libreria di base dell'ecosistema di mod fantascientifiche Neroland. È realizzata per **Minecraft 26.1.2 e 26.2** su **NeoForge, Forge e Fabric**, con id mod `nerolandcore`.

## Cos'è Core (e cosa non è)

Core è una **libreria, non una mod di contenuti**. Fornisce i materiali, i tag e le API condivisi su cui si basano le altre mod Neroland, così l'intero ecosistema si comporta in modo coerente. Da sola Core aggiunge:

- alcuni materiali di fabbricazione e i relativi blocchi (vedi [Materiali](Materials.md)),
- una scheda creativa **Neroland** con gli oggetti di Core (ogni mod Nero ha la propria scheda),
- un albero di comandi `/neroland …` (vedi [Comandi](Commands.md)).

Il vero gioco (macchine, pianeti, missioni, economia) si trova nelle mod che si basano su Core (Nerospace, Nerotech, NeroPower, NeroEconomy, NeroFactions, NeroQuests, NeroEvents).

## A chi è rivolta

- **Giocatori**: installi Core perché una mod che vuoi la richiede. Funziona silenziosamente in sottofondo.
- **Creatori di modpack**: Core è altamente regolabile tramite pacchetto dati e configurazione senza toccare il codice; vedi [Tag e pacchetti dati](Tags-and-Datapacks.md) e [Configurazione](Configuration.md).
- **Sviluppatori di mod**: Core è l'unica dipendenza stabile su cui sviluppare; vedi [Per gli sviluppatori](For-Developers.md).

## Installazione

1. Installa il loader che preferisci (NeoForge, Forge o Fabric) per Minecraft 26.1.2 o 26.2.
2. Su **Fabric**, installa anche Fabric API.
3. Metti `nerolandcore` e tutte le mod che ne dipendono nella cartella `mods/`.

Core deve caricarsi **prima** delle mod che ne dipendono; i loader se ne occupano automaticamente in base alle dichiarazioni delle dipendenze.

## Le prime cose da provare

- Apri l'inventario creativo e trova la scheda **Neroland** per vedere i materiali inclusi in Core.
- Esegui `/neroland gate list` per vedere le tappe della progressione (vedi [Requisiti di progressione](Progression-Gates.md)).
- Esegui `/neroland config list` per vedere i valori regolabili di Core (vedi [Configurazione](Configuration.md)).

## Vedi anche

- [Comandi](Commands.md)
- [Materiali](Materials.md)
- [Per gli sviluppatori](For-Developers.md)
- [Domande frequenti](FAQ.md)
