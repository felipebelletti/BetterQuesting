package betterquesting.api.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public interface IPacketRegistry {
    void registerServerHandler(@Nonnull ResourceLocation idName, @Nonnull Consumer<Tuple<CompoundTag, ServerPlayer>> method);

    @OnlyIn(Dist.CLIENT)
    void registerClientHandler(@Nonnull ResourceLocation idName, @Nonnull Consumer<CompoundTag> method);

    @Nullable
    Consumer<Tuple<CompoundTag, ServerPlayer>> getServerHandler(@Nonnull ResourceLocation idName);

    @Nullable
    @OnlyIn(Dist.CLIENT)
    Consumer<CompoundTag> getClientHandler(@Nonnull ResourceLocation idName);
}
