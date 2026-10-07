package com.cfa.mekbyproducts.mixin.jei;

import com.cfa.mekbyproducts.recipe.Byproduct;
import com.cfa.mekbyproducts.recipe.ByproductCrushingRecipe;
import java.util.List;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.recipe_viewer.jei.machine.ItemStackToItemStackRecipeCategory;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Shows byproducts (with their chance in the tooltip) on Mekanism's crushing JEI page, laid out like the crusher's byproduct row. */
@Mixin(value = ItemStackToItemStackRecipeCategory.class, remap = false)
public class CrushingCategoryMixin {

    @Shadow
    @Final
    private GuiSlot output;

    @Inject(method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lnet/minecraft/world/item/crafting/RecipeHolder;Lmezz/jei/api/recipe/IFocusGroup;)V",
          at = @At("TAIL"))
    private void mekbyproducts$byproducts(IRecipeLayoutBuilder builder, RecipeHolder<ItemStackToItemStackRecipe> holder, IFocusGroup focuses, CallbackInfo ci) {
        if (!(holder.value() instanceof ByproductCrushingRecipe recipe)) {
            return;
        }
        List<Byproduct> byproducts = recipe.getByproducts();
        for (int i = 0; i < byproducts.size(); i++) {
            Byproduct byproduct = byproducts.get(i);
            // Machine GUI: main output at (116,35), byproduct row at (88 + 18i, 53). Offsets keep the same layout here.
            int x = output.getX() + 1 - 28 + 18 * i;
            int y = output.getY() + 1 + 18;
            Component chance = Component.literal(formatChance(byproduct.chance()) + " chance").withStyle(ChatFormatting.GOLD);
            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                  .setStandardSlotBackground()
                  .addItemStack(byproduct.output())
                  .addRichTooltipCallback((view, tooltip) -> tooltip.add(chance));
        }
    }

    private static String formatChance(double chance) {
        double percent = chance * 100;
        return percent == Math.floor(percent) ? (int) percent + "%" : String.format("%.1f%%", percent);
    }
}
