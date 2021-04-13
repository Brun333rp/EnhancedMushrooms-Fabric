package com.github.teamaurora.enhanced_mushrooms.mixin;

import com.github.teamaurora.enhanced_mushrooms.block.EMSignBlock;
import com.github.teamaurora.enhanced_mushrooms.block.EMWallSignBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;

@Mixin(BlockEntityType.class)
public class MixinBlockEntityType {
    @Inject(method = "supports", at = @At("HEAD"), cancellable = true)
    private void supports(Block block, CallbackInfoReturnable<Boolean> info) {
        //noinspection EqualsBetweenInconvertibleTypes
        if (BlockEntityType.SIGN.equals(this) && block instanceof EMSignBlock) {
            info.setReturnValue(true);
        }
        if (BlockEntityType.SIGN.equals(this) && block instanceof EMWallSignBlock) {
            info.setReturnValue(true);
        }
    }
}