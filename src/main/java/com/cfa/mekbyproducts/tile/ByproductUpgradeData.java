package com.cfa.mekbyproducts.tile;

import com.cfa.mekbyproducts.recipe.ByproductProcessing;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.upgrade.MachineUpgradeData;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;

/** Standard machine upgrade data plus the byproduct slot contents, so tier upgrades don't delete byproducts. */
public class ByproductUpgradeData extends MachineUpgradeData {

    public final List<ItemStack> byproducts;

    /** @param outputSlots main outputs only; byproducts travel separately because slot layouts differ between tiers. */
    public ByproductUpgradeData(HolderLookup.Provider provider, boolean redstone, RedstoneControl controlType, IEnergyContainer energyContainer, int[] progress,
          EnergyInventorySlot energySlot, List<IInventorySlot> inputSlots, List<IInventorySlot> outputSlots, boolean sorting, List<ITileComponent> components,
          List<IInventorySlot> byproductSlots) {
        super(provider, redstone, controlType, energyContainer, progress, energySlot, inputSlots, outputSlots, sorting, components);
        this.byproducts = byproductSlots.stream().map(slot -> slot.getStack().copy()).filter(stack -> !stack.isEmpty()).toList();
    }

    /** Puts carried byproducts into the upgraded machine. */
    public static void restore(IUpgradeData data, List<IInventorySlot> byproductSlots) {
        // ponytail: no overflow handling, every upgrade goes 4 -> 6 or 6 -> 6 slots so it always fits
        if (data instanceof ByproductUpgradeData byproductData) {
            byproductData.byproducts.forEach(stack -> ByproductProcessing.insert(byproductSlots, stack.copy(), Action.EXECUTE));
        }
    }
}
