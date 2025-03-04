package io.github.joonbug03.sporkcart.block;

import io.github.joonbug03.sporkcart.TrackType;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class SplitTiesBlockEntity extends TrackTiesBlockEntity {

    public SplitTiesBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public void setNext(@Nullable BlockPos pos, @Nullable TrackType type) {
        if (this.next == null) {
            super.setNext(pos, type);
        } else if (this.next2 == null) {
            this.setNext2(pos, type);
        }
    }

    @Override
    public void setNext2(@Nullable BlockPos pos, @Nullable TrackType type) {
        if (pos == null) {
            var oldNextE = next2();
            this.next2 = null;
            if (oldNextE != null) {
                oldNextE.prev = null;
                oldNextE.sync();
                oldNextE.markDirty();
            }
        } else {
            this.next2 = pos;
            if (type != null) {
                this.nextType2 = type;
            }
            var nextE = next2();
            if (nextE != null) {
                nextE.prev = getPos();
                if (type != null) {
                    nextE.prevType = type;
                }
                nextE.sync();
                nextE.markDirty();
            }
        }

        sync();
        markDirty();
    }

    @Override
    public @Nullable TrackTiesBlockEntity next() {
        return of(this.getWorld(), this.next);
    }

    @Override
    public @Nullable TrackTiesBlockEntity next2() {
        return of(this.getWorld(), this.next2);
    }
}