package betterquesting.client.ui_builder;

import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.INBTSaveLoad;
import betterquesting.core.ModReference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.joml.Vector4f;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ComponentPanel implements INBTSaveLoad<CompoundTag> {
    // Purely for organisational purposes
    public String refName = "New Panel";
    public String panelType = new ResourceLocation(ModReference.MODID, "canvas_empty").toString();

    // Usually these two are the same but not always
    public int cvParentID = -1; // ID of the canvas we're contained within
    public int tfParentID = -1; // ID of the transform we're positioned relative to

    private CompoundTag transTag = new CompoundTag();
    private CompoundTag panelData = new CompoundTag();

    // When these are passed off to the GUI context, make sure it's stated whether it's in-editor or not
    // (only content and navigation need setting up otherwise the GUI might actually edit things before intended use)
    private final List<String> scripts = new ArrayList<>();

    public ComponentPanel() {
        setTransform(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(8, 8, 8, 8), 0));
    }

    public ComponentPanel(GuiTransform transform) {
        setTransform(transform);
    }

    public void setTransform(GuiTransform transform) {
        Vector4f anchor = transform.getAnchor();
        transTag.setFloat("anchor_left", anchor.x);
        transTag.setFloat("anchor_top", anchor.y);
        transTag.setFloat("anchor_right", anchor.z);
        transTag.setFloat("anchor_bottom", anchor.w);

        GuiPadding padding = transform.getPadding();
        transTag.setInteger("pad_left", padding.l);
        transTag.setInteger("pad_top", padding.t);
        transTag.setInteger("pad_right", padding.r);
        transTag.setInteger("pad_bottom", padding.b);

        transTag.setInteger("depth", transform.getDepth());
    }

    public CompoundTag getTransformTag() {
        return transTag;
    }

    public CompoundTag getPanelData() {
        return panelData;
    }

    public void setPanelData(@Nonnull CompoundTag tag) {
        this.panelData = tag;
    }

    public IGuiPanel build() {
        Vector4f anchor = new Vector4f(transTag.getFloat("anchor_left"), transTag.getFloat("anchor_top"), transTag.getFloat("anchor_right"), transTag.getFloat("anchor_bottom"));
        GuiPadding padding = new GuiPadding(transTag.getInteger("pad_left"), transTag.getInteger("pad_top"), transTag.getInteger("pad_right"), transTag.getInteger("pad_bottom"));
        GuiTransform transform = new GuiTransform(anchor, padding, transTag.getInteger("depth"));

        ResourceLocation res = StringUtils.isEmpty(panelType) ? new ResourceLocation(ModReference.MODID, "canvas_empty") : new ResourceLocation(panelType);
        return ComponentRegistry.INSTANCE.createNew(res, transform, panelData);
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag nbt) {
        nbt.setString("ref_name", refName);
        nbt.setString("panel_type", panelType);

        nbt.setInteger("cv_parent", cvParentID);
        nbt.setInteger("tf_parent", tfParentID);

        nbt.setTag("transform", transTag.copy());
        nbt.setTag("panel_data", panelData.copy());

        ListTag sList = new ListTag();
        scripts.forEach((str) -> sList.appendTag(new NBTTagString(str)));
        nbt.setTag("script_hooks", sList);

        return nbt;
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        refName = nbt.getString("ref_name");
        panelType = nbt.getString("panel_type");

        cvParentID = nbt.getInteger("cv_parent");
        tfParentID = nbt.getInteger("tf_parent");

        // Location of the panel
        transTag = nbt.getCompoundTag("transform").copy();
        panelData = nbt.getCompoundTag("panel_data").copy();

        scripts.clear();
        ListTag sList = nbt.getTagList("script_hooks", 8);
        for (int i = 0; i < sList.tagCount(); i++) {
            scripts.add(sList.getStringTagAt(i));
        }
    }
}
