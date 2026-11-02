package nadiendev.perfectmobfarm.item;

import nadiendev.perfectmobfarm.config.ServerConfigs;
import net.minecraft.world.item.ItemStack;

/**
 * Advanced Mob Shard. Always unbreakable.
 */
public class AdvancedMobShard extends MobShard {
    public AdvancedMobShard(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected boolean isUnbreakableShard() {
        return true;
    }

    /**
     * Still reports the (unused) configured durability
     */
    @Override
    public int getMaxDamage(ItemStack stack) {
        return ServerConfigs.getSafe(ServerConfigs.ADVANCED_MOB_SHARD_DURABILITY);
    }
}
