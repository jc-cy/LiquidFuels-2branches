package com.g1739.liquidfuels.registry;

import com.g1739.liquidfuels.LiquidFuels;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class LiquidFuelCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LiquidFuels.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.liquidfuels.main"))
            .icon(() -> LiquidFuelItems.FUEL_TANK.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(LiquidFuelItems.SMALL_FUEL_TANK.get());
                output.accept(LiquidFuelItems.FUEL_TANK.get());
                output.accept(LiquidFuelItems.LARGE_FUEL_TANK.get());
            })
            .build());

    private LiquidFuelCreativeTabs() {
    }

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
