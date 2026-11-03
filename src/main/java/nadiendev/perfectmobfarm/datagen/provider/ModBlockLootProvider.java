package nadiendev.perfectmobfarm.datagen.provider;

import nadiendev.perfectmobfarm.block.BlockManager;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;

/**
 * Block loot tables of the mod: every mob farm, normal and energy variant, drops itself (subject to explosion
 * survival). The stored energy is lost when the block is broken.
 */
public class ModBlockLootProvider extends BlockLootSubProvider {

    public ModBlockLootProvider() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    /**
     * @return The loot table provider that wraps this sub provider
     */
    public static LootTableProvider create(PackOutput output) {
        return new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(ModBlockLootProvider::new, LootContextParamSets.BLOCK)));
    }

    @Override
    protected void generate() {
        for (Block farm : getFarms()) {
            dropSelf(farm);
        }
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return getFarms();
    }

    /**
     * @return Every farm block of the mod, normal and energy variants
     */
    private static List<Block> getFarms() {
        return BlockManager.MOB_FARMS.stream().<Block>map(farm -> farm.get()).toList();
    }
}
