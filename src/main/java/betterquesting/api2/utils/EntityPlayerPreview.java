package betterquesting.api2.utils;

import betterquesting.core.ModReference;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityPlayerPreview extends EntityOtherPlayerMP {
    private final ResourceLocation resource;

    /**
     * Backup constructor. DO NOT USE
     */
    public EntityPlayerPreview(World worldIn) {
        this(worldIn, new GameProfile(null, "Notch"));
    }

    public EntityPlayerPreview(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
        this.resource = new ResourceLocation(ModReference.MODID, "textures/skin_cache/" + gameProfileIn.getName());
        this.getDataManager().set(PLAYER_MODEL_FLAG, (byte) 1);
    }

    @Override
    public ResourceLocation getLocationSkin() {
        return this.resource;
    }

    @Override
    public ResourceLocation getLocationCape() {
        return null;
    }

    @Override
    public boolean hasSkin() {
        return true;
    }

    @Override
    public Component getDisplayName() {
        return new TextComponentString("");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isWearing(EnumPlayerModelParts part) {
        return true;
    }
}
