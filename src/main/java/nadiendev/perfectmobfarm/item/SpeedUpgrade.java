package nadiendev.perfectmobfarm.item;

import nadiendev.perfectmobfarm.config.ServerConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * Speed Upgrade item. Goes in the speed upgrade slot of the Iron, Gold, Diamond and Emerald farms (up to 4)
 * and lowers their cooldown, see {@link nadiendev.perfectmobfarm.block.entity.MobFarmBlockEntity#getCooldown()}.
 */
public class SpeedUpgrade extends Item {

    public SpeedUpgrade(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, TooltipDisplay pDisplay, Consumer<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        pTooltipComponents.accept(Component.translatable("item.perfectmobfarmrevived.speed_upgrade.tooltip",
                ServerConfigs.getSafe(ServerConfigs.SPEED_UPGRADE_REDUCTION_PERCENT)).withStyle(ChatFormatting.GRAY));
        super.appendHoverText(pStack, pContext, pDisplay, pTooltipComponents, pTooltipFlag);
    }
}
