package nadiendev.perfectmobfarm.datagen.provider;

import nadiendev.perfectmobfarm.block.BlockManager;
import nadiendev.perfectmobfarm.block.FarmTier;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Block loot tables of the mod: every mob farm, normal and energy variant, drops itself (subject to explosion
 * survival). The stored energy is lost when the block is broken.
 */
public class ModBlockLootProvider extends BlockLootSubProvider {

    public ModBlockLootProvider(LootTableSubProvider.Context context) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), context);
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
     * @return Every farm block: normal and energy variant of every tier
     */
    private static List<Block> getFarms() {
        return Arrays.stream(FarmTier.values())
                .flatMap(tier -> Stream.of(BlockManager.getBlock(tier), BlockManager.getEnergyBlock(tier)))
                .<Block>map(DeferredBlock::get)
                .toList();
    }
}
