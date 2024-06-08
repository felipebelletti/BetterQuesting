package betterquesting.api.placeholders.rewards;

import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.ModReference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ResourceLocation;

public class FactoryRewardPlaceholder implements IFactoryData<RewardPlaceholder, CompoundTag> {
    public static final FactoryRewardPlaceholder INSTANCE = new FactoryRewardPlaceholder();

    private final ResourceLocation ID = new ResourceLocation(ModReference.MODID, "placeholder");

    private FactoryRewardPlaceholder() {
    }

    @Override
    public ResourceLocation getRegistryName() {
        return ID;
    }

    @Override
    public RewardPlaceholder createNew() {
        return new RewardPlaceholder();
    }

    @Override
    public RewardPlaceholder loadFromData(CompoundTag nbt) {
        RewardPlaceholder reward = createNew();
        reward.readFromNBT(nbt);
        return reward;
    }
}
