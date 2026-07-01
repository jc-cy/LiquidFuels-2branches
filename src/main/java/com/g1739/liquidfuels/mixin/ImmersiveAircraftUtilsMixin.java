package com.g1739.liquidfuels.mixin;

import com.g1739.liquidfuels.item.FuelTankItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "immersive_aircraft.util.Utils", remap = false)
public abstract class ImmersiveAircraftUtilsMixin {
    @Inject(method = "getFuelTime(Lnet/minecraft/world/item/ItemStack;)I", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private static void liquidfuels$getFuelTime(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack.getItem() instanceof FuelTankItem tankItem) {
            cir.setReturnValue(tankItem.getBurnTime(stack, null));
        }
    }
}
