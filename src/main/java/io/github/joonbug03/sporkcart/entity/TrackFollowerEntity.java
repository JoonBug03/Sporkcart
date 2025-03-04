package io.github.joonbug03.sporkcart.entity;

import io.github.joonbug03.sporkcart.Sporkcart;
import io.github.joonbug03.sporkcart.TrackType;
import io.github.joonbug03.sporkcart.block.*;
import io.github.joonbug03.sporkcart.util.Pose;
import io.github.joonbug03.sporkcart.util.SUtil;
import io.github.joonbug03.sporkcart.util.TrackSnapUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.datafixer.fix.ChunkPalettedStorageFix;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3d;
import org.joml.Matrix3dc;
import org.joml.Quaternionf;
import org.joml.Vector3d;

import java.util.Optional;
import java.util.UUID;

public class TrackFollowerEntity extends Entity {
    public static final double FRICTION = 0.997;
    public static final double CHAIN_DRIVE_SPEED = 0.36;
    public static final double MAGNETIC_SPEED_FACTOR = 1.6;
    public static final double MAGNETIC_ACCEL = 0.07;
    public static final double STATION_TRACK_SLOW_SPEED = 0.05;

    private static final double GRAVITY = 0.06;

    private @Nullable BlockPos startTie;
    private @Nullable BlockPos endTie;
    private double splinePieceProgress = 0; // t
    private double motionScale; // t-distance per block
    private double trackVelocity;
    private boolean reversed = false;

    private final Vector3d serverPosition = new Vector3d();
    private final Vector3d serverVelocity = new Vector3d();
    private int positionInterpSteps;
    private int oriInterpSteps;

    private static final TrackedData<Quaternionf> ORIENTATION = DataTracker.registerData(TrackFollowerEntity.class, TrackedDataHandlerRegistry.QUATERNIONF);
    private final Matrix3d basis = new Matrix3d().identity();

    private final Quaternionf lastClientOrientation = new Quaternionf();
    private final Quaternionf clientOrientation = new Quaternionf();

    private boolean hadPassenger = false;

    private boolean firstPositionUpdate = true;
    private boolean firstOriUpdate = true;

    private Vec3d clientMotion = Vec3d.ZERO;

    public TrackFollowerEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    public TrackFollowerEntity(World world, Vec3d startPos, BlockPos startTie, BlockPos endTie, Vec3d velocity) {
        this(Sporkcart.TRACK_FOLLOWER, world);

        setStretch(startTie, endTie);
        this.trackVelocity = velocity.multiply(1, 0, 1).length();

        var startE = TrackTiesBlockEntity.of(this.getWorld(), this.startTie);
        if (startE != null) {
            this.setPosition(startPos);
            this.getDataTracker().set(ORIENTATION, startE.pose().basis().getNormalizedRotation(new Quaternionf()));
        }
    }
    public void setStretch(BlockPos start, BlockPos end) {
        this.startTie = start;
        this.endTie = end;

        var startE = TrackTiesBlockEntity.of(this.getWorld(), this.startTie);
        if (startE != null) {
            this.basis.set(startE.pose().basis());
            var endE = TrackTiesBlockEntity.of(this.getWorld(), this.endTie);
            if (endE != null) {
                // Initial approximation of motion scale; from the next tick onward the derivative of the track spline is used
                this.motionScale = 1 / startE.pose().translation().distance(endE.pose().translation());
            } else {
                this.motionScale = 1;
            }
        }

        if (this.splinePieceProgress < 0) {
            this.splinePieceProgress = 0;
        }
    }

    // For more accurate client side position interpolation, we can conveniently use the
    // same cubic hermite spline formula rather than linear interpolation like vanilla,
    // since we have not only the position but also its derivative (velocity)
    protected void interpPos(int step) {
        double t = 1 / (double)step;

        var clientPos = new Vector3d(this.getX(), this.getY(), this.getZ());

        var cv = this.getVelocity();
        var clientVel = new Vector3d(cv.getX(), cv.getY(), cv.getZ());

        var newClientPos = new Vector3d();
        var newClientVel = new Vector3d();
        Pose.cubicHermiteSpline(t, 1, clientPos, clientVel, this.serverPosition, this.serverVelocity,
                newClientPos, newClientVel);

        this.setPosition(newClientPos.x(), newClientPos.y(), newClientPos.z());
        this.setVelocity(newClientVel.x(), newClientVel.y(), newClientVel.z());
    }

    @Override
    public void tick() {
        super.tick();

        var world = this.getWorld();
        if (world.isClient()) {
            this.clientMotion = this.getPos().negate();
            if (this.positionInterpSteps > 0) {
                this.interpPos(this.positionInterpSteps);
                this.positionInterpSteps--;
            } else {
                this.refreshPosition();
                this.setVelocity(this.serverVelocity.x(), this.serverVelocity.y(), this.serverVelocity.z());
            }
            this.clientMotion = this.clientMotion.add(this.getPos());

            this.lastClientOrientation.set(this.clientOrientation);
            if (this.oriInterpSteps > 0) {
                float delta = 1 / (float) oriInterpSteps;
                this.clientOrientation.slerp(this.getDataTracker().get(ORIENTATION), delta);
                this.oriInterpSteps--;
            } else {
                this.clientOrientation.set(this.getDataTracker().get(ORIENTATION));
            }
        } else {
            this.updateServer();
        }
    }

    public void getClientOrientation(Quaternionf q, float tickDelta) {
        this.lastClientOrientation.slerp(this.clientOrientation, tickDelta, q);
    }

    public Vec3d getClientMotion() {
        return this.clientMotion;
    }

    public Matrix3dc getServerBasis() {
        return this.basis;
    }

    public void destroy() {
        this.remove(RemovalReason.KILLED);
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    private void fullDismount(Entity passenger, BlockState ties, BlockPos tiesPos, boolean trackSwitch) {
        passenger.stopRiding();
        if(trackSwitch) {
            Vec3i newVel = SwitchTiesBlock.VELOCITY_MAP.getOrDefault(ties.get(SwitchTiesBlock.POINTING), Vec3i.ZERO);
            passenger.setVelocity(newVel.getX(), newVel.getY(), newVel.getZ());
            Vec3d startPos = passenger.getPos().add(Vec3d.of(newVel));
            passenger.setPos(startPos.x, startPos.y, startPos.z);
        } else {
            Vector3d newVel = new Vector3d(0, 0, this.trackVelocity).mul(this.basis).mul(this.reversed ? -1 : 1);
            passenger.setVelocity(newVel.x(), newVel.y(), newVel.z());
            BlockPos snapTo = TrackSnapUtil.snapToTrackOnExit(getWorld(), tiesPos, ties, reversed);
            if(snapTo != null) passenger.setPos(snapTo.getX() + 0.5, snapTo.getY(), snapTo.getZ() + 0.5);
        }
        this.destroy();
    }

    protected void updateServer() {
        for (var passenger : this.getPassengerList()) {
            passenger.fallDistance = 0;
        }

        var passenger = this.getFirstPassenger();
        if (passenger != null) {
            if (!hadPassenger) {
                hadPassenger = true;
            } else {
                var world = this.getWorld();
                var startE = TrackTiesBlockEntity.of(world, this.startTie);
                var endE = TrackTiesBlockEntity.of(world, this.endTie);
                if (startE == null || endE == null) {
                    this.destroy();
                    return;
                }

                endE.markHasCart();

                // Calculate the slope direction (uphill or downhill)
                var gradeVec = new Vector3d(0, 1, 0).mul(this.basis);
                gradeVec.mul(1, 0, 1);
                double slope = gradeVec.length(); // Slope magnitude

                // Check if the cart is on an uphill slope and has lost momentum
                if (Math.abs(this.trackVelocity) < 0.03 && slope > 0) {
                    this.reversed = !this.reversed; // Reverse direction
                    this.trackVelocity = 0.03 * (this.reversed ? 1 : -1); // Give it a small push in the opposite direction
                }

                this.splinePieceProgress += this.trackVelocity * this.motionScale * (this.reversed ? -1 : 1);
                if (this.splinePieceProgress > 1) {
                    if ((endE.getCachedState().isOf(Sporkcart.SHUTTLE_TIES) || endE.getCachedState().isOf(Sporkcart.INVISIBLE_SHUTTLE_TIES)) && endE.getCachedState().get(ShuttleTiesBlock.RUNNING)) {
                        this.reversed = !reversed;
                        return;
                    } else {
                        this.splinePieceProgress -= 1;
                        var nextE = endE.next();
                        if (nextE == null) {
                            fullDismount(passenger, endE.getCachedState(), this.endTie, false);
                            return;
                        } else {
                            if ((endE.getCachedState().isOf(Sporkcart.SPLIT_TIES) || endE.getCachedState().isOf(Sporkcart.INVISIBLE_SPLIT_TIES))) {
                                boolean isPowered = endE.getCachedState().get(SplitTiesBlock.SPLITED);
                                if (isPowered) {
                                    nextE = endE.next2();
                                } else {
                                    nextE = endE.next();
                                }
                                if (nextE == null) {
                                    nextE = endE.next();
                                }
                            }
                            assert nextE != null;
                            this.setStretch(this.endTie, nextE.getPos());
                            startE = endE;
                            endE = nextE;
                        }
                    }
                } else if (this.reversed && this.splinePieceProgress < 0) {
                    if ( (startE.getCachedState().isOf(Sporkcart.SHUTTLE_TIES) || startE.getCachedState().isOf(Sporkcart.INVISIBLE_SHUTTLE_TIES) ) && startE.getCachedState().get(ShuttleTiesBlock.RUNNING)) {
                        this.reversed = !reversed;
                        return;
                    } else {
                        this.splinePieceProgress += 1;
                        var nextE = startE.prev();
                        if((startE.getCachedState().isOf(Sporkcart.MERGE_TIES) || startE.getCachedState().isOf(Sporkcart.INVISIBLE_MERGE_TIES) ) && startE.getCachedState().get(MergeTiesBlock.MERGED) && startE.prev2() != null) {
                            nextE = startE.prev2();
                        }
                        if (nextE == null) {
                            fullDismount(passenger, startE.getCachedState(), this.startTie, false);
                            return;
                        } else {
                            this.setStretch(nextE.getPos(), this.startTie);
                            endE = startE;
                            startE = nextE;
                        }
                    }
                }

                var pos = new Vector3d();
                var grad = new Vector3d(); // Change in position per change in spline progress
                startE.pose().interpolate(endE.pose(), this.splinePieceProgress, pos, this.basis, grad);

                var gravity = (getY() - pos.y()) * GRAVITY;

                this.setPosition(pos.x(), pos.y(), pos.z());

                BlockPos turnBlockPos = this.getBlockPos().down();
                BlockState turnBlockState = world.getBlockState(turnBlockPos);
                BlockPos turnBlockPos2 = this.getBlockPos().down(2);
                BlockState turnBlockState2 = world.getBlockState(turnBlockPos2);
                BlockPos turnBlockPos3 = this.getBlockPos().down(3);
                BlockState turnBlockState3 = world.getBlockState(turnBlockPos3);
                boolean cw90 = (turnBlockState.isOf(Blocks.LIGHT_GRAY_CONCRETE) || turnBlockState2.isOf(Blocks.LIGHT_GRAY_CONCRETE) || turnBlockState3.isOf(Blocks.LIGHT_GRAY_CONCRETE));
                boolean cw180 = (turnBlockState.isOf(Blocks.LIGHT_BLUE_CONCRETE) || turnBlockState2.isOf(Blocks.LIGHT_BLUE_CONCRETE) || turnBlockState3.isOf(Blocks.LIGHT_BLUE_CONCRETE));
                boolean cw270 = (turnBlockState.isOf(Blocks.LIME_CONCRETE) || turnBlockState2.isOf(Blocks.LIME_CONCRETE) || turnBlockState3.isOf(Blocks.LIME_CONCRETE));
                boolean cw360 = (turnBlockState.isOf(Blocks.PINK_CONCRETE) || turnBlockState2.isOf(Blocks.PINK_CONCRETE) || turnBlockState3.isOf(Blocks.PINK_CONCRETE));
                boolean ccw90 = (turnBlockState.isOf(Blocks.GRAY_CONCRETE) || turnBlockState2.isOf(Blocks.GRAY_CONCRETE) || turnBlockState3.isOf(Blocks.GRAY_CONCRETE));
                boolean ccw180 = (turnBlockState.isOf(Blocks.BLUE_CONCRETE) || turnBlockState2.isOf(Blocks.BLUE_CONCRETE) || turnBlockState3.isOf(Blocks.BLUE_CONCRETE));
                boolean ccw270 = (turnBlockState.isOf(Blocks.GREEN_CONCRETE) || turnBlockState2.isOf(Blocks.GREEN_CONCRETE) || turnBlockState3.isOf(Blocks.GREEN_CONCRETE));
                boolean ccw360 = (turnBlockState.isOf(Blocks.RED_CONCRETE) || turnBlockState2.isOf(Blocks.RED_CONCRETE) || turnBlockState3.isOf(Blocks.RED_CONCRETE));

                if (ccw90) {
                    double turnAngle = Math.toRadians(90);
                    basis.rotateY(turnAngle);
                } else if (cw90) {
                    double turnAngle = Math.toRadians(90);
                    basis.rotateY(-turnAngle);
                } else if (ccw180) {
                    double turnAngle = Math.toRadians(180);
                    basis.rotateY(turnAngle);
                } else if (cw180) {
                    double turnAngle = Math.toRadians(180);
                    basis.rotateY(-turnAngle);
                } else if (ccw270) {
                    double turnAngle = Math.toRadians(270);
                    basis.rotateY(turnAngle);
                } else if (cw270) {
                    double turnAngle = Math.toRadians(270);
                    basis.rotateY(-turnAngle);
                } else if (ccw360) {
                    double turnAngle = Math.toRadians(360);
                    basis.rotateY(turnAngle);
                } else if (cw360) {
                    double turnAngle = Math.toRadians(360);
                    basis.rotateY(-turnAngle);
                }
                this.getDataTracker().set(ORIENTATION, this.basis.getNormalizedRotation(new Quaternionf()));
                double gradLen = grad.length();
                if (gradLen != 0) {
                    this.motionScale = 1 / grad.length();
                }

                double dt = this.trackVelocity * this.motionScale * (this.reversed ? -1 : 1); // Change in spline progress per tick
                grad.mul(dt); // Change in position per tick (velocity)
                this.setVelocity(grad.x(), grad.y(), grad.z());

                var passengerVel = passenger.getVelocity();
                var push = new Vector3d(passengerVel.getX(), 0.0, passengerVel.getZ());
                if (push.lengthSquared() > 0.0001) {
                    var forward = new Vector3d(0, 0, 1).mul(this.basis);

                    double linearPush = forward.dot(push) * 2.0;
                    this.trackVelocity += linearPush;
                    passenger.setVelocity(Vec3d.ZERO);
                }

                int power = endE.power();

                this.trackVelocity += gravity;
                this.trackVelocity = startE.nextType().motion.calculate(this.trackVelocity, slope, power);

                if (startE.nextType() == TrackType.STATION || startE.nextType() == TrackType.INVISIBLE_STATION) {
                    if (power > 0) {
                        this.trackVelocity = power * 0.01;
                    } else {
                        this.trackVelocity = 0;
                    }
                }
            }
        } else {
            if (this.hadPassenger) {
                this.destroy();
            }
        }
    }

    @Override
    public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps) {
        if (this.firstPositionUpdate) {
            this.firstPositionUpdate = false;
            super.updateTrackedPositionAndAngles(x, y, z, yaw, pitch, interpolationSteps);
        }

        this.serverPosition.set(x, y, z);
        this.positionInterpSteps = interpolationSteps + 2;
        this.setAngles(yaw, pitch);
    }

    // This method should be called updateTrackedVelocity, its usage is very similar to the above method
    @Override
    public void setVelocityClient(double x, double y, double z) {
        this.serverVelocity.set(x, y, z);
    }

    @Override
    protected void updatePassengerPosition(Entity passenger, PositionUpdater positionUpdater) {
        positionUpdater.accept(passenger, this.getX(), this.getY(), this.getZ());
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(ORIENTATION, new Quaternionf().identity());
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);

        if (data.equals(ORIENTATION)) {
            if (this.firstOriUpdate) {
                this.firstOriUpdate = false;
                this.clientOrientation.set(getDataTracker().get(ORIENTATION));
                this.lastClientOrientation.set(this.clientOrientation);
            }
            this.oriInterpSteps = this.getType().getTrackTickInterval() + 2;
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.startTie = SUtil.getBlockPos(nbt, "start");
        this.endTie = SUtil.getBlockPos(nbt, "end");
        this.trackVelocity = nbt.getDouble("track_velocity");
        this.motionScale = nbt.getDouble("motion_scale");
        this.splinePieceProgress = nbt.getDouble("spline_piece_progress");
        this.reversed = nbt.getBoolean("reversed");
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        SUtil.putBlockPos(nbt, this.startTie, "start");
        SUtil.putBlockPos(nbt, this.endTie, "end");
        nbt.putDouble("track_velocity", this.trackVelocity);
        nbt.putDouble("motion_scale", this.motionScale);
        nbt.putDouble("spline_piece_progress", this.splinePieceProgress);
        nbt.putBoolean("reversed", this.reversed);
    }
}
