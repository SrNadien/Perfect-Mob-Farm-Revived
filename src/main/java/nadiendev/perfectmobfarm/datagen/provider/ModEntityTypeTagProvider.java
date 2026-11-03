package nadiendev.perfectmobfarm.datagen.provider;

import nadiendev.perfectmobfarm.PerfectMobFarm;
import nadiendev.perfectmobfarm.config.MobListManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;

import java.util.concurrent.CompletableFuture;

/**
 * Entity type tags of the mod. Both mob lists are generated empty so that packs and users can fill them.
 */
public class ModEntityTypeTagProvider extends EntityTypeTagsProvider {

    public ModEntityTypeTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, PerfectMobFarm.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Creating the appender registers the (empty) tag file
        tag(MobListManager.BLACKLIST_TAG);
        tag(MobListManager.WHITELIST_TAG);
    }
}
