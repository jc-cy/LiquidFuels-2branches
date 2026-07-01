package com.g1739.liquidfuels.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public final class FuelTankContents {
    public static final int BUCKET_MB = 1000;
    public static final int FUEL_UNIT_MB = 100;
    public static final String FLUID_TAG = "Fluid";

    private FuelTankContents() {
    }

    public static FluidStack getFluid(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(FLUID_TAG, CompoundTag.TAG_COMPOUND)) {
            return FluidStack.EMPTY;
        }
        return FluidStack.loadFluidStackFromNBT(tag.getCompound(FLUID_TAG));
    }

    public static void setFluid(ItemStack stack, FluidStack fluid) {
        if (fluid.isEmpty() || fluid.getAmount() <= 0) {
            clearFluid(stack);
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        tag.put(FLUID_TAG, fluid.writeToNBT(new CompoundTag()));
        stack.setCount(1);
    }

    public static void clearFluid(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }
        tag.remove(FLUID_TAG);
        if (tag.isEmpty()) {
            stack.setTag(null);
        }
    }

    public static boolean isEmpty(ItemStack stack) {
        return getFluid(stack).isEmpty();
    }

    public static boolean consumeFuelUnit(ItemStack stack) {
        FluidStack fluid = getFluid(stack);
        if (fluid.getAmount() < FUEL_UNIT_MB) {
            return false;
        }

        fluid.shrink(FUEL_UNIT_MB);
        setFluid(stack, fluid);
        return true;
    }
}
