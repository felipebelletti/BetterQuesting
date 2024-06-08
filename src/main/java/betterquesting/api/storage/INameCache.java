package betterquesting.api.storage;

import betterquesting.api2.storage.INBTPartial;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.ListTag;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

public interface INameCache extends INBTPartial<ListTag, UUID> {
    boolean updateName(@Nonnull ServerPlayer player);

    String getName(@Nonnull UUID uuid);

    UUID getUUID(@Nonnull String name);

    List<String> getAllNames();

    // Primarily used client side for GUIs
    boolean isOP(@Nonnull UUID uuid);

    int size();

    void reset();
}
