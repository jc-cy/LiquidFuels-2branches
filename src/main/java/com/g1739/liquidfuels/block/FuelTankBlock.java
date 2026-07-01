package com.g1739.liquidfuels.block;

import com.g1739.liquidfuels.blockentity.FuelTankBlockEntity;
import com.g1739.liquidfuels.item.FuelTankItem;
import com.g1739.liquidfuels.util.FuelTankContents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

public class FuelTankBlock extends BaseEntityBlock implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private static final VoxelShape SMALL_SHAPE_NORTH_SOUTH = Shapes.box(0.343125D, 0.0D, 0.25205625D, 0.656875D, 0.6805425D, 0.75D);
    private static final VoxelShape SMALL_SHAPE_EAST_WEST = Shapes.box(0.25D, 0.0D, 0.343125D, 0.74794375D, 0.6805425D, 0.656875D);
    private static final VoxelShape MEDIUM_SHAPE_NORTH_SOUTH = Shapes.box(0.343125D, 0.0D, 0.120361875D, 0.656875D, 1.0D, 0.875D);
    private static final VoxelShape MEDIUM_SHAPE_EAST_WEST = Shapes.box(0.125D, 0.0D, 0.343125D, 0.879638125D, 1.0D, 0.656875D);
    private static final VoxelShape LARGE_SHAPE_NORTH_SOUTH = Shapes.box(0.155625D, 0.0D, 0.120361875D, 0.844375D, 1.0D, 0.875D);
    private static final VoxelShape LARGE_SHAPE_EAST_WEST = Shapes.box(0.125D, 0.0D, 0.155625D, 0.879638125D, 1.0D, 0.844375D);

    private final int capacity;

    public FuelTankBlock(int capacity, Properties properties) {
        super(properties);
        this.capacity = capacity;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FuelTankBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        if (capacity <= 1000) {
            return facing.getAxis() == Direction.Axis.X ? SMALL_SHAPE_EAST_WEST : SMALL_SHAPE_NORTH_SOUTH;
        }
        if (capacity >= 20000) {
            return facing.getAxis() == Direction.Axis.X ? LARGE_SHAPE_EAST_WEST : LARGE_SHAPE_NORTH_SOUTH;
        }
        return facing.getAxis() == Direction.Axis.X ? MEDIUM_SHAPE_EAST_WEST : MEDIUM_SHAPE_NORTH_SOUTH;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof FuelTankBlockEntity tank)) {
            return InteractionResult.PASS;
        }

        IFluidHandler handler = tank.getFluidHandler();
        if (FluidUtil.interactWithFluidHandler(player, hand, handler)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.getBlockEntity(pos) instanceof FuelTankBlockEntity tank) {
            tank.setFluid(FuelTankContents.getFluid(stack));
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative() && level.getBlockEntity(pos) instanceof FuelTankBlockEntity tank) {
            tank.discardDropOnRemove();
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide && !isMoving && level.getBlockEntity(pos) instanceof FuelTankBlockEntity tank && tank.shouldDropOnRemove()) {
                popResource(level, pos, FuelTankItem.createFilledStack(this, tank.getFluid()));
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof FuelTankBlockEntity tank) {
            return FuelTankItem.createFilledStack(this, tank.getFluid());
        }
        return new ItemStack(this);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }
}
