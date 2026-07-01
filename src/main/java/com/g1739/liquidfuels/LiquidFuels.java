package com.g1739.liquidfuels;

import com.g1739.liquidfuels.config.LiquidFuelConfig;
import com.g1739.liquidfuels.registry.LiquidFuelBlockEntities;
import com.g1739.liquidfuels.registry.LiquidFuelBlocks;
import com.g1739.liquidfuels.registry.LiquidFuelCreativeTabs;
import com.g1739.liquidfuels.registry.LiquidFuelItems;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(LiquidFuels.MOD_ID)
public final class LiquidFuels {
    public static final String MOD_ID = "liquidfuels";
    public static final Logger LOGGER = LogUtils.getLogger();

    public LiquidFuels(ModContainer modContainer, IEventBus modBus) {
        LiquidFuelBlocks.register(modBus);
        LiquidFuelItems.register(modBus);
        LiquidFuelBlockEntities.register(modBus);
        LiquidFuelCreativeTabs.register(modBus);
        modBus.addListener(LiquidFuelCapabilities::register);

        modContainer.registerConfig(ModConfig.Type.COMMON, LiquidFuelConfig.SPEC);
    }
}
