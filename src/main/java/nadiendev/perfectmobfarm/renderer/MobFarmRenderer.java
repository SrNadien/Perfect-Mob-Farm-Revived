package nadiendev.perfectmobfarm.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import nadiendev.perfectmobfarm.block.MobFarmBlock;
import nadiendev.perfectmobfarm.block.entity.MobFarmBlockEntity;
import nadiendev.perfectmobfarm.item.DataComponentManager;
import nadiendev.perfectmobfarm.item.MobShard;
import nadiendev.perfectmobfarm.item.MobShardData;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Draws the mobs captured by the shards above the Mob farm, spinning around Y.
 * <p>
 * A block entity renderer works in two steps: {@link #extractRenderState} copies what it needs from the block
 * entity into a {@link MobFarmRenderState} and {@link #submit} draws that state.
 */
public class MobFarmRenderer implements BlockEntityRenderer<MobFarmBlockEntity, MobFarmRenderer.MobFarmRenderState> {

    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    /** Scale of the mob when it is alone (centered) and when two mobs share the farm */
    private static final float SINGLE_SCALE = 0.3F;
    private static final float DOUBLE_SCALE = 0.22F;

    /** Distance of each mob to the center of the farm when there are two, along the right axis of the farm */
    private static final float DOUBLE_OFFSET = 0.18F;

    /** Degrees per tick of the rotation around Y */
    private static final float SPIN_SPEED = 2.0F;

    private final EntityRenderDispatcher entityRenderer;

    public MobFarmRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderer = context.entityRenderer();
    }

    /**
     * What the renderer needs to draw the farm: one render state of the captured mob per shard slot (null when the
     * slot has nothing to draw) and the spin and placement shared by all of them
     */
    public static class MobFarmRenderState extends BlockEntityRenderState {
        public final EntityRenderState[] mobs = new EntityRenderState[MobFarmBlockEntity.SHARD_SLOTS];
        public int count;
        public float time;
        public float scale;
        public float offsetX;
        public float offsetZ;
    }

    @Override
    public MobFarmRenderState createRenderState() {
        return new MobFarmRenderState();
    }

    @Override
    public void extractRenderState(MobFarmBlockEntity pBlockEntity, MobFarmRenderState pState, float pPartialTick,
                                   Vec3 pCameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay pBreakProgress) {
        BlockEntityRenderer.super.extractRenderState(pBlockEntity, pState, pPartialTick, pCameraPosition,
                pBreakProgress);
        pState.count = 0;
        for (int i = 0; i < pState.mobs.length; i++) {
            pState.mobs[i] = null;
        }
        if (pBlockEntity.getLevel() == null) {
            return;
        }

        // One entry per shard slot: the type of the captured mob, null when the slot is unusable, empty or incomplete
        EntityType<?>[] types = new EntityType<?>[MobFarmBlockEntity.SHARD_SLOTS];
        for (int i = 0; i < types.length; i++) {
            types[i] = getShardMobType(pBlockEntity, i);
            if (types[i] != null) {
                pState.count++;
            }
        }
        if (pState.count == 0) {
            return;
        }

        pState.time = pBlockEntity.getLevel().getGameTime() + pPartialTick;
        pState.scale = pState.count > 1 ? DOUBLE_SCALE : SINGLE_SCALE;
        // Right axis of the farm, seen from its front
        Direction right = pBlockEntity.getBlockState().getValue(MobFarmBlock.FACING).getCounterClockWise();
        pState.offsetX = right.getStepX();
        pState.offsetZ = right.getStepZ();

        for (int i = 0; i < types.length; i++) {
            if (types[i] != null) {
                pState.mobs[i] = extractMob(types[i], i, pBlockEntity, pPartialTick, pState.lightCoords);
            }
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
        return BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.tryParse(data.mobId())).orElse(null);
    }

    /**
     * Get (or create) the entity the farm keeps for the slot and extract its render state
     *
     * @param entityType   Type of the mob to draw
     * @param pIndex       Shard slot the mob comes from (each slot keeps its own entity)
     * @param pBlockEntity Farm
     * @param pPartialTick Partial tick
     * @param pLight       Packed light of the farm
     * @return The render state of the mob, or null if the entity could not be created
     */
    private EntityRenderState extractMob(EntityType<?> entityType, int pIndex, MobFarmBlockEntity pBlockEntity,
                                         float pPartialTick, int pLight) {
        Entity entityToSpawn = pBlockEntity.getEntityToDisplay(pIndex);
        if (entityToSpawn == null || entityToSpawn.getType() != entityType) {
            LOGGER.debug("NEED TO CREATE A NEW ENTITY");
            entityToSpawn = entityType.create(pBlockEntity.getLevel(), EntitySpawnReason.LOAD);
            if (entityToSpawn != null) {
                // Since 26.2 an entity has no id until the level assigns one, and the renderers read it. This mob is
                // never added to the level, so give it a negative id that no real entity can have.
                entityToSpawn.setId(-1 - pIndex);
            }
            pBlockEntity.setEntityToDisplay(pIndex, entityToSpawn);
        }
        if (entityToSpawn == null) {
            return null;
        }

        // Set head rotation to 0 to avoid head shaking
        entityToSpawn.setYHeadRot(0.0F);

        EntityRenderState mobState = entityRenderer.extractEntity(entityToSpawn, pPartialTick);
        mobState.lightCoords = pLight;
        return mobState;
    }

    @Override
    public void submit(MobFarmRenderState pState, PoseStack pPoseStack, SubmitNodeCollector pCollector,
                       CameraRenderState pCamera) {
        for (int i = 0; i < pState.mobs.length; i++) {
            if (pState.mobs[i] == null) {
                continue;
            }
            // A lone mob stays centered; with two, the first one is on the right and the second one on the left
            float offset = pState.count > 1 ? (i == 0 ? DOUBLE_OFFSET : -DOUBLE_OFFSET) : 0.0F;
            submitMob(pState.mobs[i], pState, pState.offsetX * offset, pState.offsetZ * offset, pPoseStack,
                    pCollector, pCamera);
        }
    }

    /**
     * Draw one mob just above the Mob farm, slowly spinning around Y with a tiny vertical bob
     *
     * @param pMob       Render state of the mob
     * @param pState     Render state of the farm
     * @param pOffsetX   Offset from the center of the block, on X
     * @param pOffsetZ   Offset from the center of the block, on Z
     * @param pPoseStack Pose stack
     * @param pCollector Submit node collector
     * @param pCamera    Camera
     */
    private void submitMob(EntityRenderState pMob, MobFarmRenderState pState, float pOffsetX, float pOffsetZ,
                           PoseStack pPoseStack, SubmitNodeCollector pCollector, CameraRenderState pCamera) {
        pPoseStack.pushPose();
        float rotation = pState.time * SPIN_SPEED;
        float bob = (float) Math.sin(pState.time * 0.1F) * 0.02F;

        pPoseStack.translate(0.5F + pOffsetX, 0.25F + bob, 0.5F + pOffsetZ);
        pPoseStack.scale(pState.scale, pState.scale, pState.scale);
        // The spin replaces the facing of the block
        pPoseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        entityRenderer.submit(pMob, pCamera, 0.0D, 0.0D, 0.0D, pPoseStack, pCollector);
        pPoseStack.popPose();
    }
}
