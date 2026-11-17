package za.co.neroland.nerolandcore.trophy;

import java.util.Objects;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;

/**
 * What earns a trophy. One flat JSON object dispatched on {@code "type"}; {@code nero_event} adds a
 * second key, {@code "event"}.
 *
 * <p>Core holds the codecs only and <b>evaluates nothing</b>: the registered trophy provider decides
 * when a trigger is met. Every target id (advancement, gate, milestone, faction, currency, stat, item,
 * block, dimension) is matched as text at runtime and never checked against a registry, so a trigger
 * naming an absent mod's content decodes fine and simply never fires.
 *
 * <p><b>Compatibility.</b> New permitted trigger types (and new {@code nero_event} kinds) may be added in
 * a minor Core version. A consumer that switches over them must keep a {@code default} branch that
 * treats an unknown trigger as "never met", rather than an exhaustive switch.
 *
 * @since 1.20.0
 */
public sealed interface TrophyTrigger permits TrophyTrigger.Advancement, TrophyTrigger.NeroEvent,
        TrophyTrigger.Stat, TrophyTrigger.ItemObtained, TrophyTrigger.BlockPlaced,
        TrophyTrigger.DimensionVisited {

    /** The {@code "type"} discriminator. */
    enum Type implements StringRepresentable {
        ADVANCEMENT("advancement"),
        NERO_EVENT("nero_event"),
        STAT("stat"),
        ITEM_OBTAINED("item_obtained"),
        BLOCK_PLACED("block_placed"),
        DIMENSION_VISITED("dimension_visited");

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

        private final String serializedName;

        Type(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }

    Codec<TrophyTrigger> CODEC = Type.CODEC.dispatch("type", TrophyTrigger::type, TrophyTrigger::mapCodecFor);

    Type type();

    private static MapCodec<? extends TrophyTrigger> mapCodecFor(Type type) {
        return switch (type) {
            case ADVANCEMENT -> Advancement.MAP_CODEC;
            case NERO_EVENT -> NeroEvent.MAP_CODEC;
            case STAT -> Stat.MAP_CODEC;
            case ITEM_OBTAINED -> ItemObtained.MAP_CODEC;
            case BLOCK_PLACED -> BlockPlaced.MAP_CODEC;
            case DIMENSION_VISITED -> DimensionVisited.MAP_CODEC;
        };
    }

    /** {@code {"type": "advancement", "advancement": "<id>"}} — the advancement is completed. */
    record Advancement(Identifier advancement) implements TrophyTrigger {
        public static final MapCodec<Advancement> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                TrophyCodecs.ID.fieldOf("advancement").forGetter(Advancement::advancement))
                .apply(instance, Advancement::new));

        public Advancement {
            Objects.requireNonNull(advancement, "advancement");
        }

        @Override
        public Type type() {
            return Type.ADVANCEMENT;
        }
    }

    /**
     * {@code {"type": "nero_event", "event": "<kind>", ...}} — a Core bus event.
     *
     * <ul>
     *   <li>{@code gate}: {@code "gate"} — a PLAYER-scope progression gate opened.</li>
     *   <li>{@code material_milestone}: {@code "milestone"}, optional {@code "material"} — a PLAYER-scope
     *       material milestone observed.</li>
     *   <li>{@code reputation}: {@code "faction"}, {@code "at_least"} (any int) — standing rose to the value.</li>
     *   <li>{@code currency}: optional {@code "currency"} (default {@code nerolandcore:credits}),
     *       {@code "at_least"} (1 or more) — balance rose to the value.</li>
     * </ul>
     *
     * <p>{@code target} is the gate, milestone, faction or currency id depending on {@code event};
     * {@code material} is used by {@code material_milestone} only and {@code atLeast} by
     * {@code reputation} and {@code currency} only (0 otherwise). {@code "threshold"} is reserved and
     * rejected: a {@code ThresholdEvents} crossing names no player.
     */
    record NeroEvent(Kind event, Identifier target, Optional<Identifier> material, int atLeast)
            implements TrophyTrigger {

        /** The default currency of a {@code currency} event. */
        public static final Identifier DEFAULT_CURRENCY = Identifier.fromNamespaceAndPath("nerolandcore", "credits");

        /** The {@code "event"} discriminator. */
        public enum Kind implements StringRepresentable {
            GATE("gate"),
            MATERIAL_MILESTONE("material_milestone"),
            REPUTATION("reputation"),
            CURRENCY("currency");

            public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

            private final String serializedName;

            Kind(String serializedName) {
                this.serializedName = serializedName;
            }

            @Override
            public String getSerializedName() {
                return serializedName;
            }
        }

        private static final MapCodec<NeroEvent> GATE_CODEC = TrophyCodecs.ID.fieldOf("gate")
                .xmap(NeroEvent::ofGate, NeroEvent::target);

        private static final MapCodec<NeroEvent> MILESTONE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                TrophyCodecs.ID.fieldOf("milestone").forGetter(NeroEvent::target),
                TrophyCodecs.ID.optionalFieldOf("material").forGetter(NeroEvent::material))
                .apply(instance, NeroEvent::ofMaterialMilestone));

        private static final MapCodec<NeroEvent> REPUTATION_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                TrophyCodecs.ID.fieldOf("faction").forGetter(NeroEvent::target),
                TrophyCodecs.ANY_INT.fieldOf("at_least").forGetter(NeroEvent::atLeast))
                .apply(instance, NeroEvent::ofReputation));

        private static final MapCodec<NeroEvent> CURRENCY_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                TrophyCodecs.ID.optionalFieldOf("currency", DEFAULT_CURRENCY).forGetter(NeroEvent::target),
                TrophyCodecs.POSITIVE.fieldOf("at_least").forGetter(NeroEvent::atLeast))
                .apply(instance, NeroEvent::ofCurrency));

        public static final MapCodec<NeroEvent> MAP_CODEC =
                Kind.CODEC.dispatchMap("event", NeroEvent::event, NeroEvent::codecFor);

        public NeroEvent {
            Objects.requireNonNull(event, "event");
            Objects.requireNonNull(target, "target");
            Objects.requireNonNull(material, "material");
        }

        public static NeroEvent ofGate(Identifier gate) {
            return new NeroEvent(Kind.GATE, gate, Optional.empty(), 0);
        }

        public static NeroEvent ofMaterialMilestone(Identifier milestone, Optional<Identifier> material) {
            return new NeroEvent(Kind.MATERIAL_MILESTONE, milestone, material, 0);
        }

        public static NeroEvent ofReputation(Identifier faction, int atLeast) {
            return new NeroEvent(Kind.REPUTATION, faction, Optional.empty(), atLeast);
        }

        public static NeroEvent ofCurrency(Identifier currency, int atLeast) {
            return new NeroEvent(Kind.CURRENCY, currency, Optional.empty(), atLeast);
        }

        /** The gate id, if this is a {@code gate} event. */
        public Optional<Identifier> gate() {
            return event == Kind.GATE ? Optional.of(target) : Optional.empty();
        }

        /** The milestone id, if this is a {@code material_milestone} event. */
        public Optional<Identifier> milestone() {
            return event == Kind.MATERIAL_MILESTONE ? Optional.of(target) : Optional.empty();
        }

        /** The faction id, if this is a {@code reputation} event. */
        public Optional<Identifier> faction() {
            return event == Kind.REPUTATION ? Optional.of(target) : Optional.empty();
        }

        /** The currency id, if this is a {@code currency} event. */
        public Optional<Identifier> currency() {
            return event == Kind.CURRENCY ? Optional.of(target) : Optional.empty();
        }

        private static MapCodec<NeroEvent> codecFor(Kind kind) {
            return switch (kind) {
                case GATE -> GATE_CODEC;
                case MATERIAL_MILESTONE -> MILESTONE_CODEC;
                case REPUTATION -> REPUTATION_CODEC;
                case CURRENCY -> CURRENCY_CODEC;
            };
        }

        @Override
        public Type type() {
            return Type.NERO_EVENT;
        }
    }

    /**
     * {@code {"type": "stat", "stat_type": "<id>", "stat": "<id>", "at_least": n}} — a vanilla statistic
     * reached {@code at_least}. {@code stat_type} defaults to {@code minecraft:custom}.
     */
    record Stat(Identifier statType, Identifier stat, int atLeast) implements TrophyTrigger {
        public static final Identifier DEFAULT_STAT_TYPE = Identifier.withDefaultNamespace("custom");

        public static final MapCodec<Stat> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                TrophyCodecs.ID.optionalFieldOf("stat_type", DEFAULT_STAT_TYPE).forGetter(Stat::statType),
                TrophyCodecs.ID.fieldOf("stat").forGetter(Stat::stat),
                TrophyCodecs.POSITIVE.fieldOf("at_least").forGetter(Stat::atLeast))
                .apply(instance, Stat::new));

        public Stat {
            Objects.requireNonNull(statType, "statType");
            Objects.requireNonNull(stat, "stat");
        }

        @Override
        public Type type() {
            return Type.STAT;
        }
    }

    /** {@code {"type": "item_obtained", "item": "<id>", "count": n}} — {@code count} defaults to 1. */
    record ItemObtained(Identifier item, int count) implements TrophyTrigger {
        public static final MapCodec<ItemObtained> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                TrophyCodecs.ID.fieldOf("item").forGetter(ItemObtained::item),
                TrophyCodecs.POSITIVE.optionalFieldOf("count", 1).forGetter(ItemObtained::count))
                .apply(instance, ItemObtained::new));

        public ItemObtained {
            Objects.requireNonNull(item, "item");
        }

        @Override
        public Type type() {
            return Type.ITEM_OBTAINED;
        }
    }

    /** {@code {"type": "block_placed", "block": "<id>", "count": n}} — {@code count} defaults to 1. */
    record BlockPlaced(Identifier block, int count) implements TrophyTrigger {
        public static final MapCodec<BlockPlaced> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                TrophyCodecs.ID.fieldOf("block").forGetter(BlockPlaced::block),
                TrophyCodecs.POSITIVE.optionalFieldOf("count", 1).forGetter(BlockPlaced::count))
                .apply(instance, BlockPlaced::new));

        public BlockPlaced {
            Objects.requireNonNull(block, "block");
        }

        @Override
        public Type type() {
            return Type.BLOCK_PLACED;
        }
    }

    /** {@code {"type": "dimension_visited", "dimension": "<id>"}} — the player is in the dimension. */
    record DimensionVisited(Identifier dimension) implements TrophyTrigger {
        public static final MapCodec<DimensionVisited> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                TrophyCodecs.ID.fieldOf("dimension").forGetter(DimensionVisited::dimension))
                .apply(instance, DimensionVisited::new));

        public DimensionVisited {
            Objects.requireNonNull(dimension, "dimension");
        }

        @Override
        public Type type() {
            return Type.DIMENSION_VISITED;
        }
    }
}
