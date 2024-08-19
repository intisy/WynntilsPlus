package com.wynntils.features.custom;

import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.features.combat.QuickCastFeature;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

@ConfigCategory(Category.INVENTORY)
public class AutoCastFeature extends Feature {
    @RegisterKeyBind
    public final KeyBind autoCastKeyBind =
            new KeyBind("Auto Cast The First Spell", GLFW.GLFW_KEY_F8, true, this::action);
    private boolean isActive = false;
    private static final int delayInTicks = 20;
    private int current;
    @SubscribeEvent
    public void onTick(TickEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && isActive) {
            current--;
            if (current == 0) {
                current = delayInTicks;
                Optional<Feature> feature = Managers.Feature.getFeatureFromString("QuickCast");
                feature.ifPresent(value -> ((QuickCastFeature) (value)).castFirstSpell());
            }
        }
    }
    public void action() {
        if (!isActive) {
            McUtils.sendMessageToClient(Component.literal("Enabled auto cast"));
            isActive = true;
            current = delayInTicks;
        } else {
            McUtils.sendMessageToClient(Component.literal("Disabled auto cast"));
            isActive = false;
        }
    }
}
