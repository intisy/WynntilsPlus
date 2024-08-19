package com.wynntils.features.players;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.mobtotem.MobTotem;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Position;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@ConfigCategory(Category.INVENTORY)
public class AutoWalkFeature extends Feature {
    @RegisterKeyBind
    private final KeyBind autoWalkKeyBind =
            new KeyBind("Auto Walk Towards Mob Totem", GLFW.GLFW_KEY_F9, true, this::action);
    private final Minecraft client;
    private boolean isWalking = false;
    private Vec3 position;

    public AutoWalkFeature() {
        this.client = Minecraft.getInstance();
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;

        if (player != null && isWalking) {
            if (position.distanceTo(player.position()) > 0.5) {
                pointPlayerToCoordinates(position);
                startWalking();
            } else {
                stopWalking();
            }
        }
    }

    private void startWalking() {
        isWalking = true;
        LocalPlayer player = client.player;
        if (player != null) {
            KeyMapping forwardKey = client.options.keyUp;
            forwardKey.setDown(true);
        }
    }

    private void stopWalking() {
        isWalking = false;
        KeyMapping forwardKey = client.options.keyUp;
        forwardKey.setDown(false);
    }

    private double calculateYaw(double px, double py, double pz, double tx, double ty, double tz) {
        double dx = tx - px;
        double dz = tz - pz;
        return Math.toDegrees(Math.atan2(dx, dz)) * -1;
    }

    private double calculatePitch(double px, double py, double pz, double tx, double ty, double tz) {
        double dx = tx - px;
        double dz = tz - pz;
        double dy = ty - py;
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        return Math.toDegrees(Math.atan2(dy, horizontalDistance)) * -1;
    }

    private void pointPlayerToCoordinates(Position targetPos) {
        LocalPlayer player = client.player;
        if (player != null) {
            double yaw = calculateYaw(player.getX(), player.getY(), player.getZ(), targetPos.x(), targetPos.y(), targetPos.z());
            double pitch = calculatePitch(player.getX(), player.getY(), player.getZ(), targetPos.x(), targetPos.y(), targetPos.z());
            player.setYRot((float) yaw);
            player.setXRot((float) pitch);
        }
    }
    public Vec3 getCenter() {
        List<MobTotem> totems = Models.MobTotem.getMobTotems();
        if (totems.isEmpty()) {
            return null;
        }

        double sumX = 0;
        double sumY = 0;
        double sumZ = 0;

        for (MobTotem totem : totems) {
            sumX += totem.getPosition().x();
            sumY += totem.getPosition().y();
            sumZ += totem.getPosition().z();
        }

        double centerX = sumX / totems.size();
        double centerY = sumY / totems.size();
        double centerZ = sumZ / totems.size();

        return new Vec3(centerX, centerY, centerZ);
    }
    public void action() {
        if (!isWalking) {
            position = getCenter();
            if (position != null) {
                pointPlayerToCoordinates(position);
                startWalking();
            }
        } else {
            stopWalking();
        }
    }
}
