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
