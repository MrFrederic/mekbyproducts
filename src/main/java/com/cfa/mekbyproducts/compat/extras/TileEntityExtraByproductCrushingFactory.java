package com.cfa.mekbyproducts.compat.extras;

import com.cfa.mekbyproducts.mixin.extras.ExtraFactoryAccessor;
import com.cfa.mekbyproducts.recipe.ByproductProcessing;
import com.cfa.mekbyproducts.tile.ByproductMachine;
import com.cfa.mekbyproducts.tile.ByproductUpgradeData;
import com.cfa.mekbyproducts.tile.TileEntityByproductCrushingFactory;
import com.jerry.mekextras.common.tile.factory.TileEntityExtraItemStackToItemStackFactory;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.upgrade.MachineUpgradeData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Mekanism Extras crushing factory (Absolute-Infinite) with the same shared byproduct row. Extras copies Mekanism's factory classes instead of extending them, so
 * this mirrors {@link TileEntityByproductCrushingFactory} line for line. Only loaded when Extras is installed.
 */
public class TileEntityExtraByproductCrushingFactory extends TileEntityExtraItemStackToItemStackFactory implements ByproductMachine {

    private List<IInventorySlot> byproductSlots; // no initializer, see TileEntityByproductCrusher
    private final List<IInventorySlot> mainOutputs;

    public TileEntityExtraByproductCrushingFactory(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
        mainOutputs = List.copyOf(outputSlots);
        outputSlots.addAll(byproductSlots);
        ByproductMachine.registerOutputs(configComponent, inputSlots, outputSlots);
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        IInventorySlotHolder holder = super.getInitialInventory(listener);
        IContentsListener unpauseAll = () -> {
            listener.onContentsChanged();
            for (var monitor : recipeCacheLookupMonitors) {
                monitor.unpause();
            }
        };
        int index = tier.ordinal();
        int width = 176 + 36 * (index + 2) + 2 * index; // GuiExtraFactory's width formula
        byproductSlots = TileEntityByproductCrushingFactory.createRow(unpauseAll, width);
        holder.getInventorySlots(null).addAll(byproductSlots);
        return holder;
    }

    @Override
    public List<IInventorySlot> getByproductSlots() {
        return byproductSlots;
    }

    @NotNull
    @Override
    public CachedRecipe<ItemStackToItemStackRecipe> createNewCachedRecipe(@NotNull ItemStackToItemStackRecipe recipe, int cacheIndex) {
        return ByproductProcessing.cachedRecipe(recipe, recheckAllRecipeErrors[cacheIndex], inputHandlers[cacheIndex],
                    ByproductProcessing.outputHandler(processInfoSlots[cacheIndex].outputSlot(), byproductSlots))
              .setErrorsChanged(errors -> errorTracker.onErrorsChanged(errors, cacheIndex))
              .setCanHolderFunction(this::canFunction)
              .setActive(active -> setActiveState(active, cacheIndex))
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setRequiredTicks(this::getTicksRequired)
              .setOnFinish(this::markForSave)
              .setBaselineMaxOperations(this::getOperationsPerTick)
              .setOperatingTicksChanged(operatingTicks -> progress[cacheIndex] = operatingTicks);
    }

    @NotNull
    @Override
    public MachineUpgradeData getUpgradeData(HolderLookup.Provider provider) {
        return new ByproductUpgradeData(provider, redstone, getControlType(), getEnergyContainer(), progress, ((ExtraFactoryAccessor) this).mekbyproducts$energySlot(),
              inputSlots, mainOutputs, isSorting(), getComponents(), byproductSlots);
    }

    @Override
    public void parseUpgradeData(HolderLookup.Provider provider, @NotNull IUpgradeData upgradeData) {
        super.parseUpgradeData(provider, upgradeData);
        ByproductUpgradeData.restore(upgradeData, byproductSlots);
    }
}
