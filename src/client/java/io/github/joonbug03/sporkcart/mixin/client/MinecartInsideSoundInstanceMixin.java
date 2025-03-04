package io.github.joonbug03.sporkcart.mixin.client;

import io.github.joonbug03.sporkcart.block.TrackTiesBlock;
import io.github.joonbug03.sporkcart.entity.TrackFollowerEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.sound.MinecartInsideSoundInstance;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecartInsideSoundInstance.class)
public abstract class MinecartInsideSoundInstanceMixin extends MovingSoundInstance {
    @Shadow @Final private AbstractMinecartEntity minecart;

    protected MinecartInsideSoundInstanceMixin(SoundEvent soundEvent, SoundCategory soundCategory, Random random) {
        super(soundEvent, soundCategory, random);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void sporkcart$adjustSoundWhenOnTrack(CallbackInfo info) {
        if (!this.isDone()) {
            World world = minecart.getWorld();
            BlockPos blockPosBelow = minecart.getBlockPos().down();
            BlockState blockStateBelow = world.getBlockState(blockPosBelow);
            BlockPos blockPosBelow2 = minecart.getBlockPos().down(2);
            BlockState blockStateBelow2 = world.getBlockState(blockPosBelow2);
            BlockPos blockPosBelow3 = minecart.getBlockPos().down(3);
            BlockState blockStateBelow3 = world.getBlockState(blockPosBelow3);

            if (blockStateBelow.isOf(Blocks.WHITE_WOOL) || blockStateBelow.isOf(Blocks.ORANGE_WOOL) ||
                    blockStateBelow.isOf(Blocks.MAGENTA_WOOL) || blockStateBelow.isOf(Blocks.LIGHT_BLUE_WOOL) ||
                    blockStateBelow.isOf(Blocks.YELLOW_WOOL) || blockStateBelow.isOf(Blocks.LIME_WOOL) ||
                    blockStateBelow.isOf(Blocks.PINK_WOOL) || blockStateBelow.isOf(Blocks.GRAY_WOOL) ||
                    blockStateBelow.isOf(Blocks.LIGHT_GRAY_WOOL) || blockStateBelow.isOf(Blocks.CYAN_WOOL) ||
                    blockStateBelow.isOf(Blocks.PURPLE_WOOL) || blockStateBelow.isOf(Blocks.BLUE_WOOL) ||
                    blockStateBelow.isOf(Blocks.BROWN_WOOL) || blockStateBelow.isOf(Blocks.GREEN_WOOL) ||
                    blockStateBelow.isOf(Blocks.RED_WOOL) || blockStateBelow.isOf(Blocks.BLACK_WOOL) ||
                    blockStateBelow2.isOf(Blocks.WHITE_WOOL) || blockStateBelow2.isOf(Blocks.ORANGE_WOOL) ||
                    blockStateBelow2.isOf(Blocks.MAGENTA_WOOL) || blockStateBelow2.isOf(Blocks.LIGHT_BLUE_WOOL) ||
                    blockStateBelow2.isOf(Blocks.YELLOW_WOOL) || blockStateBelow2.isOf(Blocks.LIME_WOOL) ||
                    blockStateBelow2.isOf(Blocks.PINK_WOOL) || blockStateBelow2.isOf(Blocks.GRAY_WOOL) ||
                    blockStateBelow2.isOf(Blocks.LIGHT_GRAY_WOOL) || blockStateBelow2.isOf(Blocks.CYAN_WOOL) ||
                    blockStateBelow2.isOf(Blocks.PURPLE_WOOL) || blockStateBelow2.isOf(Blocks.BLUE_WOOL) ||
                    blockStateBelow2.isOf(Blocks.BROWN_WOOL) || blockStateBelow2.isOf(Blocks.GREEN_WOOL) ||
                    blockStateBelow2.isOf(Blocks.RED_WOOL) || blockStateBelow2.isOf(Blocks.BLACK_WOOL) ||
                    blockStateBelow3.isOf(Blocks.WHITE_WOOL) || blockStateBelow3.isOf(Blocks.ORANGE_WOOL) ||
                    blockStateBelow3.isOf(Blocks.MAGENTA_WOOL) || blockStateBelow3.isOf(Blocks.LIGHT_BLUE_WOOL) ||
                    blockStateBelow3.isOf(Blocks.YELLOW_WOOL) || blockStateBelow3.isOf(Blocks.LIME_WOOL) ||
                    blockStateBelow3.isOf(Blocks.PINK_WOOL) || blockStateBelow3.isOf(Blocks.GRAY_WOOL) ||
                    blockStateBelow3.isOf(Blocks.LIGHT_GRAY_WOOL) || blockStateBelow3.isOf(Blocks.CYAN_WOOL) ||
                    blockStateBelow3.isOf(Blocks.PURPLE_WOOL) || blockStateBelow3.isOf(Blocks.BLUE_WOOL) ||
                    blockStateBelow3.isOf(Blocks.BROWN_WOOL) || blockStateBelow3.isOf(Blocks.GREEN_WOOL) ||
                    blockStateBelow3.isOf(Blocks.RED_WOOL) || blockStateBelow3.isOf(Blocks.BLACK_WOOL)) {
                this.volume = 0.0f; // Silence the sound
            } else if (minecart.getVehicle() instanceof TrackFollowerEntity trackFollower) {
                float amp = (float) trackFollower.getClientMotion().length();
                this.volume = MathHelper.lerp(MathHelper.clamp(amp, 0, 0.5f), 0, 0.7f);
            }
        }
    }
}
