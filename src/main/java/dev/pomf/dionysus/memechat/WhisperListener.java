package dev.pomf.dionysus.memechat;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

public final class WhisperListener implements Listener {
    private final StringBuilder stringBuilder = new StringBuilder();
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private String delimiterSend;
    private String delimiterReceive;
    private ChatColor chatColor;
    private final Map<String, String> usageMap = ImmutableMap.of(
            "/reply", "/reply message",
            "/msg", "/msg [player] message"
    );

    private final Map<String, BiFunction<PlayerData, String[], Boolean>> commands = ImmutableMap.of(
            "/reply", (senderData, args) -> {
                Player sender = senderData.getPlayer();
                Player rcpt = senderData.getLastReplied();
                if (rcpt != null) {
                    if (sender.getUniqueId().equals(rcpt.getUniqueId())) {
                        sender.sendMessage(ChatColor.RED + "You cannot send a private message to yourself.");
                    }
                    stringBuilder.delete(0, stringBuilder.length());
                    for (String s : args) {
                        stringBuilder.append(s);
                    }
                    String message = stringBuilder.toString();
                    PlayerData rcptData = playerDataMap.computeIfAbsent(rcpt.getUniqueId(), k -> new PlayerData(rcpt));

                    rcpt.sendMessage(chatColor + sender.getName() + delimiterReceive + message);
                    rcptData.setLastReplied(rcpt.getUniqueId());

                    sender.sendMessage(chatColor + rcpt.getName() + delimiterSend + message);
                } else {
                    sender.sendMessage(ChatColor.RED + "You have not received any messages this session.");
                }
                return true;
            },
            "/msg", (senderData, args) -> {
                if (args.length <= 1) {
                    return false;
                }
                Player sender = senderData.getPlayer();
                Player rcpt = Bukkit.getPlayerExact(args[0]);
                if (rcpt != null) {
                    if (sender.getUniqueId().equals(rcpt.getUniqueId())) {
                        sender.sendMessage(ChatColor.RED + "You cannot send a private message to yourself.");
                    }
                    stringBuilder.delete(0, stringBuilder.length());
                    for (int i = 1; i < args.length; i++) {
                        stringBuilder.append(args[i]);
                    }
                    String message = stringBuilder.toString();
                    PlayerData rcptData = playerDataMap.computeIfAbsent(rcpt.getUniqueId(), k -> new PlayerData(rcpt));

                    rcpt.sendMessage(chatColor + sender.getName() + delimiterReceive + message);
                    rcptData.setLastReplied(rcpt.getUniqueId());

                    sender.sendMessage(chatColor + rcpt.getName() + delimiterSend + message);
                } else {
                    sender.sendMessage(ChatColor.RED + "Could not find player: " + args[0]);
                }
                return true;
            }
    );

    private final Map<String, String> aliasMap = ImmutableMap.of(
            "/r", "/reply",
            "/m", "/msg"
    );

    public WhisperListener(MemeChat plugin) {
        delimiterSend = plugin.getConfig().getString(MemeChat.WHISPER_DELIMITER_SEND, " -> ");
        delimiterReceive = plugin.getConfig().getString(MemeChat.WHISPER_DELIMITER_RECEIVE, " <- ");
        chatColor = ChatColor.valueOf(plugin.getConfig().getString(MemeChat.WHISPER_COLOR, "light_purple").toUpperCase());
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        final Player sender = event.getPlayer();
        String arguments = event.getMessage();
        String command = arguments;
        int spaceIndexOf = command.indexOf(' ');
        if (spaceIndexOf != -1) {
            command = command.substring(0, spaceIndexOf);
            arguments = arguments.substring(spaceIndexOf + 1);
        } else {
            // we don't have any arguments
            arguments = "";
        }

        command = aliasMap.getOrDefault(command, command);
        if (commands.containsKey(command)) {
            event.setCancelled(true);

            PlayerData playerData = playerDataMap.computeIfAbsent(sender.getUniqueId(), k -> new PlayerData(sender));
            boolean success = commands.get(command).apply(playerData, arguments.split(" "));
            if (!success) {
                sender.sendMessage(ChatColor.RED + aliasMap.getOrDefault(command, command + ": unable to find usage information...."));
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // clean up playerDataMap to same on memory usage
        playerDataMap.remove(event.getPlayer().getUniqueId());
    }
}
