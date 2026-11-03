package nadiendev.perfectmobfarm.block.entity;

import nadiendev.perfectmobfarm.PerfectMobFarm;
import nadiendev.perfectmobfarm.block.BlockManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Manager for the mod block entities
 */
public class BlockEntityManager {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, PerfectMobFarm.MODID);

    /**
     * One block entity type shared by every registered mob farm block (normal and energy)
     */
    public static final RegistryObject<BlockEntityType<MobFarmBlockEntity>> MOB_FARM = BLOCK_ENTITIES.register("mob_farm_entity",
            () -> BlockEntityType.Builder.of(MobFarmBlockEntity::new,
                    BlockManager.MOB_FARMS.stream().map(block -> (Block) block.get()).toArray(Block[]::new)).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
