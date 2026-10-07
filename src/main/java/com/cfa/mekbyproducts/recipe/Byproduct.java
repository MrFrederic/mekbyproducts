package com.cfa.mekbyproducts.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/** One chance-based extra output of a crushing recipe. {@code chance} is in (0, 1]. */
public record Byproduct(ItemStack output, double chance) {

    public static final int MAX_PER_RECIPE = 4;

    private static final Codec<Double> CHANCE = Codec.DOUBLE.validate(d -> d > 0 && d <= 1 ? DataResult.success(d)
          : DataResult.error(() -> "Byproduct chance must be greater than 0 and at most 1, found " + d));

    public static final Codec<Byproduct> CODEC = RecordCodecBuilder.create(i -> i.group(
          ItemStack.CODEC.fieldOf("output").forGetter(Byproduct::output),
          CHANCE.fieldOf("chance").forGetter(Byproduct::chance)
    ).apply(i, Byproduct::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Byproduct> STREAM_CODEC = StreamCodec.composite(
          ItemStack.STREAM_CODEC, Byproduct::output,
          ByteBufCodecs.DOUBLE, Byproduct::chance,
          Byproduct::new
    );
}
