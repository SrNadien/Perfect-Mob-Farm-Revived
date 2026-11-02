package nadiendev.perfectmobfarm.block;

import nadiendev.perfectmobfarm.config.ServerConfigs;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

/**
 * Tiers of the mob farms. The declaration order is the progression order.
 * Some tiers are only available when another mod is loaded (see {@link #getRequiredMod()}).
 */
public enum FarmTier {
    IRON("iron", ServerConfigs.IRON_MOB_FARM_COOLDOWN, 0, null),
    GOLD("gold", ServerConfigs.GOLD_MOB_FARM_COOLDOWN, 0, null),
    DIAMOND("diamond", ServerConfigs.DIAMOND_MOB_FARM_COOLDOWN, 0, null),
    EMERALD("emerald", ServerConfigs.EMERALD_MOB_FARM_COOLDOWN, 1, null),
    NETHERITE("netherite", ServerConfigs.NETHERITE_MOB_FARM_COOLDOWN, 3, null),
    NETHER("nether", ServerConfigs.NETHER_MOB_FARM_COOLDOWN, 4, null),
    END("end", ServerConfigs.END_MOB_FARM_COOLDOWN, 5, null),
    ULTIMATE("ultimate", ServerConfigs.ULTIMATE_MOB_FARM_COOLDOWN, 6, null),
    MUSHASHITE("mushashite", ServerConfigs.MUSHASHITE_MOB_FARM_COOLDOWN, 7, "ultimatearmoury"),
    JOANFOITE("joanfoite", ServerConfigs.JOANFOITE_MOB_FARM_COOLDOWN, 8, "ultimatearmoury"),
    NADIENITE("nadienite", ServerConfigs.NADIENITE_MOB_FARM_COOLDOWN, 10, "ultimatearmoury");

    /**
     * System property set by the datagen runs, so every tier is generated even without the optional mods
     */
    public static final String DATAGEN_PROPERTY = "perfectmobfarmrevived.datagen";

    private final String name;
    private final ModConfigSpec.ConfigValue<Integer> cooldown;
    private final int lootingLevel;
    @Nullable
    private final String requiredMod;

    FarmTier(String name, ModConfigSpec.ConfigValue<Integer> cooldown, int lootingLevel, @Nullable String requiredMod) {
        this.name = name;
        this.cooldown = cooldown;
        this.lootingLevel = lootingLevel;
        this.requiredMod = requiredMod;
    }

    public String getName() {
        return name;
    }

    /**
     * @return Time needed to generate loot, in ticks. Read from the config at call time.
     */
    public int getCooldown() {
        return ServerConfigs.getSafe(cooldown);
    }

    /**
     * @return True if the farms of this tier (normal and energy variant) have a speed upgrade slot:
     * Iron, Gold, Diamond and Emerald
     */
    public boolean supportsSpeedUpgrade() {
        return ordinal() <= EMERALD.ordinal();
    }

    /**
     * @return Looting level applied to the generated loot
     */
    public int getLootingLevel() {
        return lootingLevel;
    }

    /**
     * @return Energy (FE) consumed per tick by the energy variant of this tier while it works:
     * round(base * multiplier ^ tier position), at least 1. Read from the config at call time.
     */
    public int getEnergyPerTick() {
        double base = ServerConfigs.getSafe(ServerConfigs.ENERGY_BASE_PER_TICK);
        double multiplier = ServerConfigs.getSafeDouble(ServerConfigs.ENERGY_MULTIPLIER_PER_TIER);
        long perTick = Math.round(base * Math.pow(multiplier, ordinal()));
        return (int) Math.max(1L, Math.min(perTick, Integer.MAX_VALUE));
    }

    /**
     * @return Size of the energy buffer (FE) of the energy variant of this tier:
     * energy per tick * buffer ticks, capped to the int range. Read from the config at call time.
     */
    public int getEnergyCapacity() {
        long capacity = (long) getEnergyPerTick() * ServerConfigs.getSafe(ServerConfigs.ENERGY_BUFFER_TICKS);
        return (int) Math.min(capacity, Integer.MAX_VALUE);
    }

    /**
     * @return Id of the mod that must be loaded for this tier to exist, or null if it has no requirement
     */
    @Nullable
    public String getRequiredMod() {
        return requiredMod;
    }

    /**
     * @return True if the farm of this tier is registered: no required mod, the required mod is loaded,
     * or a datagen run is in progress
     */
    public boolean isAvailable() {
        if (requiredMod == null || Boolean.getBoolean(DATAGEN_PROPERTY)) {
            return true;
        }
        ModList modList = ModList.get();
        return modList != null && modList.isLoaded(requiredMod);
    }
}
