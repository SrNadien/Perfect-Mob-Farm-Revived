package nadiendev.perfectmobfarm.screen.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/**
 * Optional slot of the mob farm menu (second shard, speed upgrade, mob slot upgrade): it only exists while the
 * farm allows it, only accepts the items of its kind and can be locked so its content can not be taken out
 */
public class FarmExtraSlot extends SlotItemHandler {

    private final Predicate<ItemStack> validator;
    private final BooleanSupplier enabled;
    private final BooleanSupplier removable;

    /**
     * @param validator Items that can be placed in the slot
     * @param enabled   Whether the slot is currently available (an unavailable slot is hidden and accepts nothing)
     * @param removable Whether the content can currently be taken out
     */
    public FarmExtraSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition,
                         Predicate<ItemStack> validator, BooleanSupplier enabled, BooleanSupplier removable) {
        super(itemHandler, index, xPosition, yPosition);
        this.validator = validator;
        this.enabled = enabled;
        this.removable = removable;
    }

    @Override
    public boolean isActive() {
        return enabled.getAsBoolean();
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return enabled.getAsBoolean() && validator.test(stack);
    }

    @Override
    public boolean mayPickup(Player player) {
        return removable.getAsBoolean() && super.mayPickup(player);
    }
}
