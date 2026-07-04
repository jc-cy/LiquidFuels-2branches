package com.g1739.liquidfuels.util;

import com.g1739.liquidfuels.item.FuelTankItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import java.util.Objects;

public final class FuelTankItemFluidHandler implements IFluidHandlerItem {
    private final ItemStack container;

    public FuelTankItemFluidHandler(ItemStack container) {
        this.container = container;
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return tank == 0 ? getStoredFluid() : FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return tank == 0 ? getCapacity() : 0;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return tank == 0 && !stack.isEmpty();
    }

    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource.isEmpty() || container.getCount() != 1) {
            return 0;
        }

        int capacity = getCapacity();
        if (capacity <= 0) {
            return 0;
        }
        FluidStack current = getStoredFluid();
        if (!current.isEmpty() && !isSameFluid(current, resource)) {
            return 0;
        }

        int filled = Math.min(capacity - current.getAmount(), resource.getAmount());
        if (filled <= 0) {
            return 0;
        }

        if (action.execute()) {
            FluidStack result = resource.copy();
            result.setAmount(current.getAmount() + filled);
            FuelTankContents.setFluid(container, result);
        }
        return filled;
    }

    @Override
    public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }

        FluidStack current = getStoredFluid();
        if (current.isEmpty() || !isSameFluid(current, resource)) {
            return FluidStack.EMPTY;
        }

        return drain(resource.getAmount(), action);
    }

    @Override
    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        if (maxDrain <= 0) {
            return FluidStack.EMPTY;
        }

        FluidStack current = getStoredFluid();
        if (current.isEmpty()) {
            return FluidStack.EMPTY;
        }

        int drained = Math.min(current.getAmount(), maxDrain);
        FluidStack result = current.copy();
        result.setAmount(drained);

        if (action.execute()) {
            current.shrink(drained);
            if (current.getAmount() <= 0) {
                current = FluidStack.EMPTY;
            }
            FuelTankContents.setFluid(container, current);
        }
        return result;
    }

    private int getCapacity() {
        return container.getItem() instanceof FuelTankItem tankItem ? tankItem.getCapacity() : 0;
    }

    private FluidStack getStoredFluid() {
        FluidStack current = FuelTankContents.getFluid(container);
        int capacity = getCapacity();
        if (!current.isEmpty() && capacity > 0 && current.getAmount() > capacity) {
            current.setAmount(capacity);
        }
        return current;
    }

    private static boolean isSameFluid(FluidStack first, FluidStack second) {
        CompoundTag firstTag = first.getTag();
        CompoundTag secondTag = second.getTag();
        return first.getFluid() == second.getFluid() && Objects.equals(firstTag, secondTag);
    }
}
