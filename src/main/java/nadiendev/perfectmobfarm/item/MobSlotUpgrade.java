package nadiendev.perfectmobfarm.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Mob Slot Upgrade item. Valid for every farm: while installed the farm has a second mob shard slot.
 */
public class MobSlotUpgrade extends Item {

    public MobSlotUpgrade(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        pTooltipComponents.add(Component.translatable("item.perfectmobfarmrevived.mob_slot_upgrade.tooltip")
                .withStyle(ChatFormatting.GRAY));
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pTooltipFlag);
    }
}
