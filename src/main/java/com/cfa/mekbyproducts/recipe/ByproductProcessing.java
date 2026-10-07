package com.cfa.mekbyproducts.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.cache.OneInputCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/** Glue between a crushing recipe and a machine's main output slot plus its byproduct slots. */
public final class ByproductProcessing {

    private static final RandomSource RANDOM = RandomSource.create();

    private ByproductProcessing() {
    }

    /** What one operation of a recipe produces: the main output plus the byproducts that may be rolled. */
    public record CrushOutput(ItemStack main, List<Byproduct> byproducts) {

        static CrushOutput of(ItemStackToItemStackRecipe recipe, ItemStack input) {
            List<Byproduct> byproducts = recipe instanceof ByproductCrushingRecipe r ? r.getByproducts() : List.of();
            return new CrushOutput(recipe.getOutput(input), byproducts);
        }
    }

    /** Drop-in replacement for {@code OneInputCachedRecipe.itemToItem} that also handles byproducts. */
    public static CachedRecipe<ItemStackToItemStackRecipe> cachedRecipe(ItemStackToItemStackRecipe recipe, BooleanSupplier recheckAllErrors,
          IInputHandler<@NotNull ItemStack> inputHandler, IOutputHandler<CrushOutput> outputHandler) {
        return new OneInputCachedRecipe<>(recipe, recheckAllErrors, inputHandler, outputHandler, recipe::getInput, input -> CrushOutput.of(recipe, input),
              ConstantPredicates.ITEM_EMPTY, output -> output.main().isEmpty()) {
        };
    }

    public static IOutputHandler<CrushOutput> outputHandler(IInventorySlot mainSlot, List<IInventorySlot> byproductSlots) {
        return new IOutputHandler<>() {
            @Override
            public void handleOutput(CrushOutput output, int operations) {
                if (operations <= 0) {
                    return;
                }
                insert(List.of(mainSlot), output.main().copyWithCount(output.main().getCount() * operations), Action.EXECUTE);
                for (Byproduct byproduct : output.byproducts()) {
                    int hits = 0;
                    for (int i = 0; i < operations; i++) {
                        if (RANDOM.nextDouble() < byproduct.chance()) {
                            hits++;
                        }
                    }
                    if (hits > 0) {
                        insert(byproductSlots, byproduct.output().copyWithCount(byproduct.output().getCount() * hits), Action.EXECUTE);
                    }
                }
            }

            @Override
            public void calculateOperationsCanSupport(OperationTracker tracker, CrushOutput output) {
                ItemStack main = output.main();
                int operations = main.getMaxStackSize() / Math.max(1, main.getCount());
                if (!main.isEmpty()) {
                    ItemStack remainder = mainSlot.insertItem(main.copyWithCount(main.getMaxStackSize()), Action.SIMULATE, AutomationType.INTERNAL);
                    operations = (main.getMaxStackSize() - remainder.getCount()) / main.getCount();
                }
                if (operations > 0 && !output.byproducts().isEmpty()) {
                    operations = Math.min(operations, byproductOperations(byproductSlots, output.byproducts(), operations));
                }
                tracker.updateOperations(operations);
                if (operations == 0) {
                    tracker.addError(RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
                }
            }
        };
    }

    /**
     * How many operations (up to {@code cap}) fit if every byproduct hits every time. Simulated on copies of the slots, because several different byproducts compete
     * for the same shared slots.
     */
    static int byproductOperations(List<IInventorySlot> slots, List<Byproduct> byproducts, int cap) {
        // ponytail: simulates one operation at a time, cap <= 64 so at most ~64*4*6 cheap checks per tick
        List<ItemStack> contents = new ArrayList<>(slots.size());
        List<Integer> limits = new ArrayList<>(slots.size());
        for (IInventorySlot slot : slots) {
            contents.add(slot.getStack().copy());
            limits.add(slot.getLimit(slot.getStack()));
        }
        for (int done = 0; done < cap; done++) {
            for (Byproduct byproduct : byproducts) {
                if (!simulateInsert(contents, limits, byproduct.output())) {
                    return done;
                }
            }
        }
        return cap;
    }

    private static boolean simulateInsert(List<ItemStack> contents, List<Integer> limits, ItemStack toAdd) {
        int left = toAdd.getCount();
        for (int i = 0; i < contents.size() && left > 0; i++) {
            ItemStack stack = contents.get(i);
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, toAdd)) {
                int moved = Math.min(left, Math.min(limits.get(i), stack.getMaxStackSize()) - stack.getCount());
                if (moved > 0) {
                    stack.grow(moved);
                    left -= moved;
                }
            }
        }
        for (int i = 0; i < contents.size() && left > 0; i++) {
            if (contents.get(i).isEmpty()) {
                int moved = Math.min(left, toAdd.getMaxStackSize());
                contents.set(i, toAdd.copyWithCount(moved));
                left -= moved;
            }
        }
        return left == 0;
    }

    /** Inserts into slots that already hold the item first, then into empty ones. Returns what didn't fit. */
    public static ItemStack insert(List<IInventorySlot> slots, ItemStack stack, Action action) {
        for (int pass = 0; pass < 2 && !stack.isEmpty(); pass++) {
            for (IInventorySlot slot : slots) {
                if (stack.isEmpty()) {
                    break;
                }
                if (slot.isEmpty() == (pass == 1)) {
                    stack = slot.insertItem(stack, action, AutomationType.INTERNAL);
                }
            }
        }
        return stack;
    }
}
