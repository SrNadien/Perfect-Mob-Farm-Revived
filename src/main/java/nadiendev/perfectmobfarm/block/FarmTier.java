package nadiendev.perfectmobfarm.block;

import nadiendev.perfectmobfarm.config.ServerConfigs;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Tiers of the mob farms. The declaration order is the progression order.
 */
public enum FarmTier {
    IRON("iron", ServerConfigs.IRON_MOB_FARM_COOLDOWN, 0),
    GOLD("gold", ServerConfigs.GOLD_MOB_FARM_COOLDOWN, 0),
    DIAMOND("diamond", ServerConfigs.DIAMOND_MOB_FARM_COOLDOWN, 0),
    EMERALD("emerald", ServerConfigs.EMERALD_MOB_FARM_COOLDOWN, 1),
    NETHERITE("netherite", ServerConfigs.NETHERITE_MOB_FARM_COOLDOWN, 3),
    NETHER("nether", ServerConfigs.NETHER_MOB_FARM_COOLDOWN, 4),
    END("end", ServerConfigs.END_MOB_FARM_COOLDOWN, 5),
    ULTIMATE("ultimate", ServerConfigs.ULTIMATE_MOB_FARM_COOLDOWN, 6);

    private final String name;
    private final ModConfigSpec.ConfigValue<Integer> cooldown;
    private final int lootingLevel;

    FarmTier(String name, ModConfigSpec.ConfigValue<Integer> cooldown, int lootingLevel) {
        this.name = name;
        this.cooldown = cooldown;
        this.lootingLevel = lootingLevel;
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
}
