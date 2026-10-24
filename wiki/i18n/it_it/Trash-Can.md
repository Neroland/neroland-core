# Cestino

> Questa pagina è una bozza tradotta automaticamente; fa fede la versione inglese: [English](../../Trash-Can.md).

Un pozzo senza fondo per le risorse indesiderate. Il Cestino è uno dei blocchi di stoccaggio inclusi in Neroland Core (vedi [Macchine, energia e potenziamenti](Machines-Power-and-Upgrades.md)); qualsiasi cosa vi immetti con tubi o tramogge viene distrutta.

> Questo blocco è passato da Nerospace a Core nella versione `1.1.0`. I blocchi `nerospace:trash_can` già piazzati vengono rimappati in `nerolandcore:trash_can` al caricamento dal meccanismo di alias degli id dei blocchi di Core: vedi il [registro delle modifiche di Nerospace](https://github.com/Neroland/nerospace) per i dettagli.

## Come funziona

- **Elimina ogni tipo di risorsa:** accetta **oggetti, fluidi e gas** da qualsiasi lato e li scarta; non c'è alcuna superficie di estrazione, quindi niente può essere recuperato.
- **Non si intasa mai:** i suoi pozzi interni vengono svuotati, così ha sempre spazio e non rifiuta né restituisce mai nulla.
- **Solo ingresso:** non è possibile estrarre nulla; si perde solo ciò che viene esplicitamente immesso *nel* Cestino con tubi o tramogge.
- **Niente energia:** non accetta energia (l'energia non è «spazzatura»), solo oggetti, fluidi e gas.

## Interfaccia

Fai clic destro per aprire un'**interfaccia simile al baule del gioco base** con **un solo slot di scarico**: metti una pila e viene eliminata alla successiva immissione. È il primo tipo di menu di Core, basato sull'infrastruttura delle schermate client di Core.

## Dettagli

- ID: `nerolandcore:trash_can` · Attrezzo: piccone, livello ferro · Drop: se stesso
- Ricetta: un anello di lingotti di ferro attorno a un cactus (vedi [Ricette](Recipes.md)).

## Vedi anche

- [Macchine, energia e potenziamenti](Machines-Power-and-Upgrades.md)
- [Batteria](Battery.md)
- [Serbatoio di fluidi](Fluid-Tank.md)
- [Serbatoio di gas](Gas-Tank.md)
- [Deposito di oggetti](Item-Store.md)
- [Ricette](Recipes.md)
- [Home](Home.md)
