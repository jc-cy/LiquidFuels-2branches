package com.g1739.liquidfuels.registry;

import com.g1739.liquidfuels.LiquidFuels;
import com.g1739.liquidfuels.blockentity.FuelTankBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class LiquidFuelBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, LiquidFuels.MOD_ID);

    public static final RegistryObject<BlockEntityType<FuelTankBlockEntity>> FUEL_TANK = BLOCK_ENTITIES.register("fuel_tank",
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
