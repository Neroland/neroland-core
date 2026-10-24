# Domande frequenti

> Questa pagina è una bozza tradotta automaticamente; fa fede la versione inglese: [English](../../FAQ.md).

## Neroland Core aggiunge contenuti di gioco da sola?

Non molti. Core è una **libreria**: include alcuni materiali di fabbricazione, tag e una serie di API su cui si basano le altre mod Neroland. La installi perché una mod che vuoi la richiede. Vedi [Per iniziare](Getting-Started.md).

## Quali versioni di Minecraft e quali loader sono supportati?

Minecraft **26.1.2 e 26.2** su **NeoForge, Forge e Fabric**. Su Fabric serve anche Fabric API.

## Quali materiali aggiunge Core?

Lega Nero, Acciaio stellare, Cristallo del vuoto e Vetro al plasma, più le loro forme (lingotti, pepite, polveri, piastre, schegge, gemme, blocchi, pannelli). Vedi [Materiali](Materials.md). Core di proposito **non include minerali**: le mod che introducono il minerale di un materiale lo fondono in questi oggetti di Core.

## Perché le texture sembrano provvisorie?

Le texture della 1.0.0 sono segnaposto procedurali destinati a essere ridisegnati in seguito. Funzionalità e tag sono definitivi; la grafica no.

## Un pacchetto per server può regolare Core senza codice?

Sì. Ricette, tabelle del bottino, tag, requisiti di estrazione e definizioni dei requisiti di progressione sono semplici dati sovrascrivibili tramite pacchetti dati, e il bilanciamento numerico si trova nella configurazione. Vedi [Tag e pacchetti dati](Tags-and-Datapacks.md) e [Configurazione](Configuration.md).

## Core ha macchine o energia?

Core include il **framework** (Nero Flux, un block entity base per le macchine, il sistema dei moduli di potenziamento) ma nessuna macchina o generatore concreto: quelli arrivano da mod come Nerotech e NeroPower. Vedi [Macchine, energia e potenziamenti](Machines-Power-and-Upgrades.md).

## Core gestisce denaro o fazioni?

Core definisce i contratti di **valuta** e **reputazione** e include un'alternativa in memoria non persistente, ma non memorizza nulla in modo permanente. NeroEconomy e NeroFactions forniscono la vera memorizzazione. Vedi [Economia e reputazione](Economy-and-Reputation.md).

## Come cancello i miei dati?

Esegui `/neroland data eraseme` per cancellare i tuoi dati Neroland in tutti i sistemi. Gli amministratori e le pulizie periodiche usano gli altri comandi `/neroland data`. Vedi [Privacy e dati](Privacy-and-Data.md) e [Comandi](Commands.md).

## Sono uno sviluppatore di mod: come sviluppo su Core?

Vedi [Per gli sviluppatori](For-Developers.md) e la guida più approfondita [`../docs/USING-CORE.md`](../../../docs/USING-CORE.md).

## Vedi anche

- [Home](Home.md)
- [Per iniziare](Getting-Started.md)
- [Comandi](Commands.md)
