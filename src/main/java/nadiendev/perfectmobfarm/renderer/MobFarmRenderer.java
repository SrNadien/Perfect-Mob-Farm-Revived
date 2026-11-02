package nadiendev.perfectmobfarm.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import nadiendev.perfectmobfarm.block.MobFarmBlock;
import nadiendev.perfectmobfarm.block.entity.MobFarmBlockEntity;
import nadiendev.perfectmobfarm.item.DataComponentManager;
import nadiendev.perfectmobfarm.item.MobShard;
import nadiendev.perfectmobfarm.item.MobShardData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

public class MobFarmRenderer implements BlockEntityRenderer<MobFarmBlockEntity> {

    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    /** Scale of the mob when it is alone (centered) and when two mobs share the farm */
    private static final float SINGLE_SCALE = 0.3F;
    private static final float DOUBLE_SCALE = 0.22F;

    /** Distance of each mob to the center of the farm when there are two, along the right axis of the farm */
    private static final float DOUBLE_OFFSET = 0.18F;

    /** Degrees per tick of the rotation around Y */
    private static final float SPIN_SPEED = 2.0F;

    private final BlockEntityRendererProvider.Context context;

    public MobFarmRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(MobFarmBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        if (pBlockEntity.getLevel() == null) {
            return;
        }

        // One entry per shard slot: the type of the captured mob, null when the slot is unusable, empty or incomplete
        EntityType<?>[] types = new EntityType<?>[MobFarmBlockEntity.SHARD_SLOTS];
        int count = 0;
        for (int i = 0; i < types.length; i++) {
            types[i] = getShardMobType(pBlockEntity, i);
            if (types[i] != null) {
                count++;
            }
        }
        if (count == 0) {
            return;
        }

        float time = pBlockEntity.getLevel().getGameTime() + pPartialTick;
        // Right axis of the farm, seen from its front
        Direction right = pBlockEntity.getBlockState().getValue(MobFarmBlock.FACING).getCounterClockWise();
        for (int i = 0; i < types.length; i++) {
            if (types[i] == null) {
                continue;
            }
            // A lone mob stays centered; with two, the first one is on the right and the second one on the left
            float offset = count > 1 ? (i == 0 ? DOUBLE_OFFSET : -DOUBLE_OFFSET) : 0.0F;
            float scale = count > 1 ? DOUBLE_SCALE : SINGLE_SCALE;
            renderGivenEntity(types[i], i, pBlockEntity, time, pPartialTick, pPoseStack, pBufferSource, pPackedLight,
                    right.getStepX() * offset, right.getStepZ() * offset, scale);
        }
    }

    /**
     * @param pBlockEntity Farm
     * @param pIndex       Shard slot
     * @return The type of the mob captured by the complete shard in the usable slot, or null if there is none
     */
    private static EntityType<?> getShardMobType(MobFarmBlockEntity pBlockEntity, int pIndex) {
        if (!pBlockEntity.isShardSlotUsable(pIndex)) {
            return null;
        }
        ItemStack itemHeld = pBlockEntity.getItemHandler().getStackInSlot(pIndex);
        if (!(itemHeld.getItem() instanceof MobShard)) {
            return null;
        }
        MobShardData data = itemHeld.get(DataComponentManager.MOB_SHARD_DATA.get());
        if (data == null || !MobShard.isComplete(itemHeld)) {
            return null;
        }
        return EntityType.byString(data.mobId()).orElse(null);
    }

    /**
     * Render the given entity type just above the Mob farm, slowly spinning around Y with a tiny vertical bob
     *
     * @param entityType    Type of the mob to draw
     * @param pIndex        Shard slot the mob comes from (each slot keeps its own entity)
     * @param pBlockEntity  Farm
     * @param pTime         Game time in ticks plus the partial tick
     * @param pPartialTick  Partial tick
     * @param pPoseStack    Pose stack
     * @param pBufferSource Buffer source
     * @param pPackedLight  Light
     * @param pOffsetX      Offset from the center of the block, on X
     * @param pOffsetZ      Offset from the center of the block, on Z
     * @param pScale        Scale of the mob
     */
    public void renderGivenEntity(EntityType<?> entityType, int pIndex, MobFarmBlockEntity pBlockEntity, float pTime,
                                  float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource,
                                  int pPackedLight, float pOffsetX, float pOffsetZ, float pScale) {
        Entity entityToSpawn = pBlockEntity.getEntityToDisplay(pIndex);
        if (entityToSpawn == null || entityToSpawn.getType() != entityType) {
            LOGGER.debug("NEED TO CREATE A NEW ENTITY");
            entityToSpawn = entityType.create(pBlockEntity.getLevel());
            pBlockEntity.setEntityToDisplay(pIndex, entityToSpawn);
        }
        if (entityToSpawn == null) {
            return;
        }

        pPoseStack.pushPose();
        float rotation = pTime * SPIN_SPEED;
        float bob = (float) Math.sin(pTime * 0.1F) * 0.02F;

        // Set head rotation to 0 to avoid head shaking
        entityToSpawn.setYHeadRot(0.0F);

        pPoseStack.translate(0.5F + pOffsetX, 0.25F + bob, 0.5F + pOffsetZ);
        pPoseStack.scale(pScale, pScale, pScale);
        // The spin replaces the facing of the block
        pPoseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        Minecraft.getInstance().getEntityRenderDispatcher().render(entityToSpawn, 0.0D, 0.0D, 0.0D, 0.0F,
                pPartialTick, pPoseStack, pBufferSource, pPackedLight);
        pPoseStack.popPose();
    }
}
