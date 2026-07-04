package com.g1739.liquidfuels.util;

import com.g1739.liquidfuels.item.FuelTankItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;

public final class InventoryFluidTransfer {
    private InventoryFluidTransfer() {
    }

    public static boolean tryFillTankFromCarried(AbstractContainerMenu menu, int slotId, int button, ClickType clickType, Player player) {
        if (clickType != ClickType.PICKUP || button != 1 || slotId < 0 || slotId >= menu.slots.size()) {
            return false;
        }

        ItemStack carried = menu.getCarried();
        if (carried.isEmpty() || carried.getCount() != 1) {
            return false;
        }

        Slot slot = menu.slots.get(slotId);
        if (!slot.hasItem() || !slot.mayPickup(player)) {
            return false;
        }

        ItemStack tankStack = slot.getItem();
        if (!(tankStack.getItem() instanceof FuelTankItem)) {
            return false;
        }

        TransferResult result = transfer(carried.copy(), singleCopy(tankStack));
        if (result == null) {
            return false;
        }

        if (player.level().isClientSide) {
            return true;
        }

        if (tankStack.getCount() == 1) {
            if (!slot.mayPlace(result.targetContainer())) {
                return false;
            }
            menu.setCarried(result.sourceContainer().isEmpty() ? ItemStack.EMPTY : result.sourceContainer());
            slot.set(result.targetContainer());
        } else {
            menu.setCarried(result.sourceContainer().isEmpty() ? ItemStack.EMPTY : result.sourceContainer());
            tankStack.shrink(1);
            slot.set(tankStack);
            giveOrDrop(player, result.targetContainer());
        }

        slot.setChanged();
        menu.broadcastChanges();
        return true;
    }

    private static @Nullable TransferResult transfer(ItemStack sourceContainer, ItemStack targetContainer) {
        IFluidHandlerItem source = getFluidHandler(sourceContainer);
        IFluidHandlerItem target = getFluidHandler(targetContainer);
        if (source == null || target == null) {
            return null;
        }

        FluidStack available = source.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
        if (available.isEmpty()) {
            return null;
        }

        int fillable = target.fill(available, IFluidHandler.FluidAction.SIMULATE);
        if (fillable <= 0) {
            return null;
        }

        FluidStack drained = source.drain(fillable, IFluidHandler.FluidAction.EXECUTE);
        if (drained.isEmpty()) {
            return null;
        }

        int filled = target.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        if (filled <= 0) {
            return null;
        }

        return new TransferResult(source.getContainer(), target.getContainer());
    }

    private static @Nullable IFluidHandlerItem getFluidHandler(ItemStack stack) {
        return FluidUtil.getFluidHandler(stack).resolve().orElse(null);
    }

    private static ItemStack singleCopy(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        return copy;
    }

    private static void giveOrDrop(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private record TransferResult(ItemStack sourceContainer, ItemStack targetContainer) {
    }
}
