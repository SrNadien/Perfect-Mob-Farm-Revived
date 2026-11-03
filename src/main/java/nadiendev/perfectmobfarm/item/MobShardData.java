package nadiendev.perfectmobfarm.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Data stored in the NBT tag of a linked mob shard
 *
 * @param mobName     Display name of the captured mob
 * @param mobId       Entity type id of the captured mob (e.g. minecraft:zombie)
 * @param lootTable   Location of the loot table of the captured mob
 * @param killedCount Number of kills registered by the shard
 */
public record MobShardData(String mobName, String mobId, String lootTable, int killedCount) {

    private static final String MOB_NAME_KEY = "mobName";
    private static final String MOB_ID_KEY = "mobId";
    private static final String LOOT_TABLE_KEY = "lootTable";
    private static final String KILLED_COUNT_KEY = "killedCount";

    /**
     * @param stack Any item stack
     * @return The data stored in the tag of the stack, or null while the stack is not linked to a mob
     */
    @Nullable
    public static MobShardData get(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(MOB_ID_KEY, Tag.TAG_STRING)) {
            return null;
        }
        return new MobShardData(tag.getString(MOB_NAME_KEY), tag.getString(MOB_ID_KEY),
                tag.getString(LOOT_TABLE_KEY), tag.getInt(KILLED_COUNT_KEY));
    }

    /**
     * Write this data in the tag of the stack, keeping the other tags
     *
     * @param stack Stack to link
     */
    public void save(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(MOB_NAME_KEY, mobName);
        tag.putString(MOB_ID_KEY, mobId);
        tag.putString(LOOT_TABLE_KEY, lootTable);
        tag.putInt(KILLED_COUNT_KEY, killedCount);
    }

    public MobShardData withKilledCount(int killedCount) {
        return new MobShardData(mobName, mobId, lootTable, killedCount);
    }
}
