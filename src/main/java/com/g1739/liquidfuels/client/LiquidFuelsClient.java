package com.g1739.liquidfuels.client;

import com.g1739.liquidfuels.LiquidFuels;
import com.g1739.liquidfuels.registry.LiquidFuelBlocks;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = LiquidFuels.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class LiquidFuelsClient {
    private LiquidFuelsClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(LiquidFuelBlocks.SMALL_FUEL_TANK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(LiquidFuelBlocks.FUEL_TANK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(LiquidFuelBlocks.LARGE_FUEL_TANK.get(), RenderType.cutout());
        });
    }
}
