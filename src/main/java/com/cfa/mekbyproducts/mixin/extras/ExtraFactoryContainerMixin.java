package com.cfa.mekbyproducts.mixin.extras;

import com.cfa.mekbyproducts.tile.ByproductMachine;
import com.jerry.mekextras.common.inventory.container.tile.ExtraFactoryContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ExtraFactoryContainer.class, remap = false)
public class ExtraFactoryContainerMixin {

    @Inject(method = "getInventoryYOffset", at = @At("HEAD"), cancellable = true)
    private void mekbyproducts$byproductRow(CallbackInfoReturnable<Integer> cir) {
        if (((ExtraFactoryContainer) (Object) this).getTileEntity() instanceof ByproductMachine) {
            cir.setReturnValue(105);
        }
    }
}
