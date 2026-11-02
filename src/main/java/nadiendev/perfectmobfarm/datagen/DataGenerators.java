package nadiendev.perfectmobfarm.datagen;

import nadiendev.perfectmobfarm.PerfectMobFarm;
import nadiendev.perfectmobfarm.datagen.provider.ModBlockLootProvider;
import nadiendev.perfectmobfarm.datagen.provider.ModConditionalBlockLootProvider;
import nadiendev.perfectmobfarm.datagen.provider.ModBlockStateProvider;
import nadiendev.perfectmobfarm.datagen.provider.ModBlockTagProvider;
import nadiendev.perfectmobfarm.datagen.provider.ModEntityTypeTagProvider;
import nadiendev.perfectmobfarm.datagen.provider.ModItemModelProvider;
import nadiendev.perfectmobfarm.datagen.provider.ModLanguageProvider;
import nadiendev.perfectmobfarm.datagen.provider.ModRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

/**
 * Registers every data provider of the mod. {@link GatherDataEvent} is a mod bus event, so the subscriber
 * is routed to the mod bus automatically.
 * <p>
 * The data run sets the {@code perfectmobfarmrevived.datagen} system property (see build.gradle),
 * so the tiers that depend on other mods are generated too.
 */
@EventBusSubscriber(modid = PerfectMobFarm.MODID)
public class DataGenerators {

    private DataGenerators() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // Client side: blockstates, models and translations
        generator.addProvider(event.includeClient(), new ModBlockStateProvider(output, existingFileHelper));
        generator.addProvider(event.includeClient(), new ModItemModelProvider(output, existingFileHelper));
        generator.addProvider(event.includeClient(), new ModLanguageProvider(output, "en_us"));
        generator.addProvider(event.includeClient(), new ModLanguageProvider(output, "es_es"));

        // Server side: recipes, loot tables and tags
        generator.addProvider(event.includeServer(), new ModRecipeProvider(output, lookupProvider));
        generator.addProvider(event.includeServer(), ModBlockLootProvider.create(output, lookupProvider));
        generator.addProvider(event.includeServer(), new ModConditionalBlockLootProvider(output, lookupProvider));
        generator.addProvider(event.includeServer(),
                new ModBlockTagProvider(output, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(),
                new ModEntityTypeTagProvider(output, lookupProvider, existingFileHelper));
    }
}
