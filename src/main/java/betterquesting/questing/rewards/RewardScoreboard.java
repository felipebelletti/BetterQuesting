package betterquesting.questing.rewards;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.client.gui2.rewards.PanelRewardScoreboard;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.rewards.factory.FactoryRewardScoreboard;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.scoreboard.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import org.apache.logging.log4j.Level;

public class RewardScoreboard implements IReward {
    public String score = "Reputation";
    public String type = "dummy";
    public boolean relative = true;
    public int value = 1;

    @Override
    public ResourceLocation getFactoryID() {
        return FactoryRewardScoreboard.INSTANCE.getRegistryName();
    }

    @Override
    public String getUnlocalisedName() {
        return "bq_standard.reward.scoreboard";
    }

    @Override
    public boolean canClaim(Player player, DBEntry<IQuest> quest) {
        return true;
    }

    @Override
    public void claimReward(Player player, DBEntry<IQuest> quest) {
        Scoreboard board = player.getWorldScoreboard();

        ScoreObjective scoreObj = board.getObjective(score);

        if (scoreObj == null) {
            try {
                IScoreCriteria criteria = IScoreCriteria.INSTANCES.get(type);
                criteria = criteria != null ? criteria : new ScoreCriteria(score);
                scoreObj = board.addScoreObjective(score, criteria);
                scoreObj.setDisplayName(score);
            } catch (Exception e) {
                BetterQuesting.logger.log(Level.ERROR, "Unable to create score '" + score + "' for reward!", e);
            }
        }

        if (scoreObj == null || scoreObj.getCriteria().isReadOnly()) {
            return;
        }

        Score s = board.getOrCreateScore(player.getName(), scoreObj);

        if (relative) {
            s.increaseScore(value);
        } else {
            s.setScorePoints(value);
        }
    }

    @Override
    public void readFromNBT(CompoundTag json) {
        score = json.getString("score");
        type = json.getString("type");
        value = json.getInteger("value");
        relative = json.getBoolean("relative");
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag json) {
        json.setString("score", score);
        json.setString("type", "dummy");
        json.setInteger("value", value);
        json.setBoolean("relative", relative);
        return json;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public IGuiPanel getRewardGui(IGuiRect rect, DBEntry<IQuest> quest) {
        return new PanelRewardScoreboard(rect, this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Screen getRewardEditor(Screen screen, DBEntry<IQuest> quest) {
        return null;
    }
}
