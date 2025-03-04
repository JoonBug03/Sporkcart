package io.github.joonbug03.sporkcart;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.joonbug03.sporkcart.block.entity.TrackTiesBlockEntityRenderer;
import io.github.joonbug03.sporkcart.config.Config;
import io.github.joonbug03.sporkcart.config.ConfigOption;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.EmptyEntityRenderer;

import java.io.IOException;

public class SporkcartClient implements ClientModInitializer {
	public static final Config CONFIG = new Config("sporkcart_client",
			() -> FabricLoader.getInstance().getConfigDir()
					.resolve("sporkcart").resolve("sporkcart_client.properties"));

	public static final ConfigOption.BooleanOption CFG_ROTATE_CAMERA = CONFIG.optBool("rotate_camera", true);
	public static final ConfigOption.IntOption CFG_TRACK_RESOLUTION = CONFIG.optInt("track_resolution", 3, 1, 16);
	public static final ConfigOption.IntOption CFG_TRACK_RENDER_DISTANCE = CONFIG.optInt("track_render_distance", 8, 4, 32);

	@Override
	public void onInitializeClient() {
		try {
			CONFIG.load();
		} catch (IOException e) {
			Sporkcart.LOGGER.error("Error loading client config on mod init", e);
		}

		BlockRenderLayerMap.INSTANCE.putBlock(Sporkcart.TRACK_TIES, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Sporkcart.SWITCH_TIES, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Sporkcart.SHUTTLE_TIES, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Sporkcart.INVISIBLE_TIES, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Sporkcart.INVISIBLE_SWITCH_TIES, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Sporkcart.INVISIBLE_SHUTTLE_TIES, RenderLayer.getCutout());


		BlockEntityRendererFactories.register(Sporkcart.TRACK_TIES_BE, TrackTiesBlockEntityRenderer::new);
		EntityRendererRegistry.register(Sporkcart.TRACK_FOLLOWER, EmptyEntityRenderer::new);

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(
					LiteralArgumentBuilder.<FabricClientCommandSource>literal("sporkcartc")
							.then(CONFIG.command(LiteralArgumentBuilder.literal("config"),
									FabricClientCommandSource::sendFeedback))
		));
	}
}