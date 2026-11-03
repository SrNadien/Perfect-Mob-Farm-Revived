package nadiendev.perfectmobfarm.datagen.provider;

import nadiendev.perfectmobfarm.PerfectMobFarm;
import nadiendev.perfectmobfarm.block.BlockManager;
import nadiendev.perfectmobfarm.block.FarmTier;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

/**
 * Block tags of the mod: every farm (normal and energy variant) is mined with a pickaxe and needs at least an iron tool.
 * The tiers of the integration mod are optional entries, since their blocks only exist when that mod is loaded.
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
        // Same tag, but by key: the optional blocks may not be registered
        TagAppender<ResourceKey<Block>, Block> optional = TagAppender.forBuilder(getOrCreateRawBuilder(tag));
        for (FarmTier farmTier : FarmTier.values()) {
            if (farmTier.getRequiredMod() != null) {
                optional.addOptional(blockKey(farmTier.getName() + "_mob_farm"));
                optional.addOptional(blockKey(farmTier.getName() + "_energy_mob_farm"));
            } else {
                appender.add(BlockManager.getBlock(farmTier).get());
                appender.add(BlockManager.getEnergyBlock(farmTier).get());
            }
        }
    }

    private static ResourceKey<Block> blockKey(String path) {
        return ResourceKey.create(Registries.BLOCK, PerfectMobFarm.id(path));
    }
}
