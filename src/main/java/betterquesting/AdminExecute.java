package betterquesting;

import net.minecraft.command.CommandResultStats.Type;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

/**
 * Elevates the player's privileges to OP level for use in command rewards
 */
public class AdminExecute implements ICommandSender {
    private final EntityPlayer player;

    public AdminExecute(EntityPlayer player) {
        this.player = player;
    }

    @Nonnull
    @Override
    public String getName() {
        return player.getName();
    }

    @Nonnull
    @Override
    public Component getDisplayName() {
        return player.getDisplayName();
    }

    @Override
    public void sendMessage(Component p_145747_1_) {
        player.sendMessage(p_145747_1_);
    }

    @Override
    public boolean canUseCommand(int p_70003_1_, @Nonnull String p_70003_2_) {
        return true;
    }

    @Nonnull
    @Override
    public BlockPos getPosition() {
        return player.getPosition();
    }

    @Nonnull
    @Override
    public Level getEntityWorld() {
        return player.getEntityWorld();
    }

    @Nonnull
    @Override
    public Vec3 getPositionVector() {
        return player.getPositionVector();
    }

    @Override
    public Entity getCommandSenderEntity() {
        return player.getCommandSenderEntity();
    }

    @Override
    public boolean sendCommandFeedback() {
        return player.sendCommandFeedback();
    }

    @Override
    public void setCommandStat(Type type, int amount) {
        player.setCommandStat(type, amount);
    }

    @Override
    public MinecraftServer getServer() {
        return player.getServer();
    }
}
