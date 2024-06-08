package betterquesting.questing.rewards.factory;

import betterquesting.api.questing.rewards.IReward;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.rewards.RewardRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class FactoryRewardRecipe implements IFactoryData<IReward, CompoundTag> {
    public static final FactoryRewardRecipe INSTANCE = new FactoryRewardRecipe();

    @Override
    public ResourceLocation getRegistryName() {
        return new ResourceLocation(BetterQuesting.MODID_STD, "recipe");
    }

    @Override
    public RewardRecipe createNew() {
        return new RewardRecipe();
    }

    @Override
    public RewardRecipe loadFromData(CompoundTag json) {
        RewardRecipe reward = new RewardRecipe();
        reward.readFromNBT(json);
        return reward;
    }

}
