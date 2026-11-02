package nadiendev.perfectmobfarm.item;

import nadiendev.perfectmobfarm.config.ServerConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Mob Shard item
 */
public class MobShard extends Item {

    public MobShard(Properties pProperties) {
        super(pProperties);
    }

    /**
     * @param stack Any item stack
     * @return True if the stack is a mob shard that the farms must never damage
     */
    public static boolean isUnbreakable(ItemStack stack) {
        return stack.getItem() instanceof MobShard shard && shard.isUnbreakableShard();
    }

    /**
     * @param stack Any item stack
     * @return The number of kills the shard needs to be complete (the normal amount if the stack is not a shard)
     */
    public static int getKillsNeeded(ItemStack stack) {
        if (stack.getItem() instanceof MobShard shard) {
            return shard.getKillsNeeded();
        }
        return ServerConfigs.getSafe(ServerConfigs.MOB_SHARD_KILL_NEEDED);
    }

    /**
     * @param stack Any item stack
     * @return True if the stack is a linked mob shard that registered enough kills to be used in a mob farm
     */
    public static boolean isComplete(ItemStack stack) {
        MobShardData data = stack.get(DataComponentManager.MOB_SHARD_DATA.get());
        return data != null && stack.getItem() instanceof MobShard && data.killedCount() >= getKillsNeeded(stack);
    }

    /**
     * @return The number of kills this kind of shard needs to be complete
     */
    protected int getKillsNeeded() {
        return ServerConfigs.getSafe(ServerConfigs.MOB_SHARD_KILL_NEEDED);
    }

    /**
     * @return True if this kind of shard is never damaged by the farms. The normal shard depends on the config.
     */
    protected boolean isUnbreakableShard() {
        return ServerConfigs.getSafeBoolean(ServerConfigs.MOB_SHARD_UNBREAKABLE);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        MobShardData data = pStack.get(DataComponentManager.MOB_SHARD_DATA.get());
        if (data != null) {
            pTooltipComponents.add(Component.literal(data.mobName()).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            // A shard that needs no kills is complete as soon as it is linked
            if (getKillsNeeded() > 0) {
                pTooltipComponents.add(Component.literal("Killed : " + data.killedCount() + "/"
                        + getKillsNeeded()).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
        }
        if (isUnbreakableShard()) {
            pTooltipComponents.add(Component.translatable("item.unbreakable").withStyle(ChatFormatting.BLUE));
        }
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }

    /**
     * The max damage comes from the config, which can only be read after the registration.
     * The default MAX_DAMAGE component only exists to make the stack damageable.
     */
    @Override
    public int getMaxDamage(ItemStack stack) {
        return ServerConfigs.getSafe(ServerConfigs.MOB_SHARD_DURABILITY);
    }

    /**
     * Unbreakable shards never show a durability bar
     */
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !isUnbreakableShard() && super.isBarVisible(stack);
    }
}
