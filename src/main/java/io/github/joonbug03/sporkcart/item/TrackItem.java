package io.github.joonbug03.sporkcart.item;

import io.github.joonbug03.sporkcart.Sporkcart;
import io.github.joonbug03.sporkcart.TrackType;
import io.github.joonbug03.sporkcart.block.MergeTiesBlock;
import io.github.joonbug03.sporkcart.block.MergeTiesBlockEntity;
import io.github.joonbug03.sporkcart.block.SplitTiesBlockEntity;
import io.github.joonbug03.sporkcart.block.TrackTiesBlockEntity;
import io.github.joonbug03.sporkcart.component.OriginComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

import javax.sound.midi.Track;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrackItem extends Item {
    public static final Map<TrackType, Item> ITEMS_BY_TYPE = new HashMap<>();

    public final TrackType track;

    public TrackItem(TrackType track, Settings settings) {
        super(settings);

        this.track = track;
        ITEMS_BY_TYPE.put(track, this);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getPlayer() != null && !context.getPlayer().canModifyBlocks()) {
            return super.useOnBlock(context);
        }

        var world = context.getWorld();
        var pos = context.getBlockPos();
        var stack = context.getStack();

        if (world.getBlockEntity(pos) instanceof TrackTiesBlockEntity ties) {
            if (world.isClient()) {
                return ActionResult.SUCCESS;
            }

            var origin = stack.get(Sporkcart.ORIGIN_POS);
            if (origin != null) {
                var oPos = origin.pos();
                if (!pos.equals(oPos) && world.getBlockEntity(oPos) instanceof TrackTiesBlockEntity oTies) {
                    boolean success = false;

                    if (oTies instanceof SplitTiesBlockEntity splitTies && (this.track == TrackType.DEFAULT || this.track == TrackType.INVISIBLE)) {
                        if (oTies.next() == null && (ties.prev() == null || (ties instanceof MergeTiesBlockEntity mergeTies && mergeTies.prev2() == null) ) ) {
                            splitTies.setNext(pos, this.track);
                            world.playSound(null, pos, SoundEvents.ENTITY_IRON_GOLEM_REPAIR, SoundCategory.BLOCKS, 1.5f, 0.7f);
                            success = true;
                        } else if (oTies.next2() == null && (ties.prev() == null || (ties instanceof MergeTiesBlockEntity mergeTies && mergeTies.prev2() == null) )) {
                            splitTies.setNext2(pos, this.track);
                            world.playSound(null, pos, SoundEvents.ENTITY_IRON_GOLEM_REPAIR, SoundCategory.BLOCKS, 1.5f, 0.7f);
                            success = true;
                        }
                    } else if (oTies.next() == null && ties instanceof MergeTiesBlockEntity mergeTies && (this.track == TrackType.DEFAULT || this.track == TrackType.INVISIBLE) && (mergeTies.prev() == null || mergeTies.prev2() == null )){
                        oTies.setNext(pos, this.track);
                        world.playSound(null, pos, SoundEvents.ENTITY_IRON_GOLEM_REPAIR, SoundCategory.BLOCKS, 1.5f, 0.7f);
                        success = true;
                    } else if (oTies.next() == null && ties.prev() == null && !(oTies instanceof SplitTiesBlockEntity) && !(ties instanceof MergeTiesBlockEntity) && !(ties instanceof SplitTiesBlockEntity) && !(oTies instanceof MergeTiesBlockEntity)) {
                        oTies.setNext(pos, this.track);
                        world.playSound(null, pos, SoundEvents.ENTITY_IRON_GOLEM_REPAIR, SoundCategory.BLOCKS, 1.5f, 0.7f);
                        success = true;
                    } else if(oTies.next() == null && ties.prev() == null && ties instanceof SplitTiesBlockEntity && (this.track == TrackType.DEFAULT || this.track == TrackType.INVISIBLE)) {
                        oTies.setNext(pos, this.track);
                        world.playSound(null, pos, SoundEvents.ENTITY_IRON_GOLEM_REPAIR, SoundCategory.BLOCKS, 1.5f, 0.7f);
                        success = true;
                    } else if(oTies.next() == null && ties.prev() == null && oTies instanceof MergeTiesBlockEntity && (this.track == TrackType.DEFAULT || this.track == TrackType.INVISIBLE)) {
                        oTies.setNext(pos, this.track);
                        world.playSound(null, pos, SoundEvents.ENTITY_IRON_GOLEM_REPAIR, SoundCategory.BLOCKS, 1.5f, 0.7f);
                        success = true;
                    }

                    if (success) {
                        stack.decrement(1); // Decrement the stack count
                        stack.remove(Sporkcart.ORIGIN_POS);
                        return ActionResult.SUCCESS;
                    }
                }

                stack.remove(Sporkcart.ORIGIN_POS);
            } else {
                stack.set(Sporkcart.ORIGIN_POS, new OriginComponent(pos));
            }
        } else {
            var origin = stack.get(Sporkcart.ORIGIN_POS);
            if (origin != null) {
                if (world.isClient()) {
                    return ActionResult.CONSUME;
                }

                stack.remove(Sporkcart.ORIGIN_POS);
            }
        }

        return super.useOnBlock(context);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);

        var origin = stack.get(Sporkcart.ORIGIN_POS);
        if (origin != null) {
            origin.appendTooltip(context, tooltip::add, type);
        }
    }
}
