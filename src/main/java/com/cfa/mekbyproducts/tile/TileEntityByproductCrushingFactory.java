package com.cfa.mekbyproducts.tile;

import com.cfa.mekbyproducts.mixin.FactoryAccessor;
import com.cfa.mekbyproducts.recipe.ByproductProcessing;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.recipe.lookup.monitor.FactoryRecipeCacheLookupMonitor;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.factory.TileEntityItemStackToItemStackFactory;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.upgrade.MachineUpgradeData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/** Mekanism crushing factory (Basic-Ultimate) plus one shared row of 6 byproduct slots. Registered in place of the stock one by MekanismTileEntityTypesMixin. */
public class TileEntityByproductCrushingFactory extends TileEntityItemStackToItemStackFactory implements ByproductMachine {

    // No initializer: filled during the super constructor (see TileEntityByproductCrusher).
    private List<IInventorySlot> byproductSlots;
    private final List<IInventorySlot> mainOutputs;

    public TileEntityByproductCrushingFactory(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
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
            for (FactoryRecipeCacheLookupMonitor<?> monitor : recipeCacheLookupMonitors) {
                monitor.unpause();
            }
        };
        int width = tier == FactoryTier.ULTIMATE ? 210 : 176;
        byproductSlots = createRow(unpauseAll, width);
        holder.getInventorySlots(null).addAll(byproductSlots); // live list, see TileEntityByproductCrusher
        return holder;
    }

    /** Six output slots in one row, centred in a GUI {@code guiWidth} wide, under the process outputs (same y as the Sawing factory's second row). */
    public static List<IInventorySlot> createRow(IContentsListener listener, int guiWidth) {
        List<IInventorySlot> row = new ArrayList<>();
        int startX = (guiWidth - FACTORY_SLOTS * 18) / 2;
        for (int i = 0; i < FACTORY_SLOTS; i++) {
            row.add(OutputInventorySlot.at(listener, startX + 18 * i, 77));
        }
        return row;
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
              .setOperatingTicksChanged(operatingTicks -> progress[cacheIndex] = operatingTicks)
              .setBaselineMaxOperations(this::getOperationsPerTick);
    }

    @NotNull
    @Override
    public MachineUpgradeData getUpgradeData(HolderLookup.Provider provider) {
        return new ByproductUpgradeData(provider, redstone, getControlType(), getEnergyContainer(), progress, ((FactoryAccessor) this).mekbyproducts$energySlot(),
              inputSlots, mainOutputs, isSorting(), getComponents(), byproductSlots);
    }

    @Override
    public void parseUpgradeData(HolderLookup.Provider provider, @NotNull IUpgradeData upgradeData) {
        super.parseUpgradeData(provider, upgradeData);
        ByproductUpgradeData.restore(upgradeData, byproductSlots);
    }
}
