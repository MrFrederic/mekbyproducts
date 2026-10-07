package com.cfa.mekbyproducts.tile;

import com.cfa.mekbyproducts.mixin.ElectricMachineAccessor;
import com.cfa.mekbyproducts.recipe.ByproductProcessing;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.tile.machine.TileEntityCrusher;
import mekanism.common.upgrade.MachineUpgradeData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/** Mekanism's Crusher plus 4 byproduct slots. Registered in place of TileEntityCrusher by MekanismTileEntityTypesMixin. */
public class TileEntityByproductCrusher extends TileEntityCrusher implements ByproductMachine {

    // No initializer: getInitialInventory fills this during the super constructor, and an initializer would wipe it afterwards.
    private List<IInventorySlot> byproductSlots;

    public TileEntityByproductCrusher(BlockPos pos, BlockState state) {
        super(pos, state);
        List<IInventorySlot> outputs = new ArrayList<>();
        outputs.add(accessor().mekbyproducts$outputSlot());
        outputs.addAll(byproductSlots);
        ByproductMachine.registerOutputs(configComponent, List.of(accessor().mekbyproducts$inputSlot()), outputs);
    }

    private ElectricMachineAccessor accessor() {
        return (ElectricMachineAccessor) this;
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        IInventorySlotHolder holder = super.getInitialInventory(listener, recipeCacheListener, recipeCacheUnpauseListener);
        byproductSlots = new ArrayList<>();
        for (int i = 0; i < CRUSHER_SLOTS; i++) {
            OutputInventorySlot slot = OutputInventorySlot.at(recipeCacheUnpauseListener, 88 + 18 * i, 53);
            slot.tracksWarnings(s -> s.warning(WarningType.NO_SPACE_IN_OUTPUT, getWarningCheck(RecipeError.NOT_ENOUGH_OUTPUT_SPACE)));
            byproductSlots.add(slot);
        }
        // ponytail: getInventorySlots(null) returns the holder's live slot list; appending after the energy slot keeps old saves' slot indices valid.
        // If Mekanism ever returns a copy here, the slots vanish from the GUI; swap for an @Invoker on ConfigInventorySlotHolder.addSlot.
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
        return ByproductProcessing.cachedRecipe(recipe, recheckAllRecipeErrors, inputHandler,
                    ByproductProcessing.outputHandler(accessor().mekbyproducts$outputSlot(), byproductSlots))
              .setErrorsChanged(this::onErrorsChanged)
              .setCanHolderFunction(this::canFunction)
              .setActive(this::setActive)
              .setEnergyRequirements(getEnergyContainer()::getEnergyPerTick, getEnergyContainer())
              .setRequiredTicks(this::getTicksRequired)
              .setOnFinish(this::markForSave)
              .setOperatingTicksChanged(this::setOperatingTicks);
    }

    @NotNull
    @Override
    public MachineUpgradeData getUpgradeData(HolderLookup.Provider provider) {
        return new ByproductUpgradeData(provider, redstone, getControlType(), getEnergyContainer(), new int[]{getOperatingTicks()},
              accessor().mekbyproducts$energySlot(), List.of(accessor().mekbyproducts$inputSlot()), List.of(accessor().mekbyproducts$outputSlot()), false,
              getComponents(), byproductSlots);
    }
}
