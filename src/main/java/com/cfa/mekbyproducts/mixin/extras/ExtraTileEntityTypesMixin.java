package com.cfa.mekbyproducts.mixin.extras;

import com.cfa.mekbyproducts.compat.extras.TileEntityExtraByproductCrushingFactory;
import com.jerry.mekextras.common.registries.ExtraTileEntityTypes;
import com.llamalad7.mixinextras.sugar.Local;
import mekanism.common.content.blocktype.FactoryType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Same constructor swap for Mekanism Extras' Absolute-Infinite crushing factories. */
@Mixin(value = ExtraTileEntityTypes.class, remap = false)
public class ExtraTileEntityTypesMixin {

    @ModifyArg(method = "registerFactory", at = @At(value = "INVOKE",
          target = "Lmekanism/common/registration/impl/TileEntityTypeDeferredRegister;mekBuilder(Lnet/neoforged/neoforge/registries/DeferredHolder;Lnet/minecraft/world/level/block/entity/BlockEntityType$BlockEntitySupplier;)Lmekanism/common/registration/impl/TileEntityTypeDeferredRegister$BlockEntityTypeBuilder;"),
          index = 1)
    private static BlockEntitySupplier<?> mekbyproducts$crushingFactory(DeferredHolder<Block, ?> block, BlockEntitySupplier<?> factory,
          @Local(argsOnly = true) FactoryType type) {
        return type == FactoryType.CRUSHING ? (pos, state) -> new TileEntityExtraByproductCrushingFactory(block, pos, state) : factory;
    }
}
