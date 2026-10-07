package com.cfa.mekbyproducts.mixin;

import com.cfa.mekbyproducts.tile.ByproductMachine;
import mekanism.common.inventory.container.tile.FactoryContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Pushes the player inventory down by one row to make room for the byproduct row (same offset the Sawing factory uses). */
@Mixin(value = FactoryContainer.class, remap = false)
public class FactoryContainerMixin {

    @Inject(method = "getInventoryYOffset", at = @At("HEAD"), cancellable = true)
    private void mekbyproducts$byproductRow(CallbackInfoReturnable<Integer> cir) {
        if (((FactoryContainer) (Object) this).getTileEntity() instanceof ByproductMachine) {
            cir.setReturnValue(105);
        }
    }
}
