package dev.example.donutsmptesttools;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

public final class DonutTestToolsClient implements ClientModInitializer {
    private static final long CLICK_WINDOW_MS = 1_000L;

    private KeyMapping toggleLoggingKey;
    private boolean loggingEnabled = true;
    private boolean previousLeftDown;
    private boolean previousRightDown;
    private int tickCounter;
    private final Deque<Long> clickTimes = new ArrayDeque<>();
    private Path logFile;

    @Override
    public void onInitializeClient() {
        toggleLoggingKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.donutsmp_testtools.toggle_logging",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                KeyMapping.Category.MISC
        ));
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onClientTick(Minecraft client) {
        if (client.player == null || client.level == null || client.getWindow() == null) {
            return;
        }

        while (toggleLoggingKey.consumeClick()) {
            loggingEnabled = !loggingEnabled;
            client.player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal(
                            "§6[DonutSMP TestTools] §fLogging "
                                    + (loggingEnabled ? "§aON" : "§cOFF")
                    ),
                    false
            );
        }

        long now = System.currentTimeMillis();
        long window = client.getWindow().handle();
        boolean leftDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean rightDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

        if ((leftDown && !previousLeftDown) || (rightDown && !previousRightDown)) {
            clickTimes.addLast(now);
        }
        previousLeftDown = leftDown;
        previousRightDown = rightDown;

        while (!clickTimes.isEmpty() && now - clickTimes.peekFirst() > CLICK_WINDOW_MS) {
            clickTimes.removeFirst();
        }

        tickCounter++;
        if (loggingEnabled && tickCounter % 20 == 0) {
            writeSnapshot(client, now);
        }
    }

    private void writeSnapshot(Minecraft client, long now) {
        double horizontalSpeed = client.player.getDeltaMovement().horizontalDistance() * 20.0;
        double targetDistance = -1.0;

        HitResult hit = client.hitResult;
        if (hit instanceof EntityHitResult entityHit) {
            targetDistance = client.player.distanceTo(entityHit.getEntity());
        }

        String line = String.format(
                Locale.ROOT,
                "%s,%.3f,%d,%.3f%n",
                Instant.ofEpochMilli(now),
                horizontalSpeed,
                clickTimes.size(),
                targetDistance
        );

        try {
            if (logFile == null) {
                Path directory = client.gameDirectory.toPath()
                        .resolve("logs")
                        .resolve("donutsmp-testtools");
                Files.createDirectories(directory);
                logFile = directory.resolve("metrics.csv");
                if (Files.notExists(logFile)) {
                    Files.writeString(
                            logFile,
                            "timestamp,horizontal_speed_blocks_per_second,local_clicks_per_second,target_distance_blocks\n",
                            StandardOpenOption.CREATE,
                            StandardOpenOption.WRITE
                    );
                }
            }
            Files.writeString(logFile, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException exception) {
            client.player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal(
                            "§c[DonutSMP TestTools] Log write failed: " + exception.getMessage()
                    ),
                    false
            );
            loggingEnabled = false;
        }
    }
}
