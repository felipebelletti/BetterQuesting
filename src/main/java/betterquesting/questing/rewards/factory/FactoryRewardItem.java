package betterquesting.questing.rewards.factory;

import betterquesting.api.questing.rewards.IReward;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.rewards.RewardItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class FactoryRewardItem implements IFactoryData<IReward, CompoundTag> {
    public static final FactoryRewardItem INSTANCE = new FactoryRewardItem();

    @Override
    public ResourceLocation getRegistryName() {
        return new ResourceLocation(BetterQuesting.MODID_STD, "item");
    }

    @Override
    public RewardItem createNew() {
        return new RewardItem();
    }

    @Override
    public RewardItem loadFromData(CompoundTag json) {
        RewardItem reward = new RewardItem();
        reward.readFromNBT(json);
        return reward;
    }

}
