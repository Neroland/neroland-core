# Configurazione

> Questa pagina è una bozza tradotta automaticamente; fa fede la versione inglese: [English](../../Configuration.md).

Neroland Core fornisce un unico servizio di configurazione, così ogni mod Neroland registra la propria configurazione in Core invece di gestire a mano la lettura e scrittura dei file. Ogni mod ha un file `<modId>.properties` nella cartella di configurazione del loader, con uno schema tipizzato, valori predefiniti, validazione, ricaricamento a caldo e sincronizzazione opzionale dal server ai client.

## Come si comportano i file di configurazione

- il file viene creato al primo avvio con i valori predefiniti documentati
- i valori numerici vengono limitati al loro intervallo `[min, max]`
- le voci errate o vuote tornano al valore predefinito
- le nuove chiavi vengono aggiunte automaticamente a un file esistente
- per il resto, le tue modifiche e i tuoi commenti restano intatti

## Builder tipizzati

Gli schemi sono costruiti con voci tipizzate. Ognuna accetta una chiave, un valore predefinito, (i limiti dell'intervallo, se applicabili), un flag `serverAuthoritative` e un commento.

- `bool`
- `intRange`
- `longRange`
- `doubleRange`
- `string`

## Valori decisi dal server

Un valore è contrassegnato `serverAuthoritative = true` quando il server deve imporlo a ogni client, di solito per il bilanciamento del gioco. Core invia un'istantanea a ogni giocatore quando entra e dopo un ricaricamento. I valori solo locali (opzioni di debug, effetti visivi del client) restano `false` e vengono letti dal file locale.

I dati sincronizzati contengono solo chiavi e valori di configurazione, mai l'identità del giocatore.

## Lo schema di Core (`CoreConfig`)

| Chiave | Scopo |
| --- | --- |
| `materialHardnessMultiplier` | Scala la durezza dei blocchi di materiale |
| `materialBlastResistanceMultiplier` | Scala la resistenza alle esplosioni dei blocchi di materiale |
| `upgradeModuleSlotCap` | Limita il numero di slot per i moduli di potenziamento |
| `upgradeStackingDiminish` | Curva dei rendimenti decrescenti per i potenziamenti sommati |
| `neroEnergyToForgeEnergyRatio` | Rapporto di conversione NF <-> Forge Energy |
| `itemHighlightsEnabled` | Bordi colorati negli slot degli oggetti Nero (vedi Evidenziazione degli oggetti) |
| `itemHighlightOpacity` | Opacità del bordo evidenziato, 0-100 per cento |
| `itemHighlightThickness` | Spessore del bordo evidenziato in pixel, 1-4 |
| `debugLogging` | Attiva i log di debug dettagliati |
| `telemetryEnabled` | Segnalazioni anonime di arresto anomalo relative solo a Neroland Core tramite Sentry (server UE); predefinito `true`, imposta `false` per disattivarle. Locale al client, non sincronizzato (vedi Privacy e dati) |
| `dataRetentionDays` | Conservazione dei dati dei giocatori inattivi; 0 = mai |
| `linkAlertsMaxPerPlayer` | Numero massimo di avvisi NeroLink conservati per giocatore (predefinito 50); vengono rimossi prima i più vecchi già confermati |
| `linkAlertsMaxPerModule` | Numero massimo di avvisi NeroLink conservati per giocatore da una singola mod (predefinito 25) |

## Comandi

Entrambi richiedono il livello op 2.

| Comando | Effetto |
| --- | --- |
| `/neroland config reload` | Rilegge ogni schema e risincronizza |
| `/neroland config list` | Stampa i valori di ogni schema, segnalando quelli decisi dal server |

## Per gli sviluppatori

L'API di configurazione completa è documentata in [../docs/CONFIG.md](../../../docs/CONFIG.md).

## Vedi anche

- [Macchine, energia e potenziamenti](Machines-Power-and-Upgrades.md)
- [Privacy e dati](Privacy-and-Data.md)
- [Comandi](Commands.md)
- [Home](Home.md)
