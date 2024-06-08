package betterquesting.questing.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import betterquesting.client.gui2.tasks.PanelTaskLocation;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.factory.FactoryTaskLocation;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class TaskLocation implements ITaskTickable {
    private final Set<UUID> completeUsers = new TreeSet<>();
    public String name = "New Location";
    public String structure = "";
    public String biome = "";
    public int x = 0;
    public int y = 0;
    public int z = 0;
    public int dim = 0;
    public int range = -1;
    public boolean visible = false;
    public boolean hideInfo = false;
    public boolean invert = false;
    public boolean taxiCab = false;

    @Override
    public ResourceLocation getFactoryID() {
        return FactoryTaskLocation.INSTANCE.getRegistryName();
    }

    @Override
    public String getUnlocalisedName() {
        return "bq_standard.task.location";
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
        if (pInfo.PLAYER.ticksExisted % 100 == 0) internalDetect(pInfo, quest);
    }

    @Override
    public void detect(@Nonnull ParticipantInfo pInfo, DBEntry<IQuest> quest) {
        internalDetect(pInfo, quest);
    }

    private void internalDetect(@Nonnull ParticipantInfo pInfo, DBEntry<IQuest> quest) {
        if (!pInfo.PLAYER.isEntityAlive() || !(pInfo.PLAYER instanceof ServerPlayer)) return;

        ServerPlayer playerMP = (ServerPlayer) pInfo.PLAYER;

        boolean flag = false;

        if (playerMP.dimension == dim && (range <= 0 || getDistance(playerMP) <= range)) {
            if (!StringUtils.isNullOrEmpty(biome) && !new ResourceLocation(biome).equals(playerMP.getServerWorld().getBiome(playerMP.getPosition()).getRegistryName())) {
                if (!invert) return;
            } else if (!StringUtils.isNullOrEmpty(structure) && !playerMP.getServerWorld().getChunkProvider().isInsideStructure(playerMP.world, structure, playerMP.getPosition())) {
                if (!invert) return;
            } else if (visible && range > 0) // Do not do ray casting with infinite range!
            {
                Vec3 pPos = new Vec3(playerMP.posX, playerMP.posY + playerMP.getEyeHeight(), playerMP.posZ);
                Vec3 tPos = new Vec3(x, y, z);
                RayTraceResult mop = playerMP.world.rayTraceBlocks(pPos, tPos, false, true, false);

                if (mop == null || mop.typeOfHit != RayTraceResult.Type.BLOCK) {
                    flag = true;
                }
            } else {
                flag = true;
            }
        }

        if (flag != invert) {
            pInfo.ALL_UUIDS.forEach((uuid) -> {
                if (!isComplete(uuid)) setComplete(uuid);
            });
            pInfo.markDirtyParty(Collections.singletonList(quest.getID()));
        }
    }

    private double getDistance(Player player) {
        if (!taxiCab) {
            return player.getDistance(x, y, z);
        } else {
            BlockPos pPos = player.getPosition();
            return Math.abs(pPos.getX() - x) + Math.abs(pPos.getY() - y) + Math.abs(pPos.getZ() - z);
        }
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag nbt) {
        nbt.setString("name", name);
        nbt.setInteger("posX", x);
        nbt.setInteger("posY", y);
        nbt.setInteger("posZ", z);
        nbt.setInteger("dimension", dim);
        nbt.setString("biome", biome);
        nbt.setString("structure", structure);
        nbt.setInteger("range", range);
        nbt.setBoolean("visible", visible);
        nbt.setBoolean("hideInfo", hideInfo);
        nbt.setBoolean("invert", invert);
        nbt.setBoolean("taxiCabDist", taxiCab);

        return nbt;
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        name = nbt.getString("name");
        x = nbt.getInteger("posX");
        y = nbt.getInteger("posY");
        z = nbt.getInteger("posZ");
        dim = nbt.getInteger("dimension");
        biome = nbt.getString("biome");
        structure = nbt.getString("structure");
        range = nbt.getInteger("range");
        visible = nbt.getBoolean("visible");
        hideInfo = nbt.getBoolean("hideInfo");
        invert = nbt.getBoolean("invert") || nbt.getBoolean("invertDistance");
        taxiCab = nbt.getBoolean("taxiCabDist");
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

    @Override
    public IGuiPanel getTaskGui(IGuiRect rect, DBEntry<IQuest> quest) {
        return new PanelTaskLocation(rect, this);
    }

    @Override
    public Screen getTaskEditor(Screen parent, DBEntry<IQuest> quest) {
        return null;
    }

    @Override
    public List<String> getTextForSearch() {
        return Collections.singletonList(name);
    }
}
