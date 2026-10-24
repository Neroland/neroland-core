# Configurazione dei lati

> Questa pagina è una bozza tradotta automaticamente; fa fede la versione inglese: [English](../../Side-Config.md).

Ogni macchina basata su Neroland Core condivide lo stesso modo di controllare come ciascuno dei suoi sei lati interagisce con i blocchi vicini. Per ogni lato stabilisci se riceve una risorsa in **ingresso**, la spinge in **uscita**, si collega all'**energia** o è **disattivato**, e il controllo funziona allo stesso modo su un generatore Nero, un processore di minerali, un Generatore di ossigeno o qualsiasi altra macchina basata su Core.

Questa pagina è per giocatori e creatori di modpack. Gli sviluppatori dovrebbero leggere [`../docs/SIDE-CONFIG.md`](../../../docs/SIDE-CONFIG.md).

## I lati

I lati di una macchina sono denominati in base al suo orientamento (davanti, dietro, sinistra, destra, sopra, sotto), così la tua configurazione resta corretta anche se distruggi la macchina e la ripiazzi rivolta in un'altra direzione. Il lato che era un'uscita resta l'uscita.

Ogni lato mostra una di queste modalità, con un colore per riconoscerla a colpo d'occhio:

- **Disattivato** (grigio): su quel lato non si collega nulla.
- **Ingresso** (blu): i vicini possono immettere la risorsa.
- **Uscita** (arancione): i vicini possono estrarre la risorsa.
- **I/O** (verde): sia ingresso sia uscita sullo stesso lato.
- **Spinta** (giallo): un'uscita che spinge attivamente la risorsa verso ciò che le sta accanto, anche senza tubi.

## Canali

Una macchina può gestire più di un tipo di risorsa: **oggetti**, **fluidi**, **gas** ed **energia**. Ogni tipo è un «canale» a sé con la propria configurazione per lato. Un generatore che gestisce solo energia mostra una sola pagina; un processore che gestisce oggetti ed energia mostra una scheda per ciascuno.

## Usare la scheda Configurazione dei lati

Apri la macchina e fai clic sulla scheda Configurazione dei lati. Sul cubo spiegato:

- **Clic sinistro** su un lato per scorrere le modalità.
- **Clic destro** su un lato per disattivarlo rapidamente.
- **Clic centrale** su un lato per scegliere una modalità specifica da una tavolozza.

Hai anche interruttori per canale di **espulsione automatica** e **immissione automatica** (la macchina spinge fuori da / tira dentro quei lati da sola, fino all'impostazione di velocità di trasferimento del modpack), un pulsante di **ripristino** per tornare alla disposizione predefinita della macchina e **copia/incolla** per applicare la disposizione di una macchina a un'altra dello stesso tipo.

## Il Configuratore

Alcune mod di contenuti (come Nerotech) aggiungono un oggetto **Configuratore** simile a una chiave inglese: fai clic destro sul lato di una macchina nel mondo per scorrere la modalità di quel lato senza aprire l'interfaccia, clic destro da accovacciato per leggere la modalità attuale, e copia/incolla un'intera disposizione tra macchine.

## Per i creatori di modpack

La velocità di espulsione / immissione automatica è un unico valore lato server, `sideConfigAutoTransferRate`, nella configurazione di Core: vedi [Configurazione](Configuration.md). Si può ricaricare a caldo con `/neroland config
reload`. Impostalo a `0` per disattivare del tutto il trasferimento automatico e affidarti ai tubi.

## Privacy

La configurazione dei lati viene salvata con la macchina (blocco) nel mondo. Registra solo come una macchina instrada le risorse, mai nulla che ti riguardi, quindi non rientra nella gestione dei dati dei giocatori di Core. Vedi [Privacy e dati](Privacy-and-Data.md).
