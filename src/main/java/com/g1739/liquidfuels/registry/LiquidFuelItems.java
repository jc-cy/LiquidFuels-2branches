package com.g1739.liquidfuels.registry;

import com.g1739.liquidfuels.LiquidFuels;
import com.g1739.liquidfuels.item.FuelTankItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class LiquidFuelItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, LiquidFuels.MOD_ID);

    public static final RegistryObject<FuelTankItem> SMALL_FUEL_TANK = ITEMS.register("small_fuel_tank",
            () -> new FuelTankItem(LiquidFuelBlocks.SMALL_FUEL_TANK.get(), new Item.Properties().stacksTo(64)));
    public static final RegistryObject<FuelTankItem> FUEL_TANK = ITEMS.register("fuel_tank",
            () -> new FuelTankItem(LiquidFuelBlocks.FUEL_TANK.get(), new Item.Properties().stacksTo(64)));
    public static final RegistryObject<FuelTankItem> LARGE_FUEL_TANK = ITEMS.register("large_fuel_tank",
            () -> new FuelTankItem(LiquidFuelBlocks.LARGE_FUEL_TANK.get(), new Item.Properties().stacksTo(64)));

    private LiquidFuelItems() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
