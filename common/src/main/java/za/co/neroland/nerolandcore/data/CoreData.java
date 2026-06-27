package za.co.neroland.nerolandcore.data;

import za.co.neroland.nerolandcore.economy.CurrencyApi;
import za.co.neroland.nerolandcore.progression.ProgressionState;
import za.co.neroland.nerolandcore.reputation.ReputationApi;

/**
 * Registers Neroland Core's own systems with the shared {@link PlayerDataErasure}
 * hook, so a single erase request purges a player's progression gates, currency,
 * reputation and activity record together. Downstream mods register their own
 * erasers the same way. Called once from {@link za.co.neroland.nerolandcore.NerolandCoreCommon#init()}.
 */
@org.jetbrains.annotations.ApiStatus.Internal
public final class CoreData {

    private CoreData() {
    }

    public static void init() {
        PlayerDataErasure.register((server, uuid) -> ProgressionState.get(server).forgetPlayer(uuid));
        PlayerDataErasure.register((server, uuid) -> CurrencyApi.provider().forgetPlayer(uuid));
        PlayerDataErasure.register((server, uuid) -> ReputationApi.provider().forgetPlayer(uuid));
        PlayerDataErasure.register((server, uuid) -> PlayerActivity.get(server).forget(uuid));
    }
}
