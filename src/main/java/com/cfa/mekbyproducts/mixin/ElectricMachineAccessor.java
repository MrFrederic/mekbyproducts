package com.cfa.mekbyproducts.mixin;

import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.tile.prefab.TileEntityElectricMachine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TileEntityElectricMachine.class, remap = false)
public interface ElectricMachineAccessor {

    @Accessor("inputSlot")
    InputInventorySlot mekbyproducts$inputSlot();

    @Accessor("outputSlot")
    OutputInventorySlot mekbyproducts$outputSlot();

    @Accessor("energySlot")
    EnergyInventorySlot mekbyproducts$energySlot();
}
