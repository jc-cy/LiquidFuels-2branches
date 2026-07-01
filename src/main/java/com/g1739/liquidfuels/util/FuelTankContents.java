package com.g1739.liquidfuels.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

public final class FuelTankContents {
    public static final int BUCKET_MB = 1000;
    public static final int FUEL_UNIT_MB = 100;
    public static final String FLUID_TAG = "Fluid";
    private static final String FLUID_ID_TAG = "id";
    private static final String AMOUNT_TAG = "Amount";

    private FuelTankContents() {
    }

    public static FluidStack getFluid(ItemStack stack) {
        CompoundTag tag = readCustomData(stack);
        if (!tag.contains(FLUID_TAG, CompoundTag.TAG_COMPOUND)) {
            return FluidStack.EMPTY;
        }
        return readFluid(tag.getCompound(FLUID_TAG));
    }

    public static void setFluid(ItemStack stack, FluidStack fluid) {
        if (fluid.isEmpty() || fluid.getAmount() <= 0) {
            clearFluid(stack);
            return;
        }

        CompoundTag tag = readCustomData(stack);
        tag.put(FLUID_TAG, writeFluid(fluid));
        writeCustomData(stack, tag);
        stack.setCount(1);
    }

    public static void clearFluid(ItemStack stack) {
        CompoundTag tag = readCustomData(stack);
        tag.remove(FLUID_TAG);
        writeCustomData(stack, tag);
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

    public static FluidStack readFluid(CompoundTag tag) {
        if (!tag.contains(FLUID_ID_TAG, CompoundTag.TAG_STRING) || !tag.contains(AMOUNT_TAG, CompoundTag.TAG_INT)) {
            return FluidStack.EMPTY;
        }

        ResourceLocation fluidId = ResourceLocation.tryParse(tag.getString(FLUID_ID_TAG));
        if (fluidId == null) {
            return FluidStack.EMPTY;
        }

        Fluid fluid = BuiltInRegistries.FLUID.get(fluidId);
        int amount = tag.getInt(AMOUNT_TAG);
        return fluid == Fluids.EMPTY || amount <= 0 ? FluidStack.EMPTY : new FluidStack(fluid, amount);
    }

    public static CompoundTag writeFluid(FluidStack fluid) {
        CompoundTag tag = new CompoundTag();
        if (!fluid.isEmpty() && fluid.getAmount() > 0) {
            tag.putString(FLUID_ID_TAG, BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString());
            tag.putInt(AMOUNT_TAG, fluid.getAmount());
        }
        return tag;
    }

    private static CompoundTag readCustomData(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        return customData == null ? new CompoundTag() : customData.copyTag();
    }

    private static void writeCustomData(ItemStack stack, CompoundTag tag) {
        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }
}
