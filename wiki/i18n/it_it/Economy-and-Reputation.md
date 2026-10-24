# Economia e reputazione

> Questa pagina è una bozza tradotta automaticamente; fa fede la versione inglese: [English](../../Economy-and-Reputation.md).

Neroland Core definisce i **contratti** per valuta e reputazione ma non memorizza nulla. NeroEconomy implementa la memorizzazione della valuta e NeroFactions la reputazione. Finché non si registra un vero fornitore, Core include un'implementazione di riferimento in memoria, non persistente, a supporto di ogni API, così l'ecosistema funziona da subito.

## Valuta

Core supporta più valute con nome fin dal primo giorno. Ogni saldo è indicizzato da una `Currency`. La valuta predefinita integrata è `nerolandcore:credits` («Crediti»).

| API | Comportamento |
| --- | --- |
| `getBalance` | Legge il saldo di un giocatore in una valuta |
| `deposit` | Aggiunge fondi |
| `withdraw` | Rimuove fondi; restituisce false se i fondi sono insufficienti |
| `transfer` | Sposta fondi tra giocatori (implementazione predefinita) |

Gli eventi di modifica vengono emessi attorno alle operazioni riuscite. Un fornitore implementa `getBalance`, `deposit` e `withdraw`; `transfer` e `forgetPlayer` hanno implementazioni predefinite.

Quando sposta denaro, una mod può anche indicare il proprio nome e un breve codice motivo (per esempio `neroquests` / `quest_reward`), così una mod economica può tenere un registro di chi ha pagato cosa e perché. L'id della mod e il codice motivo accettano solo lettere minuscole, cifre e pochi separatori, quindi non contengono mai dati personali.

## Reputazione

La reputazione è un valore intero indicizzato da (UUID del giocatore, id della fazione). Scala e limiti sono scelti dal fornitore.

| API | Comportamento |
| --- | --- |
| `getReputation` | Legge la reputazione presso una fazione |
| `adjust` | Modifica la reputazione di +/- |
| `setReputation` | Imposta direttamente la reputazione |

È possibile iscriversi agli eventi di modifica.

## Privacy

Entrambe le API usano l'UUID del giocatore come chiave. I fornitori espongono un hook di cancellazione `forgetPlayer(UUID)`, attivato dalla cancellazione condivisa di Core. Core non memorizza direttamente saldi o reputazione. Vedi [Privacy e dati](Privacy-and-Data.md).

## Per gli sviluppatori

L'API completa di economia e reputazione è documentata in [../docs/ECONOMY-REPUTATION.md](../../../docs/ECONOMY-REPUTATION.md).

## Vedi anche

- [Requisiti di progressione](Progression-Gates.md)
- [Privacy e dati](Privacy-and-Data.md)
- [Configurazione](Configuration.md)
- [Home](Home.md)
