package betterquesting.questing.rewards.factory;

import betterquesting.api.questing.rewards.IReward;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.rewards.RewardScoreboard;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class FactoryRewardScoreboard implements IFactoryData<IReward, CompoundTag> {
    public static final FactoryRewardScoreboard INSTANCE = new FactoryRewardScoreboard();

    @Override
    public ResourceLocation getRegistryName() {
        return new ResourceLocation(BetterQuesting.MODID_STD, "scoreboard");
    }

    @Override
    public RewardScoreboard createNew() {
        return new RewardScoreboard();
    }

    @Override
    public RewardScoreboard loadFromData(CompoundTag json) {
        RewardScoreboard reward = new RewardScoreboard();
        reward.readFromNBT(json);
        return reward;
    }

}
