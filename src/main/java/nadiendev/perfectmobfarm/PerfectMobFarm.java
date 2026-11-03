package nadiendev.perfectmobfarm;

import com.mojang.logging.LogUtils;
import nadiendev.perfectmobfarm.block.BlockManager;
import nadiendev.perfectmobfarm.block.entity.BlockEntityManager;
import nadiendev.perfectmobfarm.config.MobListManager;
import nadiendev.perfectmobfarm.config.ServerConfigs;
import nadiendev.perfectmobfarm.item.CreativeTabManager;
import nadiendev.perfectmobfarm.item.ItemManager;
import nadiendev.perfectmobfarm.screen.MenuManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(PerfectMobFarm.MODID)
public class PerfectMobFarm {

    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final String MODID = "perfectmobfarmrevived";

    /**
     * Build a ResourceLocation inside the mod namespace
     *
     * @param path Path of the resource
     * @return The ResourceLocation
     */
    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }

    public PerfectMobFarm() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the configs
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfigs.SPECS, "perfectmobfarmrevived-server.toml");

        ItemManager.register(modEventBus);
        BlockManager.register(modEventBus);
        BlockEntityManager.register(modEventBus);
        CreativeTabManager.register(modEventBus);
        MenuManager.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
    }

    /**
     * Create and read the mob blacklist and whitelist files
     */
    private void commonSetup(final FMLCommonSetupEvent event) {
        MobListManager.load();
    }
}
