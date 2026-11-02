package nadiendev.perfectmobfarm.screen.slot;

import nadiendev.perfectmobfarm.item.MobShard;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Slot that only accepts mob shards
 */
public class MobShardSlot extends SlotItemHandler {
    public MobShardSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return stack.getItem() instanceof MobShard;
    }
}
