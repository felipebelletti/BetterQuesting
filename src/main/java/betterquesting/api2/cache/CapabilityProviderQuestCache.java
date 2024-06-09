package betterquesting.api2.cache;

import betterquesting.core.ModReference;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.capabilities.Capability.IStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityProviderQuestCache implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {
    @CapabilityInject(QuestCache.class)
    public static Capability<QuestCache> CAP_QUEST_CACHE;
    public static final ResourceLocation LOC_QUEST_CACHE = new ResourceLocation(ModReference.MODID, "quest_cache");

    private final QuestCache cache = new QuestCache();

    @Override
    public CompoundTag serializeNBT() {
        return cache.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        cache.deserializeNBT(nbt);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable Direction facing) {
        return capability == CAP_QUEST_CACHE;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        return capability == CAP_QUEST_CACHE ? CAP_QUEST_CACHE.cast(cache) : null;
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(QuestCache.class, new IStorage<QuestCache>() {
            @Nullable
            @Override
            public Tag writeNBT(Capability<QuestCache> capability, QuestCache instance, Direction side) {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<QuestCache> capability, QuestCache instance, Direction side, Tag nbt) {
                if (nbt instanceof CompoundTag) instance.deserializeNBT((CompoundTag) nbt);
            }
        }, QuestCache::new);
    }
}
