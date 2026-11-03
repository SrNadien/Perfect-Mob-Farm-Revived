package nadiendev.perfectmobfarm.item;

import nadiendev.perfectmobfarm.block.FarmTier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Block item of the energy mob farms. It shows the stored energy as a green bar under the item, like a charged
 * item, so a farm that carries energy (block entity data of the stack) can be told apart from an empty one.
 */
public class EnergyMobFarmBlockItem extends BlockItem {

    /** Width in pixels of a full item bar */
    private static final int BAR_WIDTH = 13;
    private static final int BAR_COLOR = 0x2EE62E;

    private final FarmTier tier;

    public EnergyMobFarmBlockItem(FarmTier tier, Block block, Properties properties) {
        super(block, properties);
        this.tier = tier;
    }

    /**
     * @param stack Stack of an energy mob farm
     * @return The energy stored in the block entity data of the stack, 0 if it carries none
     */
    private static int getStoredEnergy(ItemStack stack) {
        TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        return data == null ? 0 : Math.max(0, data.copyTagWithoutId().getIntOr("energy", 0));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getStoredEnergy(stack) > 0 && tier.getEnergyCapacity() > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int capacity = tier.getEnergyCapacity();
        if (capacity <= 0) {
            return 0;
        }
        return Math.max(1, Math.min(BAR_WIDTH, Math.round(BAR_WIDTH * (float) getStoredEnergy(stack) / capacity)));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return BAR_COLOR;
    }
}
