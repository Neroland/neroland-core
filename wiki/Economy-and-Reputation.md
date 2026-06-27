# Economy and Reputation

Neroland Core defines the **contracts** for currency and reputation but stores
nothing itself. NeroEconomy implements currency storage and NeroFactions
implements reputation. Until a real provider registers, Core ships a
non-persistent, in-memory reference implementation that backs each API so the
ecosystem works out of the box.

## Currency

Core supports multiple named currencies from day one. Each balance is keyed by a
`Currency`. The built-in default is `nerolandcore:credits` ("Credits").

| API | Behaviour |
| --- | --- |
| `getBalance` | Read a player's balance for a currency |
| `deposit` | Add funds |
| `withdraw` | Remove funds; returns false on insufficient funds |
| `transfer` | Move funds between players (default implementation) |

Change events fire around successful operations. A provider implements
`getBalance`, `deposit`, and `withdraw`; `transfer` and `forgetPlayer` have
default implementations.

## Reputation

Reputation is an integer standing keyed by (player UUID, faction id). The scale
and clamping are the provider's choice.

| API | Behaviour |
| --- | --- |
| `getReputation` | Read standing with a faction |
| `adjust` | Change standing by +/- |
| `setReputation` | Set standing directly |

Change events are subscribable.

## Privacy

Both APIs key on player UUID. Providers expose a `forgetPlayer(UUID)` erasure
hook, driven by Core's shared erasure. Core does not store balances or
reputation itself. See [Privacy and Data](Privacy-and-Data.md).

## For developers

The full economy and reputation API is documented in
[../docs/ECONOMY-REPUTATION.md](../docs/ECONOMY-REPUTATION.md).

## See also

- [Progression Gates](Progression-Gates.md)
- [Privacy and Data](Privacy-and-Data.md)
- [Configuration](Configuration.md)
- [Home](Home.md)
