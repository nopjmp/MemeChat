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
    private final Map<String, BiFunction<PlayerData, String[], Boolean>> commands = ImmutableMap.of(
            "/reply", (senderData, args) -> {
                Player sender = senderData.getPlayer();
                Player rcpt = senderData.getLastReplied();
                if (rcpt != null) {
                    stringBuilder.delete(0, stringBuilder.length());
                    for (String s : args) {
                        stringBuilder.append(s);
                    }
                    String message = stringBuilder.toString();
                    PlayerData rcptData = playerDataMap.computeIfAbsent(rcpt.getUniqueId(), k -> new PlayerData(rcpt));

                    rcpt.sendMessage(sender.getName() + delimiterReceive + message);
                    rcptData.setLastReplied(rcpt.getUniqueId());

                    sender.sendMessage(rcpt.getName() + delimiterSend + message);
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
                    stringBuilder.delete(0, stringBuilder.length());
                    for (int i = 1; i < args.length; i++) {
                        stringBuilder.append(args[i]);
                    }
                    String message = stringBuilder.toString();
                    PlayerData rcptData = playerDataMap.computeIfAbsent(rcpt.getUniqueId(), k -> new PlayerData(rcpt));

                    rcpt.sendMessage(sender.getName() + delimiterReceive + message);
                    rcptData.setLastReplied(rcpt.getUniqueId());

                    sender.sendMessage(rcpt.getName() + delimiterSend + message);
                } else {
                    sender.sendMessage(ChatColor.RED + "Could not find player: " + args[0]);
                }
                return true;
            }
    );

    public WhisperListener(MemeChat plugin) {
        delimiterSend = plugin.getConfig().getString(MemeChat.WHISPER_DELIMITER_SEND, " -> ");
        delimiterReceive = plugin.getConfig().getString(MemeChat.WHISPER_DELIMITER_RECEIVE, " <- ");
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {

    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final String cmd = command.getName().toLowerCase();
        if (commands.containsKey(cmd)) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
                return true;
            }
            Player player = (Player) sender;
            PlayerData playerData = playerDataMap.computeIfAbsent(player.getUniqueId(), k -> new PlayerData(player));
            boolean success = commands.get(cmd).apply(playerData, args);
            if (!success) {
                sender.sendMessage(ChatColor.RED + command.getUsage());
            }
        }
        return true;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // clean up playerDataMap to same on memory usage
        playerDataMap.remove(event.getPlayer().getUniqueId());
    }
}
