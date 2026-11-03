package nadiendev.perfectmobfarm.screen.slot;

import nadiendev.perfectmobfarm.block.entity.MobFarmBlockEntity;
import nadiendev.perfectmobfarm.item.MobShard;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

/**
 * Slot that only accepts mob shards
 */
public class MobShardSlot extends ResourceHandlerSlot {
    public MobShardSlot(MobFarmBlockEntity.FarmItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, itemHandler::set, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() instanceof MobShard;
    }
}
