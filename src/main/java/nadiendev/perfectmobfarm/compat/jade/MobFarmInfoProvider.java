package nadiendev.perfectmobfarm.compat.jade;

import nadiendev.perfectmobfarm.PerfectMobFarm;
import nadiendev.perfectmobfarm.block.entity.MobFarmBlockEntity;
import nadiendev.perfectmobfarm.config.ServerConfigs;
import nadiendev.perfectmobfarm.item.DataComponentManager;
import nadiendev.perfectmobfarm.item.MobShard;
import nadiendev.perfectmobfarm.item.MobShardData;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;

/**
 * Jade provider for the mob farms: the server side collects the state of the farm,
 * the client side draws the captured mob, the kill count, the state and a progress bar.
 */
public enum MobFarmInfoProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = PerfectMobFarm.id("farm_info");

    // Server data keys
    private static final String KEY_HAS_MOB = PerfectMobFarm.MODID + ":has_mob";
    private static final String KEY_MOB_NAME = PerfectMobFarm.MODID + ":mob_name";
    private static final String KEY_EXTRA_SLOT = PerfectMobFarm.MODID + ":extra_slot";
    private static final String KEY_HAS_MOB_2 = PerfectMobFarm.MODID + ":has_mob_2";
    private static final String KEY_MOB_NAME_2 = PerfectMobFarm.MODID + ":mob_name_2";
    private static final String KEY_SPEED_UPGRADES = PerfectMobFarm.MODID + ":speed_upgrades";
    private static final String KEY_STATE =PerfectMobFarm.MODID + ":state";
    private static final String KEY_TIMER = PerfectMobFarm.MODID + ":timer";
    private static final String KEY_COOLDOWN = PerfectMobFarm.MODID + ":cooldown";

    // Values of KEY_STATE
    private static final int STATE_INACTIVE = 0;
    private static final int STATE_ACTIVE = 1;
    private static final int STATE_NO_CONTAINER = 2;
    private static final int STATE_REDSTONE_DISABLED = 3;
    private static final int STATE_NO_ENERGY = 4;

    private static final int PROGRESS_COLOR = 0xFF4CAF50;

    @Override
    public ResourceLocation getUid() {
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

    /**
     * Show the name of the mob captured by a shard (the kill count is only shown in the item tooltip of the shard)
     */
    private static void appendShard(ITooltip tooltip, CompoundTag data, String nameKey) {
        tooltip.add(Component.translatable("jade.perfectmobfarmrevived.mob", data.getString(nameKey)));
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!config.get(UID)) {
            return;
        }
        CompoundTag data = accessor.getServerData();
        if (!data.contains(KEY_STATE)) {
            return;
        }

        boolean extraSlot = data.getBoolean(KEY_EXTRA_SLOT);
        boolean hasMob = data.getBoolean(KEY_HAS_MOB);
        boolean hasMob2 = extraSlot && data.getBoolean(KEY_HAS_MOB_2);
        if (hasMob) {
            appendShard(tooltip, data, KEY_MOB_NAME);
        }
        if (hasMob2) {
            appendShard(tooltip, data, KEY_MOB_NAME_2);
        }
        if (!hasMob && !hasMob2) {
            tooltip.add(Component.translatable("jade.perfectmobfarmrevived.no_mob").withStyle(ChatFormatting.GRAY));
        }

        if (extraSlot) {
            tooltip.add(Component.translatableWithFallback("jade.perfectmobfarmrevived.mob_slot_upgrade",
                    "Mob Slot Upgrade installed"));
        }
        int speedUpgrades = data.getInt(KEY_SPEED_UPGRADES);
        if (speedUpgrades > 0) {
            tooltip.add(Component.translatableWithFallback("jade.perfectmobfarmrevived.speed_upgrades",
                    "Speed upgrades: %s", speedUpgrades));
        }

        int state = data.getInt(KEY_STATE);
        Component stateName = switch (state) {
            case STATE_ACTIVE -> Component.translatable("jade.perfectmobfarmrevived.state.active")
                    .withStyle(ChatFormatting.GREEN);
            case STATE_NO_CONTAINER -> Component.translatable("jade.perfectmobfarmrevived.state.no_container")
                    .withStyle(ChatFormatting.RED);
            case STATE_REDSTONE_DISABLED -> Component.translatableWithFallback(
                    "jade.perfectmobfarmrevived.state.disabled_redstone", "Disabled")
                    .withStyle(ChatFormatting.RED);
            case STATE_NO_ENERGY -> Component.translatableWithFallback(
                    "jade.perfectmobfarmrevived.state.no_energy", "No energy")
                    .withStyle(ChatFormatting.RED);
            default -> Component.translatable("jade.perfectmobfarmrevived.state.inactive")
                    .withStyle(ChatFormatting.GRAY);
        };
        tooltip.add(Component.translatable("jade.perfectmobfarmrevived.state", stateName));

        int cooldown = data.getInt(KEY_COOLDOWN);
        if (state == STATE_ACTIVE && cooldown > 0) {
            float progress = Math.min(1f, Math.max(0f, (float) data.getInt(KEY_TIMER) / cooldown));
            IElementHelper helper = IElementHelper.get();
            tooltip.add(helper.progress(progress,
                    Component.literal(Math.round(progress * 100) + "%"),
                    helper.progressStyle().color(PROGRESS_COLOR),
                    BoxStyle.getNestedBox(),
                    true));
        }

        // The energy bar of the energy farms is drawn by Jade itself, from the energy storage capability of the farm
    }
}
