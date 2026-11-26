package za.co.neroland.nerolandcore.map;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

//? if <1.20.3 {
/*import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
*///?}
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/** Shared fixtures for the map API tests: dimensions, ids and a configurable fake provider. */
final class MapTestSupport {

    //? if <1.20.3 {
    /*static {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }
    *///?}

    static final ResourceKey<Level> OVERWORLD = dim("overworld_test");
    static final ResourceKey<Level> MOON = dim("moon_test");

    private MapTestSupport() {
    }

    static ResourceKey<Level> dim(String path) {
        return ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath("maptest", path));
    }

    static Identifier id(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    static MapViewer online(UUID id, ResourceKey<Level> dimension, int x, int z) {
        return MapViewer.forTest(id, null, dimension, new BlockPos(x, 64, z));
    }

    /** What the fake provider does on a query. */
    @FunctionalInterface
    interface QueryFn {
        void query(MapViewer viewer, ResourceKey<Level> dimension, MapBounds bounds, MapLayerSink sink);
    }

    /** A provider whose layers and behaviour are given by the test. */
    static final class FakeProvider implements MapLayerProvider {
        final String modId;
        final List<MapLayerInfo> layers;
        final QueryFn fn;
        Function<MapViewer, List<MapDimensionBadge>> badges = v -> List.of();
        int layersCalls;

        FakeProvider(String modId, List<MapLayerInfo> layers, QueryFn fn) {
            this.modId = modId;
            this.layers = new ArrayList<>(layers);
            this.fn = fn;
        }

        @Override
        public String modId() {
            return modId;
        }

        @Override
        public List<MapLayerInfo> layers() {
            layersCalls++;
            return layers;
        }

        @Override
        public void query(MapViewer viewer, ResourceKey<Level> dimension, MapBounds bounds, MapLayerSink sink) {
            fn.query(viewer, dimension, bounds, sink);
        }

        @Override
        public List<MapDimensionBadge> dimensionBadges(MapViewer viewer) {
            return badges.apply(viewer);
        }
    }

    /** One record in a fake mod's store: owned (owner non-null) or unowned. */
    record Entry(String id, UUID owner, ResourceKey<Level> dimension, int x, int z, String name) {
    }
}
