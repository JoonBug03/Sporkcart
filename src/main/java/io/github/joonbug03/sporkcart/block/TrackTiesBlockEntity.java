package io.github.joonbug03.sporkcart.block;

import io.github.joonbug03.sporkcart.Sporkcart;
import io.github.joonbug03.sporkcart.TrackType;
import io.github.joonbug03.sporkcart.item.TrackItem;
import io.github.joonbug03.sporkcart.util.Pose;
import io.github.joonbug03.sporkcart.util.SUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3d;
import org.joml.Vector3d;

public class TrackTiesBlockEntity extends BlockEntity {
    public float clientTime = 0;

    TrackType nextType = TrackType.DEFAULT;
    TrackType nextType2 = TrackType.DEFAULT;
    TrackType prevType = TrackType.DEFAULT;
    TrackType prevType2 = TrackType.DEFAULT;

    BlockPos next;
    BlockPos prev;
    BlockPos next2;
    BlockPos prev2;
    private Pose pose;
    private int hasCartTicks = 0;

    private int power = -1;

    public TrackTiesBlockEntity(BlockPos pos, BlockState state) {
        super(Sporkcart.TRACK_TIES_BE, pos, state);
        updatePose(pos, state);
    }

    public void updatePose(BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof TrackTiesBlock ties) {
            this.pose = ties.getPose(state, pos);
        } else {
            this.pose = new Pose(new Vector3d(), new Matrix3d().identity());
        }
    }

    @Override
    public void setCachedState(BlockState state) {
        super.setCachedState(state);

        updatePose(this.getPos(), this.getCachedState());
    }

    public static @Nullable TrackTiesBlockEntity of(World world, @Nullable BlockPos pos) {
        if (pos != null && world.getBlockEntity(pos) instanceof TrackTiesBlockEntity e) {
            return e;
        }

        return null;
    }

    private void dropTrack(TrackType type) {
        var world = getWorld();
        var pos = Vec3d.ofCenter(getPos());
        var item = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(TrackItem.ITEMS_BY_TYPE.get(type)));

        world.spawnEntity(item);
    }

    public void setNext(@Nullable BlockPos pos, @Nullable TrackType type) {
        if (pos == null) {
            var oldNextE = next();
            this.next = null;
            if (oldNextE != null) {
                oldNextE.prev = null;
                oldNextE.sync();
                oldNextE.markDirty();
            }
        } else {
            this.next = pos;
            if (type != null) {
                this.nextType = type;
            }
            var nextE = next();
            if (nextE != null) {
                if(nextE instanceof MergeTiesBlockEntity mergeTies) {
                    if (mergeTies.prev() == null) {
                        mergeTies.prev = getPos();
                        if (type != null) {
                            mergeTies.prevType = type;
                        }
                    } else if (mergeTies.prev2() == null) {
                        mergeTies.prev2 = getPos();
                        if (type != null) {
                            mergeTies.prevType2 = type;
                        }
                    }
                } else {
                    nextE.prev = getPos();
                    if (type != null) {
                        nextE.prevType = type;
                    }
                }
                nextE.sync();
                nextE.markDirty();

            }
        }
            sync();
            markDirty();
    }

    public void setNext2(@Nullable BlockPos pos, @Nullable TrackType type) {
        if (pos == null) {
            var oldNextE = next();
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
            var nextE = next();
            if (nextE != null) {
                if (nextE instanceof MergeTiesBlockEntity mergeTies) {
                    if (mergeTies.prev() == null) {
                        mergeTies.prev = getPos();
                        if(type != null) {
                            mergeTies.prevType = type;
                        }
                    } else if (mergeTies.prev2() == null) {
                        mergeTies.prev2 = getPos();
                        if(type != null) {
                            mergeTies.prevType2 = type;
                        }
                    }
                } else {
                    nextE.prev = getPos();
                    if (type != null) {
                        nextE.prevType = type;
                    }
                }
                nextE.sync();
                nextE.markDirty();
            }
        }

        sync();
        markDirty();
    }

    public @Nullable TrackTiesBlockEntity next() {
        return of(this.getWorld(), this.next);
    }

    public @Nullable TrackTiesBlockEntity prev() {
        return of(this.getWorld(), this.prev);
    }

    public @Nullable TrackTiesBlockEntity prev2() {
        return of(this.getWorld(), this.prev2);
    }

    public @Nullable TrackTiesBlockEntity next2() {
        return of(this.getWorld(), this.next);
    }

    public @Nullable BlockPos nextPos() {
        return next;
    }

    public @Nullable BlockPos prevPos() {
        return prev;
    }

    public TrackType nextType() {
        return this.nextType;
    }
    public TrackType nextType2() {
        return this.nextType2;
    }

    public TrackType prevType() {
        return this.prevType;
    }
    public TrackType prevType2() {
        return this.prevType2;
    }

    public Pose pose() {
        return this.pose;
    }

    public void updatePower() {
        int oldPower = this.power;
        this.power = getWorld().getReceivedRedstonePower(getPos());

        if (oldPower != this.power) {
            sync();
            markDirty();
        }
    }

    public int power() {
        if (this.power < 0) {
            updatePower();
        }

        return this.power;
    }

    public void onDestroy() {
        if (this.prev != null) {
            this.dropTrack(this.prevType);
        }
        if (this.prev2 != null) {
            this.dropTrack(this.prevType2);
        }
        if (this.next != null) {
            this.dropTrack(this.nextType);
        }
        if (this.next2 != null) {
            this.dropTrack(this.nextType2);
        }

        var prevE = prev();
        var prevE2 = prev2();
        if (prevE != null && (prevE.getCachedState().isOf(Sporkcart.SPLIT_TIES) || prevE.getCachedState().isOf(Sporkcart.INVISIBLE_SPLIT_TIES) ) && this == prevE.next2()) {
            prevE.next2 = null;
            prevE.sync();
            prevE.markDirty();
        } else if (prevE != null && (prevE.getCachedState().isOf(Sporkcart.SPLIT_TIES) || prevE.getCachedState().isOf(Sporkcart.INVISIBLE_SPLIT_TIES) ) && this == prevE.next() && prevE.next2() != null) {
            prevE.next = prevE.next2;
            prevE.next2 = null;
            prevE.sync();
            prevE.markDirty();
        } else if (prevE != null && prevE2 != null) {
            prevE.next = null;
            prevE2.next = null;
            prevE.sync();
            prevE.markDirty();
            prevE2.sync();
            prevE2.markDirty();
        } else if (prevE != null) {
            prevE.next = null;
            prevE.sync();
            prevE.markDirty();
        }

        var nextE = next();
        var nextE2 = next2();
        if (nextE != null && (nextE.getCachedState().isOf(Sporkcart.MERGE_TIES) || nextE.getCachedState().isOf(Sporkcart.INVISIBLE_MERGE_TIES)) && this == nextE.prev2()) {
            nextE.prev2 = null;
            nextE.sync();
            nextE.markDirty();
        } else if (nextE != null && (nextE.getCachedState().isOf(Sporkcart.MERGE_TIES) || nextE.getCachedState().isOf(Sporkcart.INVISIBLE_MERGE_TIES)) && this == nextE.prev() && nextE.prev2() != null) {
            nextE.prev = nextE.prev2;
            nextE.prev2 = null;
            nextE.sync();
            nextE.markDirty();
        } else if (nextE != null && nextE2 != null) {
            nextE.prev = null;
            nextE2.prev = null;
            nextE.sync();
            nextE.markDirty();
            nextE2.sync();
            nextE2.markDirty();
        } else if (nextE != null) {
            nextE.prev = null;
            nextE.sync();
            nextE.markDirty();
        }

        /*if (nextE2 != null) {
            nextE2.prev = null;
            nextE2.sync();
            nextE2.markDirty();
        }*/
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        this.prev = SUtil.getBlockPos(nbt, "prev");
        this.next = SUtil.getBlockPos(nbt, "next");
        this.prev2 = SUtil.getBlockPos(nbt, "prev2");
        this.next2 = SUtil.getBlockPos(nbt, "next2");

        this.prevType = TrackType.read(nbt.getInt("prev_id"));
        this.nextType = TrackType.read(nbt.getInt("next_id"));
        this.prevType2 = TrackType.read(nbt.getInt("prev_id2"));
        this.nextType2 = TrackType.read(nbt.getInt("next_id2"));

        this.power = nbt.getInt("power");
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);

        SUtil.putBlockPos(nbt, this.prev, "prev");
        SUtil.putBlockPos(nbt, this.next, "next");
        SUtil.putBlockPos(nbt, this.prev2, "prev2");
        SUtil.putBlockPos(nbt, this.next2, "next2");

        nbt.putInt("prev_id", this.prevType.write());
        nbt.putInt("next_id", this.nextType.write());
        nbt.putInt("prev_id2", this.prevType2.write());
        nbt.putInt("next_id2", this.nextType2.write());

        nbt.putInt("power", this.power);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        var nbt = super.toInitialChunkDataNbt(registryLookup);
        writeNbt(nbt, registryLookup);
        return nbt;
    }

    public void sync() {
        getWorld().updateListeners(getPos(), getCachedState(), getCachedState(), 3);
    }

    private void tick(World world, BlockPos pos, BlockState state) {
        if(hasCartTicks > 0) {
            hasCartTicks--;
            if(hasCartTicks == 0) {world.updateComparators(pos, state.getBlock());}
        }
    }

    public static void staticTick(World world, BlockPos pos, BlockState state, BlockEntity be) {
        if(be instanceof TrackTiesBlockEntity ttBE) ttBE.tick(world, pos, state);
    }

    public boolean hasCart() {
        return hasCartTicks > 0;
    }

    public void markHasCart() {
        boolean didNotHaveCart = !hasCart();
        hasCartTicks = 2;
        if(didNotHaveCart && world != null) {
            world.updateComparators(pos, getCachedState().getBlock());
        }
    }


}
