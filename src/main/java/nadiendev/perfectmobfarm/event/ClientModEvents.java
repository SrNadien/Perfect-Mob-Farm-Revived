package nadiendev.perfectmobfarm.event;

import nadiendev.perfectmobfarm.PerfectMobFarm;
import nadiendev.perfectmobfarm.block.entity.BlockEntityManager;
import nadiendev.perfectmobfarm.item.ItemManager;
import nadiendev.perfectmobfarm.item.MobShard;
import nadiendev.perfectmobfarm.renderer.MobFarmRenderer;
import nadiendev.perfectmobfarm.screen.MenuManager;
import nadiendev.perfectmobfarm.screen.MobFarmScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client side mod bus events: screens, block entity renderers and item properties
 */
@Mod.EventBusSubscriber(modid = PerfectMobFarm.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEvents {

    /** 1.0 when the shard has killed enough mobs, 0.0 otherwise */
    private static final ClampedItemPropertyFunction COMPLETED = (pStack, pLevel, pEntity, pSeed) -> {
        return MobShard.isComplete(pStack) ? 1.0F : 0.0F;
    };

    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityManager.MOB_FARM.get(), MobFarmRenderer::new);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(MenuManager.MOB_FARM_MENU.get(), MobFarmScreen::new);
            ItemProperties.register(ItemManager.MOB_SHARD.get(), PerfectMobFarm.id("completed"), COMPLETED);
            ItemProperties.register(ItemManager.ADVANCED_MOB_SHARD.get(), PerfectMobFarm.id("completed"), COMPLETED);
            ItemProperties.register(ItemManager.ULTIMATE_MOB_SHARD.get(), PerfectMobFarm.id("completed"), COMPLETED);
        });
    }
}
