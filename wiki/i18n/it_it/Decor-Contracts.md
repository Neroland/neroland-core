# Contratti decorativi

> Questa pagina è una bozza tradotta automaticamente; fa fede la versione inglese: [English](../../Decor-Contracts.md).

Contratti condivisi e **puramente estetici** che Core espone per il livello decorativo dell'ecosistema (aggiunti in Core 1.9.0). NeroDecor è il primo a usarli, ma qualsiasi mod o pacchetto dati può farlo. Nessuno di questi contiene dati dei giocatori, quindi nessuno è soggetto a cancellazione.

## Esportazione della tavolozza

Un registro di sola lettura di **finiture** con nome (colore + metadati di emissione luminosa), così blocchi decorativi, rifiniture delle macchine e texture corrispondono esattamente ai toni dei materiali di Core. Core include i quattro materiali (`neroland:nero_alloy`, `neroland:starsteel`, `neroland:void_crystal`, `neroland:plasma_glass`) più un set di 16 colori d'accento (`neroland:accent/<dye-name>`). Leggili tramite `PaletteRegistry.getFinish(id)`.

## Superfici per pannelli

Ologrammi e pannelli di controllo espongono una piccola API di contenuto (testo, icona, colore di stato, indirizzati per dimensione + posizione del blocco) su cui mod come NeroSecurity e NeroLogistics possono inviare letture. Core definisce solo l'interfaccia e un registro vuoto; il rendering lo fa la mod che fornisce il driver. Senza driver, la superficie resta semplicemente vuota.

## Tag decorativi

I tag di blocco `neroland:decor/*` (`hull`, `panel`, `glass`, `neon`, `trim`, `dashboard_surface`, `planet` + uno per pianeta) raggruppano le superfici decorative, così altre mod (e mod esterne, solo tramite tag) possono affiancarsi alle costruzioni di NeroDecor. L'appartenenza è dichiarata con JSON dei pacchetti dati nella mod proprietaria.

Vedi la documentazione per sviluppatori: [`../docs/DECOR-CONTRACTS.md`](../../../docs/DECOR-CONTRACTS.md).
