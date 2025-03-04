package io.github.joonbug03.sporkcart;

import io.github.joonbug03.sporkcart.block.TrackTiesBlockEntity;
import io.github.joonbug03.sporkcart.entity.TrackFollowerEntity;
import io.github.joonbug03.sporkcart.util.SUtil;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public enum TrackType {
    DEFAULT(0, MotionModifier.FRICTION, null),
    CHAIN_DRIVE(1,
            (m, g, p) -> Math.max(m * TrackFollowerEntity.FRICTION, TrackFollowerEntity.CHAIN_DRIVE_SPEED),
            (p, t, col, v) -> v[0] = t * 0.05f
    ),
    MAGNETIC(2,
            (m, g, p) -> {
                double speed = (p / 15.0) * TrackFollowerEntity.MAGNETIC_SPEED_FACTOR;
                m = (m * TrackFollowerEntity.FRICTION);
                return m + ((speed - m) * TrackFollowerEntity.MAGNETIC_ACCEL * (1.0 - g));
            },
            (p, t, col, v) -> col.set(SUtil.REDSTONE_COLOR_LUT[p])
    ),
    STATION(3, MotionModifier.STATION_MODIFIER, null),



    INVISIBLE(0, MotionModifier.FRICTION, null
    ),
    INVISIBLE_CHAIN_DRIVE(1,
            (m, g, p) -> Math.max(m * TrackFollowerEntity.FRICTION, TrackFollowerEntity.CHAIN_DRIVE_SPEED),
            (p, t, col, v) -> v[0] = t * 0.05f
    ),
    INVISIBLE_MAGNETIC(2,
            MotionModifier.MAGNETIC_MODIFIER,
            (p, t, col, v) -> col.set(SUtil.REDSTONE_COLOR_LUT[p])
    ),
    INVISIBLE_STATION(3, MotionModifier.STATION_MODIFIER, null
    );



    public final int textureU;
    public final MotionModifier motion;
    public final @Nullable Overlay overlay;

    TrackType(int textureU, MotionModifier motion, @Nullable Overlay overlay) {
        this.textureU = textureU;
        this.motion = motion;
        this.overlay = overlay;
    }

    public static TrackType read(int type) {
        if (type < 0 || type >= values().length) {
            return DEFAULT;
        }

        return values()[type];
    }

    public int write() {
        return ordinal();
    }

    @FunctionalInterface
    public interface MotionModifier {
        MotionModifier FRICTION = (m, g, p) -> m * TrackFollowerEntity.FRICTION;

        MotionModifier STATION_MODIFIER = (m, g, p) -> {
            if (p > 0) {
                double speed = 0.01 * p;
                return speed;
            } else {
                return 0; // Stop the minecart if there's no power
            }
        };

        MotionModifier MAGNETIC_MODIFIER = (m, g, p) -> {
            double speed = (p / 15.0) * TrackFollowerEntity.MAGNETIC_SPEED_FACTOR;
            m = (m * TrackFollowerEntity.FRICTION);
            return m + ((speed - m) * TrackFollowerEntity.MAGNETIC_ACCEL * (1.0 - g));
        };
        double calculate(double motion, double grade, int redstonePower);
    }

    @FunctionalInterface
    public interface Overlay {
        void calculateEffects(int redstonePower, float time, Vector3f outputColor, float[] outputVOffset);
    }
}
