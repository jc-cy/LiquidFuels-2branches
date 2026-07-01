package com.g1739.liquidfuels.registry;

import com.g1739.liquidfuels.LiquidFuels;
import com.g1739.liquidfuels.blockentity.FuelTankBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class LiquidFuelBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, LiquidFuels.MOD_ID);

    public static final Supplier<BlockEntityType<FuelTankBlockEntity>> FUEL_TANK = BLOCK_ENTITIES.register("fuel_tank",
            () -> BlockEntityType.Builder.of(FuelTankBlockEntity::new,
                    LiquidFuelBlocks.SMALL_FUEL_TANK.get(),
                    LiquidFuelBlocks.FUEL_TANK.get(),
                    LiquidFuelBlocks.LARGE_FUEL_TANK.get()).build(null));

    private LiquidFuelBlockEntities() {
    }

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
