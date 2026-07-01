package com.g1739.liquidfuels.mixin;

import com.g1739.liquidfuels.util.InventoryFluidTransfer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true, require = 1)
    private void liquidfuels$fillTankFromCarriedFluidContainer(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        if (InventoryFluidTransfer.tryFillTankFromCarried((AbstractContainerMenu) (Object) this, slotId, button, clickType, player)) {
            ci.cancel();
        }
    }
}
