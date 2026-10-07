package com.cfa.mekbyproducts.gametest;

import com.cfa.mekbyproducts.MekByproducts;
import com.cfa.mekbyproducts.compat.extras.TileEntityExtraByproductCrushingFactory;
import com.cfa.mekbyproducts.tile.ByproductMachine;
import com.cfa.mekbyproducts.tile.TileEntityByproductCrusher;
import com.cfa.mekbyproducts.tile.TileEntityByproductCrushingFactory;
import com.jerry.mekextras.common.registries.ExtraBlocks;
import com.jerry.mekextras.common.tier.ExtraFactoryTier;
import java.util.List;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world checks: the swapped block entities exist, process the test recipe (test/sponge.json, both byproducts at 100%) and keep byproducts across upgrades. */
@GameTestHolder(MekByproducts.MODID)
@PrefixGameTestTemplate(false)
public class CrushingGameTests {

    private static final BlockPos POS = new BlockPos(2, 1, 2);

    private static <T> T place(GameTestHelper helper, Block block, Class<T> expected) {
        helper.setBlock(POS, block);
        Object tile = helper.getBlockEntity(POS);
        if (!expected.isInstance(tile)) {
            throw new GameTestAssertException("Expected " + expected.getSimpleName() + " but got " + (tile == null ? "nothing" : tile.getClass().getName()));
        }
        return expected.cast(tile);
    }

    private static int count(List<IInventorySlot> slots, Item item) {
        return slots.stream().filter(slot -> slot.getStack().is(item)).mapToInt(IInventorySlot::getCount).sum();
    }

    private static void runAndExpectByproducts(GameTestHelper helper, TileEntityMekanism tile, IInventorySlot input, IInventorySlot mainOutput) {
        tile.getEnergyContainers(null).forEach(container -> container.setEnergy(container.getMaxEnergy()));
        input.setStack(new ItemStack(Items.SPONGE, 1));
        List<IInventorySlot> byproducts = ((ByproductMachine) tile).getByproductSlots();
        helper.succeedWhen(() -> {
            helper.assertTrue(mainOutput.getStack().is(Items.RAW_IRON), "main output missing");
            helper.assertTrue(count(byproducts, Items.GOLD_NUGGET) == 2, "expected 2 gold nuggets as byproduct");
            helper.assertTrue(count(byproducts, Items.FLINT) == 1, "expected 1 flint as byproduct");
        });
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void crusherMakesByproducts(GameTestHelper helper) {
        TileEntityByproductCrusher crusher = place(helper, MekanismBlocks.CRUSHER.get(), TileEntityByproductCrusher.class);
        List<IInventorySlot> slots = crusher.getInventorySlots(null);
        // Stock slot order is input, output, energy; byproducts are appended after.
        helper.assertTrue(slots.size() == 3 + ByproductMachine.CRUSHER_SLOTS, "unexpected slot count " + slots.size());
        runAndExpectByproducts(helper, crusher, slots.get(0), slots.get(1));
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void crushingFactoryMakesByproducts(GameTestHelper helper) {
        TileEntityByproductCrushingFactory factory = place(helper, MekanismBlocks.getFactory(FactoryTier.BASIC, FactoryType.CRUSHING).get(),
              TileEntityByproductCrushingFactory.class);
        List<IInventorySlot> slots = factory.getInventorySlots(null);
        // Per process: input then output.
        runAndExpectByproducts(helper, factory, slots.get(0), slots.get(1));
    }

    @GameTest(template = "empty")
    public static void upgradeKeepsByproducts(GameTestHelper helper) {
        TileEntityByproductCrusher crusher = place(helper, MekanismBlocks.CRUSHER.get(), TileEntityByproductCrusher.class);
        crusher.getByproductSlots().get(0).setStack(new ItemStack(Items.FLINT, 10));
        crusher.getByproductSlots().get(3).setStack(new ItemStack(Items.GOLD_NUGGET, 7));
        var data = crusher.getUpgradeData(helper.getLevel().registryAccess());
        TileEntityByproductCrushingFactory factory = place(helper, MekanismBlocks.getFactory(FactoryTier.BASIC, FactoryType.CRUSHING).get(),
              TileEntityByproductCrushingFactory.class);
        factory.parseUpgradeData(helper.getLevel().registryAccess(), data);
        helper.assertTrue(count(factory.getByproductSlots(), Items.FLINT) == 10, "flint lost in upgrade");
        helper.assertTrue(count(factory.getByproductSlots(), Items.GOLD_NUGGET) == 7, "nuggets lost in upgrade");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void extrasFactoryMakesByproducts(GameTestHelper helper) {
        if (!ModList.get().isLoaded("mekanism_extras")) {
            helper.succeed(); // nothing to check without Extras
            return;
        }
        ExtrasCheck.run(helper);
    }

    /** Separate class so Extras types are only loaded when Extras is present. */
    private static final class ExtrasCheck {

        static void run(GameTestHelper helper) {
            TileEntityExtraByproductCrushingFactory factory = place(helper, ExtraBlocks.getExtraFactory(ExtraFactoryTier.ABSOLUTE, FactoryType.CRUSHING).get(),
                  TileEntityExtraByproductCrushingFactory.class);
            List<IInventorySlot> slots = factory.getInventorySlots(null);
            runAndExpectByproducts(helper, factory, slots.get(0), slots.get(1));
        }
    }
}
