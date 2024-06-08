package betterquesting.api.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ResourceLocation;

public final class QuestingPacket {
    private final ResourceLocation handler;
    private final CompoundTag payload;

    public QuestingPacket(ResourceLocation handler, CompoundTag payload) {
        this.handler = handler;
        this.payload = payload;
    }

    public ResourceLocation getHandler() {
        return handler;
    }

    public CompoundTag getPayload() {
        return payload;
    }
}
