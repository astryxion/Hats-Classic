package com.astryxion.hats.client.gui;

import com.astryxion.hats.client.gui.widget.HatsButton;

import net.minecraft.network.chat.Component;

/**
 * Legacy alias for the 1.16.5 Hats button widget.
 */
public class StyledButton extends HatsButton {

    public StyledButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress);
    }
}
