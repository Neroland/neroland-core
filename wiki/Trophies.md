# Trophies

Since **Core 1.20.0**, trophies are a shared, data-driven registry in Core. Any mod or datapack can
define trophies by shipping JSON; a trophy mod (NeroTrophies) evaluates them, keeps the record and shows
them. Core itself **evaluates nothing and stores nothing**: without a trophy mod installed, the
definitions load, `TrophyApi.award` returns `false` and no player data exists.

## Where the files go

| What | Path | Id |
| ---- | ---- | -- |
| A trophy | `data/<namespace>/neroland_trophies/<path>.json` | `<namespace>:<path>` |
| A collection | `data/<namespace>/neroland_trophy_collections/<path>.json` | `<namespace>:<path>` |

`<path>` may contain `/`, for example `mypack:space/first_launch`. A collection may not share its id
with a trophy.

## A trophy

```json
{
  "schema": 1,
  "icon": "nerospace:rocket_tier_1",
  "tier": "gold",
  "trigger": { "type": "advancement", "advancement": "nerospace:worlds/mars/arrive" },
  "hidden": false,
  "title": "trophy.mypack.mars.title",
  "description": "trophy.mypack.mars.desc",
  "requires_mod": "nerospace",
  "category": "nerospace",
  "order": 10,
  "reward": { "coins": 50 }
}
```

| Key | Required | Meaning |
| --- | -------- | ------- |
| `icon` | yes | An item id. If the item is not registered the trophy is skipped quietly. |
| `tier` | yes | `bronze`, `silver`, `gold` or `nero`. |
| `trigger` | no | What earns it (below). Without a trigger, the trophy is earned only when a mod calls `TrophyApi.award`, or as a collection's completion trophy. |
| `hidden` | no | `true` hides the trophy until earned. Default `false`. |
| `title`, `description` | no | **Lang keys**, not text (`[a-z0-9_.-]`, up to 128 characters). Default `trophy.<namespace>.<path>.title` / `.desc`, with `/` in the path turned into `.`. |
| `requires_mod` | no | A mod id, or a list of up to 8. If any is not loaded the trophy is skipped quietly. |
| `category` | no | The Trophy Book tab. Default: the first `requires_mod`, else the namespace. |
| `order` | no | Sort order within the category (then by id). Default `0`. |
| `reward` | no | `{ "coins": n }`, 0 to 10 000. Overrides the trophy mod's tier default; paid only when a currency provider is installed and the trophy mod's rewards are on. |
| `schema` | no | Default `1`. Unknown keys are ignored, so files written for later versions still load. |

## Triggers

Target ids are matched as text and never checked against a registry, so a trigger naming a mod that
is not installed loads fine and simply never fires.

| `type` | Fields | Earned when |
| ------ | ------ | ----------- |
| `advancement` | `advancement` | The advancement is completed. Don't use recipe advancements (`<ns>:recipes/...`): they fire on ingredient pickup. |
| `nero_event` | `event` + fields below | A Core event for that player. |
| `stat` | `stat_type` (default `minecraft:custom`), `stat`, `at_least` | A vanilla statistic reaches the value, e.g. `minecraft:killed` + an entity id. |
| `item_obtained` | `item`, `count` (default 1) | The player picked up or crafted that many, or holds them. A gift counts. |
| `block_placed` | `block`, `count` (default 1) | The player placed that many by hand (machines and dispensers don't count). |
| `dimension_visited` | `dimension` | The player is in the dimension. Past visits are not counted; use the world's advancement for that. |

`nero_event` kinds:

| `event` | Fields | Earned when |
| ------- | ------ | ----------- |
| `gate` | `gate` | A **player-scope** progression gate opens for the player. Team and server gates never award. |
| `material_milestone` | `milestone`, optional `material` | A **player-scope** material milestone is observed. |
| `reputation` | `faction`, `at_least` (any whole number) | The player's standing rises to the value (needs a reputation provider). |
| `currency` | `currency` (default `nerolandcore:credits`), `at_least` (1 or more) | The balance rises to the value, by any deposit (needs a currency provider). |

`"event": "threshold"` is reserved and rejected: threshold crossings name no player.

Numbers must be whole numbers in range: `at_least` and `count` are 1 to 2 147 483 647.

## A collection

```json
{
  "schema": 1,
  "icon": "nerospace:mars_regolith",
  "title": "collection.mypack.astronaut.title",
  "description": "collection.mypack.astronaut.desc",
  "trim": "orbital_plasma",
  "members": ["mypack:luna", "mypack:mars"],
  "trophy": "mypack:astronaut",
  "requires_mod": "nerospace"
}
```

Completing every member unlocks the cabinet `trim` (`[a-z0-9_]`, up to 32 characters; the trophy mod
knows the trim table) and, if `trophy` names a loaded trophy, awards it. Members (up to 256) skipped
because their mod is absent or their icon is missing are dropped quietly; a collection left with fewer
than two members is not loaded.

## What gets skipped, and what gets reported

Nothing in a trophy file can crash the game. Per file, in id order:

1. Over a bound: a file larger than 16 KiB, more than 4 096 trophies or 512 collections, more than
   8 192 files in one folder, or a collection with more than 256 members: rejected and reported.
2. Not valid JSON, or failing the schema: rejected and reported (`malformed`).
3. A `requires_mod` not loaded: skipped quietly.
4. Icon item not registered: skipped quietly.
5. Collections: a member that never had a loadable file is rejected (`unknown_member`); an id shared
   with a trophy is rejected (`id_clash`).

Core writes one summary line per load: debug when clean, a warning with counts only when files were
rejected. It never logs file contents. `/reload` picks up changes.

## For mod developers

- Award a trophy from any mod with only a Core dependency (1.20.0 or later):
  `TrophyApi.award(serverPlayer, Identifier.fromNamespaceAndPath("mymod", "first_boss"))`. It returns
  `false` and does nothing when no trophy mod is installed or the id is not loaded. Server thread only.
- Listen with `TrophyEvents.onAwarded(...)`, `onRevoked`, `onCollectionCompleted`, `onRecordErased`.
- A trophy mod becomes the store with `TrophyApi.setProvider(provider)`.

See [For Developers](For-Developers.md) and the repository's `USING-CORE.md` for the full contract.

## Privacy

Core holds no trophy data. A trophy mod keeps the record and covers it in its own privacy notice;
it must handle erasure through Core's [player-data erasure](Privacy-and-Data.md).

## See also

- [Progression Gates](Progression-Gates.md)
- [Economy & Reputation](Economy-and-Reputation.md)
- [Privacy & Data](Privacy-and-Data.md)
