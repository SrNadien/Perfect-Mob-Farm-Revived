package nadiendev.perfectmobfarm.event;

import com.mojang.logging.LogUtils;
import nadiendev.perfectmobfarm.PerfectMobFarm;
import nadiendev.perfectmobfarm.config.MobListManager;
import nadiendev.perfectmobfarm.config.ServerConfigs;
import nadiendev.perfectmobfarm.item.MobShard;
import nadiendev.perfectmobfarm.item.MobShardData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Manager for the mod events
 */
@Mod.EventBusSubscriber(modid = PerfectMobFarm.MODID)
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
                MobShardData data = MobShardData.get(shardStack);
                if (data == null) {
                    new MobShardData(
                            mob.getName().getString(),
                            EntityType.getKey(mob.getType()).toString(),
                            mob.getLootTable().toString(),
                            0).save(shardStack);
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
                    MobShardData data = MobShardData.get(is);
                    if (data != null && data.mobId().equals(killedMobId)) {
                        int killedCount = data.killedCount();
                        if (killedCount < MobShard.getKillsNeeded(is)) {
                            data.withKilledCount(killedCount + 1).save(is);

                            // Break when one mob shard has been incremented to avoid incrementation of every mob shard
                            break;
                        }
                    }
                }
            }
        }
    }
}
