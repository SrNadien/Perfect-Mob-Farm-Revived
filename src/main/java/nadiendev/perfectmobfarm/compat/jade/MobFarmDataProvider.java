package nadiendev.perfectmobfarm.compat.jade;

import nadiendev.perfectmobfarm.PerfectMobFarm;
import nadiendev.perfectmobfarm.block.entity.MobFarmBlockEntity;
import nadiendev.perfectmobfarm.item.DataComponentManager;
import nadiendev.perfectmobfarm.item.MobShard;
import nadiendev.perfectmobfarm.item.MobShardData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

/**
 * Server side of the Jade integration for the mob farms: collects the state of the farm. Since Minecraft 1.21.6 Jade
 * does not allow a data provider to also be a component provider, the tooltip is in {@link MobFarmComponentProvider}.
 */
public enum MobFarmDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final Identifier UID = PerfectMobFarm.id("farm_info_data");

    // Server data keys, read by MobFarmComponentProvider
    static final String KEY_HAS_MOB = PerfectMobFarm.MODID + ":has_mob";
    static final String KEY_MOB_NAME = PerfectMobFarm.MODID + ":mob_name";
    static final String KEY_EXTRA_SLOT = PerfectMobFarm.MODID + ":extra_slot";
    static final String KEY_HAS_MOB_2 = PerfectMobFarm.MODID + ":has_mob_2";
    static final String KEY_MOB_NAME_2 = PerfectMobFarm.MODID + ":mob_name_2";
    static final String KEY_SPEED_UPGRADES = PerfectMobFarm.MODID + ":speed_upgrades";
    static final String KEY_STATE = PerfectMobFarm.MODID + ":state";
    static final String KEY_TIMER = PerfectMobFarm.MODID + ":timer";
    static final String KEY_COOLDOWN = PerfectMobFarm.MODID + ":cooldown";

    // Values of KEY_STATE
    static final int STATE_INACTIVE = 0;
    static final int STATE_ACTIVE = 1;
    static final int STATE_NO_CONTAINER = 2;
    static final int STATE_REDSTONE_DISABLED = 3;
    static final int STATE_NO_ENERGY = 4;

    @Override
    public Identifier getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof MobFarmBlockEntity farm)) {
            return;
        }

        putShard(data, farm.getItemHandler().getStackInSlot(0), KEY_HAS_MOB, KEY_MOB_NAME);
        if (farm.hasExtraMobSlot()) {
            data.putBoolean(KEY_EXTRA_SLOT, true);
            putShard(data, farm.getItemHandler().getStackInSlot(1),
                    KEY_HAS_MOB_2, KEY_MOB_NAME_2);
        }
        if (farm.getSpeedUpgrades() > 0) {
            data.putInt(KEY_SPEED_UPGRADES, farm.getSpeedUpgrades());
        }

        // A farm that is not working (no complete shard) stays "inactive" whatever the lever or the energy say
        int state = STATE_INACTIVE;
        if (farm.isActive()) {
            if (farm.isRedstoneDisabled()) {
                state = STATE_REDSTONE_DISABLED;
            } else if (farm.getNearbyContainer() == null) {
                state = STATE_NO_CONTAINER;
            } else if (farm.isEnergyStarved()) {
                state = STATE_NO_ENERGY;
            } else {
                state = STATE_ACTIVE;
            }
        }
        data.putInt(KEY_STATE, state);
        data.putInt(KEY_TIMER, farm.getTimer());
        data.putInt(KEY_COOLDOWN, farm.getCooldown());
    }

    /**
     * Write the mob captured by a shard (nothing when the stack is not a shard with data)
     */
    private static void putShard(CompoundTag data, ItemStack shard, String hasKey, String nameKey) {
        MobShardData shardData = shard.getItem() instanceof MobShard
                ? shard.get(DataComponentManager.MOB_SHARD_DATA.get()) : null;
        if (shardData != null) {
            data.putBoolean(hasKey, true);
            data.putString(nameKey, shardData.mobName());
        }
    }
}
