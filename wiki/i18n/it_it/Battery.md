# Batteria

> Questa pagina è una bozza tradotta automaticamente; fa fede la versione inglese: [English](../../Battery.md).

Un accumulatore passivo di **Nero Flux (NF)** che fa da riserva per la tua rete elettrica. La Batteria è uno dei quattro blocchi di stoccaggio inclusi in Neroland Core (vedi [Macchine, energia e potenziamenti](Machines-Power-and-Upgrades.md)); immagazzina energia ma non svolge alcun lavoro.

> Questi blocchi sono passati da Nerospace a Core nella versione `1.1.0`, così tutte le mod Neroland condividono un unico insieme di punti di stoccaggio. I blocchi `nerospace:battery` già piazzati non vengono migrati automaticamente: vedi il [registro delle modifiche di Nerospace](https://github.com/Neroland/nerospace) per i dettagli.

## Come funziona

- Contiene una grande riserva di **NF** e accetta e fornisce energia su **ogni lato**.
- I generatori la caricano attraverso la rete e le macchine la scaricano allo stesso modo: così un calo di produzione non lascia la tua base al buio.
- Espone la capability `nerolandcore:energy` di Core, quindi qualsiasi macchina, cavo o tubo Nero (di qualsiasi mod Neroland) può caricarla o prelevarne energia.

La variante **Batteria creativa** (solo nella scheda creativa) è una sorgente e un pozzo di energia infiniti per testare le reti: vedi [Blocchi sorgente creativi](Creative-Source-Blocks.md).

## Dettagli

- ID: `nerolandcore:battery` · Attrezzo: piccone, livello ferro · Drop: se stessa
- Ricetta: vedi [Ricette](Recipes.md).

## Vedi anche

- [Macchine, energia e potenziamenti](Machines-Power-and-Upgrades.md)
- [Serbatoio di fluidi](Fluid-Tank.md)
- [Serbatoio di gas](Gas-Tank.md)
- [Deposito di oggetti](Item-Store.md)
- [Ricette](Recipes.md)
- [Home](Home.md)
