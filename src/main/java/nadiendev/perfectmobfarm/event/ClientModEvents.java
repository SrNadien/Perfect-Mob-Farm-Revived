package nadiendev.perfectmobfarm.event;

import nadiendev.perfectmobfarm.PerfectMobFarm;
import nadiendev.perfectmobfarm.block.entity.BlockEntityManager;
import nadiendev.perfectmobfarm.renderer.MobFarmRenderer;
import nadiendev.perfectmobfarm.renderer.ShardCompletedProperty;
import nadiendev.perfectmobfarm.screen.MenuManager;
import nadiendev.perfectmobfarm.screen.MobFarmScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Client side mod bus events: screens, block entity renderers and item model properties
 */
@EventBusSubscriber(modid = PerfectMobFarm.MODID, value = Dist.CLIENT)
public class ClientModEvents {

    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(MenuManager.MOB_FARM_MENU.get(), MobFarmScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityManager.MOB_FARM.get(), MobFarmRenderer::new);
    }

    /**
     * The "completed" condition of the item definitions of the mob shards: true when the shard has killed enough mobs
     */
    @SubscribeEvent
    public static void registerItemModelProperties(RegisterConditionalItemModelPropertyEvent event) {
        event.register(PerfectMobFarm.id("completed"), ShardCompletedProperty.MAP_CODEC);
    }
}
