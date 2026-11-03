package nadiendev.perfectmobfarm.item;

import nadiendev.perfectmobfarm.PerfectMobFarm;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Manager for the mod items
 */
public class ItemManager {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PerfectMobFarm.MODID);

    // The durability given here only makes the stack damageable (and defines the default values).
    // The real max damage is read from the config in MobShard#getMaxDamage.
    public static final RegistryObject<Item> MOB_SHARD = ITEMS.register("mob_shard",
            () -> new MobShard(new Item.Properties().durability(256)));

    public static final RegistryObject<Item> ADVANCED_MOB_SHARD = ITEMS.register("advanced_mob_shard",
            () -> new AdvancedMobShard(new Item.Properties().durability(1024)));

    public static final RegistryObject<Item> ULTIMATE_MOB_SHARD = ITEMS.register("ultimate_mob_shard",
            () -> new UltimateMobShard(new Item.Properties().durability(4096)));

    public static final RegistryObject<Item> SPEED_UPGRADE = ITEMS.register("speed_upgrade",
            () -> new SpeedUpgrade(new Item.Properties().stacksTo(4)));

    public static final RegistryObject<Item> MOB_SLOT_UPGRADE = ITEMS.register("mob_slot_upgrade",
            () -> new MobSlotUpgrade(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
