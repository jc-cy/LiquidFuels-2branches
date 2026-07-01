package com.g1739.liquidfuels.registry;

import com.g1739.liquidfuels.LiquidFuels;
import com.g1739.liquidfuels.block.FuelTankBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class LiquidFuelBlocks {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, LiquidFuels.MOD_ID);

    public static final Supplier<FuelTankBlock> SMALL_FUEL_TANK = registerTank("small_fuel_tank", 1000);
    public static final Supplier<FuelTankBlock> FUEL_TANK = registerTank("fuel_tank", 5000);
    public static final Supplier<FuelTankBlock> LARGE_FUEL_TANK = registerTank("large_fuel_tank", 20000);

    private LiquidFuelBlocks() {
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }

    private static Supplier<FuelTankBlock> registerTank(String name, int capacity) {
        return BLOCKS.register(name, () -> new FuelTankBlock(capacity, BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(0.67F, 6.0F)
                .sound(SoundType.METAL)
                .noOcclusion()));
    }
}
