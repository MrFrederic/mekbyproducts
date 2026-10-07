package com.cfa.mekbyproducts;

import com.cfa.mekbyproducts.recipe.ByproductCrushingRecipe;
import com.cfa.mekbyproducts.recipe.ByproductCrushingSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(MekByproducts.MODID)
public class MekByproducts {

    public static final String MODID = "mekbyproducts";

    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MODID);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ByproductCrushingRecipe>> CRUSHING =
          SERIALIZERS.register("crushing", ByproductCrushingSerializer::new);

    public MekByproducts(IEventBus modBus) {
        SERIALIZERS.register(modBus);
    }
}
