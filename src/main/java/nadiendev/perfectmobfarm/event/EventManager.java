package nadiendev.perfectmobfarm.event;

import com.mojang.logging.LogUtils;
import nadiendev.perfectmobfarm.PerfectMobFarm;
import nadiendev.perfectmobfarm.config.MobListManager;
import nadiendev.perfectmobfarm.config.ServerConfigs;
import nadiendev.perfectmobfarm.item.DataComponentManager;
import nadiendev.perfectmobfarm.item.MobShard;
import nadiendev.perfectmobfarm.item.MobShardData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slf4j.Logger;

/**
 * Manager for the mod events
 */
@EventBusSubscriber(modid = PerfectMobFarm.MODID)
public class EventManager {
    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Called when a player right click on a mob with a mob shard
     *
     * @param event
     */
    @SubscribeEvent
    public static void clickMobShard(PlayerInteractEvent.EntityInteract event) {
        Player pPlayer = event.getEntity();
        if (!pPlayer.level().isClientSide()) {
            if (event.getItemStack().getItem() instanceof MobShard && event.getTarget() instanceof Mob mob) {
                if (!MobListManager.canCapture(mob.getType())) {
                    pPlayer.sendSystemMessage(Component.literal("You can't capture this mob"));
                    return;
                }
                ItemStack shardStack = event.getItemStack();
                MobShardData data = shardStack.get(DataComponentManager.MOB_SHARD_DATA.get());
                if (data == null) {
                    shardStack.set(DataComponentManager.MOB_SHARD_DATA.get(), new MobShardData(
                            mob.getName().getString(),
                            EntityType.getKey(mob.getType()).toString(),
                            mob.getLootTable().location().toString(),
                            0));
                    pPlayer.sendSystemMessage(Component.literal(mob.getDisplayName().getString() + " captured !"));
                } else {
                    pPlayer.sendSystemMessage(Component.literal("You already captured a " + data.mobName()));
                }
            }

        }
    }

    /**
     * Called when a player kill a mob. It will check if the player has a mob shard
     * and increment the killed count
     *
     * @param event
     */
    @SubscribeEvent
    public static void onPlayerKillMob(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player && !player.level().isClientSide()) {
            String killedMobId = EntityType.getKey(event.getEntity().getType()).toString();
            for (ItemStack is : player.getInventory().items) {
                if (is.getItem() instanceof MobShard) {
                    MobShardData data = is.get(DataComponentManager.MOB_SHARD_DATA.get());
                    if (data != null && data.mobId().equals(killedMobId)) {
                        int killedCount = data.killedCount();
                        if (killedCount < MobShard.getKillsNeeded(is)) {
                            is.set(DataComponentManager.MOB_SHARD_DATA.get(), data.withKilledCount(killedCount + 1));

                            // Break when one mob shard has been incremented to avoid incrementation of every mob shard
                            break;
                        }
                    }
                }
            }
        }
    }
}
