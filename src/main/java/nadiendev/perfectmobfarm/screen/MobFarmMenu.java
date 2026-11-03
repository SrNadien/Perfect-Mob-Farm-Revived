package nadiendev.perfectmobfarm.screen;

import nadiendev.perfectmobfarm.block.MobFarmBlock;
import nadiendev.perfectmobfarm.block.entity.MobFarmBlockEntity;
import nadiendev.perfectmobfarm.item.MobShard;
import nadiendev.perfectmobfarm.item.MobSlotUpgrade;
import nadiendev.perfectmobfarm.item.SpeedUpgrade;
import nadiendev.perfectmobfarm.screen.slot.FarmExtraSlot;
import nadiendev.perfectmobfarm.screen.slot.MobShardSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Mob Farm menu displayed when right clicking on a mob farm block
 */
public class MobFarmMenu extends AbstractContainerMenu {

    /** Gui position (top left of the 16x16 item area) of the farm slots, all in the same row */
    public static final int SLOTS_Y = 20;
    public static final int SHARD_A_X = 80;
    public static final int SHARD_B_X = 58;
    public static final int SPEED_UPGRADE_X = 104;
    public static final int MOB_SLOT_UPGRADE_X = 124;

    /** Menu indexes of the farm slots (they come after the 36 player slots) */
    public static final int SHARD_A_INDEX = 36;
    public static final int SHARD_B_INDEX = 37;
    public static final int SPEED_UPGRADE_INDEX = 38;
    public static final int MOB_SLOT_UPGRADE_INDEX = 39;

    /** Clicked entity */
    private final MobFarmBlockEntity farmEntity;

    /** Current level */
    private final Level level;

    /** Data synced from the farm (timer, cooldown, active flag, redstone and energy state); a client side copy on the client */
    private final ContainerData data;

    /** Client side constructor: the block position is sent by the server as extra data */
    public MobFarmMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, (MobFarmBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(MobFarmBlockEntity.DATA_COUNT));
    }

    /** Server side constructor */
    public MobFarmMenu(int pContainerId, Inventory inv, MobFarmBlockEntity entity, ContainerData data) {
        super(MenuManager.MOB_FARM_MENU.get(), pContainerId);
        checkContainerSize(inv, 1);
        checkContainerDataCount(data, MobFarmBlockEntity.DATA_COUNT);
        this.farmEntity = entity;
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        // Menu indexes: 36 shard A, 37 shard B, 38 speed upgrade, 39 mob slot upgrade
        this.addSlot(new MobShardSlot(this.farmEntity.getItemHandler(), 0, SHARD_A_X, SLOTS_Y));
        this.addSlot(new FarmExtraSlot(this.farmEntity.getItemHandler(), 1, SHARD_B_X, SLOTS_Y,
                stack -> stack.getItem() instanceof MobShard,
                () -> this.farmEntity.isShardSlotUsable(1),
                () -> true));
        this.addSlot(new FarmExtraSlot(this.farmEntity.getUpgradeHandler(), MobFarmBlockEntity.UPGRADE_SPEED,
                SPEED_UPGRADE_X, SLOTS_Y,
                stack -> stack.getItem() instanceof SpeedUpgrade,
                () -> this.farmEntity.getTier().supportsSpeedUpgrade(),
                () -> true));
        // The mob slot upgrade stays while the second shard slot holds a shard
        this.addSlot(new FarmExtraSlot(this.farmEntity.getUpgradeHandler(), MobFarmBlockEntity.UPGRADE_MOB_SLOT,
                MOB_SLOT_UPGRADE_X, SLOTS_Y,
                stack -> stack.getItem() instanceof MobSlotUpgrade,
                () -> true,
                () -> this.farmEntity.getItemHandler().getStackInSlot(1).isEmpty()));
        this.addDataSlots(data);
    }

    /**
     * @return True if the farm is generating loot
     */
    public boolean isActive() {
        return data.get(MobFarmBlockEntity.DATA_ACTIVE) != 0;
    }

    /**
     * @return Ticks elapsed since the last generation
     */
    public int getTimer() {
        return data.get(MobFarmBlockEntity.DATA_TIMER);
    }

    /**
     * @return Ticks needed by the farm to generate loot
     */
    public int getCooldown() {
        return data.get(MobFarmBlockEntity.DATA_COOLDOWN);
    }

    /**
     * @param pixels Size of the full progress bar, in pixels
     * @return The part of the bar to fill: timer * pixels / cooldown, 0 when the cooldown is 0 or the farm inactive
     */
    public int getProgress(int pixels) {
        int cooldown = getCooldown();
        if (cooldown <= 0 || !isActive()) {
            return 0;
        }
        return Math.min(pixels, getTimer() * pixels / cooldown);
    }

    /**
     * @return True if the farm is lever controlled and every attached lever is OFF
     */
    public boolean isRedstoneDisabled() {
        return data.get(DATA_REDSTONE_DISABLED) != 0;
    }

    /**
     * @return True for the energy variants of the farm
     */
    public boolean usesEnergy() {
        return data.get(DATA_USES_ENERGY) != 0;
    }

    /**
     * @return Energy stored in the farm (FE), 0 for normal farms
     */
    public int getEnergy() {
        return readInt(DATA_ENERGY_LOW, DATA_ENERGY_HIGH);
    }

    /**
     * @return Energy capacity of the farm (FE), 0 for normal farms
     */
    public int getEnergyCapacity() {
        return readInt(DATA_CAPACITY_LOW, DATA_CAPACITY_HIGH);
    }

    /**
     * @param pixels Size of the full energy bar, in pixels
     * @return The part of the bar to fill: energy * pixels / capacity, at least 1 when there is any energy
     */
    public int getEnergyScaled(int pixels) {
        int capacity = getEnergyCapacity();
        int energy = getEnergy();
        if (capacity <= 0 || energy <= 0) {
            return 0;
        }
        return (int) Math.max(1L, Math.min(pixels, (long) energy * pixels / capacity));
    }

    /**
     * @return True for an energy farm that is working and enabled but holds less energy than it uses per tick
     */
    public boolean isEnergyStarved() {
        return usesEnergy() && isActive() && !isRedstoneDisabled()
                && getEnergy() < this.farmEntity.getTier().getEnergyPerTick();
    }

    /** Rebuild an int sent by the server as two 16 bit halves (a ContainerData value travels as a short) */
    private int readInt(int lowIndex, int highIndex) {
        return (data.get(lowIndex) & 0xFFFF) | ((data.get(highIndex) & 0xFFFF) << 16);
    }

    // ContainerData layout (see MobFarmBlockEntity); only the indices not exposed as constants there
    private static final int DATA_REDSTONE_DISABLED = 3;
    private static final int DATA_ENERGY_LOW = 4;
    private static final int DATA_ENERGY_HIGH = 5;
    private static final int DATA_CAPACITY_LOW = 6;
    private static final int DATA_CAPACITY_HIGH = 7;
    private static final int DATA_USES_ENERGY = 8;

    // CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
    // must assign a slot number to each of the slots used by the GUI.
    // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
    // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
    //  0 - 8 = hotbar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
    //  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
    //  36 - 44 = TileInventory slots, which map to our TileEntity slot numbers 0 - 8)
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    // THIS YOU HAVE TO DEFINE!
    private static final int TE_INVENTORY_SLOT_COUNT = 4;  // shard A, shard B, speed upgrade, mob slot upgrade

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;  //EMPTY_ITEM
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (index < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // This is a vanilla container slot so merge the stack into the matching farm slots: shards go to A or B,
            // speed upgrades to the speed slot and mob slot upgrades to their slot (a closed slot never accepts)
            int first;
            int last;
            if (sourceStack.getItem() instanceof MobShard) {
                first = SHARD_A_INDEX;
                last = SHARD_B_INDEX + 1;
            } else if (sourceStack.getItem() instanceof SpeedUpgrade) {
                first = SPEED_UPGRADE_INDEX;
                last = SPEED_UPGRADE_INDEX + 1;
            } else if (sourceStack.getItem() instanceof MobSlotUpgrade) {
                first = MOB_SLOT_UPGRADE_INDEX;
                last = MOB_SLOT_UPGRADE_INDEX + 1;
            } else {
                return ItemStack.EMPTY;
            }
            if (!moveItemStackTo(sourceStack, first, last, false)) {
                return ItemStack.EMPTY;  // EMPTY_ITEM
            }
        } else if (index < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            // This is a farm slot (a locked one, like the mob slot upgrade under a second shard, keeps its item)
            if (!sourceSlot.mayPickup(playerIn)) {
                return ItemStack.EMPTY;
            }
            // so merge the stack into the players inventory
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            // Invalid slot index
            return ItemStack.EMPTY;
        }
        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        if (!(this.level.getBlockState(this.farmEntity.getBlockPos()).getBlock() instanceof MobFarmBlock)) {
            return false;
        } else {
            return !(pPlayer.distanceToSqr((double) this.farmEntity.getBlockPos().getX() + 0.5D, (double) this.farmEntity.getBlockPos().getY() + 0.5D, (double) this.farmEntity.getBlockPos().getZ() + 0.5D) > 64.0D);
        }
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 51 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 109));
        }
    }

}
