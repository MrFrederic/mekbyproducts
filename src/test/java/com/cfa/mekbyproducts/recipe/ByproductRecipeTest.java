package com.cfa.mekbyproducts.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class ByproductRecipeTest {

    private static final String GALENA = """
          {"input":{"count":1,"item":"minecraft:iron_ore"},
           "output":{"count":1,"id":"minecraft:raw_iron"},
           "byproducts":[{"chance":0.12,"output":{"count":1,"id":"minecraft:gold_nugget"}},
                         {"chance":1.0,"output":{"count":2,"id":"minecraft:flint"}}]}""";

    /** Returns null when the codec rejects the JSON. */
    private static ByproductCrushingRecipe parse(String json) {
        JsonElement element = JsonParser.parseString(json);
        return ByproductCrushingSerializer.CODEC.codec().parse(JsonOps.INSTANCE, element).result().orElse(null);
    }

    @Test
    void parsesAndRoundTripsOverTheNetwork() {
        ByproductCrushingRecipe recipe = parse(GALENA);
        assertEquals(Items.RAW_IRON, recipe.getOutputRaw().getItem());
        assertEquals(2, recipe.getByproducts().size());
        assertEquals(0.12, recipe.getByproducts().getFirst().chance());

        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        ByproductCrushingSerializer.STREAM_CODEC.encode(buf, recipe);
        ByproductCrushingRecipe decoded = ByproductCrushingSerializer.STREAM_CODEC.decode(buf);
        assertEquals(recipe.getByproducts().size(), decoded.getByproducts().size());
        assertTrue(ItemStack.matches(recipe.getByproducts().get(1).output(), decoded.getByproducts().get(1).output()));
        assertEquals(1.0, decoded.getByproducts().get(1).chance());
    }

    @Test
    void plainCrushingJsonStillParsesWithNoByproducts() {
        ByproductCrushingRecipe recipe = parse("""
              {"input":{"count":1,"item":"minecraft:iron_ingot"},"output":{"count":1,"id":"minecraft:iron_nugget"}}""");
        assertTrue(recipe.getByproducts().isEmpty());
    }

    @Test
    void rejectsBadChancesAndTooManyByproducts() {
        String byproduct = "{\"chance\":%s,\"output\":{\"count\":1,\"id\":\"minecraft:flint\"}}";
        String template = "{\"input\":{\"count\":1,\"item\":\"minecraft:iron_ore\"},\"output\":{\"count\":1,\"id\":\"minecraft:raw_iron\"},\"byproducts\":[%s]}";
        assertNull(parse(template.formatted(byproduct.formatted("0"))));
        assertNull(parse(template.formatted(byproduct.formatted("1.5"))));
        String five = String.join(",", List.of(byproduct.formatted("0.5"), byproduct.formatted("0.5"), byproduct.formatted("0.5"),
              byproduct.formatted("0.5"), byproduct.formatted("0.5")));
        assertNull(parse(template.formatted(five)));
    }

    @Test
    void sharedSlotsLimitOperationsWhenDifferentByproductsCompete() {
        List<IInventorySlot> slots = List.of(OutputInventorySlot.at(null, 0, 0), OutputInventorySlot.at(null, 0, 0));
        List<Byproduct> two = List.of(new Byproduct(new ItemStack(Items.FLINT, 16), 0.5), new Byproduct(new ItemStack(Items.GOLD_NUGGET, 32), 0.5));
        // Empty: flint fills one slot after 4 ops (64), nuggets the other after 2 (64).
        assertEquals(2, ByproductProcessing.byproductOperations(slots, two, 64));
        // A third distinct item takes a slot, so nuggets have nowhere to go.
        slots.get(1).setStack(new ItemStack(Items.DIRT));
        assertEquals(0, ByproductProcessing.byproductOperations(slots, two, 64));
        // Insert prefers stacking onto a matching slot over an empty one.
        slots.get(1).setEmpty();
        slots.get(1).setStack(new ItemStack(Items.FLINT, 10));
        ByproductProcessing.insert(slots, new ItemStack(Items.FLINT, 5), Action.EXECUTE);
        assertTrue(slots.getFirst().isEmpty());
        assertEquals(15, slots.get(1).getCount());
    }
}
