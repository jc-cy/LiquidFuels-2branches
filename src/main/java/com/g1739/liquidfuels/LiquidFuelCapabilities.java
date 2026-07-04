package com.g1739.liquidfuels;

import com.g1739.liquidfuels.registry.LiquidFuelBlockEntities;
import com.g1739.liquidfuels.registry.LiquidFuelItems;
import com.g1739.liquidfuels.util.FuelTankItemFluidHandler;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class LiquidFuelCapabilities {
    private LiquidFuelCapabilities() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                LiquidFuelBlockEntities.FUEL_TANK.get(),
                (tank, side) -> tank.getFluidHandler()
        );

        event.registerItem(
                Capabilities.FluidHandler.ITEM,
                (stack, context) -> new FuelTankItemFluidHandler(stack),
                LiquidFuelItems.SMALL_FUEL_TANK.get()
        );
        event.registerItem(
                Capabilities.FluidHandler.ITEM,
                (stack, context) -> new FuelTankItemFluidHandler(stack),
                LiquidFuelItems.FUEL_TANK.get()
        );
        event.registerItem(
                Capabilities.FluidHandler.ITEM,
                (stack, context) -> new FuelTankItemFluidHandler(stack),
                LiquidFuelItems.LARGE_FUEL_TANK.get()
        );
    }
}
