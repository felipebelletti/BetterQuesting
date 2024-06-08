package betterquesting;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

/**
 * Elevates the player's privileges to OP level for use in command rewards
 */
public class AdminExecute {
    private final Player player;
    private final CommandSourceStack commandSourceStack;

    public AdminExecute(Player player) {
        this.player = player;
        this.commandSourceStack = new CommandSourceStack(
                CommandSource.NULL,
                player.position(),
                player.getRotationVector(),
                player.getLevel(),
                4,
                player.getName().getString(),
                player.getDisplayName(),
                player.getServer(),
                player
        );
    }

    @Nonnull
    public String getName() {
        return player.getName().getString();
    }

    @Nonnull
    public Component getDisplayName() {
        return player.getDisplayName();
    }

    public void sendMessage(Component message) {
        player.sendSystemMessage(message);
    }

    public boolean canUseCommand(int permissionLevel, @Nonnull String commandName) {
        return true; // Always return true as the admin has all permissions
    }

    @Nonnull
    public BlockPos getPosition() {
        return player.blockPosition();
    }

    @Nonnull
    public Level getEntityWorld() {
        return player.getLevel();
    }

    @Nonnull
    public Vec3 getPositionVector() {
        return player.position();
    }

    public Entity getCommandSenderEntity() {
        return player;
    }

    public boolean sendCommandFeedback() {
        return true; // Always return true as the admin sends feedback
    }

    public MinecraftServer getServer() {
        return player.getServer();
    }

    public CommandSourceStack getCommandSourceStack() {
        return this.commandSourceStack;
    }
}
