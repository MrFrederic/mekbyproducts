package com.cfa.mekbyproducts.recipe;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

/** JSON: {@code input}, {@code output} (same as mekanism:crushing) plus optional {@code byproducts: [{output, chance}]}, at most 4. */
public class ByproductCrushingSerializer implements RecipeSerializer<ByproductCrushingRecipe> {

    public static final MapCodec<ByproductCrushingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
          ItemStackIngredient.CODEC.fieldOf("input").forGetter(ByproductCrushingRecipe::getInput),
          ItemStack.CODEC.fieldOf("output").forGetter(ByproductCrushingRecipe::getOutputRaw),
          Byproduct.CODEC.listOf().validate(list -> list.size() <= Byproduct.MAX_PER_RECIPE ? DataResult.success(list)
                      : DataResult.error(() -> "At most " + Byproduct.MAX_PER_RECIPE + " byproducts are allowed, found " + list.size()))
                .optionalFieldOf("byproducts", List.of()).forGetter(ByproductCrushingRecipe::getByproducts)
    ).apply(i, ByproductCrushingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ByproductCrushingRecipe> STREAM_CODEC = StreamCodec.composite(
          ItemStackIngredient.STREAM_CODEC, ByproductCrushingRecipe::getInput,
          ItemStack.STREAM_CODEC, ByproductCrushingRecipe::getOutputRaw,
          Byproduct.STREAM_CODEC.apply(ByteBufCodecs.list(Byproduct.MAX_PER_RECIPE)), ByproductCrushingRecipe::getByproducts,
          ByproductCrushingRecipe::new
    );

    @Override
    public MapCodec<ByproductCrushingRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ByproductCrushingRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
