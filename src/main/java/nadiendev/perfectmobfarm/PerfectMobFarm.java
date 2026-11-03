package nadiendev.perfectmobfarm;

import com.mojang.logging.LogUtils;
import nadiendev.perfectmobfarm.block.BlockManager;
import nadiendev.perfectmobfarm.block.entity.BlockEntityManager;
import nadiendev.perfectmobfarm.config.MobListManager;
import nadiendev.perfectmobfarm.config.ServerConfigs;
import nadiendev.perfectmobfarm.item.CreativeTabManager;
import nadiendev.perfectmobfarm.item.DataComponentManager;
import nadiendev.perfectmobfarm.item.ItemManager;
import nadiendev.perfectmobfarm.screen.MenuManager;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import nadiendev.perfectmobfarm.block.entity.MobFarmBlockEntity;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(PerfectMobFarm.MODID)
public class PerfectMobFarm {

    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final String MODID = "perfectmobfarmrevived";

    /**
     * Build an Identifier inside the mod namespace
     *
     * @param path Path of the resource
     * @return The Identifier
     */
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    public PerfectMobFarm(IEventBus modEventBus, ModContainer modContainer) {

        // Register the configs
        modContainer.registerConfig(ModConfig.Type.SYNCED, ServerConfigs.SPECS, "perfectmobfarmrevived-server.toml");

        ItemManager.register(modEventBus);
        BlockManager.register(modEventBus);
        BlockEntityManager.register(modEventBus);
        DataComponentManager.register(modEventBus);
        CreativeTabManager.register(modEventBus);
        MenuManager.register(modEventBus);

        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::commonSetup);
    }

    /**
     * Create and read the mob blacklist and whitelist files
     */
    private void commonSetup(final FMLCommonSetupEvent event) {
        MobListManager.load();
    }

    /**
     * Expose the item handler of the mob farm (the shard slot) and, for the energy farms, the energy storage
     * (every side, receive only) to the other blocks
     */
    private void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, BlockEntityManager.MOB_FARM.get(),
                (blockEntity, side) -> blockEntity.getItemHandler());
        // The normal farms return null, so they expose no energy capability
        event.registerBlockEntity(Capabilities.Energy.BLOCK, BlockEntityManager.MOB_FARM.get(),
                (blockEntity, side) -> blockEntity.getEnergyStorage());
    }
}
