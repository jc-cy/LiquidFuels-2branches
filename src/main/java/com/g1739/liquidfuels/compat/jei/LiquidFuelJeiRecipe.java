package com.g1739.liquidfuels.compat.jei;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public record LiquidFuelJeiRecipe(ResourceLocation fluidId, FluidStack fluid, int burnTimeFor1000mb) {
}
