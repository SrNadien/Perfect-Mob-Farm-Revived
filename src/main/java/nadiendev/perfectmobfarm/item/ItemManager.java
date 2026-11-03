package nadiendev.perfectmobfarm.item;

import nadiendev.perfectmobfarm.PerfectMobFarm;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Manager for the mod items
 */
public class ItemManager {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PerfectMobFarm.MODID);

    // The durability given here only makes the stack damageable (and defines the default values).
    // The real max damage is read from the config in MobShard#getMaxDamage.
    public static final DeferredItem<Item> MOB_SHARD = ITEMS.registerItem("mob_shard",
            MobShard::new, properties -> properties.durability(256));

    public static final DeferredItem<Item> ADVANCED_MOB_SHARD = ITEMS.registerItem("advanced_mob_shard",
            AdvancedMobShard::new, properties -> properties.durability(1024));

    public static final DeferredItem<Item> ULTIMATE_MOB_SHARD = ITEMS.registerItem("ultimate_mob_shard",
            UltimateMobShard::new, properties -> properties.durability(4096));

    public static final DeferredItem<Item> SPEED_UPGRADE = ITEMS.registerItem("speed_upgrade",
            SpeedUpgrade::new, properties -> properties.stacksTo(4));

    public static final DeferredItem<Item> MOB_SLOT_UPGRADE = ITEMS.registerItem("mob_slot_upgrade",
            MobSlotUpgrade::new, properties -> properties.stacksTo(1));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
