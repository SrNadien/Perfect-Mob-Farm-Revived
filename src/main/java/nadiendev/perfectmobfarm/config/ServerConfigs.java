package nadiendev.perfectmobfarm.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfigs {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPECS;

    public static final ModConfigSpec.ConfigValue<Integer> IRON_MOB_FARM_COOLDOWN;
    public static final ModConfigSpec.ConfigValue<Integer> GOLD_MOB_FARM_COOLDOWN;
    public static final ModConfigSpec.ConfigValue<Integer> DIAMOND_MOB_FARM_COOLDOWN;
    public static final ModConfigSpec.ConfigValue<Integer> EMERALD_MOB_FARM_COOLDOWN;
    public static final ModConfigSpec.ConfigValue<Integer> NETHERITE_MOB_FARM_COOLDOWN;
    public static final ModConfigSpec.ConfigValue<Integer> NETHER_MOB_FARM_COOLDOWN;
    public static final ModConfigSpec.ConfigValue<Integer> END_MOB_FARM_COOLDOWN;
    public static final ModConfigSpec.ConfigValue<Integer> ULTIMATE_MOB_FARM_COOLDOWN;
    public static final ModConfigSpec.ConfigValue<Integer> MUSHASHITE_MOB_FARM_COOLDOWN;
    public static final ModConfigSpec.ConfigValue<Integer> JOANFOITE_MOB_FARM_COOLDOWN;
    public static final ModConfigSpec.ConfigValue<Integer> NADIENITE_MOB_FARM_COOLDOWN;
    public static final ModConfigSpec.ConfigValue<Integer> MOB_SHARD_KILL_NEEDED;
    public static final ModConfigSpec.ConfigValue<Integer> MOB_SHARD_DURABILITY;
    public static final ModConfigSpec.ConfigValue<Boolean> MOB_SHARD_UNBREAKABLE;
    public static final ModConfigSpec.ConfigValue<Integer> ADVANCED_MOB_SHARD_DURABILITY;
    public static final ModConfigSpec.ConfigValue<Integer> ULTIMATE_MOB_SHARD_DURABILITY;
    public static final ModConfigSpec.ConfigValue<Boolean> ULTIMATE_MOB_SHARD_UNBREAKABLE;
    public static final ModConfigSpec.ConfigValue<Integer> ULTIMATE_MOB_SHARD_KILL_NEEDED;
    public static final ModConfigSpec.ConfigValue<Integer> ENERGY_BASE_PER_TICK;
    public static final ModConfigSpec.ConfigValue<Double> ENERGY_MULTIPLIER_PER_TIER;
    public static final ModConfigSpec.ConfigValue<Integer> ENERGY_BUFFER_TICKS;
    public static final ModConfigSpec.ConfigValue<Integer> SPEED_UPGRADE_REDUCTION_PERCENT;

    static {
        BUILDER.push("Perfect Mob farm Configs");

        IRON_MOB_FARM_COOLDOWN = BUILDER.comment("Time needed by an Iron Mob farm to generate loot (in ticks).").define("Iron Cooldown", 800);
        GOLD_MOB_FARM_COOLDOWN = BUILDER.comment("Time needed by a Gold Mob farm to generate loot (in ticks).").define("Gold Cooldown", 400);
        DIAMOND_MOB_FARM_COOLDOWN = BUILDER.comment("Time needed by a Diamond Mob farm to generate loot (in ticks).").define("Diamond Cooldown", 200);
        EMERALD_MOB_FARM_COOLDOWN = BUILDER.comment("Time needed by an Emerald Mob farm to generate loot (in ticks).").define("Emerald Cooldown", 100);
        NETHERITE_MOB_FARM_COOLDOWN = BUILDER.comment("Time needed by a Netherite Mob farm to generate loot (in ticks).").define("Netherite Cooldown", 40);
        NETHER_MOB_FARM_COOLDOWN = BUILDER.comment("Time needed by a Nether Mob farm to generate loot (in ticks).").defineInRange("Nether Cooldown", 30, 1, Integer.MAX_VALUE);
        END_MOB_FARM_COOLDOWN = BUILDER.comment("Time needed by an End Mob farm to generate loot (in ticks).").defineInRange("End Cooldown", 20, 1, Integer.MAX_VALUE);
        ULTIMATE_MOB_FARM_COOLDOWN = BUILDER.comment("Time needed by an Ultimate Mob farm to generate loot (in ticks).").defineInRange("Ultimate Cooldown", 10, 1, Integer.MAX_VALUE);
        MUSHASHITE_MOB_FARM_COOLDOWN = BUILDER.comment("Time needed by a Mushashite Mob farm to generate loot (in ticks). Only used when Ultimate Armoury is installed.").defineInRange("Mushashite Cooldown", 6, 1, Integer.MAX_VALUE);
        JOANFOITE_MOB_FARM_COOLDOWN = BUILDER.comment("Time needed by a Joanfoite Mob farm to generate loot (in ticks). Only used when Ultimate Armoury is installed.").defineInRange("Joanfoite Cooldown", 4, 1, Integer.MAX_VALUE);
        NADIENITE_MOB_FARM_COOLDOWN = BUILDER.comment("Time needed by a Nadienite Mob farm to generate loot (in ticks). Only used when Ultimate Armoury is installed.").defineInRange("Nadienite Cooldown", 1, 1, Integer.MAX_VALUE);
        MOB_SHARD_KILL_NEEDED = BUILDER.comment("Number of kills needed to complete a mob shard.").define("Kill Needed", 10);
        MOB_SHARD_DURABILITY = BUILDER.comment("Durability of a normal mob shard.").define("Normal mob shard durability", 256);
        MOB_SHARD_UNBREAKABLE = BUILDER.comment("If true, normal mob shards are never damaged by the mob farms.").define("Normal mob shard unbreakable", false);
        ADVANCED_MOB_SHARD_DURABILITY = BUILDER.comment("UNUSED: advanced mob shards are always unbreakable. Kept so existing configs stay valid.").define("Advanced mob shard durability", 1024);
        ULTIMATE_MOB_SHARD_DURABILITY = BUILDER.comment("Durability of an ultimate mob shard. Only used when 'Ultimate mob shard unbreakable' is false.").define("Ultimate mob shard durability", 4096);
        ULTIMATE_MOB_SHARD_KILL_NEEDED = BUILDER.comment("Number of kills needed to complete an ultimate mob shard. 0 means that linking it to a mob (right click) is enough.").defineInRange("Ultimate mob shard kill needed", 0, 0, Integer.MAX_VALUE);
        ULTIMATE_MOB_SHARD_UNBREAKABLE = BUILDER.comment("If true, ultimate mob shards are never damaged by the mob farms. If false they use 'Ultimate mob shard durability'.").define("Ultimate mob shard unbreakable", true);
        ENERGY_BASE_PER_TICK = BUILDER.comment("Energy (FE) per tick used by an Iron Energy Mob farm while it works. Higher tiers use this value times 'Energy multiplier per tier' to the power of the tier position.").defineInRange("Energy base per tick", 10, 1, 1_000_000);
        ENERGY_MULTIPLIER_PER_TIER = BUILDER.comment("Energy consumption multiplier applied once per tier above Iron (energy per tick = base * multiplier ^ tier position, at least 1).").defineInRange("Energy multiplier per tier", 2.0D, 0.0D, 100.0D);
        ENERGY_BUFFER_TICKS = BUILDER.comment("Size of the energy buffer of the Energy Mob farms, in ticks of work (capacity = energy per tick * this value).").defineInRange("Energy buffer ticks", 1200, 1, 1_000_000);
        SPEED_UPGRADE_REDUCTION_PERCENT = BUILDER.comment("Percent of the cooldown removed by each Speed Upgrade installed in a farm (up to 4 per farm). Energy use per tick is not changed.").defineInRange("Speed upgrade reduction percent", 20, 1, 25);

        BUILDER.pop();
        SPECS = BUILDER.build();
    }

    /**
     * Read a config value, falling back to its default when the config is not loaded yet
     * (e.g. an item queried on the client before joining a world).
     *
     * @param value Config value
     * @return The configured value, or the default one if the config is not loaded
     */
    public static int getSafe(ModConfigSpec.ConfigValue<Integer> value) {
        try {
            return value.get();
        } catch (IllegalStateException | NullPointerException e) {
            return value.getDefault();
        }
    }

    /**
     * Double version of {@link #getSafe(ModConfigSpec.ConfigValue)}
     *
     * @param value Config value
     * @return The configured value, or the default one if the config is not loaded
     */
    public static double getSafeDouble(ModConfigSpec.ConfigValue<Double> value) {
        try {
            return value.get();
        } catch (IllegalStateException | NullPointerException e) {
            return value.getDefault();
        }
    }

    /**
     * Boolean version of {@link #getSafe(ModConfigSpec.ConfigValue)}
     *
     * @param value Config value
     * @return The configured value, or the default one if the config is not loaded
     */
    public static boolean getSafeBoolean(ModConfigSpec.ConfigValue<Boolean> value) {
        try {
            return value.get();
        } catch (IllegalStateException | NullPointerException e) {
            return value.getDefault();
        }
    }
}
