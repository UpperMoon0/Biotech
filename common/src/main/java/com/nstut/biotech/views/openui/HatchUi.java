package com.nstut.biotech.views.openui;

import com.nstut.openui.api.UIComponent;
import com.nstut.openui.api.Ui;
import net.minecraft.network.chat.Component;
import java.util.function.IntSupplier;
import static com.nstut.biotech.views.openui.BiotechWidgets.*;

/** Production hatch layouts, also used by the isolated PNG preview runner. */
public final class HatchUi {
    private HatchUi() { }

    public static UIComponent items(Component title) {
        return Ui.stack(at(text(title::getString, BiotechStyle.TEXT), 1, -23, 174, 12),
                at(caption("storage"), 8, 5, 150, 10),
                at(Ui.text(Component.translatable("container.inventory")), 8, 73, 160, 10));
    }

    public static UIComponent fluid(Component title, UIComponent tank) {
        UIComponent root = items(title);
        root.addChild(at(tank, 62, 17, 16, 52));
        return root;
    }

    public static UIComponent energy(Component title, IntSupplier stored, IntSupplier capacity) {
        UIComponent root = items(title);
        root.addChild(at(gauge(stored, capacity, true,
                () -> stored.getAsInt() + " / " + capacity.getAsInt() + " FE"), 80, 17, 16, 52));
        return root;
    }
}
