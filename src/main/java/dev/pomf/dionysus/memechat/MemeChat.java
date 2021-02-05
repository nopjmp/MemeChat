package dev.pomf.dionysus.memechat;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class MemeChat extends JavaPlugin implements Listener {
    public static final String WHISPER_ACTIVE = "whispers.active";
    public static final String WHISPER_MONITORING = "whispers.monitoring";

    public static final String CHAT_PREFIX = "prefix.";

    public static final String[] CHAT_PREFIXES = new String[] { "green" };

    @Override
    public void onEnable() {
        getConfig().addDefault(WHISPER_ACTIVE, true);
        getConfig().addDefault(WHISPER_MONITORING, true);
        getConfig().addDefault(CHAT_PREFIX + "green", ">");

        getConfig().options().copyDefaults(true);
        saveConfig();

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public String colorizeChatMessage(String msg) {
        for (String prefixColor : CHAT_PREFIXES) {
            String prefix = getConfig().getString(CHAT_PREFIX + prefixColor);
            if (!prefix.isEmpty() && msg.startsWith(prefix)) {
                ChatColor color = ChatColor.valueOf(prefixColor.toUpperCase());
                return color + msg;
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

    private void readConfig() {
        FileConfiguration config = this.getConfig();

        saveDefaultConfig();
        saveConfig();
    }
}
