# Serbatoio di gas

> Questa pagina è una bozza tradotta automaticamente; fa fede la versione inglese: [English](../../Gas-Tank.md).

Un deposito pressurizzato per **un solo gas**. Il Serbatoio di gas è uno dei quattro blocchi di stoccaggio inclusi in Neroland Core (vedi [Macchine, energia e potenziamenti](Machines-Power-and-Upgrades.md)); contiene un singolo gas e lo scambia tramite la capability per i gas.

> Questi blocchi sono passati da Nerospace a Core nella versione `1.1.0`. I blocchi `nerospace:gas_tank` già piazzati non vengono migrati automaticamente: vedi il [registro delle modifiche di Nerospace](https://github.com/Neroland/nerospace) per i dettagli.

## Come funziona

- Contiene **un solo** gas alla volta, misurato in millisecchi (mB).
- Si riempie e si svuota su **ogni lato** tramite la capability `nerolandcore:gas` di Core, così i tubi per gas di qualsiasi mod Neroland possono immettere o estrarre gas.
- Un clic destro a mani vuote mostra il contenuto.

Core non include gas specifici: il serbatoio contiene qualsiasi gas una mod a valle vi immetta tramite tubo (internamente i gas sono identificati da un id; il serbatoio adotta il primo gas che riceve).

La variante **Serbatoio di gas creativo** fornisce gas all'infinito: vedi [Blocchi sorgente creativi](Creative-Source-Blocks.md).

## Dettagli

- ID: `nerolandcore:gas_tank` · Attrezzo: piccone, livello ferro · Drop: se stesso
- Ricetta: vedi [Ricette](Recipes.md).

## Vedi anche

- [Macchine, energia e potenziamenti](Machines-Power-and-Upgrades.md)
- [Batteria](Battery.md)
- [Serbatoio di fluidi](Fluid-Tank.md)
- [Deposito di oggetti](Item-Store.md)
- [Ricette](Recipes.md)
- [Home](Home.md)
