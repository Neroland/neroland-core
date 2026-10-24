# Blocchi sorgente creativi

> Questa pagina è una bozza tradotta automaticamente; fa fede la versione inglese: [English](../../Creative-Source-Blocks.md).

Sorgenti infinite (e vuoti) per testare le reti: **solo nella scheda creativa**, indistruttibili in sopravvivenza, senza ricette. Esiste una variante creativa di tre dei [blocchi di stoccaggio](Machines-Power-and-Upgrades.md) di Core: la Batteria, il Serbatoio di fluidi e il Serbatoio di gas. (Il normale [Deposito di oggetti](Item-Store.md) basta da solo per la maggior parte dei test; le mod a valle possono aggiungere una sorgente infinita di oggetti.)

> Questi blocchi sono passati da Nerospace a Core nella versione `1.1.0`. Sono generici (Core non include fluidi o gas specifici), quindi le sorgenti di fluidi e gas si configurano nel mondo invece di essere fisse.

| Blocco | Fornisce | Configurazione |
| --- | --- | --- |
| **Batteria creativa** | energia infinita; accetta (ed elimina) qualsiasi NF immesso | — |
| **Serbatoio di fluidi creativo** | il fluido che preferisci, all'infinito | parte **vuoto**: fai clic destro con un **secchio pieno** per impostare il fluido |
| **Serbatoio di gas creativo** | gas infinito | **adotta il primo gas che vi entra tramite tubo** |

Ognuno funziona anche come vuoto: tutto ciò che vi viene immesso scompare, il che li rende perfetti come destinazione per i test di portata.

## Note sui valori generici predefiniti

Poiché Core è una libreria generica e non include fluidi o gas specifici, due di questi blocchi non sono precaricati:

- **Serbatoio di fluidi creativo**: parte vuoto; il fluido che imposti con un secchio pieno diventa la sua scorta infinita.
- **Serbatoio di gas creativo**: adotta il primo gas che vi entra tramite tubo, poi lo fornisce all'infinito.

Una mod a valle che include i propri fluidi o gas (per esempio il carburante per razzi e l'ossigeno di Nerospace) fornisce i secchi e le sorgenti di gas con cui configurarli.

## Dettagli

- ID: `nerolandcore:creative_battery`, `nerolandcore:creative_fluid_tank`, `nerolandcore:creative_gas_tank` (e `nerolandcore:creative_item_store` dove presente).
- Solo nella scheda creativa · indistruttibili in sopravvivenza · nessuna ricetta di fabbricazione.

## Vedi anche

- [Macchine, energia e potenziamenti](Machines-Power-and-Upgrades.md)
- [Batteria](Battery.md)
- [Serbatoio di fluidi](Fluid-Tank.md)
- [Serbatoio di gas](Gas-Tank.md)
- [Deposito di oggetti](Item-Store.md)
- [Home](Home.md)
