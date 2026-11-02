package nadiendev.perfectmobfarm.screen;

import nadiendev.perfectmobfarm.PerfectMobFarm;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Manager for the mod menus
 */
public class MenuManager {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, PerfectMobFarm.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<MobFarmMenu>> MOB_FARM_MENU =
            MENUS.register("mob_farm_menu", () -> IMenuTypeExtension.create(MobFarmMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
