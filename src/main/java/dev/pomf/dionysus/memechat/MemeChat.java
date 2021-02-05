package dev.pomf.dionysus.memechat;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
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

    public static final String CHAT_PREFIX = "prefix.";

    public static final Map<String, ChatColor> CHAT_PREFIXES = Collections.unmodifiableMap(
            Arrays.stream(ChatColor.values())
                    .filter(c -> !c.isFormat() && c != ChatColor.RESET)
                    .collect(Collectors.toMap(c -> CHAT_PREFIX + c.name().toLowerCase(), c -> c))
    );

    @Override
    public void onEnable() {
        getConfig().addDefault(WHISPER_ACTIVE, true);
        getConfig().addDefault(WHISPER_MONITORING, true);
        getConfig().addDefault(CHAT_PREFIX + "dark_green", ">");

        getConfig().options().copyDefaults(true);
        saveConfig();

        this.getLogger().log(Level.INFO, "registering events");
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
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
        event.setMessage(msg);
    }
}
