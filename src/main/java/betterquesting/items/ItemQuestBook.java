package betterquesting.items;

import betterquesting.core.BetterQuesting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class ItemQuestBook extends Item {

    public ItemQuestBook() {

        this.setTranslationKey("betterquesting.quest_book");
        this.setCreativeTab(BetterQuesting.tabQuesting);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull Level world, @Nonnull Player player, @Nonnull InteractionHand hand) {

        ItemStack stack = player.getHeldItem(hand);

        if(world.isRemote && stack.getItem() == BetterQuesting.questBook) {
            player.openGui(BetterQuesting.instance, 3, world, player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());
        }

        return new ActionResult<>(EnumActionResult.PASS, stack);
    }
}
