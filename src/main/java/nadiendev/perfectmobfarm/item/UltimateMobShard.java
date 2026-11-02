package nadiendev.perfectmobfarm.item;

import nadiendev.perfectmobfarm.config.ServerConfigs;
import net.minecraft.world.item.ItemStack;

/**
 * Ultimate Mob Shard. Unbreakable by default, breakable (with its own durability) when the config says so.
 */
public class UltimateMobShard extends MobShard {
    public UltimateMobShard(Properties pProperties) {
        super(pProperties);
    }

    /**
     * The ultimate shard only needs one kill by default
     */
    @Override
    protected int getKillsNeeded() {
        return ServerConfigs.getSafe(ServerConfigs.ULTIMATE_MOB_SHARD_KILL_NEEDED);
    }

    @Override
    protected boolean isUnbreakableShard() {
        return ServerConfigs.getSafeBoolean(ServerConfigs.ULTIMATE_MOB_SHARD_UNBREAKABLE);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return ServerConfigs.getSafe(ServerConfigs.ULTIMATE_MOB_SHARD_DURABILITY);
    }
}
