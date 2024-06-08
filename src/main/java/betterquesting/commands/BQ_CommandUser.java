package betterquesting.commands;

import betterquesting.commands.user.QuestCommandHelp;
import betterquesting.commands.user.QuestCommandRefresh;
import betterquesting.commands.user.QuestCommandSPHardcore;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.commands.CommandSource;
import net.minecraft.command.WrongUsageException;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.ChatFormatting;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BQ_CommandUser extends CommandBase {
    private final List<QuestCommandBase> coms = new ArrayList<>();

    public BQ_CommandUser() {
        PermissionAPI.registerNode("betterquesting.command.user", DefaultPermissionLevel.ALL, "user commmand permission");

        coms.add(new QuestCommandHelp());
        coms.add(new QuestCommandRefresh());
        coms.add(new QuestCommandSPHardcore());
    }

    @Override
    public String getName() {
        return "bq_user";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, CommandSource sender) {
        return !(sender instanceof Player) || PermissionAPI.hasPermission((Player) sender, "betterquesting.command.user");
    }

    @Override
    public String getUsage(CommandSource sender) {
        StringBuilder txt = new StringBuilder();

        for (int i = 0; i < coms.size(); i++) {
            QuestCommandBase c = coms.get(i);
            txt.append("/bq_user ").append(c.getCommand());

            if (c.getUsageSuffix().length() > 0) {
                txt.append(" ").append(c.getUsageSuffix());
            }

            if (i < coms.size() - 1) {
                txt.append(", ");
            }
        }

        return txt.toString();
    }

    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
    @Override
    public List<String> getTabCompletions(MinecraftServer server, CommandSource sender, String[] strings, BlockPos pos) {
        if (strings.length == 1) {
            List<String> base = new ArrayList<>();
            for (QuestCommandBase c : coms) {
                if (!(sender instanceof Player) || PermissionAPI.hasPermission((Player) sender, c.getPermissionNode())) {
                    base.add(c.getCommand());
                }
            }
            return getListOfStringsMatchingLastWord(strings, base.toArray(new String[0]));
        } else if (strings.length > 1) {
            for (QuestCommandBase c : coms) {
                if (c.getCommand().equalsIgnoreCase(strings[0])) {
                    if (!(sender instanceof Player) || PermissionAPI.hasPermission((Player) sender, c.getPermissionNode())) {
                        return c.autoComplete(server, sender, strings);
                    }
                }
            }
        }

        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
        if (args.length < 1) {
            throw new WrongUsageException(this.getUsage(sender));
        }

        for (QuestCommandBase c : coms) {
            if (c.getCommand().equalsIgnoreCase(args[0])) {
                if (!(sender instanceof Player) || PermissionAPI.hasPermission((Player) sender, c.getPermissionNode())) {
                    if (c.validArgs(args)) {
                        c.runCommand(server, this, sender, args);
                        return;
                    } else {
                        throw c.getException(this);
                    }
                } else {
                    TextComponentTranslation cc = new TextComponentTranslation("commands.generic.permission");
                    cc.getStyle().setColor(TextFormatting.RED);
                    sender.sendMessage(cc);
                    return;
                }
            }
        }

        throw new WrongUsageException(this.getUsage(sender));
    }

    /**
     * Return whether the specified command parameter index is a username parameter.
     */
    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        if (args.length < 1) {
            return false;
        }

        for (QuestCommandBase c : coms) {
            if (c.getCommand().equalsIgnoreCase(args[0])) {
                return c.isArgUsername(args, index);
            }
        }

        return false;
    }
}
