package io.github.joonbug03.sporkcart;

import io.github.joonbug03.sporkcart.block.ShuttleTiesBlock;
import io.github.joonbug03.sporkcart.block.SwitchTiesBlock;
import io.github.joonbug03.sporkcart.block.TrackTiesBlock;
import io.github.joonbug03.sporkcart.block.TrackTiesBlockEntity;
import io.github.joonbug03.sporkcart.component.OriginComponent;
import io.github.joonbug03.sporkcart.entity.TrackFollowerEntity;
import io.github.joonbug03.sporkcart.item.TrackItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

import java.util.List;

public class Sporkcart implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("sporkcart");

	public static final TrackTiesBlock TRACK_TIES = Registry.register(Registries.BLOCK, id("track_ties"),
			new TrackTiesBlock(AbstractBlock.Settings.copy(Blocks.RAIL)));
	public static final SwitchTiesBlock SWITCH_TIES = Registry.register(Registries.BLOCK, id("switch_ties"),
			new SwitchTiesBlock(AbstractBlock.Settings.copy(Blocks.RAIL)));
	public static final ShuttleTiesBlock SHUTTLE_TIES = Registry.register(Registries.BLOCK, id("shuttle_ties"),
			new ShuttleTiesBlock(AbstractBlock.Settings.copy(Blocks.RAIL)));
	public static final ShuttleTiesBlock SPLIT_TIES = Registry.register(Registries.BLOCK, id("split_ties"),
			new ShuttleTiesBlock(AbstractBlock.Settings.copy(Blocks.RAIL)));



	public static final TrackTiesBlock INVISIBLE_TIES = Registry.register(Registries.BLOCK, id("invisible_ties"),
			new TrackTiesBlock(AbstractBlock.Settings.copy(Blocks.RAIL)) {
				@Override
				protected BlockRenderType getRenderType(BlockState state) {
					return BlockRenderType.INVISIBLE;
				}
			});
	public static final TrackTiesBlock INVISIBLE_SWITCH_TIES = Registry.register(Registries.BLOCK, id("invisible_switch_ties"),
			new SwitchTiesBlock(AbstractBlock.Settings.copy(Blocks.RAIL)) {
				@Override
				protected BlockRenderType getRenderType(BlockState state) {
					return BlockRenderType.INVISIBLE;
				}
			});
	public static final TrackTiesBlock INVISIBLE_SHUTTLE_TIES = Registry.register(Registries.BLOCK, id("invisible_shuttle_ties"),
			new ShuttleTiesBlock(AbstractBlock.Settings.copy(Blocks.RAIL)) {
				@Override
				protected BlockRenderType getRenderType(BlockState state) {
					return BlockRenderType.INVISIBLE;
				}
			});
	public static final TrackTiesBlock INVISIBLE_SPLIT_TIES = Registry.register(Registries.BLOCK, id("invisible_split_ties"),
			new ShuttleTiesBlock(AbstractBlock.Settings.copy(Blocks.RAIL)) {
				@Override
				protected BlockRenderType getRenderType(BlockState state) {
					return BlockRenderType.INVISIBLE;
				}
			});
	public static final BlockEntityType<TrackTiesBlockEntity> TRACK_TIES_BE = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("track_ties"),
			BlockEntityType.Builder.create(TrackTiesBlockEntity::new, TRACK_TIES, SWITCH_TIES, SHUTTLE_TIES, SPLIT_TIES, INVISIBLE_TIES, INVISIBLE_SHUTTLE_TIES, INVISIBLE_SWITCH_TIES, INVISIBLE_SPLIT_TIES).build());

	public static final TrackItem TRACK = Registry.register(Registries.ITEM, id("track"),
			new TrackItem(TrackType.DEFAULT, new Item.Settings().component(DataComponentTypes.LORE,
					lore(Text.translatable("item.sporkcart.track.desc").formatted(Formatting.GRAY))
			)));
	public static final TrackItem CHAIN_DRIVE_TRACK = Registry.register(Registries.ITEM, id("chain_drive_track"),
			new TrackItem(TrackType.CHAIN_DRIVE, new Item.Settings().component(DataComponentTypes.LORE,
					lore(Text.translatable("item.sporkcart.chain_drive_track.desc").formatted(Formatting.GRAY))
			)));
	public static final TrackItem MAGNETIC_TRACK = Registry.register(Registries.ITEM, id("magnetic_track"),
			new TrackItem(TrackType.MAGNETIC, new Item.Settings().component(DataComponentTypes.LORE,
					lore(Text.translatable("item.sporkcart.magnetic_track.desc").formatted(Formatting.GRAY))
			)));
	public static final TrackItem STATION_TRACK = Registry.register(Registries.ITEM, id("station_track"),
			new TrackItem(TrackType.STATION, new Item.Settings().component(DataComponentTypes.LORE,
					lore(Text.translatable("item.sporkcart.station_track.desc").formatted(Formatting.GRAY))
			)));



	public static final TrackItem INVISIBLE_TRACK = Registry.register(Registries.ITEM, id("invisible_track"),
			new TrackItem(TrackType.INVISIBLE, new Item.Settings().component(DataComponentTypes.LORE,
					lore(Text.translatable("item.sporkcart.invisible_track.desc").formatted(Formatting.GRAY))
			)));
	public static final TrackItem INVISIBLE_CHAIN_DRIVE_TRACK = Registry.register(Registries.ITEM, id("invisible_chain_drive_track"),
			new TrackItem(TrackType.INVISIBLE_CHAIN_DRIVE, new Item.Settings().component(DataComponentTypes.LORE,
					lore(
							Text.translatable("item.sporkcart.chain_drive_track.desc").formatted(Formatting.GRAY),
							Text.translatable("item.sporkcart.invisible_track.desc").formatted(Formatting.GRAY)
					)
			)));
	public static final TrackItem INVISIBLE_MAGNETIC_TRACK = Registry.register(Registries.ITEM, id("invisible_magnetic_track"),
			new TrackItem(TrackType.INVISIBLE_MAGNETIC, new Item.Settings().component(DataComponentTypes.LORE,
					lore(Text.translatable("item.sporkcart.magnetic_track.desc").formatted(Formatting.GRAY),
							Text.translatable("item.sporkcart.invisible_track.desc").formatted(Formatting.GRAY))
			)));
	public static final TrackItem INVISIBLE_STATION_TRACK = Registry.register(Registries.ITEM, id("invisible_station_track"),
			new TrackItem(TrackType.INVISIBLE_STATION, new Item.Settings().component(DataComponentTypes.LORE,
					lore(Text.translatable("item.sporkcart.station_track.desc").formatted(Formatting.GRAY),
							Text.translatable("item.sporkcart.invisible_track.desc").formatted(Formatting.GRAY))
			)));

	public static final ComponentType<OriginComponent> ORIGIN_POS = Registry.register(Registries.DATA_COMPONENT_TYPE, id("origin"),
			ComponentType.<OriginComponent>builder().codec(OriginComponent.CODEC).build());

	public static final EntityType<TrackFollowerEntity> TRACK_FOLLOWER = Registry.register(Registries.ENTITY_TYPE, id("track_follower"),
			EntityType.Builder.<TrackFollowerEntity>create(TrackFollowerEntity::new, SpawnGroup.MISC).trackingTickInterval(2).dimensions(0.25f, 0.25f).build());

	public static final RegistryKey<ItemGroup> MOD_GROUP = Registry.registerReference(Registries.ITEM_GROUP, id("sporkcart"),
			FabricItemGroup.builder()
					.displayName(Text.translatable("itemGroup.sporkcart"))
					.icon(() -> new ItemStack(TRACK))
					.build()).getKey().orElseThrow();

	public static final TagKey<EntityType<?>> CARTS = TagKey.of(RegistryKeys.ENTITY_TYPE, id("carts"));

	@Override
	public void onInitialize() {
		var tieItem = Registry.register(Registries.ITEM, id("track_ties"),
				new BlockItem(TRACK_TIES, new Item.Settings()
						.component(DataComponentTypes.LORE,
								lore(Text.translatable("item.sporkcart.track_ties.desc").formatted(Formatting.GRAY))
						)));
		BlockItem switchTieItem = Registry.register(Registries.ITEM, id("switch_ties"),
				new BlockItem(SWITCH_TIES, new Item.Settings()
						.component(DataComponentTypes.LORE, lore(
								Text.translatable("item.sporkcart.track_ties.desc").formatted(Formatting.GRAY),
								Text.translatable("item.sporkcart.switch_ties.desc").formatted(Formatting.GRAY)
						))));
		BlockItem shuttleTieItem = Registry.register(Registries.ITEM, id("shuttle_ties"),
				new BlockItem(SHUTTLE_TIES, new Item.Settings()
						.component(DataComponentTypes.LORE, lore(
								Text.translatable("item.sporkcart.track_ties.desc").formatted(Formatting.GRAY),
								Text.translatable("item.sporkcart.shuttle_ties.desc").formatted(Formatting.GRAY)
						))));



		BlockItem invisibleTieItem = Registry.register(Registries.ITEM, id("invisible_ties"),
				new BlockItem(INVISIBLE_TIES, new Item.Settings()
						.component(DataComponentTypes.LORE, lore(
								Text.translatable("item.sporkcart.track_ties.desc").formatted(Formatting.GRAY),
								Text.translatable("item.sporkcart.invisible_ties.desc").formatted(Formatting.GRAY)
						))));
		BlockItem invisibleSwitchTieItem = Registry.register(Registries.ITEM, id("invisible_switch_ties"),
				new BlockItem(INVISIBLE_SWITCH_TIES, new Item.Settings()
						.component(DataComponentTypes.LORE, lore(
								Text.translatable("item.sporkcart.track_ties.desc").formatted(Formatting.GRAY),
								Text.translatable("item.sporkcart.switch_ties.desc").formatted(Formatting.GRAY),
								Text.translatable("item.sporkcart.invisible_ties.desc").formatted(Formatting.GRAY)
						))));
		BlockItem invisibleShuttleTieItem = Registry.register(Registries.ITEM, id("invisible_shuttle_ties"),
				new BlockItem(INVISIBLE_SHUTTLE_TIES, new Item.Settings()
						.component(DataComponentTypes.LORE, lore(
								Text.translatable("item.sporkcart.track_ties.desc").formatted(Formatting.GRAY),
								Text.translatable("item.sporkcart.shuttle_ties.desc").formatted(Formatting.GRAY),
								Text.translatable("item.sporkcart.invisible_ties.desc").formatted(Formatting.GRAY)
						))));

		ItemGroupEvents.modifyEntriesEvent(MOD_GROUP).register(entries -> {
			entries.add(tieItem.getDefaultStack());
			entries.add(switchTieItem.getDefaultStack());
			entries.add(shuttleTieItem.getDefaultStack());
			entries.add(invisibleTieItem.getDefaultStack());
			entries.add(invisibleSwitchTieItem.getDefaultStack());
			entries.add(invisibleShuttleTieItem.getDefaultStack());
			entries.add(TRACK.getDefaultStack());
			entries.add(CHAIN_DRIVE_TRACK.getDefaultStack());
			entries.add(MAGNETIC_TRACK.getDefaultStack());
			entries.add(STATION_TRACK.getDefaultStack());
			entries.add(INVISIBLE_TRACK.getDefaultStack());
			entries.add(INVISIBLE_CHAIN_DRIVE_TRACK.getDefaultStack());
			entries.add(INVISIBLE_MAGNETIC_TRACK.getDefaultStack());
			entries.add(INVISIBLE_STATION_TRACK.getDefaultStack());
		});
	}

	public static LoreComponent lore(Text lore) {
		return new LoreComponent(List.of(lore));
	}

	public static LoreComponent lore(Text lore, Text lore2) {
		return new LoreComponent(List.of(lore, lore2));
	}
	public static LoreComponent lore(Text lore, Text lore2, Text lore3) {
		return new LoreComponent(List.of(lore, lore2, lore3));
	}

	public static Identifier id(String path) {
		return Identifier.of("sporkcart", path);
	}
}