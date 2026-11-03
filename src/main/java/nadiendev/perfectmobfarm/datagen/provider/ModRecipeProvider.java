package nadiendev.perfectmobfarm.datagen.provider;

import nadiendev.perfectmobfarm.PerfectMobFarm;
import nadiendev.perfectmobfarm.block.BlockManager;
import nadiendev.perfectmobfarm.block.FarmTier;
import nadiendev.perfectmobfarm.item.ItemManager;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

/**
 * Generates the recipes of the mod: the three mob shards, the two farm upgrades, every mob farm and every energy farm. Each farm is
 * crafted from the farm of the previous tier, and each energy farm from the normal farm of its tier.
 */
public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> recipeOutput) {
        shardRecipes(recipeOutput);
        upgradeRecipes(recipeOutput);
        farmRecipes(recipeOutput);
    }

    private void upgradeRecipes(Consumer<FinishedRecipe> recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemManager.SPEED_UPGRADE.get())
                .pattern("#R#")
                .pattern("RDR")
                .pattern("#R#")
                .define('#', Items.GOLD_INGOT)
                .define('R', Items.REDSTONE)
                .define('D', Items.DIAMOND)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(recipeOutput, PerfectMobFarm.id("speed_upgrade"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemManager.MOB_SLOT_UPGRADE.get())
                .pattern("#E#")
                .pattern("EDE")
                .pattern("#E#")
                .define('#', Items.ENDER_EYE)
                .define('E', Items.EMERALD)
                .define('D', Items.DIAMOND_BLOCK)
                .unlockedBy("has_diamond_block", has(Items.DIAMOND_BLOCK))
                .save(recipeOutput, PerfectMobFarm.id("mob_slot_upgrade"));
    }

    private void shardRecipes(Consumer<FinishedRecipe> recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemManager.MOB_SHARD.get())
                .pattern(" # ")
                .pattern("#D#")
                .pattern(" # ")
                .define('#', Items.BLAZE_POWDER)
                .define('D', Items.DIAMOND)
                .unlockedBy("has_blaze_powder", has(Items.BLAZE_POWDER))
                .save(recipeOutput, PerfectMobFarm.id("mob_shard"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemManager.ADVANCED_MOB_SHARD.get())
                .pattern(" # ")
                .pattern("RDR")
                .pattern(" # ")
                .define('#', Items.ENDER_EYE)
                .define('D', Items.DIAMOND)
                .define('R', ItemManager.MOB_SHARD.get())
                .unlockedBy("has_mob_shard", has(ItemManager.MOB_SHARD.get()))
                .save(recipeOutput, PerfectMobFarm.id("advanced_mob_shard"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemManager.ULTIMATE_MOB_SHARD.get())
                .pattern(" # ")
                .pattern("RDR")
                .pattern(" # ")
                .define('#', Items.NETHER_STAR)
                .define('D', Items.NETHERITE_INGOT)
                .define('R', ItemManager.ADVANCED_MOB_SHARD.get())
                .unlockedBy("has_advanced_mob_shard", has(ItemManager.ADVANCED_MOB_SHARD.get()))
                .save(recipeOutput, PerfectMobFarm.id("ultimate_mob_shard"));
    }

    private void farmRecipes(Consumer<FinishedRecipe> recipeOutput) {
        // Iron: the base farm is crafted from a plain mob shard
        farm(recipeOutput, FarmTier.IRON, ItemManager.MOB_SHARD.get(),
                Items.IRON_BLOCK, Items.ROTTEN_FLESH, Items.BONE);
        farm(recipeOutput, FarmTier.GOLD, farmItem(FarmTier.IRON),
                Items.GOLD_BLOCK, Items.SLIME_BALL, Items.ENDER_PEARL);
        farm(recipeOutput, FarmTier.DIAMOND, farmItem(FarmTier.GOLD),
                Items.DIAMOND_BLOCK, Items.BLAZE_ROD, Items.GHAST_TEAR);
        farm(recipeOutput, FarmTier.EMERALD, farmItem(FarmTier.DIAMOND),
                Items.EMERALD, Items.GUNPOWDER, Items.MAGMA_CREAM);

        // Netherite has a different layout: a nether star replaces the top middle ingredient
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, farmItem(FarmTier.NETHERITE))
                .pattern("#V#")
                .pattern("BPB")
                .pattern("#R#")
                .define('#', Items.NETHERITE_INGOT)
                .define('R', Items.ENDER_EYE)
                .define('B', Items.QUARTZ)
                .define('P', farmItem(FarmTier.EMERALD))
                .define('V', Items.NETHER_STAR)
                .unlockedBy("has_previous_farm", has(farmItem(FarmTier.EMERALD)))
                .save(recipeOutput, farmId(FarmTier.NETHERITE));

        farm(recipeOutput, FarmTier.NETHER, farmItem(FarmTier.NETHERITE),
                Items.NETHERITE_INGOT, Items.BLAZE_ROD, Items.WITHER_SKELETON_SKULL);
        farm(recipeOutput, FarmTier.END, farmItem(FarmTier.NETHER),
                Items.END_CRYSTAL, Items.DRAGON_BREATH, Items.SHULKER_SHELL);
        farm(recipeOutput, FarmTier.ULTIMATE, farmItem(FarmTier.END),
                Items.NETHER_STAR, Items.DRAGON_HEAD, Items.ENCHANTED_GOLDEN_APPLE);

        for (FarmTier tier : FarmTier.values()) {
            energyFarm(recipeOutput, tier);
        }
    }

    /**
     * Energy farm: the normal farm of the same tier surrounded by four redstone blocks in a cross, {@code  R /RPR/ R }.
     * There is no recipe to go back.
     */
    private void energyFarm(Consumer<FinishedRecipe> recipeOutput, FarmTier tier) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BlockManager.getEnergyBlock(tier).get())
                .pattern(" R ")
                .pattern("RPR")
                .pattern(" R ")
                .define('R', Items.REDSTONE_BLOCK)
                .define('P', farmItem(tier))
                .unlockedBy("has_farm", has(farmItem(tier)))
                .save(recipeOutput, PerfectMobFarm.id(tier.getName() + "_energy_mob_farm"));
    }

    /**
     * Standard farm layout: {@code #R#/BPB/#R#} where P is the previous farm (or the shard).
     */
    private void farm(Consumer<FinishedRecipe> recipeOutput, FarmTier tier, ItemLike previous,
                      ItemLike corner, ItemLike vertical, ItemLike side) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, farmItem(tier))
                .pattern("#R#")
                .pattern("BPB")
                .pattern("#R#")
                .define('#', corner)
                .define('R', vertical)
                .define('B', side)
                .define('P', previous)
                .unlockedBy("has_previous_farm", has(previous))
                .save(recipeOutput, farmId(tier));
    }

    private static ItemLike farmItem(FarmTier tier) {
        return BlockManager.getBlock(tier).get();
    }

    private static ResourceLocation farmId(FarmTier tier) {
        return PerfectMobFarm.id(tier.getName() + "_mob_farm");
    }
}
