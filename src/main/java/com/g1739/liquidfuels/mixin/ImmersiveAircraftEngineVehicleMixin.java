package com.g1739.liquidfuels.mixin;

import com.g1739.liquidfuels.item.FuelTankItem;
import com.g1739.liquidfuels.util.FuelTankContents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "immersive_aircraft.entity.EngineVehicle", remap = false)
public abstract class ImmersiveAircraftEngineVehicleMixin {
    @Redirect(
            method = "refuel(I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;m_41774_(I)V"),
            require = 0,
            remap = false
    )
    private void liquidfuels$consumeFluidFuel(ItemStack stack, int decrement) {
        if (decrement == 1 && FuelTankItem.canUseStoredFuel(stack) && FuelTankContents.consumeFuelUnit(stack)) {
            return;
        }
        stack.shrink(decrement);
    }
}
