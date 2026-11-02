package nadiendev.perfectmobfarm.item;

import nadiendev.perfectmobfarm.PerfectMobFarm;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Manager for the mod data components
 */
public class DataComponentManager {

    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, PerfectMobFarm.MODID);

    /**
     * Data of a linked mob shard. Absent while the shard is not linked to a mob.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MobShardData>> MOB_SHARD_DATA =
            DATA_COMPONENTS.registerComponentType("mob_shard_data",
                    builder -> builder.persistent(MobShardData.CODEC).networkSynchronized(MobShardData.STREAM_CODEC));

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }
}
