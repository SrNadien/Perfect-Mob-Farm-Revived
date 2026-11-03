package nadiendev.perfectmobfarm.datagen.provider;

import nadiendev.perfectmobfarm.PerfectMobFarm;
import nadiendev.perfectmobfarm.block.BlockManager;
import nadiendev.perfectmobfarm.block.FarmTier;
import nadiendev.perfectmobfarm.block.MobFarmBlock;
import nadiendev.perfectmobfarm.item.ItemManager;
import nadiendev.perfectmobfarm.renderer.ShardCompletedProperty;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

/**
 * Generates the blockstates, block models, item models and client items of the mod: the mob farms, the mob shards
 * (one model while incomplete and another one once completed) and the farm upgrades.
 */
public class ModModelProvider extends ModelProvider {

    /** Namespace of the optional mod whose blocks are the material of the integration tiers */
    private static final String ULTIMATE_ARMOURY = "ultimatearmoury";

    /** Texture slots of the farm model */
    private static final TextureSlot MATERIAL = TextureSlot.create("material");
    private static final TextureSlot MAIN = TextureSlot.create("main");

    /** The mob shards: each one has a {@code <name>_completed} model with its own texture */
    private static final List<Holder<Item>> SHARDS = List.of(ItemManager.MOB_SHARD, ItemManager.ADVANCED_MOB_SHARD,
            ItemManager.ULTIMATE_MOB_SHARD);

    /**
     * Model of a farm: a glass shell with the tier texture and a base made of the tier material
     */
    private static final ModelTemplate FARM_TEMPLATE = ExtendedModelTemplateBuilder.builder()
            .parent(Identifier.withDefaultNamespace("block/orientable"))
            .requiredTextureSlot(TextureSlot.PARTICLE)
            .requiredTextureSlot(MATERIAL)
            .requiredTextureSlot(MAIN)
            // Glass shell
            .element(shell -> shell
                    .from(0, 0, 0).to(16, 16, 16)
                    .allFaces((direction, face) -> face.uvs(0, 0, 16, 16).texture(MAIN)))
            // Material base
            .element(base -> base
                    .from(2, 0.1F, 2).to(14, 3, 14)
                    .allFaces((direction, face) -> {
                        if (direction.getAxis().isVertical()) {
                            face.uvs(2, 2, 14, 14);
                        } else {
                            face.uvs(2, 13, 14, 16);
                        }
                        face.texture(MATERIAL);
                    }))
            .build();

    public ModModelProvider(PackOutput output) {
        super(output, PerfectMobFarm.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        for (FarmTier tier : FarmTier.values()) {
            farmState(blockModels, getBlock(tier), tier, false);
            farmState(blockModels, getEnergyBlock(tier), tier, true);
        }

        for (Holder<Item> shard : SHARDS) {
            shardModel(itemModels, shard.value());
        }
        itemModels.generateFlatItem(ItemManager.SPEED_UPGRADE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ItemManager.MOB_SLOT_UPGRADE.get(), ModelTemplates.FLAT_ITEM);
        // The block items of the farms use the model of their block, see ModelProvider
    }

    /**
     * Writes the blockstate of a farm: the model rotated to face the block direction. Does nothing for a null block
     * (tier that is not available).
     */
    private static void farmState(BlockModelGenerators blockModels, @Nullable Block block, FarmTier tier,
                                  boolean energy) {
        if (block == null) {
            return;
        }
        MultiVariant variant = BlockModelGenerators.plainVariant(farmModel(blockModels, block, tier, energy));
        // Only the horizontal directions rotate the model; up and down reuse the unrotated one
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variant)
                .with(PropertyDispatch.modify(MobFarmBlock.FACING)
                        .select(Direction.NORTH, BlockModelGenerators.NOP)
                        .select(Direction.SOUTH, BlockModelGenerators.Y_ROT_180)
                        .select(Direction.WEST, BlockModelGenerators.Y_ROT_270)
                        .select(Direction.EAST, BlockModelGenerators.Y_ROT_90)
                        .select(Direction.UP, BlockModelGenerators.NOP)
                        .select(Direction.DOWN, BlockModelGenerators.NOP)));
    }

    /**
     * Writes the block model of a farm. The energy variant has the same structure and only changes the glass texture.
     *
     * @return The location of the model
     */
    private static Identifier farmModel(BlockModelGenerators blockModels, Block block, FarmTier tier,
                                        boolean energy) {
        String name = tier.getName() + (energy ? "_energy_mob_farm" : "_mob_farm");
        Material material = new Material(materialTexture(tier));
        TextureMapping textures = new TextureMapping()
                .put(TextureSlot.PARTICLE, material)
                .put(MATERIAL, material)
                .put(MAIN, new Material(PerfectMobFarm.id("block/" + name)));
        return FARM_TEMPLATE.create(block, textures, blockModels.modelOutput);
    }

    /**
     * A shard uses its completed model while the {@code completed} property is true
     */
    private static void shardModel(ItemModelGenerators itemModels, Item shard) {
        Identifier normal = itemModels.createFlatItemModel(shard, ModelTemplates.FLAT_ITEM);
        Identifier completed = itemModels.createFlatItemModel(shard, "_completed", ModelTemplates.FLAT_ITEM);
        itemModels.itemModelOutput.accept(shard, ItemModelUtils.conditional(new ShardCompletedProperty(),
                ItemModelUtils.plainModel(completed), ItemModelUtils.plainModel(normal)));
    }

    /**
     * @return Texture used for the base and the particles of the farm of this tier
     */
    private static Identifier materialTexture(FarmTier tier) {
        return switch (tier) {
            case IRON, GOLD, DIAMOND, EMERALD, NETHERITE ->
                    Identifier.withDefaultNamespace("block/" + tier.getName() + "_block");
            case NETHER -> Identifier.withDefaultNamespace("block/nether_bricks");
            case END -> Identifier.withDefaultNamespace("block/end_stone_bricks");
            case ULTIMATE -> Identifier.withDefaultNamespace("block/obsidian");
            case MUSHASHITE, JOANFOITE, NADIENITE ->
                    Identifier.fromNamespaceAndPath(ULTIMATE_ARMOURY, "block/" + tier.getName() + "_block");
        };
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return BlockManager.MOB_FARMS.stream();
    }

    @Nullable
    private static Block getBlock(FarmTier tier) {
        var farm = BlockManager.getBlock(tier);
        return farm == null ? null : farm.get();
    }

    @Nullable
    private static Block getEnergyBlock(FarmTier tier) {
        var farm = BlockManager.getEnergyBlock(tier);
        return farm == null ? null : farm.get();
    }
}
