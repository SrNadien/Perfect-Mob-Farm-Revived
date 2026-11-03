package nadiendev.perfectmobfarm.renderer;

import com.mojang.serialization.MapCodec;
import nadiendev.perfectmobfarm.item.MobShard;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Item model condition (registered client side as {@code perfectmobfarmrevived:completed}): true when the shard has
 * killed enough mobs, so the item definition of the shards can switch to their completed model
 */
public record ShardCompletedProperty() implements ConditionalItemModelProperty {

    public static final MapCodec<ShardCompletedProperty> MAP_CODEC = MapCodec.unit(new ShardCompletedProperty());

    @Override
    public boolean get(ItemStack pStack, @Nullable ClientLevel pLevel, @Nullable LivingEntity pOwner, int pSeed,
                       ItemDisplayContext pDisplayContext) {
        return MobShard.isComplete(pStack);
    }

    @Override
    public MapCodec<ShardCompletedProperty> type() {
        return MAP_CODEC;
    }
}
