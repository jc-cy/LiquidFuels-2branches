package com.g1739.liquidfuels.item;

import com.g1739.liquidfuels.block.FuelTankBlock;
import com.g1739.liquidfuels.config.LiquidFuelConfig;
import com.g1739.liquidfuels.util.FuelTankContents;
import com.g1739.liquidfuels.util.FuelTankItemFluidHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FuelTankItem extends BlockItem {
    public FuelTankItem(FuelTankBlock block, Item.Properties properties) {
        super(block, properties);
    }

    public int getCapacity() {
        return getBlock() instanceof FuelTankBlock tank ? tank.getCapacity() : 0;
    }

    public static ItemStack createFilledStack(FuelTankBlock block, FluidStack fluid) {
        ItemStack stack = new ItemStack(block.asItem());
        if (stack.getItem() instanceof FuelTankItem tankItem && !fluid.isEmpty()) {
            FluidStack stored = fluid.copy();
            stored.setAmount(Math.min(stored.getAmount(), tankItem.getCapacity()));
            FuelTankContents.setFluid(stack, stored);
        }
        return stack;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return FuelTankContents.isEmpty(stack) ? super.getMaxStackSize(stack) : 1;
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return canUseStoredFuel(stack);
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        if (!canUseStoredFuel(stack)) {
            return ItemStack.EMPTY;
        }
        ItemStack remaining = stack.copy();
        remaining.setCount(1);
        FuelTankContents.consumeFuelUnit(remaining);
        return remaining;
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        FluidStack fluid = getUsableFluid(itemStack);
        if (fluid.getAmount() < FuelTankContents.FUEL_UNIT_MB) {
            return 0;
        }
        return LiquidFuelConfig.getBurnTimeForUnit(fluid.getFluid());
    }

    public static boolean canUseStoredFuel(ItemStack stack) {
        FluidStack fluid = getUsableFluid(stack);
        return fluid.getAmount() >= FuelTankContents.FUEL_UNIT_MB && LiquidFuelConfig.getBurnTimeForUnit(fluid.getFluid()) > 0;
    }

    private static FluidStack getUsableFluid(ItemStack stack) {
        if (!(stack.getItem() instanceof FuelTankItem tankItem)) {
            return FluidStack.EMPTY;
        }
        FluidStack fluid = FuelTankContents.getFluid(stack);
        int capacity = tankItem.getCapacity();
        if (!fluid.isEmpty() && capacity > 0 && fluid.getAmount() > capacity) {
            fluid.setAmount(capacity);
        }
        return fluid;
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final LazyOptional<IFluidHandlerItem> handler = LazyOptional.of(() -> new FuelTankItemFluidHandler(stack));

            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                if (cap == ForgeCapabilities.FLUID_HANDLER_ITEM) {
                    return handler.cast();
                }
                return LazyOptional.empty();
            }
        };
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() != null && context.getPlayer().isSecondaryUseActive()) {
            return super.useOn(context);
        }
        InteractionResult pickupResult = tryPickupWorldFluid(context.getLevel(), context.getPlayer(), context.getHand(), context.getClickedPos());
        if (pickupResult.consumesAction()) {
            return pickupResult;
        }
        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        ItemStack held = player.getItemInHand(hand);
        if (hit.getType() == HitResult.Type.BLOCK) {
            InteractionResult pickupResult = tryPickupWorldFluid(level, player, hand, hit.getBlockPos());
            if (pickupResult.consumesAction()) {
                return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
            }
        }
        return InteractionResultHolder.pass(held);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int capacity = getCapacity();
        FluidStack fluid = getUsableFluid(stack);
        if (fluid.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.liquidfuels.empty", capacity).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("tooltip.liquidfuels.fluid", fluid.getDisplayName(), fluid.getAmount(), capacity).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.liquidfuels.fuel_unit", FuelTankContents.FUEL_UNIT_MB).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !FuelTankContents.isEmpty(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        FluidStack fluid = FuelTankContents.getFluid(stack);
        if (fluid.isEmpty()) {
            return 0;
        }
        int capacity = getCapacity();
        return capacity <= 0 ? 0 : Math.min(13, Math.round(13.0F * Math.min(fluid.getAmount(), capacity) / capacity));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xE68A2E;
    }

    private InteractionResult tryPickupWorldFluid(Level level, @Nullable Player player, InteractionHand hand, BlockPos pos) {
        if (player == null) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);
        FluidState fluidState = level.getFluidState(pos);
        BlockState state = level.getBlockState(pos);
        if (!fluidState.isSource() || !(state.getBlock() instanceof BucketPickup pickup)) {
            return InteractionResult.PASS;
        }

        FluidStack sourceFluid = new FluidStack(fluidState.getType(), FuelTankContents.BUCKET_MB);
        ItemStack target = held.getCount() > 1 ? held.copy() : held;
        target.setCount(1);
        FuelTankItemFluidHandler handler = new FuelTankItemFluidHandler(target);
        if (handler.fill(sourceFluid, IFluidHandler.FluidAction.SIMULATE) < sourceFluid.getAmount()) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack pickedUp = pickup.pickupBlock(level, pos, state);
        if (pickedUp.isEmpty()) {
            return InteractionResult.PASS;
        }

        handler.fill(sourceFluid, IFluidHandler.FluidAction.EXECUTE);
        if (held.getCount() > 1) {
            held.shrink(1);
            giveOrDrop(player, target);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }

    private static void giveOrDrop(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
