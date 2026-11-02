package nadiendev.perfectmobfarm.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import nadiendev.perfectmobfarm.PerfectMobFarm;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Blacklist and whitelist of the mobs that can be captured by a mob shard.
 * <p>
 * Each list is the union of a JSON file ({@code config/perfectmobfarmrevived/blacklist.json} and
 * {@code whitelist.json}) and an entity type tag ({@code perfectmobfarmrevived:blacklist} and
 * {@code perfectmobfarmrevived:whitelist}). A mob can be captured when it is not blacklisted and
 * the whitelist is either empty or contains it.
 */
public class MobListManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String DIRECTORY = PerfectMobFarm.MODID;
    private static final String BLACKLIST_FILE = "blacklist.json";
    private static final String WHITELIST_FILE = "whitelist.json";
    private static final String MOBS_KEY = "mobs";

    public static final TagKey<EntityType<?>> BLACKLIST_TAG = TagKey.create(Registries.ENTITY_TYPE, PerfectMobFarm.id("blacklist"));
    public static final TagKey<EntityType<?>> WHITELIST_TAG = TagKey.create(Registries.ENTITY_TYPE, PerfectMobFarm.id("whitelist"));

    private static final String[] DEFAULT_BLACKLIST = {"minecraft:ender_dragon", "minecraft:wither", "minecraft:elder_guardian"};

    private static volatile boolean loaded = false;
    private static Set<ResourceLocation> jsonBlacklist = Set.of();
    private static Set<ResourceLocation> jsonWhitelist = Set.of();

    private MobListManager() {
    }

    /**
     * Create the default files if they do not exist and read both of them.
     * Called at common setup; also called lazily by {@link #canCapture(EntityType)}.
     */
    public static synchronized void load() {
        Path directory = FMLPaths.CONFIGDIR.get().resolve(DIRECTORY);
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            LOGGER.warn("Could not create the mob list directory {}", directory, e);
        }
        Path blacklistPath = directory.resolve(BLACKLIST_FILE);
        Path whitelistPath = directory.resolve(WHITELIST_FILE);
        createDefaultFile(blacklistPath, DEFAULT_BLACKLIST);
        createDefaultFile(whitelistPath, new String[0]);
        jsonBlacklist = readFile(blacklistPath);
        jsonWhitelist = readFile(whitelistPath);
        loaded = true;
    }

    /**
     * @param type Type of the mob to capture
     * @return True if a mob shard can capture this type, according to the blacklist and the whitelist
     */
    public static boolean canCapture(EntityType<?> type) {
        if (!loaded) {
            load();
        }
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (jsonBlacklist.contains(id) || type.is(BLACKLIST_TAG)) {
            return false;
        }
        boolean whitelistEmpty = jsonWhitelist.isEmpty() && !hasEntries(WHITELIST_TAG);
        return whitelistEmpty || jsonWhitelist.contains(id) || type.is(WHITELIST_TAG);
    }

    /**
     * @return True if the tag exists and has at least one entry. A missing or empty tag is ignored.
     */
    private static boolean hasEntries(TagKey<EntityType<?>> tag) {
        return BuiltInRegistries.ENTITY_TYPE.getTag(tag).map(HolderSet.Named::size).orElse(0) > 0;
    }

    private static void createDefaultFile(Path path, String[] mobs) {
        if (Files.exists(path)) {
            return;
        }
        JsonArray array = new JsonArray();
        for (String mob : mobs) {
            array.add(mob);
        }
        JsonObject root = new JsonObject();
        root.add(MOBS_KEY, array);
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            GSON.toJson(root, writer);
        } catch (IOException e) {
            LOGGER.warn("Could not create the mob list file {}", path, e);
        }
    }

    /**
     * Read the {"mobs": [...]} file. An invalid file is logged and treated as empty.
     */
    private static Set<ResourceLocation> readFile(Path path) {
        Set<ResourceLocation> result = new HashSet<>();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonObject() || !root.getAsJsonObject().has(MOBS_KEY)
                    || !root.getAsJsonObject().get(MOBS_KEY).isJsonArray()) {
                LOGGER.warn("Invalid mob list file {}: expected {\"mobs\": [...]}. Treating it as empty.", path);
                return Set.of();
            }
            for (JsonElement element : root.getAsJsonObject().getAsJsonArray(MOBS_KEY)) {
                ResourceLocation id = element.isJsonPrimitive() ? ResourceLocation.tryParse(element.getAsString()) : null;
                if (id == null) {
                    LOGGER.warn("Invalid mob id {} in {}, ignoring it", element, path);
                } else {
                    result.add(id);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Could not read the mob list file {}. Treating it as empty.", path, e);
            return Set.of();
        }
        return result;
    }
}
