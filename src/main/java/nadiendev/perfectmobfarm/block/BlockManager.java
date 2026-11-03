package nadiendev.perfectmobfarm.block;

import nadiendev.perfectmobfarm.PerfectMobFarm;
import nadiendev.perfectmobfarm.item.EnergyMobFarmBlockItem;
import nadiendev.perfectmobfarm.item.ItemManager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Manager for the mod blocks. Every tier has two blocks: the normal one and the energy one.
 */
public class BlockManager {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, PerfectMobFarm.MODID);

    private static final Map<FarmTier, RegistryObject<Block>> FARMS_BY_TIER = new EnumMap<>(FarmTier.class);
    private static final Map<FarmTier, RegistryObject<Block>> ENERGY_FARMS_BY_TIER = new EnumMap<>(FarmTier.class);
    private static final List<RegistryObject<Block>> ALL_FARMS = new ArrayList<>();

    static {
        for (FarmTier tier : FarmTier.values()) {
            RegistryObject<Block> farm = registerBlock(tier.getName() + "_mob_farm",
                    () -> new MobFarmBlock(tier, false, farmProperties(tier)));
            RegistryObject<Block> energyFarm = BLOCKS.register(tier.getName() + "_energy_mob_farm",
                    () -> new MobFarmBlock(tier, true, farmProperties(tier)));
            // The energy farm item shows its stored energy as a bar
            ItemManager.ITEMS.register(tier.getName() + "_energy_mob_farm",
                    () -> new EnergyMobFarmBlockItem(tier, energyFarm.get(), new Item.Properties()));
            FARMS_BY_TIER.put(tier, farm);
            ENERGY_FARMS_BY_TIER.put(tier, energyFarm);
            ALL_FARMS.add(farm);
            ALL_FARMS.add(energyFarm);
        }
    }

    /**
     * All the registered mob farms, in tier order: for each tier the normal farm followed by the energy farm
     */
    public static final List<RegistryObject<Block>> MOB_FARMS = List.copyOf(ALL_FARMS);

    /**
     * @param tier Tier of the farm
     * @return The normal farm block (no energy) of this tier
     */
    public static RegistryObject<Block> getBlock(FarmTier tier) {
        return FARMS_BY_TIER.get(tier);
    }

    /**
     * @param tier Tier of the farm
     * @return The energy farm block of this tier
     */
    public static RegistryObject<Block> getEnergyBlock(FarmTier tier) {
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

    private static RegistryObject<Block> registerBlock(String name, Supplier<Block> block) {
        RegistryObject<Block> toReturn = BLOCKS.register(name, block);
        ItemManager.ITEMS.register(name, () -> new BlockItem(toReturn.get(), new Item.Properties()));
        return toReturn;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
