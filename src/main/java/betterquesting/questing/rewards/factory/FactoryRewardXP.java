package betterquesting.questing.rewards.factory;

import betterquesting.api.questing.rewards.IReward;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.rewards.RewardXP;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class FactoryRewardXP implements IFactoryData<IReward, CompoundTag> {
    public static final FactoryRewardXP INSTANCE = new FactoryRewardXP();

    @Override
    public ResourceLocation getRegistryName() {
        return new ResourceLocation(BetterQuesting.MODID_STD, "xp");
    }

    @Override
    public RewardXP createNew() {
        return new RewardXP();
    }

    @Override
    public RewardXP loadFromData(CompoundTag json) {
        RewardXP reward = new RewardXP();
        reward.readFromNBT(json);
        return reward;
    }

}
