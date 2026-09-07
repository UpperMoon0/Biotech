package com.nstut.biotech.views.openui;

import com.nstut.openui.api.UIComponent;
import com.nstut.openui.api.Ui;
import com.nstut.openui.api.UiRender;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/** Live OpenUI components shared by the machine and hatch layouts. */
public final class BiotechWidgets {
    private BiotechWidgets() { }
    public static UIComponent at(UIComponent child, int x, int y, int w, int h) {
        return Ui.positioned(child.width(w).height(h)).left(x).top(y);
    }
    public static UIComponent label(Supplier<String> text) { return Ui.text(() -> Component.literal(text.get())).centered(); }
    public static UIComponent gauge(IntSupplier amount, IntSupplier capacity, boolean vertical, Supplier<String> tip) {
        return new LiveTooltipComponent() {
            @Override public int preferredWidth(Font f) { return vertical ? 12 : 26; }
            @Override public int preferredHeight(Font f) { return vertical ? 76 : 12; }
            @Override protected String tooltipText(int mx, int my) { return tip.get(); }
            @Override public void render(GuiGraphicsExtractor g, Font f, int mx, int my, float pt) {
                UiRender.roundedRect(g, x, y, width, height, 3, 0xFF0D1713);
                int fill = DisplayMath.fill(amount.getAsInt(), capacity.getAsInt(), vertical ? height : width);
                if (fill > 0) UiRender.roundedRect(g, x, vertical ? y + height - fill : y,
                    vertical ? width : fill, vertical ? fill : height, 3, vertical ? 0xFFE8BA58 : 0xFF67CD9A);
            }
        };
    }
    public record Entry(ItemStack stack, String detail) { }
    /** Recipe previews are deliberately not interactive inventory slots. */
    public static UIComponent items(Supplier<List<Entry>> entries, int columns, int spacing, boolean centered) {
        return new LiveTooltipComponent() {
            @Override public int preferredWidth(Font f) { return columns * spacing; }
            @Override public int preferredHeight(Font f) { return 96; }
            @Override protected String tooltipText(int mx, int my) {
                List<Entry> values = entries.get();
                for (int i = 0; i < values.size(); i++) {
                    int cx = cellX(i, values.size()), cy = y + (i / columns) * spacing;
                    if (mx >= cx && mx < cx + 18 && my >= cy && my < cy + 18) {
                        Entry e = values.get(i);
                        return e.stack().getHoverName().getString() + "\n" + e.stack().getCount()
                            + (e.detail().isEmpty() ? "" : " (" + e.detail() + ")");
                    }
                }
                return null;
            }
            private int cellX(int i, int count) {
                return x + (centered ? (width - Math.min(count, columns) * spacing) / 2 : 0) + (i % columns) * spacing;
            }
            @Override public void render(GuiGraphicsExtractor g, Font f, int mx, int my, float pt) {
                List<Entry> values = entries.get();
                for (int i = 0; i < values.size(); i++) {
                    Entry e = values.get(i);
                    int cx = cellX(i, values.size()), cy = y + (i / columns) * spacing;
                    UiRender.slot(g, cx, cy, 18, 18);
                    g.item(e.stack(), cx + 1, cy + 1);
                    g.itemDecorations(f, e.stack(), cx + 1, cy + 1);
                    if (columns == 1) UiRender.text(g, f, e.detail(), cx + 22, cy + 5, 0xFFD4E9DD);
                    else if (spacing >= 28) UiRender.text(g, f, e.detail(), cx, cy + 20, 0xFFD4E9DD);
                }
            }
        };
    }
}
