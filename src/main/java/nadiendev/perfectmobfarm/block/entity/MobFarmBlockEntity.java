package nadiendev.perfectmobfarm.block.entity;

import com.mojang.logging.LogUtils;
import nadiendev.perfectmobfarm.block.FarmTier;
import nadiendev.perfectmobfarm.block.MobFarmBlock;
import nadiendev.perfectmobfarm.config.ServerConfigs;
import nadiendev.perfectmobfarm.item.DataComponentManager;
import nadiendev.perfectmobfarm.item.MobShard;
import nadiendev.perfectmobfarm.item.MobShardData;
import nadiendev.perfectmobfarm.item.MobSlotUpgrade;
import nadiendev.perfectmobfarm.item.SpeedUpgrade;
import nadiendev.perfectmobfarm.screen.MobFarmMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * Entity for the Mob Farm blocks (every tier)
 */
public class MobFarmBlockEntity extends BlockEntity implements MenuProvider {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Indexes of the {@link ContainerData} synced to the menu */
    public static final int DATA_TIMER = 0;
    public static final int DATA_COOLDOWN = 1;
    public static final int DATA_ACTIVE = 2;
    public static final int DATA_REDSTONE_DISABLED = 3;
    public static final int DATA_ENERGY_LOW = 4;
    public static final int DATA_ENERGY_HIGH = 5;
    public static final int DATA_CAPACITY_LOW = 6;
    public static final int DATA_CAPACITY_HIGH = 7;
    public static final int DATA_USES_ENERGY = 8;
    public static final int DATA_COUNT = 9;

    /** Number of shard slots (the second one needs a Mob Slot Upgrade) and of upgrade slots */
    public static final int SHARD_SLOTS = 2;
    public static final int UPGRADE_SLOTS = 2;
    /** Indexes of the upgrade slots */
    public static final int UPGRADE_SPEED = 0;
    public static final int UPGRADE_MOB_SLOT = 1;
    /** Maximum number of speed upgrades in a farm */
    public static final int MAX_SPEED_UPGRADES = 4;

    private static final Direction[] DIRECTIONS = Direction.values();

    /**
     * Timer that counts ticks
     */
    private int timer;

    /**
     * Boolean value that determines if the farm is currently generating loots or not
     */
    private boolean isActive;

    /**
     * True for the energy variant of the farm, which needs FE to work
     */
    private final boolean usesEnergy;

    /**
     * Energy buffer, null for the normal variant
     */
    @Nullable
    private final FarmEnergyStorage energyStorage;

    /**
     * Cached: true when at least one lever is attached to the farm and every attached lever is off
     */
    private boolean redstoneDisabled;

    /**
     * True when the cached redstone state has to be read again (done on the next tick, once the neighbors are loaded)
     */
    private boolean redstoneDirty = true;

    /**
     * Cache of the item handler of the container below (server side only, created on first use)
     */
    @Nullable
    private BlockCapabilityCache<ResourceHandler<ItemResource>, Direction> containerCache;

    /**
     * Shard slots: slot 0 is always usable, slot 1 only while a {@link MobSlotUpgrade} is installed
     */
    private final FarmItemHandler itemHandler = new FarmItemHandler(SHARD_SLOTS) {
        @Override
        protected void onContentsChanged(int slot, ItemStack previousContents) {
            isActive = computeActive();
            timer = 0;
            notifyChanged();
        }

        @Override
        public boolean isValid(int slot, ItemResource resource) {
            return resource.getItem() instanceof MobShard && isShardSlotUsable(slot);
        }

        @Override
        protected int getCapacity(int slot, ItemResource resource) {
            return 1;
        }
    };

    /**
     * Upgrade slots: {@link #UPGRADE_SPEED} holds up to 4 {@link SpeedUpgrade}, {@link #UPGRADE_MOB_SLOT} one
     * {@link MobSlotUpgrade}
     */
    private final FarmItemHandler upgradeHandler = new FarmItemHandler(UPGRADE_SLOTS) {
        @Override
        protected void onContentsChanged(int slot, ItemStack previousContents) {
            // The second shard slot may have been enabled or disabled
            isActive = computeActive();
            notifyChanged();
        }

        @Override
        public boolean isValid(int slot, ItemResource resource) {
            return switch (slot) {
                case UPGRADE_SPEED -> resource.getItem() instanceof SpeedUpgrade && getTier().supportsSpeedUpgrade();
                case UPGRADE_MOB_SLOT -> resource.getItem() instanceof MobSlotUpgrade;
                default -> false;
            };
        }

        @Override
        protected int getCapacity(int slot, ItemResource resource) {
            return slot == UPGRADE_SPEED ? MAX_SPEED_UPGRADES : 1;
        }

        @Override
        public int extract(int slot, ItemResource resource, int amount, TransactionContext transaction) {
            // The mob slot upgrade stays while the second shard slot holds a shard
            if (slot == UPGRADE_MOB_SLOT && !itemHandler.getStackInSlot(1).isEmpty()) {
                return 0;
            }
            return super.extract(slot, resource, amount, transaction);
        }
    };

    /**
     * Data synced to the open menu: index 0 is the timer, 1 the effective cooldown (with speed upgrades), 2 the active flag (0 or 1),
     * 3 the redstone disabled flag (0 or 1), 4 and 5 the low and high 16 bits of the stored energy, 6 and 7 the low and
     * high 16 bits of the energy capacity, 8 the uses energy flag (0 or 1).
     * The cooldown and the capacity are read live from the config through the tier. Menu data travels as 16 bit values,
     * so the halves are plain unsigned 16 bit numbers: the reader must mask them with 0xFFFF.
     */
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_TIMER -> timer;
                case DATA_COOLDOWN -> getCooldown();
                case DATA_ACTIVE -> isActive ? 1 : 0;
                case DATA_REDSTONE_DISABLED -> redstoneDisabled ? 1 : 0;
                case DATA_ENERGY_LOW -> getEnergyStored() & 0xFFFF;
                case DATA_ENERGY_HIGH -> (getEnergyStored() >>> 16) & 0xFFFF;
                case DATA_CAPACITY_LOW -> getEnergyCapacity() & 0xFFFF;
                case DATA_CAPACITY_HIGH -> (getEnergyCapacity() >>> 16) & 0xFFFF;
                case DATA_USES_ENERGY -> usesEnergy ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATA_TIMER -> timer = value;
                case DATA_ACTIVE -> isActive = value != 0;
                default -> {
                    // The other values are read only
                }
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    /**
     * Entities to display inside the farm, one per shard slot (client side only)
     */
    private final Entity[] entitiesToDisplay = new Entity[SHARD_SLOTS];

    /**
     * Mobs used to create loots, one per shard slot (they are only created again when the mob type changes)
     */
    private final Mob[] mobsToLoot = new Mob[SHARD_SLOTS];

    public MobFarmBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(BlockEntityManager.MOB_FARM.get(), pWorldPosition, pBlockState);
        this.usesEnergy = pBlockState.getBlock() instanceof MobFarmBlock farmBlock && farmBlock.usesEnergy();
        this.energyStorage = usesEnergy ? new FarmEnergyStorage() : null;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        // The levers are read on the first tick: the neighbor chunks may not be loaded yet
        redstoneDirty = true;
    }

    /**
     * @return The tier of the farm, read from the block state
     */
    public FarmTier getTier() {
        if (getBlockState().getBlock() instanceof MobFarmBlock farmBlock) {
            return farmBlock.getTier();
        }
        return FarmTier.IRON;
    }

    /**
     * Function called each tick.
     * If the mob farm is active, enabled by the levers and (energy variant) powered, then call generate drop.
     * An energy farm uses its energy per tick only while the container below is available; without enough energy
     * the timer pauses.
     */
    public void tick() {
        if (redstoneDirty) {
            refreshRedstone();
        }
        if (!isActive || redstoneDisabled) {
            return;
        }

        int energyUsed = 0;
        if (energyStorage != null && getNearbyContainer() != null) {
            energyUsed = getTier().getEnergyPerTick();
            if (energyStorage.getAmountAsInt() < energyUsed) {
                return;
            }
        }

        timer++;
        if (energyUsed > 0) {
            energyStorage.consume(energyUsed);
        }
        if (timer >= getCooldown()) {
            generateDrop();
            timer = 0;
        }
    }

    /**
     * Read again the levers attached to the farm and update the cached state. A lever at the position next to the farm
     * in direction d is attached to it when the lever is connected in direction d. A farm with attached levers only
     * works while at least one of them is on; without attached levers it always works. Server side only.
     */
    public void refreshRedstone() {
        if (level == null || level.isClientSide()) {
            return;
        }
        redstoneDirty = false;

        boolean leverControlled = false;
        boolean anyLeverOn = false;
        BlockPos farmPos = getBlockPos();
        for (Direction direction : DIRECTIONS) {
            BlockPos leverPos = farmPos.relative(direction);
            if (!level.isLoaded(leverPos)) {
                continue;
            }
            BlockState leverState = level.getBlockState(leverPos);
            if (leverState.is(Blocks.LEVER) && getConnectedDirection(leverState) == direction) {
                leverControlled = true;
                if (leverState.getValue(LeverBlock.POWERED)) {
                    anyLeverOn = true;
                    break;
                }
            }
        }
        redstoneDisabled = leverControlled && !anyLeverOn;
    }

    /**
     * Same as the vanilla face attached block: direction from the block holding the lever towards the lever
     */
    private static Direction getConnectedDirection(BlockState leverState) {
        return switch (leverState.getValue(LeverBlock.FACE)) {
            case CEILING -> Direction.DOWN;
            case FLOOR -> Direction.UP;
            default -> leverState.getValue(LeverBlock.FACING);
        };
    }

    /**
     * Generate drops from the mob type captured by every usable mob shard in the mob farm (the second slot only counts
     * while a Mob Slot Upgrade is installed). Every shard is damaged as usual.
     */
    public void generateDrop() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        ResourceHandler<ItemResource> container = null;
        boolean generatedAny = false;
        for (int slot = 0; slot < SHARD_SLOTS; slot++) {
            ItemStack mobShard = itemHandler.getStackInSlot(slot);
            if (!isShardSlotUsable(slot) || !(mobShard.getItem() instanceof MobShard) || !MobShard.isComplete(mobShard)) {
                continue;
            }
            MobShardData data = mobShard.get(DataComponentManager.MOB_SHARD_DATA.get());
            if (data == null) {
                continue;
            }

            // The container is the same for every shard: read it once
            if (container == null) {
                container = getNearbyContainer();
                if (container == null) {
                    LOGGER.debug("GENERATE-DROP ~ No container found");
                    return;
                }
            }

            if (generateShardDrop(serverLevel, slot, mobShard, data, container)) {
                generatedAny = true;
            }
        }

        if (generatedAny) {
            spawnParticles(serverLevel);
        }
    }

    /**
     * Generate and insert the loot of one shard in the container below.
     *
     * @param serverLevel Level of the farm
     * @param slot        Shard slot of the shard
     * @param mobShard    The (complete) mob shard
     * @param data        Data of the mob shard
     * @param container   Container below the farm
     * @return True if the loot was generated (at least one item placed, or air generated) and the shard was damaged
     */
    private boolean generateShardDrop(ServerLevel serverLevel, int slot, ItemStack mobShard, MobShardData data,
                                     ResourceHandler<ItemResource> container) {
        Identifier loc = Identifier.tryParse(data.lootTable());
        if (loc == null) {
            LOGGER.error("In generateDrop(): Invalid loot table location {}.", data.lootTable());
            return false;
        }
        LootTable lt = serverLevel.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, loc));

        Optional<EntityType<?>> optEntityType = EntityType.byString(data.mobId());
        if (optEntityType.isEmpty()) {
            LOGGER.error("In generateDrop(): Unable to get entity type.");
            return false;
        }

        Mob mob = getOrCreateMobToLoot(slot, optEntityType.get());
        if (mob == null) {
            LOGGER.error("In generateDrop(): Unable to create the mob.");
            return false;
        }

        LootParams params = constructLootParams(serverLevel, mob);
        List<ItemStack> generated = lt.getRandomItems(params);
        LOGGER.debug("ITEMS GENERATED ~~ " + generated.toString());

        // Boolean value to determine if we can place at least one of the generated itemStacks
        boolean canBePlaced = false;
        for (ItemStack itemStack : generated) {

            // Air can be generated from loot table and needs to be count as an item
            // If it only generates Air, then mob shard should be damaged
            if (itemStack.getItem() == Items.AIR) {
                canBePlaced = true;
                continue;
            }
            ItemStack remainingStack = ItemUtil.insertItemReturnRemaining(container, itemStack, false, null);

            // If the counts are different, then at least one item as been placed
            if (itemStack.getCount() != remainingStack.getCount()) {
                canBePlaced = true;
            }
        }

        // If no item can be placed, then stop damaging the mob shard
        if (canBePlaced) {
            damageShard(slot, mobShard);
        }
        return canBePlaced;
    }

    /**
     * Send the particles of a cycle that generated loot: happy villager for the normal farms, electric spark for the
     * energy farms, around the top of the block.
     */
    private void spawnParticles(ServerLevel serverLevel) {
        BlockPos pos = getBlockPos();
        serverLevel.sendParticles(usesEnergy ? ParticleTypes.ELECTRIC_SPARK : ParticleTypes.HAPPY_VILLAGER,
                pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 6, 0.3D, 0.15D, 0.3D, 0.02D);
    }

    /**
     * Damage the mob shard by one. If its durability is exhausted, then remove it from the farm.
     *
     * @param slot     Shard slot of the shard
     * @param mobShard Mob shard in the farm
     */
    private void damageShard(int slot, ItemStack mobShard) {
        if (MobShard.isUnbreakable(mobShard)) {
            return;
        }

        int newDamage = mobShard.getDamageValue() + 1;
        if (mobShard.getMaxDamage() <= newDamage) {
            // Broken shard: empty the slot (also updates the active flag and syncs the client)
            itemHandler.setStackInSlot(slot, ItemStack.EMPTY);
        } else {
            mobShard.setDamageValue(newDamage);
            setChanged();
        }
    }

    /**
     * Get the item handler of the block below and return it
     *
     * @return The item handler below, or null if there is none
     */
    @Nullable
    public ResourceHandler<ItemResource> getNearbyContainer() {
        if (level == null) {
            return null;
        }
        // On the server a capability cache avoids a full lookup each tick
        if (level instanceof ServerLevel serverLevel) {
            if (containerCache == null) {
                containerCache = BlockCapabilityCache.create(Capabilities.Item.BLOCK, serverLevel,
                        getBlockPos().below(), Direction.UP);
            }
            return containerCache.getCapability();
        }
        return level.getCapability(Capabilities.Item.BLOCK, getBlockPos().below(), Direction.UP);
    }

    /**
     * Construct the loot params, as if a player had killed the mob.
     *
     * @param serverLevel Level of the farm
     * @param mob         Mob from which drops come
     * @return The loot params
     */
    protected LootParams constructLootParams(ServerLevel serverLevel, Mob mob) {
        Vec3 position = Vec3.atLowerCornerOf(getBlockPos());
        Player fakePlayer = FakePlayerFactory.getMinecraft(serverLevel);

        // The fake player is shared between every farm, so its weapon is always set (empty if no looting)
        ItemStack weapon = ItemStack.EMPTY;
        int lootingLevel = getTier().getLootingLevel();
        if (lootingLevel > 0) {
            weapon = new ItemStack(Items.DIAMOND_SWORD);
            weapon.enchant(serverLevel.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING), lootingLevel);
        }
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, weapon);

        return new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, position)
                .withParameter(LootContextParams.THIS_ENTITY, mob)
                .withParameter(LootContextParams.DAMAGE_SOURCE, serverLevel.damageSources().playerAttack(fakePlayer))
                .withParameter(LootContextParams.ATTACKING_ENTITY, fakePlayer)
                .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, fakePlayer)
                .create(LootContextParamSets.ENTITY);
    }

    /**
     * Called when a mob farm block is removed.
     * Drop on the ground the mob shards and the upgrades. The stored energy is lost.
     */
    public void drop() {
        if (level == null || level.isClientSide()) {
            return;
        }
        dropContents(itemHandler);
        dropContents(upgradeHandler);
    }

    /**
     * Drop on the ground every stack of a handler
     */
    private void dropContents(FarmItemHandler handler) {
        for (int slot = 0; slot < handler.size(); slot++) {
            ItemStack itemStack = handler.getStackInSlot(slot);
            if (!itemStack.isEmpty()) {
                Containers.dropItemStack(level, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), itemStack);
            }
        }
    }

    /**
     * Mark the farm as changed and send it to the clients: the renderer needs the shards
     */
    private void notifyChanged() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    /**
     * @return True if at least one usable shard slot holds a complete mob shard
     */
    private boolean computeActive() {
        for (int slot = 0; slot < SHARD_SLOTS; slot++) {
            if (isShardSlotUsable(slot) && MobShard.isComplete(itemHandler.getStackInSlot(slot))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
        return new MobFarmMenu(pContainerId, pInventory, this, data);
    }

    @Override
    protected void loadAdditional(ValueInput pInput) {
        super.loadAdditional(pInput);
        itemHandler.deserialize(pInput.childOrEmpty("inventory"));
        upgradeHandler.deserialize(pInput.childOrEmpty("upgrades"));
        isActive = computeActive();
        timer = pInput.getIntOr("timer", 0);
        if (energyStorage != null) {
            energyStorage.deserialize(pInput);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput pOutput) {
        super.saveAdditional(pOutput);
        itemHandler.serialize(pOutput.child("inventory"));
        upgradeHandler.serialize(pOutput.child("upgrades"));
        pOutput.putBoolean("isActive", isActive);
        pOutput.putInt("timer", timer);
        if (energyStorage != null) {
            energyStorage.serialize(pOutput);
        }
    }

    /**
     * Called when the block is replaced or removed: drop the shards and the upgrades
     */
    @Override
    public void preRemoveSideEffects(BlockPos pPos, BlockState pState) {
        super.preRemoveSideEffects(pPos, pState);
        drop();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        return saveWithoutMetadata(pRegistries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /**
     * @return The shard slots (size {@link #SHARD_SLOTS}, slot 0 is always usable, see {@link #isShardSlotUsable(int)})
     */
    public FarmItemHandler getItemHandler() {
        return itemHandler;
    }

    /**
     * @return The upgrade slots (size {@link #UPGRADE_SLOTS}): {@link #UPGRADE_SPEED} and {@link #UPGRADE_MOB_SLOT}
     */
    public FarmItemHandler getUpgradeHandler() {
        return upgradeHandler;
    }

    /**
     * @return True if a Mob Slot Upgrade is installed, which enables the second shard slot
     */
    public boolean hasExtraMobSlot() {
        return upgradeHandler.getStackInSlot(UPGRADE_MOB_SLOT).getItem() instanceof MobSlotUpgrade;
    }

    /**
     * @return Number of speed upgrades installed, 0 if the tier of the farm does not support them
     */
    public int getSpeedUpgrades() {
        ItemStack stack = upgradeHandler.getStackInSlot(UPGRADE_SPEED);
        if (!(stack.getItem() instanceof SpeedUpgrade) || !getTier().supportsSpeedUpgrade()) {
            return 0;
        }
        return Math.min(stack.getCount(), MAX_SPEED_UPGRADES);
    }

    /**
     * @param index Shard slot index
     * @return True if the shard slot works: the first one always, the second one with a Mob Slot Upgrade
     */
    public boolean isShardSlotUsable(int index) {
        return index == 0 || (index > 0 && index < SHARD_SLOTS && hasExtraMobSlot());
    }

    /**
     * @return True for the energy variant of the farm
     */
    public boolean usesEnergy() {
        return usesEnergy;
    }

    /**
     * @return Energy (FE) stored, always 0 for the normal variant
     */
    public int getEnergyStored() {
        return energyStorage == null ? 0 : energyStorage.getAmountAsInt();
    }

    /**
     * @return Size of the energy buffer (FE), always 0 for the normal variant
     */
    public int getEnergyCapacity() {
        return energyStorage == null ? 0 : energyStorage.getCapacityAsInt();
    }

    /**
     * @return The energy storage (receive only), or null for the normal variant
     */
    @Nullable
    public EnergyHandler getEnergyStorage() {
        return energyStorage;
    }

    /**
     * @return True when at least one lever is attached to the farm and every attached lever is off
     */
    public boolean isRedstoneDisabled() {
        return redstoneDisabled;
    }

    /**
     * @return True for an energy farm that is active and enabled by the levers but holds less energy than it needs
     * for one tick of work
     */
    public boolean isEnergyStarved() {
        return energyStorage != null && isActive && !redstoneDisabled
                && energyStorage.getAmountAsInt() < getTier().getEnergyPerTick();
    }

    /**
     * @return The data synced to the menu (see {@link #data} for the indexes)
     */
    public ContainerData getContainerData() {
        return data;
    }

    /**
     * @return Ticks elapsed since the last generation
     */
    public int getTimer() {
        return timer;
    }

    /**
     * @return Ticks needed by the tier of this farm to generate loot (without upgrades), read live from the config
     */
    public int getBaseCooldown() {
        return getTier().getCooldown();
    }

    /**
     * @return Ticks needed by this farm to generate loot, with the speed upgrades: each one removes the configured
     * percent of the base cooldown (at least 1 tick). Read live from the config.
     */
    public int getCooldown() {
        int base = getBaseCooldown();
        int upgrades = getSpeedUpgrades();
        if (upgrades <= 0) {
            return base;
        }
        double percent = ServerConfigs.getSafe(ServerConfigs.SPEED_UPGRADE_REDUCTION_PERCENT);
        long reduced = Math.round(base * (1.0D - percent / 100.0D * upgrades));
        return (int) Math.max(1L, reduced);
    }

    /**
     * @return True if at least one usable shard slot holds a complete mob shard and the farm is generating loot
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * @param index Shard slot index (0 or 1)
     * @return The entity cached by the renderer for the shard slot (client side only), may be null
     */
    public Entity getEntityToDisplay(int index) {
        return entitiesToDisplay[index];
    }

    public void setEntityToDisplay(int index, Entity entityToDisplay) {
        this.entitiesToDisplay[index] = entityToDisplay;
    }

    /**
     * Item handler of the farm (shards or upgrades). Besides the resource handler API it gives direct access to the
     * stacks, because the farm damages the shards in place.
     */
    public static class FarmItemHandler extends ItemStacksResourceHandler {

        public FarmItemHandler(int size) {
            super(size);
        }

        /**
         * @param slot Slot index
         * @return The stack of the slot itself, not a copy
         */
        public ItemStack getStackInSlot(int slot) {
            return stacks.get(slot);
        }

        /**
         * Replace the stack of a slot
         */
        public void setStackInSlot(int slot, ItemStack stack) {
            set(slot, ItemResource.of(stack), stack.getCount());
        }
    }

    /**
     * Energy buffer of the energy farms: accepts energy from every side, never gives it back.
     * The capacity is read live from the config through the tier.
     */
    private final class FarmEnergyStorage extends SimpleEnergyHandler {

        private FarmEnergyStorage() {
            super(0, Integer.MAX_VALUE, 0);
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            capacity = getTier().getEnergyCapacity();
            return super.insert(amount, transaction);
        }

        /**
         * Use energy for the work of the farm (not an extraction through the capability)
         */
        private void consume(int amount) {
            set(Math.max(0, getAmountAsInt() - amount));
        }

        @Override
        public long getAmountAsLong() {
            return Math.min(energy, getCapacityAsLong());
        }

        @Override
        public long getCapacityAsLong() {
            return getTier().getEnergyCapacity();
        }

        @Override
        protected void onEnergyChanged(int previousAmount) {
            setChanged();
        }
    }

    /**
     * Check the current mob to loot of a shard slot and create a new one or return the current one depending on the state.
     *
     * @param slot       Shard slot
     * @param entityType Entity type to compare
     * @return If the mob of the slot is null, create a new one. If its type is different from the passed one, create a new one. Otherwise return the current.
     */
    @Nullable
    private Mob getOrCreateMobToLoot(int slot, EntityType<?> entityType) {
        Mob cached = mobsToLoot[slot];
        if (cached == null || cached.getType() != entityType) {
            Entity created = entityType.create(this.level, EntitySpawnReason.LOAD);
            cached = created instanceof Mob mob ? mob : null;
            mobsToLoot[slot] = cached;
        }
        return cached;
    }
}
