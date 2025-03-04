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

public class MergeTiesBlock extends TrackTiesBlock {

    public static final BooleanProperty MERGED = BooleanProperty.of("merged");

    public MergeTiesBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(FACING, Direction.UP).with(POINTING, 0).with(MERGED, false));
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        boolean merged = state.get(MERGED);
        boolean powered = world.isReceivingRedstonePower(pos);
        if (merged != powered) { world.setBlockState(pos, state.with(MERGED, powered), Block.NOTIFY_ALL); }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(MERGED);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        boolean merged = state.get(MERGED);
        boolean powered = world.isReceivingRedstonePower(pos);
        if (merged != powered) {
            world.setBlockState(pos, state.with(MERGED, powered), Block.NOTIFY_ALL);
        }
    }
}


