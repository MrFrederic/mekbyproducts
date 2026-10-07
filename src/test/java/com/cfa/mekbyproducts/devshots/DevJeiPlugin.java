package com.cfa.mekbyproducts.devshots;

import com.cfa.mekbyproducts.MekByproducts;
import com.mojang.logging.LogUtils;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Dev-only: grabs the JEI runtime so DevScreenshots can open Mekanism's crushing page for the test sponge recipe. */
@JeiPlugin
public class DevJeiPlugin implements IModPlugin {

    private static IJeiRuntime runtime;

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(MekByproducts.MODID, "devshots");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    static void showSpongeRecipes() {
        if (runtime == null) {
            LogUtils.getLogger().warn("devshot: JEI runtime not available");
            return;
        }
        runtime.getRecipeManager().getRecipeType(ResourceLocation.fromNamespaceAndPath("mekanism", "crushing"))
              .ifPresentOrElse(DevJeiPlugin::show, () -> LogUtils.getLogger().warn("devshot: no JEI recipe type mekanism:crushing"));
    }

    private static <T> void show(RecipeType<T> type) {
        IFocus<ItemStack> focus = runtime.getJeiHelpers().getFocusFactory().createFocus(RecipeIngredientRole.INPUT, VanillaTypes.ITEM_STACK, new ItemStack(Items.SPONGE));
        IRecipeCategory<T> category = runtime.getRecipeManager().createRecipeCategoryLookup().limitTypes(List.of(type)).get()
              .map(c -> (IRecipeCategory<T>) c).findFirst().orElseThrow();
        List<T> recipes = runtime.getRecipeManager().createRecipeLookup(type).limitFocus(List.of(focus)).get().toList();
        runtime.getRecipesGui().showRecipes(category, recipes, List.of(focus));
    }
}
