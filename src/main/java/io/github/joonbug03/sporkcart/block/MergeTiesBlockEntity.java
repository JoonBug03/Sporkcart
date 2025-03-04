package io.github.joonbug03.sporkcart.block;

import io.github.joonbug03.sporkcart.TrackType;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class MergeTiesBlockEntity extends TrackTiesBlockEntity {

    public MergeTiesBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public @Nullable TrackTiesBlockEntity prev() {
        return of(this.getWorld(), this.prev);
    }

    public @Nullable TrackTiesBlockEntity prev2() {
        return of(this.getWorld(), this.prev2);
    }
}