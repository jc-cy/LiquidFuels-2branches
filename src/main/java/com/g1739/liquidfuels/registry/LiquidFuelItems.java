package com.g1739.liquidfuels.registry;

import com.g1739.liquidfuels.LiquidFuels;
import com.g1739.liquidfuels.item.FuelTankItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class LiquidFuelItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, LiquidFuels.MOD_ID);

    public static final Supplier<FuelTankItem> SMALL_FUEL_TANK = ITEMS.register("small_fuel_tank",
            () -> new FuelTankItem(LiquidFuelBlocks.SMALL_FUEL_TANK.get(), new Item.Properties().stacksTo(64)));
    public static final Supplier<FuelTankItem> FUEL_TANK = ITEMS.register("fuel_tank",
            () -> new FuelTankItem(LiquidFuelBlocks.FUEL_TANK.get(), new Item.Properties().stacksTo(64)));
    public static final Supplier<FuelTankItem> LARGE_FUEL_TANK = ITEMS.register("large_fuel_tank",
            () -> new FuelTankItem(LiquidFuelBlocks.LARGE_FUEL_TANK.get(), new Item.Properties().stacksTo(64)));

    private LiquidFuelItems() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
