package com.cfa.mekbyproducts.mixin.extras.client;

import com.cfa.mekbyproducts.tile.ByproductMachine;
import com.jerry.mekextras.client.gui.machine.GuiExtraFactory;
import com.jerry.mekextras.common.tile.factory.TileEntityExtraFactory;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiExtraFactory.class, remap = false)
public abstract class GuiExtraFactoryMixin extends GuiConfigurableTile<TileEntityExtraFactory<?>, MekanismTileContainer<TileEntityExtraFactory<?>>> {

    private GuiExtraFactoryMixin(MekanismTileContainer<TileEntityExtraFactory<?>> container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void mekbyproducts$taller(CallbackInfo ci) {
        if (tile instanceof ByproductMachine) {
            imageHeight += 21;
            inventoryLabelY = 95;
        }
    }

    @ModifyArg(method = "addGuiElements", at = @At(value = "INVOKE",
          target = "Lmekanism/client/gui/element/bar/GuiVerticalPowerBar;<init>(Lmekanism/client/gui/IGuiWrapper;Lmekanism/api/energy/IEnergyContainer;III)V"), index = 4)
    private int mekbyproducts$powerBarHeight(int height) {
        return tile instanceof ByproductMachine ? 73 : height;
    }
}
