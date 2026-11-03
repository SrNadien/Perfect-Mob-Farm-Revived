package nadiendev.perfectmobfarm.datagen.provider;

import nadiendev.perfectmobfarm.PerfectMobFarm;
import nadiendev.perfectmobfarm.block.BlockManager;
import nadiendev.perfectmobfarm.block.FarmTier;
import nadiendev.perfectmobfarm.item.ItemManager;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.Locale;

/**
 * Generates the translations of the mod. One instance per language: {@code en_us} (the source of truth)
 * and {@code es_es}.
 */
public class ModLanguageProvider extends LanguageProvider {

    private static final String MODID = PerfectMobFarm.MODID;

    private final boolean spanish;

    /**
     * @param output Pack output
     * @param locale Language code, {@code en_us} or {@code es_es}
     */
    public ModLanguageProvider(PackOutput output, String locale) {
        super(output, MODID, locale);
        this.spanish = locale.equals("es_es");
    }

    /**
     * @return The Spanish text when generating {@code es_es}, the English one otherwise
     */
    private String tr(String english, String spanishText) {
        return spanish ? spanishText : english;
    }

    @Override
    protected void addTranslations() {
        addItems();
        addBlocks();
        addUpgradeTooltips();

        add("itemGroup." + MODID + ".main", "Perfect Mob Farm");

        add("gui." + MODID + ".inactive", tr("Inactive", "Inactiva"));
        add("gui." + MODID + ".disabled_redstone", tr("Disabled", "Desactivada"));
        add("gui." + MODID + ".no_energy", tr("No energy", "Sin energía"));
        add("gui." + MODID + ".energy", tr("%s / %s FE", "%s / %s FE"));
        add("gui." + MODID + ".second_shard_slot", tr("Second mob shard slot", "Segunda ranura de fragmento de mob"));
        add("gui." + MODID + ".speed_upgrade_slot", tr("Speed Upgrade (up to 4)", "Mejora de velocidad (hasta 4)"));
        add("gui." + MODID + ".mob_slot_upgrade_slot", tr("Mob Slot Upgrade", "Mejora de ranura de mob"));
        add("tooltip." + MODID + ".filled_with_energy", tr("Filled with energy", "Cargada de energía"));

        addJade();
        addJei();
    }

    private void addItems() {
        addItem(ItemManager.MOB_SHARD, tr("Mob Shard", "Fragmento de mob"));
        addItem(ItemManager.ADVANCED_MOB_SHARD, tr("Advanced Mob Shard", "Fragmento de mob avanzado"));
        addItem(ItemManager.ULTIMATE_MOB_SHARD, tr("Ultimate Mob Shard", "Fragmento de mob definitivo"));
        addItem(ItemManager.SPEED_UPGRADE, tr("Speed Upgrade", "Mejora de velocidad"));
        addItem(ItemManager.MOB_SLOT_UPGRADE, tr("Mob Slot Upgrade", "Mejora de ranura de mob"));
    }

    /**
     * Tooltips of the upgrade items. The speed one receives the cooldown reduction percent as its argument
     */
    private void addUpgradeTooltips() {
        add("item." + MODID + ".speed_upgrade.tooltip", tr(
                "Valid for Iron, Gold, Diamond and Emerald farms. Each one lowers the cooldown by %s%%.",
                "Válida para granjas de hierro, oro, diamante y esmeralda. Cada una reduce el tiempo de espera un %s%%."));
        add("item." + MODID + ".mob_slot_upgrade.tooltip", tr(
                "Adds a second mob shard slot to a mob farm.",
                "Añade una segunda ranura de fragmento de mob a una granja de mobs."));
    }

    private void addBlocks() {
        for (FarmTier tier : FarmTier.values()) {
            addBlock(BlockManager.getBlock(tier), tr(capitalize(tier.getName()) + " Mob Farm", "Granja de mobs " + spanishTier(tier)));
            addBlock(BlockManager.getEnergyBlock(tier), tr(capitalize(tier.getName()) + " Energy Mob Farm",
                    "Granja de mobs " + spanishTier(tier) + " con energía"));
        }
    }

    private void addJade() {
        add("config.jade.plugin_" + MODID, "Perfect Mob Farm Revived");
        add("config.jade.plugin_" + MODID + ".farm_info", tr("Mob Farm Info", "Información de la granja de mobs"));
        add("jade." + MODID + ".mob", tr("Mob: %s", "Mob: %s"));
        add("jade." + MODID + ".no_mob", tr("No mob captured", "Ningún mob capturado"));
        add("jade." + MODID + ".state", tr("State: %s", "Estado: %s"));
        add("jade." + MODID + ".state.active", tr("Active", "Activa"));
        add("jade." + MODID + ".state.inactive", tr("Inactive", "Inactiva"));
        add("jade." + MODID + ".state.no_container", tr("No container", "Sin contenedor"));
        add("jade." + MODID + ".state.disabled_redstone", tr("Disabled", "Desactivada"));
        add("jade." + MODID + ".state.no_energy", tr("No energy", "Sin energía"));
        add("jade." + MODID + ".speed_upgrades", tr("Speed upgrades: %s", "Mejoras de velocidad: %s"));
        add("jade." + MODID + ".mob_slot_upgrade", tr("Mob Slot Upgrade installed", "Mejora de ranura de mob instalada"));
    }

    private void addJei() {
        add("jei." + MODID + ".shard.link", tr(
                "Right click a mob with the shard to link it to that mob type, then kill %s mobs of that type with the shard in your inventory to complete it.",
                "Haz clic derecho sobre un mob con el fragmento para vincularlo a ese tipo de mob y luego mata %s mobs de ese tipo con el fragmento en tu inventario para completarlo."));
        add("jei." + MODID + ".shard.link.instant", tr(
                "Right click a mob with the shard to link it to that mob type. That is enough to complete it.",
                "Haz clic derecho sobre un mob con el fragmento para vincularlo a ese tipo de mob. Con eso basta para completarlo."));
        add("jei." + MODID + ".shard.use", tr(
                "Put a completed shard in a Mob Farm to generate the loot of the linked mob.",
                "Coloca un fragmento completado en una granja de mobs para generar el botín del mob vinculado."));
        add("jei." + MODID + ".shard.durability.normal", tr(
                "Loses 1 durability each time the farm generates loot (%s uses). It only becomes unbreakable if enabled in the config.",
                "Pierde 1 de durabilidad cada vez que la granja genera botín (%s usos). Solo se vuelve irrompible si se activa en la configuración."));
        add("jei." + MODID + ".shard.durability.advanced", tr(
                "Always unbreakable.",
                "Siempre irrompible."));
        add("jei." + MODID + ".shard.durability.ultimate", tr(
                "Unbreakable by default (can be changed in the config).",
                "Irrompible por defecto (se puede cambiar en la configuración)."));
        add("jei." + MODID + ".farm.use", tr(
                "Right click the farm to open it and insert a completed Mob Shard.",
                "Haz clic derecho sobre la granja para abrirla e insertar un fragmento de mob completado."));
        add("jei." + MODID + ".farm.container", tr(
                "Place a container (chest, hopper...) directly below the farm; the loot is generated into it.",
                "Coloca un contenedor (cofre, tolva...) justo debajo de la granja; el botín se genera dentro de él."));
        add("jei." + MODID + ".farm.stats", tr(
                "Generates loot every %s seconds with Looting %s.",
                "Genera botín cada %s segundos con Botín %s."));
        add("jei." + MODID + ".farm.lever", tr(
                "Place a lever on the farm: it only works while the lever is ON",
                "Coloca una palanca en la granja: solo funciona mientras la palanca esté activada"));
        add("jei." + MODID + ".farm.energy", tr(
                "Needs energy to work: it uses %s FE/t and holds up to %s FE",
                "Necesita energía para funcionar: consume %s FE/t y almacena hasta %s FE"));
        add("jei." + MODID + ".farm.mob_slot_upgrade", tr(
                "Accepts a Mob Slot Upgrade (second shard)",
                "Acepta una Mejora de ranura de mob (segundo fragmento)"));
        add("jei." + MODID + ".farm.speed_upgrade", tr(
                "Accepts up to 4 Speed Upgrades",
                "Acepta hasta 4 Mejoras de velocidad"));
        add("jei." + MODID + ".upgrade.install", tr(
                "Place it in the upgrade slot of the mob farm screen.",
                "Colócala en la ranura de mejora de la pantalla de la granja de mobs."));
        add("jei." + MODID + ".upgrade.mob_slot_lock", tr(
                "It can not be taken out while the second shard slot holds a shard.",
                "No se puede sacar mientras la segunda ranura de fragmento contenga un fragmento."));
    }

    /**
     * @return The tier name with its first letter in upper case, used as the English display name
     */
    private static String capitalize(String name) {
        return name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
    }

    /**
     * @return The tier as it follows "Granja de mobs" in Spanish
     */
    private static String spanishTier(FarmTier tier) {
        return switch (tier) {
            case IRON -> "de hierro";
            case GOLD -> "de oro";
            case DIAMOND -> "de diamante";
            case EMERALD -> "de esmeralda";
            case NETHERITE -> "de netherita";
            case NETHER -> "del Nether";
            case END -> "del End";
            case ULTIMATE -> "definitiva";
        };
    }
}
