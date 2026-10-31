package com.astryxion.hats.client.gui.widget;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 1.16.5 {@code ElementButton} used as a value cycle, matching Hats config rows.
 */
public class HatsCycleButton<T> extends HatsButton {

    private final Component name;
    private final List<T> values;
    private final Function<T, Component> stringifier;
    private final Consumer<T> onChange;
    private int index;

    public HatsCycleButton(int x, int y, int width, int height, Component name, List<T> values, T initial, Function<T, Component> stringifier, String tooltipKey, Consumer<T> onChange) {
        super(x, y, width, height, Component.empty(), button -> {});
        this.name = name;
        this.values = List.copyOf(values);
        this.stringifier = stringifier;
        this.onChange = onChange;
        this.index = Math.max(0, this.values.indexOf(initial));
        setMessage(label());
        if (tooltipKey != null) {
            setTooltip(Tooltip.create(Component.translatable(tooltipKey)));
        }
    }

    @Override
    public void onPress() {
        if (values.isEmpty()) {
            return;
        }
        index = (index + 1) % values.size();
        setMessage(label());
        onChange.accept(values.get(index));
    }

    private Component label() {
        return CommonComponents.optionNameValue(name, stringifier.apply(values.get(index)));
    }
}
