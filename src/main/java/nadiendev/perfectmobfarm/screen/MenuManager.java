package nadiendev.perfectmobfarm.screen;

import nadiendev.perfectmobfarm.PerfectMobFarm;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Manager for the mod menus
 */
public class MenuManager {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, PerfectMobFarm.MODID);

    public static final RegistryObject<MenuType<MobFarmMenu>> MOB_FARM_MENU =
            MENUS.register("mob_farm_menu", () -> IForgeMenuType.create(MobFarmMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
