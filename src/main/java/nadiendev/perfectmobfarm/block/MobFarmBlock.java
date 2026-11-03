package nadiendev.perfectmobfarm.block;

import nadiendev.perfectmobfarm.block.entity.BlockEntityManager;
import nadiendev.perfectmobfarm.block.entity.MobFarmBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;


/**
 * Mob Farm block. One class for every tier and for both variants (normal and energy), see {@link FarmTier}.
 */
public class MobFarmBlock extends BaseEntityBlock {

    public static final EnumProperty<Direction> FACING = DirectionalBlock.FACING;

    private final FarmTier tier;
    private final boolean usesEnergy;

    /**
     * @param tier       Tier of the farm
     * @param usesEnergy True for the energy variant, which needs FE to work
     * @param properties Block properties
     */
    public MobFarmBlock(FarmTier tier, boolean usesEnergy, Properties properties) {
        super(properties);
        this.tier = tier;
        this.usesEnergy = usesEnergy;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public FarmTier getTier() {
        return tier;
    }

    /**
     * @return True if this is the energy variant of the farm
     */
    public boolean usesEnergy() {
        return usesEnergy;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new MobFarmBlockEntity(pPos, pState);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, BlockHitResult pHitResult) {
        if (!pLevel.isClientSide()) {
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if (entity instanceof MobFarmBlockEntity farmEntity) {
                pPlayer.openMenu(farmEntity, pPos);
            } else {
                throw new IllegalStateException("Our Container provider is missing!");
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return pLevel.isClientSide() ? null : createTickerHelper(pBlockEntityType, BlockEntityManager.MOB_FARM.get(),
                (level, pos, state, blockEntity) -> blockEntity.tick());
    }

    @Override
    protected void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        super.onPlace(pState, pLevel, pPos, pOldState, pIsMoving);
        refreshRedstone(pLevel, pPos);
    }

    @Override
    protected void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock,
                                   @Nullable Orientation pOrientation, boolean pMovedByPiston) {
        super.neighborChanged(pState, pLevel, pPos, pNeighborBlock, pOrientation, pMovedByPiston);
        refreshRedstone(pLevel, pPos);
    }

    /**
     * Refresh the cached lever state of the farm. Does nothing on the client, or if the block entity does not exist yet
     * (it reads the levers by itself when it is loaded).
     */
    private static void refreshRedstone(Level pLevel, BlockPos pPos) {
        if (!pLevel.isClientSide() && pLevel.getBlockEntity(pPos) instanceof MobFarmBlockEntity farmEntity) {
            farmEntity.refreshRedstone();
        }
    }

    @Override
    protected RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }
}
