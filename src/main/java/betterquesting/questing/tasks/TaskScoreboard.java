package betterquesting.questing.tasks;

import betterquesting.ScoreboardBQ;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import betterquesting.client.gui2.editors.tasks.GuiEditTaskScoreboard;
import betterquesting.client.gui2.tasks.PanelTaskScoreboard;
import betterquesting.core.BetterQuesting;
import betterquesting.network.handlers.NetScoreSync;
import betterquesting.questing.tasks.factory.FactoryTaskScoreboard;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class TaskScoreboard implements ITaskTickable {
    private final Set<UUID> completeUsers = new TreeSet<>();
    public String scoreName = "Score";
    public String scoreDisp = "Score";
    public String type = "dummy";
    public int target = 1;
    public float conversion = 1F;
    public String suffix = "";
    public ScoreOperation operation = ScoreOperation.MORE_OR_EQUAL;

    @Override
    public ResourceLocation getFactoryID() {
        return FactoryTaskScoreboard.INSTANCE.getRegistryName();
    }

    @Override
    public String getUnlocalisedName() {
        return "bq_standard.task.scoreboard";
    }

    @Override
    public boolean isComplete(UUID uuid) {
        return completeUsers.contains(uuid);
    }

    @Override
    public void setComplete(UUID uuid) {
        completeUsers.add(uuid);
    }

    @Override
    public void resetUser(@Nullable UUID uuid) {
        if (uuid == null) {
            completeUsers.clear();
        } else {
            completeUsers.remove(uuid);
        }
    }

    @Override
    public void tickTask(@Nonnull ParticipantInfo pInfo, DBEntry<IQuest> quest) {
        if (pInfo.PLAYER.ticksExisted % 20 == 0) detect(pInfo, quest); // Auto-detect once per second
    }

    @Override
    public void detect(@Nonnull ParticipantInfo pInfo, DBEntry<IQuest> quest) {
        Scoreboard board = pInfo.PLAYER.getWorldScoreboard();
        ScoreObjective scoreObj = board.getObjective(scoreName);

        if (scoreObj == null) {
            try {
                IScoreCriteria criteria = IScoreCriteria.INSTANCES.computeIfAbsent(type, (t) -> new ScoreCriteria(scoreName));
                scoreObj = board.addScoreObjective(scoreName, criteria);
                scoreObj.setDisplayName(scoreDisp);
            } catch (Exception e) {
                BetterQuesting.logger.log(Level.ERROR, "Unable to create score '" + scoreName + "' for task!", e);
                return;
            }
        }

        int points = board.getOrCreateScore(pInfo.PLAYER.getName(), scoreObj).getScorePoints();
        int lastValue = ScoreboardBQ.INSTANCE.getScore(pInfo.UUID, scoreName);

        if (points != lastValue) {
            ScoreboardBQ.INSTANCE.setScore(pInfo.UUID, scoreName, points);
            if (pInfo.PLAYER instanceof ServerPlayer) NetScoreSync.sendScore((ServerPlayer) pInfo.PLAYER);
        }

        if (operation.checkValues(points, target)) {
            setComplete(pInfo.UUID);
            pInfo.markDirty(Collections.singletonList(quest.getID()));
        }
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag nbt) {
        nbt.setString("scoreName", scoreName);
        nbt.setString("scoreDisp", scoreDisp);
        nbt.setString("type", type);
        nbt.setInteger("target", target);
        nbt.setFloat("unitConversion", conversion);
        nbt.setString("unitSuffix", suffix);
        nbt.setString("operation", operation.name());

        return nbt;
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        scoreName = nbt.getString("scoreName");
        scoreName = scoreName.replaceAll(" ", "_");
        scoreDisp = nbt.getString("scoreDisp");
        type = nbt.hasKey("type", 8) ? nbt.getString("type") : "dummy";
        target = nbt.getInteger("target");
        conversion = nbt.getFloat("unitConversion");
        suffix = nbt.getString("unitSuffix");
        try {
            operation = ScoreOperation.valueOf(nbt.hasKey("operation", 8) ? nbt.getString("operation") : "MORE_OR_EQUAL");
        } catch (Exception e) {
            operation = ScoreOperation.MORE_OR_EQUAL;
        }
    }

    @Override
    public CompoundTag writeProgressToNBT(CompoundTag nbt, @Nullable List<UUID> users) {
        ListTag jArray = new ListTag();

        completeUsers.forEach((uuid) -> {
            if (users == null || users.contains(uuid)) jArray.appendTag(new NBTTagString(uuid.toString()));
        });

        nbt.setTag("completeUsers", jArray);

        return nbt;
    }

    @Override
    public void readProgressFromNBT(CompoundTag nbt, boolean merge) {
        if (!merge) completeUsers.clear();
        ListTag cList = nbt.getTagList("completeUsers", 8);
        for (int i = 0; i < cList.tagCount(); i++) {
            try {
                completeUsers.add(UUID.fromString(cList.getStringTagAt(i)));
            } catch (Exception e) {
                BetterQuesting.logger.log(Level.ERROR, "Unable to load UUID for task", e);
            }
        }
    }

    public enum ScoreOperation {
        EQUAL("="),
        LESS_THAN("<"),
        MORE_THAN(">"),
        LESS_OR_EQUAL("<="),
        MORE_OR_EQUAL(">="),
        NOT("=/=");

        private final String text;

        ScoreOperation(String text) {
            this.text = text;
        }

        public String GetText() {
            return text;
        }

        public boolean checkValues(int n1, int n2) {
            switch (this) {
                case EQUAL:
                    return n1 == n2;
                case LESS_THAN:
                    return n1 < n2;
                case MORE_THAN:
                    return n1 > n2;
                case LESS_OR_EQUAL:
                    return n1 <= n2;
                case MORE_OR_EQUAL:
                    return n1 >= n2;
                case NOT:
                    return n1 != n2;
            }

            return false;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public IGuiPanel getTaskGui(IGuiRect rect, DBEntry<IQuest> quest) {
        return new PanelTaskScoreboard(rect, this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Screen getTaskEditor(Screen parent, DBEntry<IQuest> quest) {
        return new GuiEditTaskScoreboard(parent, quest, this);
    }

    @Override
    public List<String> getTextForSearch() {
        List<String> texts = new ArrayList<>();
        texts.add(scoreName);
        texts.add(scoreDisp);
        return texts;
    }
}
