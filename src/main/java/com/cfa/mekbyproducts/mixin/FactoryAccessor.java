package com.cfa.mekbyproducts.mixin;

import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.tile.factory.TileEntityFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TileEntityFactory.class, remap = false)
public interface FactoryAccessor {

    @Accessor("energySlot")
    EnergyInventorySlot mekbyproducts$energySlot();
}
