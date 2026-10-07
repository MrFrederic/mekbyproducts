package com.cfa.mekbyproducts.mixin.client;

import com.cfa.mekbyproducts.tile.ByproductMachine;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.machine.GuiFactory;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.factory.TileEntityFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Gives our crushing factories the same extra row of height the Sawing factory gets. Slot backgrounds draw themselves (dynamicSlots). */
@Mixin(value = GuiFactory.class, remap = false)
public abstract class GuiFactoryMixin extends GuiConfigurableTile<TileEntityFactory<?>, MekanismTileContainer<TileEntityFactory<?>>> {

    private GuiFactoryMixin(MekanismTileContainer<TileEntityFactory<?>> container, Inventory inv, Component title) {
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
