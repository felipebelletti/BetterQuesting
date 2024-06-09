package betterquesting.api2.utils;

import betterquesting.core.ModReference;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import org.jetbrains.annotations.NotNull;

public class EntityPlayerPreview extends RemotePlayer {
    private final ResourceLocation resource;

    /**
     * Backup constructor. DO NOT USE
     */
    public EntityPlayerPreview(ClientLevel worldIn) {
        this(worldIn, new GameProfile(null, "Notch"));
    }

    public EntityPlayerPreview(ClientLevel worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
        this.resource = new ResourceLocation(ModReference.MODID, "textures/skin_cache/" + gameProfileIn.getName());
        this.getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, (byte) 1);
    }

    @Override
    public @NotNull ResourceLocation getSkinTextureLocation() {
        return this.resource != null ? this.resource : DefaultPlayerSkin.getDefaultSkin(this.getUUID());
    }

    @Override
    public ResourceLocation getCloakTextureLocation() {
        return null;
    }

    @Override
    public boolean isModelPartShown(@NotNull PlayerModelPart part) {
        return true;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isWearing(PlayerModelPart part) {
        return true;
    }
}
