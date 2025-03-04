package io.github.joonbug03.sporkcart.block.entity;

import io.github.joonbug03.sporkcart.Sporkcart;
import io.github.joonbug03.sporkcart.SporkcartClient;
import io.github.joonbug03.sporkcart.TrackType;
import io.github.joonbug03.sporkcart.block.TrackTiesBlockEntity;
import io.github.joonbug03.sporkcart.util.Pose;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.joml.Matrix3d;
import org.joml.Vector3d;
import org.joml.Vector3f;

import static com.mojang.text2speech.Narrator.LOGGER;

public class TrackTiesBlockEntityRenderer implements BlockEntityRenderer<TrackTiesBlockEntity> {
    public static final int WHITE = 0xFFFFFFFF;
    public static final Vector3f WHITEF = new Vector3f(1, 1, 1);
    public static final Identifier TRACK_TEXTURE = Sporkcart.id("textures/track.png");
    public static final Identifier TRACK_OVERLAY_TEXTURE = Sporkcart.id("textures/track_overlay.png");
    public static final Identifier POSE_TEXTURE_DEBUG = Sporkcart.id("textures/debug.png");

    public TrackTiesBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(TrackTiesBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if(entity.getCachedState().isOf(Sporkcart.INVISIBLE_TIES) || entity.getCachedState().isOf(Sporkcart.INVISIBLE_SHUTTLE_TIES) || entity.getCachedState().isOf(Sporkcart.INVISIBLE_SPLIT_TIES) || entity.getCachedState().isOf(Sporkcart.INVISIBLE_MERGE_TIES)) {
            MinecraftClient client = MinecraftClient.getInstance();
            if(client != null && client.player != null) {

                boolean holdingInvisibleItem = client.player.getStackInHand(Hand.MAIN_HAND).isOf(Sporkcart.INVISIBLE_TRACK) || client.player.getStackInHand(Hand.OFF_HAND).isOf(Sporkcart.INVISIBLE_TRACK) ||
                        client.player.getStackInHand(Hand.MAIN_HAND).isOf(Sporkcart.INVISIBLE_TIES.asItem()) || client.player.getStackInHand(Hand.OFF_HAND).isOf(Sporkcart.INVISIBLE_TIES.asItem()) ||
                        client.player.getStackInHand(Hand.MAIN_HAND).isOf(Sporkcart.INVISIBLE_CHAIN_DRIVE_TRACK) || client.player.getStackInHand(Hand.OFF_HAND).isOf(Sporkcart.INVISIBLE_CHAIN_DRIVE_TRACK) ||
                        client.player.getStackInHand(Hand.MAIN_HAND).isOf(Sporkcart.INVISIBLE_MAGNETIC_TRACK) || client.player.getStackInHand(Hand.OFF_HAND).isOf(Sporkcart.INVISIBLE_MAGNETIC_TRACK) ||
                        client.player.getStackInHand(Hand.MAIN_HAND).isOf(Sporkcart.INVISIBLE_STATION_TRACK) || client.player.getStackInHand(Hand.OFF_HAND).isOf(Sporkcart.INVISIBLE_STATION_TRACK) ||
                        client.player.getStackInHand(Hand.MAIN_HAND).isOf(Sporkcart.INVISIBLE_SHUTTLE_TIES.asItem()) || client.player.getStackInHand(Hand.OFF_HAND).isOf(Sporkcart.INVISIBLE_SHUTTLE_TIES.asItem()) ||
                        client.player.getStackInHand(Hand.MAIN_HAND).isOf(Sporkcart.INVISIBLE_SPLIT_TIES.asItem()) || client.player.getStackInHand(Hand.OFF_HAND).isOf(Sporkcart.INVISIBLE_SPLIT_TIES.asItem()) ||
                        client.player.getStackInHand(Hand.MAIN_HAND).isOf(Sporkcart.INVISIBLE_MERGE_TIES.asItem()) || client.player.getStackInHand(Hand.OFF_HAND).isOf(Sporkcart.INVISIBLE_MERGE_TIES.asItem());


                if(holdingInvisibleItem) {

                    BlockRenderManager brm = client.getBlockRenderManager();
                    BlockState toRender = entity.getCachedState();
                    BakedModel model = brm.getModel(toRender);
                    int color = client.getBlockColors().getColor(toRender, entity.getWorld(), entity.getPos(), 0);
                    float r = (float) (color >> 16 & 0xFF) / 255.0F;
                    float g = (float) (color >> 8 & 0xFF) / 255.0F;
                    float b = (float) (color & 0xFF) / 255.0F;
                    brm.getModelRenderer().render(
                            matrices.peek(),
                            vertexConsumers.getBuffer(RenderLayers.getEntityBlockLayer(toRender, false)),
                            toRender,
                            model,
                            r, g, b,
                            light,
                            overlay
                    );
                }
            }
        }

        var start = entity.pose();
        var pos = entity.getPos();

        entity.clientTime += tickDelta;

        var trackType = entity.nextType();
        if (trackType == null) {
            LOGGER.warn("Track type is null for entity at {}", entity.getPos());
            return;
        }
        if(trackType == TrackType.INVISIBLE || trackType == TrackType.INVISIBLE_MAGNETIC || trackType == TrackType.INVISIBLE_CHAIN_DRIVE || trackType == TrackType.INVISIBLE_STATION) {
            MinecraftClient client = MinecraftClient.getInstance();
            if(client != null && client.player != null) {
                boolean holdingInvisibleItem = client.player.getStackInHand(Hand.MAIN_HAND).isOf(Sporkcart.INVISIBLE_TRACK) || client.player.getStackInHand(Hand.OFF_HAND).isOf(Sporkcart.INVISIBLE_TRACK) ||
                        client.player.getStackInHand(Hand.MAIN_HAND).isOf(Sporkcart.INVISIBLE_TIES.asItem()) || client.player.getStackInHand(Hand.OFF_HAND).isOf(Sporkcart.INVISIBLE_TIES.asItem()) ||
                        client.player.getStackInHand(Hand.MAIN_HAND).isOf(Sporkcart.INVISIBLE_CHAIN_DRIVE_TRACK) || client.player.getStackInHand(Hand.OFF_HAND).isOf(Sporkcart.INVISIBLE_CHAIN_DRIVE_TRACK) ||
                        client.player.getStackInHand(Hand.MAIN_HAND).isOf(Sporkcart.INVISIBLE_MAGNETIC_TRACK) || client.player.getStackInHand(Hand.OFF_HAND).isOf(Sporkcart.INVISIBLE_MAGNETIC_TRACK) ||
                        client.player.getStackInHand(Hand.MAIN_HAND).isOf(Sporkcart.INVISIBLE_STATION_TRACK) || client.player.getStackInHand(Hand.OFF_HAND).isOf(Sporkcart.INVISIBLE_STATION_TRACK) ||
                        client.player.getStackInHand(Hand.MAIN_HAND).isOf(Sporkcart.INVISIBLE_SHUTTLE_TIES.asItem()) || client.player.getStackInHand(Hand.OFF_HAND).isOf(Sporkcart.INVISIBLE_SHUTTLE_TIES.asItem()) ||
                        client.player.getStackInHand(Hand.MAIN_HAND).isOf(Sporkcart.INVISIBLE_SPLIT_TIES.asItem()) || client.player.getStackInHand(Hand.OFF_HAND).isOf(Sporkcart.INVISIBLE_SPLIT_TIES.asItem()) ||
                        client.player.getStackInHand(Hand.MAIN_HAND).isOf(Sporkcart.INVISIBLE_MERGE_TIES.asItem()) || client.player.getStackInHand(Hand.OFF_HAND).isOf(Sporkcart.INVISIBLE_MERGE_TIES.asItem());

                if(!holdingInvisibleItem) {
                    return;
                }
            }
        }

        if (MinecraftClient.getInstance().getDebugHud().shouldShowDebugHud()) {
            matrices.push();

            matrices.translate(0.5, 0.5, 0.5);
            var buffer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(POSE_TEXTURE_DEBUG));
            renderDebug(start, matrices.peek(), buffer);

            matrices.pop();
        }

        var buffer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(getTexture()));

        boolean reinitBuffer = false;

        var nextE = entity.next();
        var nextE2 = entity.next2();

        if (nextE != null) {
            var end = nextE.pose();
            var world = entity.getWorld();

            matrices.push();

            matrices.translate(-pos.getX(), -pos.getY(), -pos.getZ());

            float u0 = trackType.textureU * 0.0625f;
            float u1 = u0 + 0.0625f;

            int segs = SporkcartClient.CFG_TRACK_RESOLUTION.get() * Math.max((int) start.translation().distance(end.translation()), 2);
            var origin = new Vector3d(start.translation());
            var basis = new Matrix3d(start.basis());
            var grad = new Vector3d(0, 0, 1).mul(start.basis());
            double[] totalDist = {0};

            for (int i = 0; i < segs; i++) {
                double t0 = (double)i / segs;
                double t1 = (double)(i + 1) / segs;

                renderPart(world, matrices.peek(), buffer, start, end, u0, u1, 0, WHITEF, t0, t1, totalDist, origin, basis, grad, overlay);
            }

            if (trackType.overlay != null) {
                reinitBuffer = true;
                var olBuffer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(getTrackOverlayTexture()));

                float[] olVOffset = {0};
                Vector3f olColor = new Vector3f(WHITEF);
                int power = nextE.power();
                trackType.overlay.calculateEffects(power, entity.clientTime, olColor, olVOffset);

                for (int i = 0; i < segs; i++) {
                    double t0 = (double)i / segs;
                    double t1 = (double)(i + 1) / segs;

                    renderPart(world, matrices.peek(), olBuffer, start, end, u0, u1, olVOffset[0], olColor, t0, t1, totalDist, origin, basis, grad, overlay);
                }
            }

            matrices.pop();
        }
        var buffer2 = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(getTexture()));
        if (nextE2 != null) {
            var end = nextE2.pose();
            var world = entity.getWorld();

            matrices.push();
            matrices.translate(-pos.getX(), -pos.getY(), -pos.getZ());

            float u0 = trackType.textureU * 0.0625f;
            float u1 = u0 + 0.0625f;

            int segs = SporkcartClient.CFG_TRACK_RESOLUTION.get() * Math.max((int) start.translation().distance(end.translation()), 2);
            var origin = new Vector3d(start.translation());
            var basis = new Matrix3d(start.basis());
            var grad = new Vector3d(0, 0, 1).mul(start.basis());
            double[] totalDist = {0};

            // Render the base track for nextE2
            for (int i = 0; i < segs; i++) {
                double t0 = (double) i / segs;
                double t1 = (double) (i + 1) / segs;
                renderPart(world, matrices.peek(), buffer2, start, end, u0, u1, 0, WHITEF, t0, t1, totalDist, origin, basis, grad, overlay);
            }

            // Render the overlay for nextE2 (if applicable)
            if (trackType.overlay != null) {
                var olBuffer2 = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(getTrackOverlayTexture()));
                float[] olVOffset = {0};
                Vector3f olColor = new Vector3f(WHITEF);
                int power = nextE2.power();
                trackType.overlay.calculateEffects(power, entity.clientTime, olColor, olVOffset);

                for (int i = 0; i < segs; i++) {
                    double t0 = (double) i / segs;
                    double t1 = (double) (i + 1) / segs;
                    renderPart(world, matrices.peek(), olBuffer2, start, end, u0, u1, olVOffset[0], olColor, t0, t1, totalDist, origin, basis, grad, overlay);
                }
            }

            matrices.pop();
        }

        var prevE = entity.prev();
        if ((prevE == null) ^ (nextE == null || nextE2 == null)) {
            if (reinitBuffer) {
                buffer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(getTexture()));
            }

            float z0 = -0.5f;
            float z1 = 0;
            float v0 = 1;
            float v1 = 0.5f;

            if (nextE == null || nextE2 == null) {
                z0 = 0;
                z1 = 0.5f;
                v0 = 0.5f;
                v1 = 0;
            }

            matrices.push();

            matrices.translate(0.5, 0.5, 0.5);

            var entry = matrices.peek();
            var posMat = entry.getPositionMatrix();
            var nmlMat = entry.getNormalMatrix();
            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < 3; y++) {
                    posMat.setRowColumn(x, y, (float) start.basis().getRowColumn(x, y));
                    nmlMat.setRowColumn(x, y, (float) start.basis().getRowColumn(x, y));
                }
            }

            matrices.translate(0, -0.4375, 0);

            buffer.vertex(entry, 0.5f, 0, z0).color(WHITE).texture(0.0625f, v0).overlay(overlay).light(light).normal(entry, 0, 1, 0);
            buffer.vertex(entry, -0.5f, 0, z0).color(WHITE).texture(0, v0).overlay(overlay).light(light).normal(entry, 0, 1, 0);

            buffer.vertex(entry, -0.5f, 0, z1).color(WHITE).texture(0, v1).overlay(overlay).light(light).normal(entry, 0, 1, 0);
            buffer.vertex(entry, 0.5f, 0, z1).color(WHITE).texture(0.0625f, v1).overlay(overlay).light(light).normal(entry, 0, 1, 0);

            matrices.pop();
        }
    }

    protected Identifier getTexture() {
        return TRACK_TEXTURE;
    }

    protected Identifier getTrackOverlayTexture() {
        return TRACK_OVERLAY_TEXTURE;
    }

    @Override
    public boolean rendersOutsideBoundingBox(TrackTiesBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getRenderDistance() {
        return SporkcartClient.CFG_TRACK_RENDER_DISTANCE.get() * 16;
    }

    private void renderDebug(Pose pose, MatrixStack.Entry entry, VertexConsumer buffer) {
        var posMat = entry.getPositionMatrix();
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                posMat.setRowColumn(x, y, (float) pose.basis().getRowColumn(x, y));
            }
        }

        buffer.vertex(entry, 1, 0, 1).color(WHITE).texture(0, 0)
                .overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                .normal(entry, 0, 1, 0);
        buffer.vertex(entry, 0, 0, 1).color(WHITE).texture(1, 0)
                .overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                .normal(entry, 0, 1, 0);
        buffer.vertex(entry, 0, 0, 0).color(WHITE).texture(1, 1)
                .overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                .normal(entry, 0, 1, 0);
        buffer.vertex(entry, 1, 0, 0).color(WHITE).texture(0, 1)
                .overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                .normal(entry, 0, 1, 0);
    }

    private void renderPart(World world, MatrixStack.Entry entry, VertexConsumer buffer, Pose start, Pose end,
                            float u0, float u1, float vOffset, Vector3f color, double t0, double t1, double[] blockProgress,
                            Vector3d origin0, Matrix3d basis0, Vector3d grad0, int overlay) {
        start.interpolate(end, t0, origin0, basis0, grad0);
        var norm0 = new Vector3d(0, 1, 0).mul(basis0);

        var origin1 = new Vector3d(origin0);
        var basis1 = new Matrix3d(basis0);
        var grad1 = new Vector3d(grad0);
        start.interpolate(end, t1, origin1, basis1, grad1);
        var norm1 = new Vector3d(0, 1, 0).mul(basis1);

        float v0 = (float) blockProgress[0];
        while (v0 > 1) v0 -= 1;
        float v1 = v0 + (float) (grad0.length() * (t1 - t0));

        blockProgress[0] = v1;

        v1 = 1 - v1 + vOffset;
        v0 = 1 - v0 + vOffset;

        var pos0 = new BlockPos(MathHelper.floor(origin0.x()), MathHelper.floor(origin0.y()), MathHelper.floor(origin0.z()));
        var pos1 = new BlockPos(MathHelper.floor(origin1.x()), MathHelper.floor(origin1.y()), MathHelper.floor(origin1.z()));

        int light0 = WorldRenderer.getLightmapCoordinates(world, pos0);
        int light1 = WorldRenderer.getLightmapCoordinates(world, pos1);

        var point = new Vector3f();

        point.set(0.5, 0, 0).mul(basis0).add((float) origin0.x(), (float) origin0.y(), (float) origin0.z());
        buffer.vertex(entry, point).color(color.x(), color.y(), color.z(), 1).texture(u0, v0).overlay(overlay)
                .light(light0).normal(entry, (float) norm0.x(), (float) norm0.y(), (float) norm0.z());
        point.set(-0.5, 0, 0).mul(basis0).add((float) origin0.x(), (float) origin0.y(), (float) origin0.z());
        buffer.vertex(entry, point).color(color.x(), color.y(), color.z(), 1).texture(u1, v0).overlay(overlay)
                .light(light0).normal(entry, (float) norm0.x(), (float) norm0.y(), (float) norm0.z());

        point.set(-0.5, 0, 0).mul(basis1).add((float) origin1.x(), (float) origin1.y(), (float) origin1.z());
        buffer.vertex(entry, point).color(color.x(), color.y(), color.z(), 1).texture(u1, v1).overlay(overlay)
                .light(light1).normal(entry, (float) norm1.x(), (float) norm1.y(), (float) norm1.z());
        point.set(0.5, 0, 0).mul(basis1).add((float) origin1.x(), (float) origin1.y(), (float) origin1.z());
        buffer.vertex(entry, point).color(color.x(), color.y(), color.z(), 1).texture(u0, v1).overlay(overlay)
                .light(light1).normal(entry, (float) norm1.x(), (float) norm1.y(), (float) norm1.z());
    }

}