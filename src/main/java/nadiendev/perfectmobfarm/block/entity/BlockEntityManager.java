package nadiendev.perfectmobfarm.block.entity;

import nadiendev.perfectmobfarm.PerfectMobFarm;
import nadiendev.perfectmobfarm.block.BlockManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Manager for the mod block entities
 */
public class BlockEntityManager {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, PerfectMobFarm.MODID);

    /**
     * One block entity type shared by every registered mob farm block (normal and energy, only the available tiers)
     */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MobFarmBlockEntity>> MOB_FARM = BLOCK_ENTITIES.register("mob_farm_entity",
            () -> BlockEntityType.Builder.of(MobFarmBlockEntity::new,
                    BlockManager.MOB_FARMS.stream().map(block -> (Block) block.get()).toArray(Block[]::new)).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
