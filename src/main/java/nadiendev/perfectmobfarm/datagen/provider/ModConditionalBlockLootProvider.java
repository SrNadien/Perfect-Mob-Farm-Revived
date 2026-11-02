package nadiendev.perfectmobfarm.datagen.provider;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import nadiendev.perfectmobfarm.PerfectMobFarm;
import nadiendev.perfectmobfarm.block.BlockManager;
import nadiendev.perfectmobfarm.block.FarmTier;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.conditions.WithConditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Block loot tables of the farms that depend on another mod. The vanilla loot table provider cannot write
 * conditions, and without the condition the game logs an error for every table whose block does not exist.
 */
public class ModConditionalBlockLootProvider implements DataProvider {

    private final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public ModConditionalBlockLootProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "loot_table");
        this.registries = registries;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return registries.thenCompose(provider -> {
            RegistryOps<JsonElement> ops = provider.createSerializationContext(JsonOps.INSTANCE);
            Codec<Optional<WithConditions<LootTable>>> codec = ConditionalOps.createConditionalCodecWithConditions(LootTable.DIRECT_CODEC);

            List<CompletableFuture<?>> saves = new ArrayList<>();
            for (FarmTier tier : FarmTier.values()) {
                String requiredMod = tier.getRequiredMod();
                if (requiredMod == null) {
                    continue;
                }

                save(cache, ops, codec, saves, requiredMod,
                        BlockManager.getBlock(tier).get(), tier.getName() + "_mob_farm");
                save(cache, ops, codec, saves, requiredMod,
                        BlockManager.getEnergyBlock(tier).get(), tier.getName() + "_energy_mob_farm");
            }
            return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
        });
    }

    /**
     * Writes the self drop loot table of one farm block, conditioned on the mod that provides its tier
     */
    private void save(CachedOutput cache, RegistryOps<JsonElement> ops, Codec<Optional<WithConditions<LootTable>>> codec,
                      List<CompletableFuture<?>> saves, String requiredMod, Block farm, String name) {
        LootTable table = LootTable.lootTable()
                .setParamSet(LootContextParamSets.BLOCK)
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(farm))
                        .when(ExplosionCondition.survivesExplosion()))
                .build();

        JsonElement json = codec.encodeStart(ops,
                Optional.of(new WithConditions<>(table, new ModLoadedCondition(requiredMod)))).getOrThrow();
        saves.add(DataProvider.saveStable(cache, json, pathProvider.json(PerfectMobFarm.id("blocks/" + name))));
    }

    @Override
    public String getName() {
        return "Conditional block loot tables";
    }
}
