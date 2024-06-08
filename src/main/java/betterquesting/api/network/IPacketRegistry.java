package betterquesting.api.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public interface IPacketRegistry {
    void registerServerHandler(@Nonnull ResourceLocation idName, @Nonnull Consumer<Tuple<CompoundTag, ServerPlayer>> method);

    @SideOnly(Side.CLIENT)
    void registerClientHandler(@Nonnull ResourceLocation idName, @Nonnull Consumer<CompoundTag> method);

    @Nullable
    Consumer<Tuple<CompoundTag, ServerPlayer>> getServerHandler(@Nonnull ResourceLocation idName);

    @Nullable
    @SideOnly(Side.CLIENT)
    Consumer<CompoundTag> getClientHandler(@Nonnull ResourceLocation idName);
}
