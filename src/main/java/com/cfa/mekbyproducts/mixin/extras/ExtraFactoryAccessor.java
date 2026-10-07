package com.cfa.mekbyproducts.mixin.extras;

import com.jerry.mekextras.common.tile.factory.TileEntityExtraFactory;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TileEntityExtraFactory.class, remap = false)
public interface ExtraFactoryAccessor {

    @Accessor("energySlot")
    EnergyInventorySlot mekbyproducts$energySlot();
}
