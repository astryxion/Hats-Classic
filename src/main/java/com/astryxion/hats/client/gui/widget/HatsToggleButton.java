package com.astryxion.hats.client.gui.widget;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * 1.16.5-style boolean option. The value is represented by its ON/OFF label;
 * button chrome remains consistent so ON does not look disabled.
 */
public class HatsToggleButton extends HatsButton {

    private final Component name;
    private boolean toggled;
    private final Consumer<Boolean> onChange;

    public HatsToggleButton(int x, int y, int width, int height, Component name, boolean value, String tooltipKey, Consumer<Boolean> onChange) {
        super(x, y, width, height, label(name, value), button -> {});
        this.name = name;
        this.toggled = value;
        this.onChange = onChange;
        if (tooltipKey != null) {
            setTooltip(Tooltip.create(Component.translatable(tooltipKey)));
        }
    }

    @Override
    public void onPress() {
        toggled = !toggled;
        setMessage(label(name, toggled));
        onChange.accept(toggled);
    }

    private static Component label(Component name, boolean value) {
        return CommonComponents.optionNameValue(name, value ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }
}
