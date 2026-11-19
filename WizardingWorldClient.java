package com.example.wizardingworld.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class WizardingWorldClient implements ClientModInitializer {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("wizardingworld", "spellbook"));
    private static KeyMapping spellbook;

    @Override public void onInitializeClient() {
        spellbook = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.wizardingworld.spellbook", InputConstants.Type.KEYBOARD, InputConstants.KEY_Z, CATEGORY));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (spellbook.consumeClick()) client.gui.setScreen(new SpellbookScreen());
        });
    }
}
