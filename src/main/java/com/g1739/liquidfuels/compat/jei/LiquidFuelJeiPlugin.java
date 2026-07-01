package com.g1739.liquidfuels.compat.jei;

import com.g1739.liquidfuels.LiquidFuels;
import com.g1739.liquidfuels.config.LiquidFuelConfig;
import com.g1739.liquidfuels.registry.LiquidFuelItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@JeiPlugin
public final class LiquidFuelJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(LiquidFuels.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new LiquidFuelJeiCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(LiquidFuelJeiCategory.TYPE, createRecipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(LiquidFuelItems.SMALL_FUEL_TANK.get()), LiquidFuelJeiCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(LiquidFuelItems.FUEL_TANK.get()), LiquidFuelJeiCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(LiquidFuelItems.LARGE_FUEL_TANK.get()), LiquidFuelJeiCategory.TYPE);
    }

    private static List<LiquidFuelJeiRecipe> createRecipes() {
        return LiquidFuelConfig.getBurnTimesFor1000mb().entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().toString()))
                .map(LiquidFuelJeiPlugin::createRecipe)
                .flatMap(List::stream)
                .toList();
    }

    private static List<LiquidFuelJeiRecipe> createRecipe(Map.Entry<ResourceLocation, Integer> entry) {
        Fluid fluid = BuiltInRegistries.FLUID.get(entry.getKey());
        if (fluid == null || fluid == Fluids.EMPTY || entry.getValue() <= 0) {
            return List.of();
        }
        return List.of(new LiquidFuelJeiRecipe(entry.getKey(), new FluidStack(fluid, 1000), entry.getValue()));
    }
}
