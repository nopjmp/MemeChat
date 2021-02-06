package dev.pomf.dionysus.memechat;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class MemeChat extends JavaPlugin implements Listener {
    public static final String WHISPER_ACTIVE = "whispers.active";
    public static final String WHISPER_MONITORING = "whispers.monitoring";
    public static final String WHISPER_COLOR = "whispers.color";
    public static final String WHISPER_DELIMITER_SEND = "whispers.delimiter.send";
    public static final String WHISPER_DELIMITER_RECEIVE = "whispers.separator.receive";

    public static final String COLORIZE_NAMES = "colorize-names";

    public static final String CHAT_PREFIX = "prefix.";

    public static final Map<String, ChatColor> CHAT_PREFIXES = Collections.unmodifiableMap(
            Arrays.stream(ChatColor.values())
                    .filter(c -> !c.isFormat() && c != ChatColor.RESET)
                    .collect(Collectors.toMap(c -> CHAT_PREFIX + c.name().toLowerCase(), c -> c))
    );

    private final WhisperListener whisperListener = new WhisperListener(this);

    @Override
    public void onEnable() {
        getConfig().addDefault(WHISPER_ACTIVE, true);
        getConfig().addDefault(WHISPER_MONITORING, true);
        getConfig().addDefault(WHISPER_COLOR, "light_purple");
        getConfig().addDefault(WHISPER_DELIMITER_SEND, " -> ");
        getConfig().addDefault(WHISPER_DELIMITER_RECEIVE, " <- ");
        getConfig().addDefault(COLORIZE_NAMES, true);
        getConfig().addDefault(CHAT_PREFIX + "dark_green", ">");

        getConfig().options().copyDefaults(true);
        saveConfig();

        this.getLogger().log(Level.INFO, "registering events");
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(whisperListener, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    // XorShift to shuffle the UUID around to get a color
    public String colorizeNameFormat(Player player) {
        long x = player.getUniqueId().getLeastSignificantBits();
        x ^= x << 13;
        x ^= x >> 7;
        x ^= x << 17;

        x ^= player.getUniqueId().getMostSignificantBits();
        x ^= x << 13;
        x ^= x >> 7;
        x ^= x << 17;

        ChatColor playerColor = ChatColor.values()[(int) (x % 16)];

        // private String format = "<%1$s> %2$s";
        return "<" + playerColor + "%1$s" + ChatColor.RESET + "> %2$s";
    }

    public String colorizeChatMessage(String msg) {
        for (Map.Entry<String, ChatColor> prefixColor : CHAT_PREFIXES.entrySet()) {
            String prefix = getConfig().getString(prefixColor.getKey(), "");
            if (!prefix.isEmpty() && msg.startsWith(prefix)) {
                return prefixColor.getValue() + msg;
            }
        }

        return msg;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled())
            return;

        String msg = colorizeChatMessage(event.getMessage());
        if (getConfig().getBoolean(COLORIZE_NAMES, false)) {
            event.setFormat(colorizeNameFormat(event.getPlayer()));
        }
        event.setMessage(msg);
    }
}
