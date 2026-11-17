package za.co.neroland.nerolandcore.demo;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** A configurable fake {@link DemoModule} for the pure-logic demo tests. */
final class DemoTestSupport {

    private DemoTestSupport() {
    }

    static final class FakeModule implements DemoModule {
        final String id;
        final int order;
        DemoFootprint footprint;
        List<String> required;
        int footprintCalls;

        FakeModule(String id, int order, DemoFootprint footprint, List<String> required) {
            this.id = id;
            this.order = order;
            this.footprint = footprint;
            this.required = required;
        }

        FakeModule(String id, int order) {
            this(id, order, DemoFootprint.of(10, 10), List.of());
        }

        @Override
        public String modId() {
            return this.id;
        }

        @Override
        public Component displayName() {
            return Component.literal(this.id);
        }

        @Override
        public Identifier icon() {
            throw new AssertionError("icon() is client-only and must not be called by the registry");
        }

        @Override
        public int sortOrder() {
            return this.order;
        }

        @Override
        public List<String> requiredMods() {
            return this.required;
        }

        @Override
        public DemoFootprint footprint() {
            this.footprintCalls++;
            return this.footprint;
        }

        @Override
        public void build(DemoBuildContext context) {
        }
    }
}
