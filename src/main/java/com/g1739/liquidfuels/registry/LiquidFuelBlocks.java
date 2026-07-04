package com.g1739.liquidfuels.registry;

import com.g1739.liquidfuels.LiquidFuels;
import com.g1739.liquidfuels.block.FuelTankBlock;
import com.g1739.liquidfuels.config.LiquidFuelConfig;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class LiquidFuelBlocks {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, LiquidFuels.MOD_ID);

    public static final RegistryObject<FuelTankBlock> SMALL_FUEL_TANK = registerTank("small_fuel_tank", LiquidFuelConfig.DEFAULT_SMALL_FUEL_TANK_CAPACITY);
    public static final RegistryObject<FuelTankBlock> FUEL_TANK = registerTank("fuel_tank", LiquidFuelConfig.DEFAULT_FUEL_TANK_CAPACITY);
    public static final RegistryObject<FuelTankBlock> LARGE_FUEL_TANK = registerTank("large_fuel_tank", LiquidFuelConfig.DEFAULT_LARGE_FUEL_TANK_CAPACITY);

    private LiquidFuelBlocks() {
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }

    private static RegistryObject<FuelTankBlock> registerTank(String name, int capacity) {
        return BLOCKS.register(name, () -> new FuelTankBlock(capacity, BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(0.67F, 6.0F)
                .sound(SoundType.METAL)
                .noOcclusion()));
    }
}
