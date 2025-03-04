package io.github.joonbug03.sporkcart.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SplitTiesBlock extends TrackTiesBlock {

    public static final BooleanProperty SPLITED = BooleanProperty.of("splited");

    public SplitTiesBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(FACING, Direction.UP).with(POINTING, 0).with(SPLITED, false));
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        boolean splited = state.get(SPLITED);
        boolean powered = world.isReceivingRedstonePower(pos);
        if (splited != powered) { world.setBlockState(pos, state.with(SPLITED, powered), Block.NOTIFY_ALL); }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(SPLITED);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        boolean splited = state.get(SPLITED);
        boolean powered = world.isReceivingRedstonePower(pos);
        if (splited != powered) {
            world.setBlockState(pos, state.with(SPLITED, powered), Block.NOTIFY_ALL);
        }
    }
}