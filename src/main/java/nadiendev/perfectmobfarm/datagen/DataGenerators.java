package nadiendev.perfectmobfarm.datagen;

import nadiendev.perfectmobfarm.PerfectMobFarm;
import nadiendev.perfectmobfarm.datagen.provider.ModBlockLootProvider;
import nadiendev.perfectmobfarm.datagen.provider.ModBlockTagProvider;
import nadiendev.perfectmobfarm.datagen.provider.ModEntityTypeTagProvider;
import nadiendev.perfectmobfarm.datagen.provider.ModLanguageProvider;
import nadiendev.perfectmobfarm.datagen.provider.ModModelProvider;
import nadiendev.perfectmobfarm.datagen.provider.ModRecipeProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;

/**
 * Registers every data provider of the mod. {@link GatherDataEvent.Client} and {@link GatherDataEvent.Server} are
 * mod bus events, so the subscribers are routed to the mod bus automatically.
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
     * Recipes and loot tables: since 26.3 they are datapack registry entries, so they are gathered here instead of
     * being written by a provider of the server run
     */
    @SubscribeEvent
    public static void gatherRegistryEntries(GatherDataRegistryEntriesEvent event) {
        event.recipe(ModRecipeProvider::new)
                .lootTable(new LootTableProvider.SubProviderEntry(ModBlockLootProvider::new, LootContextParamSets.BLOCK));
    }

    /**
     * Server side: tags
     */
    @SubscribeEvent
    public static void gatherServerData(GatherDataEvent.Server event) {
        event.createProvider(ModBlockTagProvider::new);
        event.createProvider(ModEntityTypeTagProvider::new);
    }
}
