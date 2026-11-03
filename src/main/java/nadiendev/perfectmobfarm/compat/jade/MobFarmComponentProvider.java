package nadiendev.perfectmobfarm.compat.jade;

import nadiendev.perfectmobfarm.PerfectMobFarm;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.view.ProgressView;

import static nadiendev.perfectmobfarm.compat.jade.MobFarmDataProvider.*;

/**
 * Client side of the Jade integration for the mob farms: draws the captured mob, the state of the farm, the upgrades
 * and a progress bar from the data collected by {@link MobFarmDataProvider}.
 */
public enum MobFarmComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final Identifier UID = PerfectMobFarm.id("farm_info");

    private static final int PROGRESS_COLOR = 0xFF4CAF50;

    @Override
    public Identifier getUid() {
        return UID;
    }

    /**
     * Show the name of the mob captured by a shard (the kill count is only shown in the item tooltip of the shard)
     */
    private static void appendShard(ITooltip tooltip, CompoundTag data, String nameKey) {
        tooltip.add(Component.translatable("jade.perfectmobfarmrevived.mob", data.getStringOr(nameKey, "")));
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

        boolean extraSlot = data.getBooleanOr(KEY_EXTRA_SLOT, false);
        boolean hasMob = data.getBooleanOr(KEY_HAS_MOB, false);
        boolean hasMob2 = extraSlot && data.getBooleanOr(KEY_HAS_MOB_2, false);
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
        int speedUpgrades = data.getIntOr(KEY_SPEED_UPGRADES, 0);
        if (speedUpgrades > 0) {
            tooltip.add(Component.translatableWithFallback("jade.perfectmobfarmrevived.speed_upgrades",
                    "Speed upgrades: %s", speedUpgrades));
        }

        int state = data.getIntOr(KEY_STATE, 0);
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

        int cooldown = data.getIntOr(KEY_COOLDOWN, 0);
        if (state == STATE_ACTIVE && cooldown > 0) {
            float progress = Math.min(1f, Math.max(0f, (float) data.getIntOr(KEY_TIMER, 0) / cooldown));
            tooltip.add(JadeUI.progress(new ProgressView(ProgressView.Part.of(progress, PROGRESS_COLOR),
                    Component.literal(Math.round(progress * 100) + "%"), JadeUI.progressStyle(),
                    BoxStyle.nestedBox())));
        }

        // The energy bar of the energy farms is drawn by Jade itself, from the energy storage capability of the farm
    }
}
