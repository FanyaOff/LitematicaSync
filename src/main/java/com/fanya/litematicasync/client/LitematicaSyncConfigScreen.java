package com.fanya.litematicasync.client;

import com.fanya.litematicasync.config.LitematicaSyncConfig;
import com.fanya.litematicasync.config.ServerGroup;
//? if >=26 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?} else {
import net.minecraft.client.gui.GuiGraphics;
//?}
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class LitematicaSyncConfigScreen extends Screen {
    private static final int HEADER_HEIGHT = 48;
    private static final int FOOTER_HEIGHT = 48;
    private static final int ROW_HEIGHT = 22;
    private static final int ENTRY_WIDTH = 340;
    private static final int BUTTON_HEIGHT = 20;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int CATEGORY_COLOR = 0xFFFFE616;
    private static final int ROW_HOVER = 0x22FFFFFF;
    private static final int LIST_SHADE = 0x68000000;
    private static final int HEADER_SHADE = 0xAA000000;
    private static final int SCROLL_TRACK = 0x66000000;
    private static final int SCROLL_THUMB = 0xFF808080;
    private static final int TREE_LINE = 0xFF888888;

    private final Screen parent;
    private final List<GroupDraft> groups = new ArrayList<>();
    private final List<RowDraft> visibleRows = new ArrayList<>();
    private int scrollOffset;
    private int maxScroll;

    public LitematicaSyncConfigScreen(Screen parent) {
        super(Component.translatable("title.litematicasync.config"));
        this.parent = parent;

        for (ServerGroup group : LitematicaSyncConfig.get().groups()) {
            this.groups.add(new GroupDraft(group));
        }
    }

    @Override
    protected void init() {
        this.rebuildConfigWidgets();
    }

    //? if >=26 {
    /*@Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        this.extractChrome(graphics, mouseX, mouseY);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private void extractChrome(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int listTop = this.listTop();
        int listBottom = this.listBottom();
        graphics.fillGradient(0, listTop, this.width, listBottom, LIST_SHADE, LIST_SHADE);
        graphics.fillGradient(0, 0, this.width, listTop, HEADER_SHADE, HEADER_SHADE);
        graphics.fillGradient(0, listBottom, this.width, this.height, HEADER_SHADE, HEADER_SHADE);
        graphics.fillGradient(0, listTop, this.width, listTop + 4, 0xAA000000, 0x00000000);
        graphics.fillGradient(0, listBottom - 4, this.width, listBottom, 0x00000000, 0xAA000000);
        graphics.centeredText(this.font, this.title, this.width / 2, 18, TEXT_COLOR);
        this.extractRows(graphics, mouseX, mouseY);
        this.extractScrollbar(graphics);
    }

    private void extractRows(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        for (RowDraft row : this.visibleRows) {
            int x = this.entryX();
            int width = this.entryWidth();

            if (row.isHovered(mouseX, mouseY, x, width)) {
                graphics.fillGradient(x, row.y, x + width, row.y + row.height(), ROW_HOVER, ROW_HOVER);
            }

            if (row.type == RowType.ADDRESS || row.type == RowType.ADD_ADDRESS) {
                int lineX = x + 10;
                int lineY = row.y;
                int midY = row.y + row.height() / 2;
                if (row.type == RowType.ADDRESS) {
                    graphics.fill(lineX, lineY - 4, lineX + 1, row.y + row.height() + 4, TREE_LINE);
                } else {
                    graphics.fill(lineX, lineY - 4, lineX + 1, midY, TREE_LINE);
                }
                graphics.fill(lineX, midY, lineX + 12, midY + 1, TREE_LINE);
            }

            if (row.type == RowType.GROUP) {
                boolean hoverV = mouseX >= x && mouseX <= x + 20 && mouseY >= row.y + 1 && mouseY <= row.y + 21;
                graphics.centeredText(this.font, Component.literal(row.group.expanded ? "v" : ">"), x + 10, row.y + 7, hoverV ? 0xFFFFFFFF : 0xFF888888);

                boolean hoverX = mouseX >= x + width - 20 && mouseX <= x + width && mouseY >= row.y + 1 && mouseY <= row.y + 21;
                graphics.centeredText(this.font, Component.literal("x"), x + width - 10, row.y + 7, hoverX ? 0xFFFFFFFF : 0xFF888888);
            } else if (row.type == RowType.ADDRESS) {
                boolean hoverX = mouseX >= x + width - 20 && mouseX <= x + width && mouseY >= row.y + 1 && mouseY <= row.y + 21;
                graphics.centeredText(this.font, Component.literal("x"), x + width - 10, row.y + 7, hoverX ? 0xFFFFFFFF : 0xFF888888);
            }
        }
    }

    private void extractScrollbar(GuiGraphicsExtractor graphics) {
        if (this.maxScroll <= 0) return;
        int top = this.listTop();
        int bottom = this.listBottom();
        int x = this.width - 8;
        int trackHeight = bottom - top;
        int thumbHeight = Math.max(32, trackHeight * trackHeight / Math.max(trackHeight + this.maxScroll, 1));
        int thumbTop = top + this.scrollOffset * (trackHeight - thumbHeight) / this.maxScroll;
        graphics.fill(x, top, x + 6, bottom, SCROLL_TRACK);
        graphics.fill(x, thumbTop, x + 6, thumbTop + thumbHeight, SCROLL_THUMB);
    }
    *///?} else {
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderChrome(graphics, mouseX, mouseY);
        super.render(graphics, mouseX, mouseY, delta);
    }

    private void renderChrome(GuiGraphics graphics, int mouseX, int mouseY) {
        int listTop = this.listTop();
        int listBottom = this.listBottom();
        graphics.fillGradient(0, listTop, this.width, listBottom, LIST_SHADE, LIST_SHADE);
        graphics.fillGradient(0, 0, this.width, listTop, HEADER_SHADE, HEADER_SHADE);
        graphics.fillGradient(0, listBottom, this.width, this.height, HEADER_SHADE, HEADER_SHADE);
        graphics.fillGradient(0, listTop, this.width, listTop + 4, 0xAA000000, 0x00000000);
        graphics.fillGradient(0, listBottom - 4, this.width, listBottom, 0x00000000, 0xAA000000);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 18, TEXT_COLOR);
        this.renderRows(graphics, mouseX, mouseY);
        this.renderScrollbar(graphics);
    }

    private void renderRows(GuiGraphics graphics, int mouseX, int mouseY) {
        for (RowDraft row : this.visibleRows) {
            int x = this.entryX();
            int width = this.entryWidth();

            if (row.isHovered(mouseX, mouseY, x, width)) {
                graphics.fillGradient(x, row.y, x + width, row.y + row.height(), ROW_HOVER, ROW_HOVER);
            }

            if (row.type == RowType.ADDRESS || row.type == RowType.ADD_ADDRESS) {
                int lineX = x + 10;
                int lineY = row.y;
                int midY = row.y + row.height() / 2;
                if (row.type == RowType.ADDRESS) {
                    graphics.fill(lineX, lineY - 4, lineX + 1, row.y + row.height() + 4, TREE_LINE);
                } else {
                    graphics.fill(lineX, lineY - 4, lineX + 1, midY, TREE_LINE);
                }
                graphics.fill(lineX, midY, lineX + 12, midY + 1, TREE_LINE);
            }

            if (row.type == RowType.GROUP) {
                boolean hoverV = mouseX >= x && mouseX <= x + 20 && mouseY >= row.y + 1 && mouseY <= row.y + 21;
                graphics.drawCenteredString(this.font, row.group.expanded ? "v" : ">", x + 10, row.y + 7, hoverV ? 0xFFFFFFFF : 0xFF888888);

                boolean hoverX = mouseX >= x + width - 20 && mouseX <= x + width && mouseY >= row.y + 1 && mouseY <= row.y + 21;
                graphics.drawCenteredString(this.font, "x", x + width - 10, row.y + 7, hoverX ? 0xFFFFFFFF : 0xFF888888);
            } else if (row.type == RowType.ADDRESS) {
                boolean hoverX = mouseX >= x + width - 20 && mouseX <= x + width && mouseY >= row.y + 1 && mouseY <= row.y + 21;
                graphics.drawCenteredString(this.font, "x", x + width - 10, row.y + 7, hoverX ? 0xFFFFFFFF : 0xFF888888);
            }
        }
    }

    private void renderScrollbar(GuiGraphics graphics) {
        if (this.maxScroll <= 0) return;
        int top = this.listTop();
        int bottom = this.listBottom();
        int x = this.width - 8;
        int trackHeight = bottom - top;
        int thumbHeight = Math.max(32, trackHeight * trackHeight / Math.max(trackHeight + this.maxScroll, 1));
        int thumbTop = top + this.scrollOffset * (trackHeight - thumbHeight) / this.maxScroll;
        graphics.fill(x, top, x + 6, bottom, SCROLL_TRACK);
        graphics.fill(x, thumbTop, x + 6, thumbTop + thumbHeight, SCROLL_THUMB);
    }
    //?}

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.maxScroll <= 0 || mouseY < this.listTop() || mouseY > this.listBottom()) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        this.scrollOffset = Math.max(0, Math.min(this.maxScroll, this.scrollOffset - (int) (scrollY * 20)));
        this.rebuildConfigWidgets();
        return true;
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            //? if >=26
            /*this.minecraft.setScreenAndShow(this.parent);*/
            //? if <26
            this.minecraft.setScreen(this.parent);
        }
    }

    private void rebuildConfigWidgets() {
        this.clearWidgets();
        this.visibleRows.clear();
        this.recalculateScroll();

        int x = this.entryX();
        int width = this.entryWidth();
        int y = this.listTop() + 6 - this.scrollOffset;

        for (GroupDraft group : this.groups) {
            if (this.isVisible(y, ROW_HEIGHT)) {
                this.addRow(new RowDraft(RowType.GROUP, group, null, y));
                
                Button expandBtn = Button.builder(Component.empty(), button -> {
                    group.expanded = !group.expanded;
                    this.rebuildConfigWidgets();
                }).bounds(x, y + 1, 20, 20).build();
                expandBtn.setAlpha(0.0f);
                this.addRenderableWidget(expandBtn);

                EditBox name = new EditBox(this.font, x + 24, y + 6, width - 60, 12, Component.translatable("field.litematicasync.group_name"));
                name.setMaxLength(96);
                name.setValue(group.name);
                name.setResponder(value -> group.name = value);
                name.setTextColor(CATEGORY_COLOR);
                name.setBordered(false);
                this.addRenderableWidget(name);

                Button delGroupBtn = Button.builder(Component.empty(), button -> {
                    this.groups.remove(group);
                    this.rebuildConfigWidgets();
                }).bounds(x + width - 20, y + 1, 20, 20).build();
                delGroupBtn.setAlpha(0.0f);
                this.addRenderableWidget(delGroupBtn);
            }
            y += ROW_HEIGHT;

            if (group.expanded) {
                for (AddressDraft address : group.addresses) {
                    if (this.isVisible(y, ROW_HEIGHT)) {
                        this.addRow(new RowDraft(RowType.ADDRESS, group, address, y));
                        
                        EditBox addressBox = new EditBox(this.font, x + 28, y + 6, width - 60, 12, Component.translatable("field.litematicasync.address"));
                        addressBox.setMaxLength(160);
                        addressBox.setValue(address.value);
                        addressBox.setResponder(value -> address.value = value);
                        addressBox.setBordered(false);
                        this.addRenderableWidget(addressBox);

                        Button delAddrBtn = Button.builder(Component.empty(), button -> {
                            group.addresses.remove(address);
                            this.rebuildConfigWidgets();
                        }).bounds(x + width - 20, y + 1, 20, 20).build();
                        delAddrBtn.setAlpha(0.0f);
                        this.addRenderableWidget(delAddrBtn);
                    }
                    y += ROW_HEIGHT;
                }

                if (this.isVisible(y, ROW_HEIGHT)) {
                    this.addRow(new RowDraft(RowType.ADD_ADDRESS, group, null, y));
                    this.addRenderableWidget(Button.builder(Component.translatable("button.litematicasync.add_address"), button -> {
                        group.addresses.add(new AddressDraft(""));
                        this.rebuildConfigWidgets();
                    }).bounds(x + 24, y + 1, width - 44, 20).build());
                }
                y += ROW_HEIGHT;
            }
        }

        if (this.groups.isEmpty() && this.isVisible(y, ROW_HEIGHT)) {
            this.addRow(new RowDraft(RowType.EMPTY, null, null, y));
        }
        y += 8;

        if (this.isVisible(y, ROW_HEIGHT)) {
            this.addRow(new RowDraft(RowType.ADD_GROUP, null, null, y));
            this.addRenderableWidget(Button.builder(Component.translatable("button.litematicasync.add_group"), button -> {
                GroupDraft group = new GroupDraft("Group " + (this.groups.size() + 1));
                this.groups.add(group);
                this.scrollOffset = this.maxScroll + ROW_HEIGHT;
                this.rebuildConfigWidgets();
            }).bounds(x + width / 2 - 100, y, 200, BUTTON_HEIGHT).build());
        }

        int footerY = this.height - 30;
        int center = this.width / 2;
        int buttonWidths = Math.min(200, (this.width - 50 - 12) / 2);
        
        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> this.onClose())
                .bounds(center - buttonWidths - 3, footerY, buttonWidths, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("text.cloth-config.save_and_done"), button -> this.saveAndClose())
                .bounds(center + 3, footerY, buttonWidths, 20)
                .build());
    }

    private void addRow(RowDraft row) {
        if (this.isVisible(row.y, row.height())) {
            this.visibleRows.add(row);
        }
    }

    private void saveAndClose() {
        List<ServerGroup> savedGroups = new ArrayList<>();
        for (GroupDraft group : this.groups) {
            String groupName = group.name.trim();
            List<String> addresses = new ArrayList<>();
            for (AddressDraft address : group.addresses) {
                String value = address.value.trim();
                if (!value.isEmpty()) addresses.add(value);
            }
            if (!groupName.isEmpty() && !addresses.isEmpty()) {
                savedGroups.add(new ServerGroup(groupName, addresses));
            }
        }
        LitematicaSyncConfig.get().replaceGroups(savedGroups);
        this.onClose();
    }

    private void recalculateScroll() {
        int contentHeight = 12;
        for (GroupDraft group : this.groups) {
            contentHeight += ROW_HEIGHT;
            if (group.expanded) {
                contentHeight += group.addresses.size() * ROW_HEIGHT + ROW_HEIGHT;
            }
        }
        if (this.groups.isEmpty()) contentHeight += ROW_HEIGHT;
        contentHeight += ROW_HEIGHT + 8; // Add group button + margin
        int viewportHeight = this.listBottom() - this.listTop();
        this.maxScroll = Math.max(0, contentHeight - viewportHeight);
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, this.maxScroll));
    }

    private boolean isVisible(int y, int height) {
        return y + height >= this.listTop() && y <= this.listBottom();
    }

    private int listTop() {
        return HEADER_HEIGHT;
    }

    private int listBottom() {
        return this.height - FOOTER_HEIGHT;
    }

    private int entryX() {
        return (this.width - this.entryWidth()) / 2;
    }

    private int entryWidth() {
        return Math.min(ENTRY_WIDTH, Math.max(260, this.width - 80));
    }

    private enum RowType {
        GROUP, ADDRESS, ADD_ADDRESS, ADD_GROUP, EMPTY
    }

    private static class GroupDraft {
        private String name;
        private final List<AddressDraft> addresses = new ArrayList<>();
        private boolean expanded = true;

        private GroupDraft(ServerGroup group) {
            this.name = group.name();
            for (String address : group.addresses()) {
                this.addresses.add(new AddressDraft(address));
            }
        }
        private GroupDraft(String name) {
            this.name = name;
            this.addresses.add(new AddressDraft(""));
        }
    }

    private static class AddressDraft {
        private String value;
        private AddressDraft(String value) { this.value = value; }
    }

    private static class RowDraft {
        private final RowType type;
        private final GroupDraft group;
        private final AddressDraft address;
        private final int y;

        private RowDraft(RowType type, GroupDraft group, AddressDraft address, int y) {
            this.type = type;
            this.group = group;
            this.address = address;
            this.y = y;
        }

        private int height() {
            return ROW_HEIGHT;
        }

        private boolean isHovered(int mouseX, int mouseY, int x, int width) {
            return mouseX >= x && mouseX <= x + width && mouseY >= this.y && mouseY <= this.y + this.height();
        }
    }
}
