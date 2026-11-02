package nadiendev.perfectmobfarm.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Data stored in a linked mob shard (replaces the old NBT tags)
 *
 * @param mobName     Display name of the captured mob
 * @param mobId       Entity type id of the captured mob (e.g. minecraft:zombie)
 * @param lootTable   Location of the loot table of the captured mob
 * @param killedCount Number of kills registered by the shard
 */
public record MobShardData(String mobName, String mobId, String lootTable, int killedCount) {

    public static final Codec<MobShardData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("mob_name").forGetter(MobShardData::mobName),
            Codec.STRING.fieldOf("mob_id").forGetter(MobShardData::mobId),
            Codec.STRING.fieldOf("loot_table").forGetter(MobShardData::lootTable),
            Codec.INT.fieldOf("killed_count").forGetter(MobShardData::killedCount)
    ).apply(instance, MobShardData::new));

    public static final StreamCodec<ByteBuf, MobShardData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, MobShardData::mobName,
            ByteBufCodecs.STRING_UTF8, MobShardData::mobId,
            ByteBufCodecs.STRING_UTF8, MobShardData::lootTable,
            ByteBufCodecs.VAR_INT, MobShardData::killedCount,
            MobShardData::new);

    public MobShardData withKilledCount(int killedCount) {
        return new MobShardData(mobName, mobId, lootTable, killedCount);
    }
}
