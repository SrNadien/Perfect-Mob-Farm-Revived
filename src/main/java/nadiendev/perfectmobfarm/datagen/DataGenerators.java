package nadiendev.perfectmobfarm.datagen;

import nadiendev.perfectmobfarm.PerfectMobFarm;
import nadiendev.perfectmobfarm.datagen.provider.ModBlockLootProvider;
import nadiendev.perfectmobfarm.datagen.provider.ModBlockTagProvider;
import nadiendev.perfectmobfarm.datagen.provider.ModConditionalBlockLootProvider;
import nadiendev.perfectmobfarm.datagen.provider.ModEntityTypeTagProvider;
import nadiendev.perfectmobfarm.datagen.provider.ModLanguageProvider;
import nadiendev.perfectmobfarm.datagen.provider.ModModelProvider;
import nadiendev.perfectmobfarm.datagen.provider.ModRecipeProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * Registers every data provider of the mod. {@link GatherDataEvent.Client} and {@link GatherDataEvent.Server} are
 * mod bus events, so the subscribers are routed to the mod bus automatically.
 * <p>
 * The data run sets the {@code perfectmobfarmrevived.datagen} system property (see build.gradle),
 * so the tiers that depend on other mods are generated too.
 */
@EventBusSubscriber(modid = PerfectMobFarm.MODID)
public class DataGenerators {

    private DataGenerators() {
    }

    /**
     * Client side: blockstates, models, item definitions and translations
     */
    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        event.createProvider(ModModelProvider::new);
        event.addProvider(new ModLanguageProvider(event.getGenerator().getPackOutput(), "en_us"));
        event.addProvider(new ModLanguageProvider(event.getGenerator().getPackOutput(), "es_es"));
    }

    /**
     * Server side: recipes, loot tables and tags
     */
    @SubscribeEvent
    public static void gatherServerData(GatherDataEvent.Server event) {
        event.createProvider(ModRecipeProvider.Runner::new);
        event.createProvider(ModBlockLootProvider::create);
        event.createProvider(ModConditionalBlockLootProvider::new);
        event.createProvider(ModBlockTagProvider::new);
        event.createProvider(ModEntityTypeTagProvider::new);
    }
}
