package com.g1739.liquidfuels.util;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

public final class FuelTankItemFluidHandler implements IFluidHandlerItem {
    private final ItemStack container;
    private final int capacity;

    public FuelTankItemFluidHandler(ItemStack container, int capacity) {
        this.container = container;
        this.capacity = capacity;
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
        return tank == 0 ? FuelTankContents.getFluid(container) : FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return tank == 0 ? capacity : 0;
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

        FluidStack current = FuelTankContents.getFluid(container);
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

        FluidStack current = FuelTankContents.getFluid(container);
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

        FluidStack current = FuelTankContents.getFluid(container);
        if (current.isEmpty()) {
            return FluidStack.EMPTY;
        }

        int drained = Math.min(current.getAmount(), maxDrain);
        FluidStack result = current.copy();
        result.setAmount(drained);

        if (action.execute()) {
            current.shrink(drained);
            FuelTankContents.setFluid(container, current);
        }
        return result;
    }

    private static boolean isSameFluid(FluidStack first, FluidStack second) {
        return FluidStack.isSameFluidSameComponents(first, second);
    }
}
