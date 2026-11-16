package pl.autokopanie;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;

import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoKopanie implements ClientModInitializer {

    // Wyłapuje "/kopanie KOD" z wiadomości na czacie. Jeśli Twój serwer pisze inaczej, zmień regex.
    private static final Pattern PATTERN = Pattern.compile("/kopanie\\s+(\\S+)", Pattern.CASE_INSENSITIVE);

    private String pendingCode = null;
    private int ticksLeft = 0;

    @Override
    public void onInitializeClient() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) return;
            Matcher m = PATTERN.matcher(message.getString());
            if (m.find()) {
                pendingCode = m.group(1).replaceAll("[\\p{Punct}&&[^_-]]+$", ""); // utnij kropkę/przecinek na końcu
                // losowe opóźnienie 0.4 - 1.2 s (8-24 ticki), żeby wyglądało naturalnie
                ticksLeft = ThreadLocalRandom.current().nextInt(8, 25);
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (pendingCode == null) return;
            if (--ticksLeft > 0) return;
            if (client.player != null && client.getNetworkHandler() != null) {
                client.getNetworkHandler().sendChatCommand("kopanie " + pendingCode);
            }
            pendingCode = null;
        });
    }
}
