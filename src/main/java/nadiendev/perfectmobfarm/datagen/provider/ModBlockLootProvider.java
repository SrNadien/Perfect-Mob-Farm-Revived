package nadiendev.perfectmobfarm.datagen.provider;

import nadiendev.perfectmobfarm.block.BlockManager;
import nadiendev.perfectmobfarm.block.FarmTier;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Block loot tables of the mod: every mob farm, normal and energy variant, drops itself (subject to explosion
 * survival). The stored energy is lost when the block is broken.
 */
public class ModBlockLootProvider extends BlockLootSubProvider {

    public ModBlockLootProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    /**
     * @return The loot table provider that wraps this sub provider
     */
    public static LootTableProvider create(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        return new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(ModBlockLootProvider::new, LootContextParamSets.BLOCK)),
                registries);
    }

    @Override
    protected void generate() {
        for (Block farm : getUnconditionalFarms()) {
            dropSelf(farm);
        }
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return getUnconditionalFarms();
    }

    /**
     * Farms of tiers that need another mod have their loot table written by {@link ModConditionalBlockLootProvider}
     *
     * @return The farm blocks that always exist
     */
    private static List<Block> getUnconditionalFarms() {
        return Arrays.stream(FarmTier.values())
                .filter(tier -> tier.getRequiredMod() == null)
                .flatMap(tier -> Stream.of(BlockManager.getBlock(tier), BlockManager.getEnergyBlock(tier)))
                .<Block>map(DeferredBlock::get)
                .toList();
    }
}
