package nadiendev.perfectmobfarm.item;

import nadiendev.perfectmobfarm.PerfectMobFarm;
import nadiendev.perfectmobfarm.block.BlockManager;
import nadiendev.perfectmobfarm.block.MobFarmBlock;
import nadiendev.perfectmobfarm.block.entity.BlockEntityManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/**
 * Manager for the mod creative tabs
 */
public class CreativeTabManager {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PerfectMobFarm.MODID);

    /**
     * Creative mode tab of the mod, holding every item and block item
     */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + PerfectMobFarm.MODID + ".main"))
                    .icon(() -> new ItemStack(ItemManager.MOB_SHARD.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ItemManager.MOB_SHARD.get());
                        output.accept(ItemManager.ADVANCED_MOB_SHARD.get());
                        output.accept(ItemManager.ULTIMATE_MOB_SHARD.get());
                        output.accept(ItemManager.SPEED_UPGRADE.get());
                        output.accept(ItemManager.MOB_SLOT_UPGRADE.get());
                        // Only the available tiers are registered, in tier order: normal farm, then energy farm
                        // empty, then energy farm filled with energy
                        BlockManager.MOB_FARMS.forEach(farm -> {
                            Block block = farm.get();
                            output.accept(block);
                            if (block instanceof MobFarmBlock farmBlock && farmBlock.usesEnergy()) {
                                output.accept(createFilledEnergyFarm(farmBlock));
                            }
                        });
                    })
                    .build());

    /**
     * @param farmBlock Energy farm block
     * @return A stack of the farm whose energy buffer is full (the block entity data is read when it is placed)
     */
    private static ItemStack createFilledEnergyFarm(MobFarmBlock farmBlock) {
        ItemStack stack = new ItemStack(farmBlock);
        CompoundTag tag = new CompoundTag();
        tag.putInt("energy", farmBlock.getTier().getEnergyCapacity());
        stack.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(BlockEntityManager.MOB_FARM.get(), tag));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.translatableWithFallback("tooltip." + PerfectMobFarm.MODID + ".filled_with_energy", "Filled with energy")
                        .withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false)))));
        return stack;
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_TABS.register(eventBus);
    }
}
