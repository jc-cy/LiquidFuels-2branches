package com.g1739.liquidfuels;

import com.g1739.liquidfuels.config.LiquidFuelConfig;
import com.g1739.liquidfuels.registry.LiquidFuelBlockEntities;
import com.g1739.liquidfuels.registry.LiquidFuelBlocks;
import com.g1739.liquidfuels.registry.LiquidFuelCreativeTabs;
import com.g1739.liquidfuels.registry.LiquidFuelItems;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(LiquidFuels.MOD_ID)
public final class LiquidFuels {
    public static final String MOD_ID = "liquidfuels";
    public static final Logger LOGGER = LogUtils.getLogger();

    public LiquidFuels() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        LiquidFuelBlocks.register(modBus);
        LiquidFuelItems.register(modBus);
        LiquidFuelBlockEntities.register(modBus);
        LiquidFuelCreativeTabs.register(modBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, LiquidFuelConfig.SPEC);
    }
}
