package com.cfa.mekbyproducts.mixin;

import com.cfa.mekbyproducts.tile.TileEntityByproductCrusher;
import com.cfa.mekbyproducts.tile.TileEntityByproductCrushingFactory;
import com.llamalad7.mixinextras.sugar.Local;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismTileEntityTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Makes Mekanism build our crusher and crushing-factory block entities. Blocks, items and registry ids stay Mekanism's. */
@Mixin(value = MekanismTileEntityTypes.class, remap = false)
public class MekanismTileEntityTypesMixin {

    private static final String MEK_BUILDER = "Lmekanism/common/registration/impl/TileEntityTypeDeferredRegister;mekBuilder(Lnet/neoforged/neoforge/registries/DeferredHolder;Lnet/minecraft/world/level/block/entity/BlockEntityType$BlockEntitySupplier;)Lmekanism/common/registration/impl/TileEntityTypeDeferredRegister$BlockEntityTypeBuilder;";

    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = MEK_BUILDER), index = 1)
    private static BlockEntitySupplier<?> mekbyproducts$crusher(DeferredHolder<Block, ?> block, BlockEntitySupplier<?> factory) {
        return block == MekanismBlocks.CRUSHER ? TileEntityByproductCrusher::new : factory;
    }

    @ModifyArg(method = "registerFactory", at = @At(value = "INVOKE", target = MEK_BUILDER), index = 1)
    private static BlockEntitySupplier<?> mekbyproducts$crushingFactory(DeferredHolder<Block, ?> block, BlockEntitySupplier<?> factory,
          @Local(argsOnly = true) FactoryType type) {
        return type == FactoryType.CRUSHING ? (pos, state) -> new TileEntityByproductCrushingFactory(block, pos, state) : factory;
    }
}
