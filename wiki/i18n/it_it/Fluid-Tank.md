# Serbatoio di fluidi

> Questa pagina è una bozza tradotta automaticamente; fa fede la versione inglese: [English](../../Fluid-Tank.md).

Un deposito passivo per un singolo fluido. Il Serbatoio di fluidi è uno dei quattro blocchi di stoccaggio inclusi in Neroland Core (vedi [Macchine, energia e potenziamenti](Machines-Power-and-Upgrades.md)); contiene un fluido e lo mette a disposizione di tubi e secchi.

> Questi blocchi sono passati da Nerospace a Core nella versione `1.1.0`. I blocchi `nerospace:fluid_tank` già piazzati non vengono migrati automaticamente: vedi il [registro delle modifiche di Nerospace](https://github.com/Neroland/nerospace) per i dettagli.

## Come funziona

- Contiene **un solo** fluido alla volta, misurato in millisecchi (mB).
- **Secchi:** fai clic destro con un secchio pieno per versare, con un secchio vuoto per prelevare.
- **Tubi:** si riempie e si svuota su **ogni lato**, sia tramite la capability `nerolandcore:fluid` di Core sia tramite la capability per fluidi **standard** del loader (NeoForge `Capabilities.Fluid`, Forge `FLUID_HANDLER`, Fabric `FluidStorage.SIDED`): così i tubi per fluidi di qualsiasi mod Neroland *e* di qualsiasi altra mod possono immettere o estrarre fluido. Il serbatoio preleva anche dai serbatoi di terze parti adiacenti e li riempie.
- Un clic destro a mani vuote mostra il contenuto.

Core non include fluidi specifici: il serbatoio contiene qualsiasi fluido fornito da una mod a valle o dal gioco base.

La variante **Serbatoio di fluidi creativo** fornisce all'infinito il fluido che preferisci: vedi [Blocchi sorgente creativi](Creative-Source-Blocks.md).

## Dettagli

- ID: `nerolandcore:fluid_tank` · Attrezzo: piccone, livello ferro · Drop: se stesso
- Ricetta: vedi [Ricette](Recipes.md).

## Vedi anche

- [Macchine, energia e potenziamenti](Machines-Power-and-Upgrades.md)
- [Batteria](Battery.md)
- [Serbatoio di gas](Gas-Tank.md)
- [Deposito di oggetti](Item-Store.md)
- [Ricette](Recipes.md)
- [Home](Home.md)
