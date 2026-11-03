package nadiendev.perfectmobfarm.datagen.provider;

import nadiendev.perfectmobfarm.PerfectMobFarm;
import nadiendev.perfectmobfarm.block.BlockManager;
import nadiendev.perfectmobfarm.block.FarmTier;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

/**
 * Block tags of the mod: every farm (normal and energy variant) is mined with a pickaxe and needs at least an iron tool.
 */
public class ModBlockTagProvider extends BlockTagsProvider {

    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, PerfectMobFarm.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        addFarms(BlockTags.MINEABLE_WITH_PICKAXE);
        addFarms(BlockTags.NEEDS_IRON_TOOL);
    }

    private void addFarms(TagKey<Block> tag) {
        var appender = tag(tag);
        for (FarmTier farmTier : FarmTier.values()) {
            appender.add(BlockManager.getBlock(farmTier).getKey());
            appender.add(BlockManager.getEnergyBlock(farmTier).getKey());
        }
    }
}
