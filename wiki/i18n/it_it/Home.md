# Wiki di Neroland Core

> Questa pagina è una bozza tradotta automaticamente; fa fede la versione inglese: [English](../../Home.md).

Documentazione per giocatori e contributori di **Neroland Core**, la libreria di base dell'ecosistema Neroland.

> **Stato:** base V1 completata, versione `1.3.0`. Core è una **libreria, non una mod di contenuti**: include materiali, tag e una serie di API su cui si basano le altre mod Neroland (Nerospace, Nerotech, NeroPower, NeroEconomy, NeroFactions, NeroQuests, NeroEvents). Da sola aggiunge alcuni materiali di fabbricazione, una scheda creativa e un insieme di blocchi di stoccaggio passivi (Batteria, Serbatoio di fluidi, Serbatoio di gas, Deposito di oggetti, Cestino); il gioco vero e proprio (macchine, generatori, pianeti) si trova nelle mod che ne dipendono.

Realizzata per **Minecraft 26.1.2 e 26.2** su **NeoForge, Forge e Fabric**.

## Inizia da qui

- [Per iniziare](Getting-Started.md): cos'è Core, a chi è rivolta e come installarla.
- [Comandi](Commands.md): tutti i comandi `/neroland …` in un unico posto.
- [Domande frequenti](FAQ.md): risposte rapide.

## Contenuti e sistemi

- [Materiali](Materials.md): Lega Nero, Acciaio stellare, Cristallo del vuoto, Vetro al plasma e le loro forme.
- [Ricette](Recipes.md): le ricette di compattazione incluse in Core.
- [Macchine, energia e potenziamenti](Machines-Power-and-Upgrades.md): Nero Flux + framework delle macchine.
- [Configurazione dei lati](Side-Config.md): il controllo per lato di ingresso/uscita/energia/disattivazione condiviso da tutte le macchine.
- Blocchi di stoccaggio, i punti passivi inclusi in Core: [Batteria](Battery.md), [Serbatoio di fluidi](Fluid-Tank.md), [Serbatoio di gas](Gas-Tank.md), [Deposito di oggetti](Item-Store.md), [Cestino](Trash-Can.md) e i [Blocchi sorgente creativi](Creative-Source-Blocks.md).
- [Requisiti di progressione](Progression-Gates.md): il sistema condiviso delle tappe.
- [Registro dei materiali delle meteore](Meteor-Material-Registry.md): l'elenco condiviso dei materiali macinabili a resa casuale.
- [Economia e reputazione](Economy-and-Reputation.md): i contratti per valuta e reputazione presso le fazioni.
- [Evidenziazione degli oggetti](Item-Highlights.md): i bordi colorati negli slot che segnalano gli oggetti Nero negli inventari.
- [Configurazione](Configuration.md): il file di configurazione e il comando di ricaricamento.
- [Tag e pacchetti dati](Tags-and-Datapacks.md): tag `c:`/`neroland:` e regolazione tramite pacchetti.
- [Contratti decorativi](Decor-Contracts.md): esportazione della tavolozza, superfici per pannelli e tag `neroland:decor/*`.
- [Tag spaziali e registrazione delle entità](Space-Tags-and-Entities.md): il vocabolario di biomi `neroland:space/*` e il punto di aggancio multi-loader per registrare le creature.
- [Privacy e dati](Privacy-and-Data.md): POPIA/GDPR, cancellazione e conservazione.

## Per gli sviluppatori

- [Per gli sviluppatori](For-Developers.md): come dipendere da Core e usare ogni sistema.
- La documentazione API più approfondita si trova in [`../docs/`](../../../docs/USING-CORE.md).

## Vedi anche

- [Contesto per build e contributori](../../../AGENTS.md)
