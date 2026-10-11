# Neroland Core — Currency & Reputation APIs

Core defines the contracts; **NeroEconomy** and **NeroFactions** implement the
storage. Any mod reads/writes through the facades and never against a concrete
store. Part of [V1](V1-PLAN.md) Phase 5.

## Principle: Core stores nothing

Core ships interfaces, change events, and a non-persistent in-memory reference
implementation that backs each API until a real provider registers. The in-memory
impl exists so the API works (and is testable) without the downstream mod — it holds
nothing across a restart and is replaced the moment a provider is set. Core never
persists balances or standings itself.

## Currency

Multiple named currencies from day one — every balance is keyed by a `Currency`
(`nerolandcore:credits` is the built-in default; NeroEconomy or a pack can add
per-faction or per-planet scrip).

```java
// read / move money (any mod)
long bal = CurrencyApi.getBalance(playerUuid, CoreCurrencies.CREDITS);
CurrencyApi.deposit(playerUuid, CoreCurrencies.CREDITS, 100);
CurrencyApi.withdraw(playerUuid, CoreCurrencies.CREDITS, 50);   // false on insufficient funds
CurrencyApi.transfer(fromUuid, toUuid, CoreCurrencies.CREDITS, 25);

// react to changes
CurrencyEvents.onChange(change ->
    LOGGER.info("{} now has {}", change.player(), change.newBalance()));
```

### Implementing it (NeroEconomy)

```java
public final class NeroEconomyStore implements CurrencyProvider {
    public long getBalance(UUID player, Currency currency) { /* your store */ }
    public boolean deposit(UUID player, Currency currency, long amount) { /* ... */ }
    public boolean withdraw(UUID player, Currency currency, long amount) { /* ... */ }
    // transfer() and forgetPlayer() have sensible defaults
}

// during NeroEconomy init:
CurrencyApi.setProvider(new NeroEconomyStore());
```

`CurrencyApi` fires `CurrencyEvents` around successful operations, so NeroEconomy
only implements storage — it doesn't have to fire events itself.

### Attributed calls (1.15.0)

Every money movement can name the mod behind it and a short reason code, so a provider that keeps
a ledger can record who moved the money and why:

```java
CurrencyApi.deposit(playerUuid, CoreCurrencies.CREDITS, 100, "neroquests", "quest_reward");
CurrencyApi.withdraw(playerUuid, CoreCurrencies.CREDITS, 50, "mymod", "shop_purchase");
CurrencyApi.transfer(fromUuid, toUuid, CoreCurrencies.CREDITS, 25, "mymod", "player_trade");

CurrencyEvents.onAttributedChange(c -> { /* c.change(), c.sourceModId(), c.reason() */ });
```

- `sourceModId` must match `[a-z0-9_.-]{1,64}` and `reason` must match `[a-z0-9_]{1,32}`
  (`event.SourceIds`). An invalid value makes the call return `false` without reaching the provider.
- `CurrencyProvider` gains matching default overloads that delegate to the 3-argument methods, so
  existing providers keep working unchanged; override them to record the attribution.
- `onChange` listeners still receive the bare `BalanceChange`. `onAttributedChange` listeners get an
  `AttributedChange` for every movement, with `null` attribution for the 3-argument calls.
- A listener that throws is logged once per listener class (never with the change) and skipped.

## Item sources (1.15.0)

`item.ItemSources` lets a mod that keeps items for a player (a storage network, a colony
warehouse, a vault) expose them to other mods. Shops, quest turn-ins and NeroLink views can then
count or draw those items without importing the owning mod:

```java
ItemSources.register("mymod", new ItemSource() {
    public long count(MinecraftServer server, UUID player, Identifier itemId) { /* owner-scoped */ }
    public int extract(MinecraftServer server, UUID player, Identifier itemId, int n, boolean simulate) { /* ... */ }
});

long total = ItemSources.count(server, playerUuid, itemId);             // summed across sources
int taken  = ItemSources.extract(server, playerUuid, itemId, 8, false); // registration order
```

Sources must work for offline players and report only what the player owns, never a server-wide
view. A source that throws is logged once per source class and treated as holding nothing.

## Threshold crossings: publisher identity (1.15.0)

`ThresholdEvents.fire(sourceModId, crossing)` names the publishing mod. Listeners that care
register with `ThresholdEvents.onSourcedCrossing(...)` and receive a `SourcedCrossing(sourceModId,
crossing)`. The original `fire(crossing)` still works and reports a `null` (unknown) source, and
`onCrossing` listeners are unchanged. An invalid source id (outside `[a-z0-9_.-]{1,64}`) is
reported as `null`.

## Reputation

Integer standing keyed by `(player UUID, faction id)`; the scale and clamping are the
provider's choice.

```java
int standing = ReputationApi.getReputation(playerUuid, faction);
ReputationApi.adjust(playerUuid, faction, +10);
ReputationApi.setReputation(playerUuid, faction, 0);

ReputationEvents.onChange(change -> { /* unlock a faction perk, trigger an event */ });
```

### Implementing it (NeroFactions)

```java
public final class NeroFactionsStore implements ReputationProvider {
    public int getReputation(UUID player, Identifier faction) { /* ... */ }
    public void setReputation(UUID player, Identifier faction, int value) { /* ... */ }
}
ReputationApi.setProvider(new NeroFactionsStore());
```

## Privacy (POPIA/GDPR)

Both APIs key on player UUID. `CurrencyProvider` and `ReputationProvider` expose a
`forgetPlayer(UUID)` erasure hook (the in-memory impls clear the player; real
providers wipe their store). This is the contract Core's shared per-player erasure
(Phase 7) drives so one request purges a player across economy, reputation, and
progression together.
