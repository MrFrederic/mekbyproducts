package com.cfa.mekbyproducts.devshots;

import com.cfa.mekbyproducts.MekByproducts;
import com.cfa.mekbyproducts.tile.ByproductMachine;
import com.jerry.mekextras.common.registries.ExtraBlocks;
import com.jerry.mekextras.common.tier.ExtraFactoryTier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Dev-only (test source set, needs -Dmekbyproducts.devshots=true): after joining a world, places each replaced machine, fills it with sample items, opens its GUI
 * and saves a screenshot to run/client/screenshots, then shows the JEI crushing page and quits.
 */
@EventBusSubscriber(modid = MekByproducts.MODID, value = Dist.CLIENT)
public final class DevScreenshots {

    private static final boolean ENABLED = Boolean.getBoolean("mekbyproducts.devshots");
    private static final int STEP = 60;
    private static final List<Runnable> STEPS = new ArrayList<>();
    private static int ticks;
    private static int waiting;

    private DevScreenshots() {
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (!ENABLED) {
            return;
        }
        if (mc.player == null || mc.getSingleplayerServer() == null) {
            // Diagnose a stuck boot: log and capture whatever screen is up every ~10 s.
            if (++waiting % 200 == 0) {
                com.mojang.logging.LogUtils.getLogger().info("devshot waiting on screen {}", mc.screen == null ? "none" : mc.screen.getClass().getName());
                shot(mc, "waiting_" + waiting / 200);
            }
            return;
        }
        if (ticks == 0) {
            plan(mc);
        }
        ticks++;
        if (ticks % STEP == 0 && ticks / STEP - 1 < STEPS.size()) {
            STEPS.get(ticks / STEP - 1).run();
        }
    }

    private static void plan(Minecraft mc) {
        machine(mc, "crusher", () -> MekanismBlocks.CRUSHER.get());
        machine(mc, "factory_basic", () -> MekanismBlocks.getFactory(FactoryTier.BASIC, FactoryType.CRUSHING).get());
        machine(mc, "factory_ultimate", () -> MekanismBlocks.getFactory(FactoryTier.ULTIMATE, FactoryType.CRUSHING).get());
        if (ModList.get().isLoaded("mekanism_extras")) {
            machine(mc, "factory_extras_absolute", () -> ExtraBlocks.getExtraFactory(ExtraFactoryTier.ABSOLUTE, FactoryType.CRUSHING).get());
            machine(mc, "factory_extras_infinite", () -> ExtraBlocks.getExtraFactory(ExtraFactoryTier.INFINITE, FactoryType.CRUSHING).get());
        }
        STEPS.add(() -> {
            mc.setScreen(null);
            DevJeiPlugin.showSpongeRecipes();
        });
        STEPS.add(() -> shot(mc, "jei_crushing"));
        STEPS.add(mc::stop);
    }

    /** Three steps: place + fill on the server, open the GUI once the client has the block, then screenshot. */
    private static void machine(Minecraft mc, String name, Supplier<Block> block) {
        BlockPos[] pos = new BlockPos[1];
        STEPS.add(() -> {
            mc.setScreen(null);
            mc.getSingleplayerServer().execute(() -> {
                ServerPlayer player = mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();
                pos[0] = player.blockPosition().offset(2, 0, 0);
                player.serverLevel().setBlockAndUpdate(pos[0], block.get().defaultBlockState());
                TileEntityMekanism tile = (TileEntityMekanism) player.serverLevel().getBlockEntity(pos[0]);
                List<IInventorySlot> slots = tile.getInventorySlots(null);
                slots.get(0).setStack(new ItemStack(Items.SPONGE, 8));
                slots.get(1).setStack(new ItemStack(Items.RAW_IRON, 5));
                List<IInventorySlot> byproducts = ((ByproductMachine) tile).getByproductSlots();
                byproducts.get(0).setStack(new ItemStack(Items.GOLD_NUGGET, 12));
                byproducts.get(1).setStack(new ItemStack(Items.FLINT, 3));
                byproducts.getLast().setStack(new ItemStack(Items.AMETHYST_SHARD, 1));
            });
        });
        STEPS.add(() -> mc.getSingleplayerServer().execute(() -> {
            ServerPlayer player = mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();
            ((TileEntityMekanism) player.serverLevel().getBlockEntity(pos[0])).openGui(player);
        }));
        STEPS.add(() -> shot(mc, name));
    }

    private static void shot(Minecraft mc, String name) {
        Screenshot.grab(mc.gameDirectory, name + ".png", mc.getMainRenderTarget(), msg -> com.mojang.logging.LogUtils.getLogger().info("devshot {}: {}", name, msg.getString()));
    }
}
