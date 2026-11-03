package nadiendev.perfectmobfarm.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import nadiendev.perfectmobfarm.PerfectMobFarm;
import nadiendev.perfectmobfarm.block.BlockManager;
import nadiendev.perfectmobfarm.block.FarmTier;
import nadiendev.perfectmobfarm.config.ServerConfigs;
import nadiendev.perfectmobfarm.item.ItemManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * JEI integration. Optional: the class is only loaded by JEI itself, so the mod works without it.
 * Every reference to the JEI API must stay inside this package.
 * <p>
 * Adds description pages for the mob shards and for every mob farm.
 */
@JeiPlugin
public class PerfectMobFarmJeiPlugin implements IModPlugin {

    private static final DecimalFormat SECONDS_FORMAT = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    @Override
    public ResourceLocation getPluginUid() {
        return PerfectMobFarm.id("jei_plugin");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registerShards(registration);
        registerUpgrades(registration);
        registerFarms(registration);
    }

    private static void registerShards(IRecipeRegistration registration) {
        int killNeeded = ServerConfigs.getSafe(ServerConfigs.MOB_SHARD_KILL_NEEDED);

        registration.addIngredientInfo(ItemManager.MOB_SHARD.get(),
                Component.translatable("jei.perfectmobfarmrevived.shard.link", killNeeded),
                Component.translatable("jei.perfectmobfarmrevived.shard.use"),
                Component.translatable("jei.perfectmobfarmrevived.shard.durability.normal",
                        ServerConfigs.getSafe(ServerConfigs.MOB_SHARD_DURABILITY)));

        registration.addIngredientInfo(ItemManager.ADVANCED_MOB_SHARD.get(),
                Component.translatable("jei.perfectmobfarmrevived.shard.link", killNeeded),
                Component.translatable("jei.perfectmobfarmrevived.shard.use"),
                Component.translatable("jei.perfectmobfarmrevived.shard.durability.advanced"));

        int ultimateKillNeeded = ServerConfigs.getSafe(ServerConfigs.ULTIMATE_MOB_SHARD_KILL_NEEDED);
        registration.addIngredientInfo(ItemManager.ULTIMATE_MOB_SHARD.get(),
                ultimateKillNeeded > 0
                        ? Component.translatable("jei.perfectmobfarmrevived.shard.link", ultimateKillNeeded)
                        : Component.translatable("jei.perfectmobfarmrevived.shard.link.instant"),
                Component.translatable("jei.perfectmobfarmrevived.shard.use"),
                Component.translatable("jei.perfectmobfarmrevived.shard.durability.ultimate"));
    }

    private static void registerFarms(IRecipeRegistration registration) {
        for (FarmTier tier : FarmTier.values()) {
            registration.addIngredientInfo(BlockManager.getBlock(tier).get(), farmLines(tier, false));
            registration.addIngredientInfo(BlockManager.getEnergyBlock(tier).get(), farmLines(tier, true));
        }
    }

    /**
     * @param tier   Tier of the farm
     * @param energy True for the energy variant
     * @return The description paragraphs of the farm page, with the accepted upgrades before the stats
     */
    private static Component[] farmLines(FarmTier tier, boolean energy) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("jei.perfectmobfarmrevived.farm.use"));
        lines.add(Component.translatable("jei.perfectmobfarmrevived.farm.container"));
        lines.add(Component.translatableWithFallback("jei.perfectmobfarmrevived.farm.lever",
                "Place a lever on the farm: it only works while the lever is ON"));
        if (energy) {
            lines.add(Component.translatableWithFallback("jei.perfectmobfarmrevived.farm.energy",
                    "Needs energy to work: it uses %s FE/t and holds up to %s FE",
                    tier.getEnergyPerTick(), tier.getEnergyCapacity()));
        }
        lines.add(Component.translatableWithFallback("jei.perfectmobfarmrevived.farm.mob_slot_upgrade",
                "Accepts a Mob Slot Upgrade (second shard)"));
        if (tier.supportsSpeedUpgrade()) {
            lines.add(Component.translatableWithFallback("jei.perfectmobfarmrevived.farm.speed_upgrade",
                    "Accepts up to 4 Speed Upgrades"));
        }
        lines.add(Component.translatable("jei.perfectmobfarmrevived.farm.stats",
                formatSeconds(tier.getCooldown()), tier.getLootingLevel()));
        return lines.toArray(new Component[0]);
    }

    private static void registerUpgrades(IRecipeRegistration registration) {
        registration.addIngredientInfo(ItemManager.SPEED_UPGRADE.get(),
                Component.translatable("item.perfectmobfarmrevived.speed_upgrade.tooltip",
                        ServerConfigs.getSafe(ServerConfigs.SPEED_UPGRADE_REDUCTION_PERCENT)),
                Component.translatableWithFallback("jei.perfectmobfarmrevived.upgrade.install",
                        "Place it in the upgrade slot of the mob farm screen."));

        registration.addIngredientInfo(ItemManager.MOB_SLOT_UPGRADE.get(),
                Component.translatable("item.perfectmobfarmrevived.mob_slot_upgrade.tooltip"),
                Component.translatableWithFallback("jei.perfectmobfarmrevived.upgrade.install",
                        "Place it in the upgrade slot of the mob farm screen."),
                Component.translatableWithFallback("jei.perfectmobfarmrevived.upgrade.mob_slot_lock",
                        "It can not be taken out while the second shard slot holds a shard."));
    }

    /**
     * @param ticks Duration in ticks
     * @return The duration in seconds, with up to two decimals
     */
    private static String formatSeconds(int ticks) {
        return SECONDS_FORMAT.format(ticks / 20.0);
    }
}
