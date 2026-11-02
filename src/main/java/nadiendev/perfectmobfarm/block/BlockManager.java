package nadiendev.perfectmobfarm.block;

import nadiendev.perfectmobfarm.PerfectMobFarm;
import nadiendev.perfectmobfarm.item.EnergyMobFarmBlockItem;
import nadiendev.perfectmobfarm.item.ItemManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Manager for the mod blocks. A farm is only registered when its tier is available
 * (see {@link FarmTier#isAvailable()}). Every tier has two blocks: the normal one and the energy one.
 */
public class BlockManager {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PerfectMobFarm.MODID);

    private static final Map<FarmTier, DeferredBlock<Block>> FARMS_BY_TIER = new EnumMap<>(FarmTier.class);
    private static final Map<FarmTier, DeferredBlock<Block>> ENERGY_FARMS_BY_TIER = new EnumMap<>(FarmTier.class);
    private static final List<DeferredBlock<Block>> AVAILABLE_FARMS = new ArrayList<>();

    static {
        for (FarmTier tier : FarmTier.values()) {
            if (!tier.isAvailable()) {
                continue;
            }
            DeferredBlock<Block> farm = registerBlock(tier.getName() + "_mob_farm",
                    () -> new MobFarmBlock(tier, false, farmProperties(tier)));
            DeferredBlock<Block> energyFarm = BLOCKS.register(tier.getName() + "_energy_mob_farm",
                    () -> new MobFarmBlock(tier, true, farmProperties(tier)));
            // The energy farm item shows its stored energy as a bar
            ItemManager.ITEMS.register(tier.getName() + "_energy_mob_farm",
                    () -> new EnergyMobFarmBlockItem(tier, energyFarm.get(), new Item.Properties()));
            FARMS_BY_TIER.put(tier, farm);
            ENERGY_FARMS_BY_TIER.put(tier, energyFarm);
            AVAILABLE_FARMS.add(farm);
            AVAILABLE_FARMS.add(energyFarm);
        }
    }

    /**
     * All the registered mob farms (only the available tiers), in tier order: for each tier the normal farm
     * followed by the energy farm
     */
    public static final List<DeferredBlock<Block>> MOB_FARMS = List.copyOf(AVAILABLE_FARMS);

    /**
     * @param tier Tier of the farm
     * @return The normal farm block (no energy) of this tier, or null if the tier is not available
     */
    @Nullable
    public static DeferredBlock<Block> getBlock(FarmTier tier) {
        return FARMS_BY_TIER.get(tier);
    }

    /**
     * @param tier Tier of the farm
     * @return The energy farm block of this tier, or null if the tier is not available
     */
    @Nullable
    public static DeferredBlock<Block> getEnergyBlock(FarmTier tier) {
        return ENERGY_FARMS_BY_TIER.get(tier);
    }

    private static BlockBehaviour.Properties farmProperties(FarmTier tier) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of().sound(SoundType.GLASS)
                .requiresCorrectToolForDrops().strength(5f).noOcclusion();
        // Netherite and every tier above it
        if (tier.compareTo(FarmTier.NETHERITE) >= 0) {
            properties.explosionResistance(1200F);
        }
        return properties;
    }

    private static DeferredBlock<Block> registerBlock(String name, Supplier<Block> block) {
        DeferredBlock<Block> toReturn = BLOCKS.register(name, block);
        ItemManager.ITEMS.registerSimpleBlockItem(name, toReturn);
        return toReturn;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
