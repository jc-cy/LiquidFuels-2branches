package com.g1739.liquidfuels.client;

import com.g1739.liquidfuels.LiquidFuels;
import com.g1739.liquidfuels.registry.LiquidFuelBlocks;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = LiquidFuels.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
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
