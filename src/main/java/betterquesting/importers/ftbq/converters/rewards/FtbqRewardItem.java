package betterquesting.importers.ftbq.converters.rewards;

import betterquesting.api.questing.rewards.IReward;
import betterquesting.importers.ftbq.FTBQUtils;
import betterquesting.questing.rewards.RewardItem;
import net.minecraft.nbt.CompoundTag;

public class FtbqRewardItem {
    public IReward[] convertTask(CompoundTag tag) {
        RewardItem reward = new RewardItem();

        reward.items.add(FTBQUtils.convertItem(tag.getTag("item"))); // One item per reward. Isn't that a PITA?

        return new IReward[]{reward};
    }
}
