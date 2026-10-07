package com.cfa.mekbyproducts.recipe;

import com.cfa.mekbyproducts.MekByproducts;
import java.util.List;
import mekanism.api.recipes.basic.BasicCrushingRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * A normal Mekanism crushing recipe (same recipe type, so every crusher, factory, cache and JEI page sees it) that also carries byproducts. Code that doesn't know
 * about byproducts just sees the main output.
 */
public class ByproductCrushingRecipe extends BasicCrushingRecipe {

    private final List<Byproduct> byproducts;

    public ByproductCrushingRecipe(ItemStackIngredient input, ItemStack output, List<Byproduct> byproducts) {
        super(input, output);
        this.byproducts = List.copyOf(byproducts);
    }

    public List<Byproduct> getByproducts() {
        return byproducts;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public RecipeSerializer<BasicCrushingRecipe> getSerializer() {
        return (RecipeSerializer) MekByproducts.CRUSHING.get();
    }
}
