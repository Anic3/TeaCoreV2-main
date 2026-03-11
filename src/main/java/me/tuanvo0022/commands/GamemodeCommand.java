package me.tuanvo0022.commands;

import me.tuanvo0022.Main;
import me.tuanvo0022.utils.MessageUtil;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GamemodeCommand extends Command {
    private final Main plugin;
    // If fixedMode is null, the command is /gm and reads mode from args[0].
    // Otherwise the command is a shortcut (gms/gmc/gmsp/gma) with a fixed mode.
    private final String fixedMode;

    private static final List<String> MODES = Arrays.asList(
        "survival", "creative", "spectator", "adventure", "0", "1", "2", "3"
    );

    public GamemodeCommand(Main plugin, String name, String fixedMode) {
        super(name);
        this.plugin = plugin;
        this.fixedMode = fixedMode;

        if (fixedMode == null) {
            setDescription("Thay đổi gamemode của bạn hoặc người chơi khác.");
            setUsage("/<command> <survival|creative|spectator|adventure|0|1|2|3> [player]");
        } else {
            setDescription("Chuyển sang gamemode " + fixedMode + ".");
            setUsage("/<command> [player]");
        }
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player staff)) {
            MessageUtil.sendMessage(sender, "error_player_only");
            return true;
        }

        if (!staff.hasPermission("teacore.gamemode")) {
            MessageUtil.sendMessage(staff, "error_permission");
            return true;
        }

        String modeString;
        String[] remainingArgs;

        if (fixedMode != null) {
            modeString = fixedMode;
            remainingArgs = args;
        } else {
            if (args.length < 1) {
                MessageUtil.sendMessage(staff, "gamemode_command");
                return true;
            }
            modeString = args[0].toLowerCase();
            remainingArgs = Arrays.copyOfRange(args, 1, args.length);
        }

        GameMode gameMode = parseGameMode(modeString);
        if (gameMode == null) {
            MessageUtil.sendMessage(staff, "gamemode_invalid");
            return true;
        }

        if (remainingArgs.length >= 1) {
            if (!staff.hasPermission("teacore.gamemode.other")) {
                MessageUtil.sendMessage(staff, "error_permission");
                return true;
            }

            Player target = plugin.getServer().getPlayerExact(remainingArgs[0]);
            if (target == null) {
                MessageUtil.sendMessage(staff, "error_player_not_found");
                return true;
            }

            GameMode finalGameMode = gameMode;
            Main.getScheduler().runAtEntity(target, task -> {
                target.setGameMode(finalGameMode);
                MessageUtil.sendMessage(staff, "gamemode_set_other",
                    "%target%", target.getName(),
                    "%mode%", getModeName(finalGameMode));
                MessageUtil.sendMessage(target, "gamemode_received",
                    "%mode%", getModeName(finalGameMode),
                    "%staff%", staff.getName());
            });
        } else {
            GameMode finalGameMode = gameMode;
            Main.getScheduler().runAtEntity(staff, task -> {
                staff.setGameMode(finalGameMode);
                MessageUtil.sendMessage(staff, "gamemode_set_self",
                    "%mode%", getModeName(finalGameMode));
            });
        }

        return true;
    }

    private GameMode parseGameMode(String input) {
        return switch (input.toLowerCase()) {
            case "survival", "s", "0" -> GameMode.SURVIVAL;
            case "creative", "c", "1" -> GameMode.CREATIVE;
            case "spectator", "sp", "2" -> GameMode.SPECTATOR;
            case "adventure", "a", "3" -> GameMode.ADVENTURE;
            default -> null;
        };
    }

    private String getModeName(GameMode mode) {
        return switch (mode) {
            case SURVIVAL -> "Survival";
            case CREATIVE -> "Creative";
            case SPECTATOR -> "Spectator";
            case ADVENTURE -> "Adventure";
        };
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (fixedMode == null) {
            if (args.length == 1) {
                String partial = args[0].toLowerCase();
                for (String mode : MODES) {
                    if (mode.startsWith(partial)) suggestions.add(mode);
                }
                return suggestions;
            }
            if (args.length == 2 && sender.hasPermission("teacore.gamemode.other")) {
                String partial = args[1].toLowerCase();
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(partial)) suggestions.add(p.getName());
                }
                return suggestions;
            }
        } else {
            if (args.length == 1 && sender.hasPermission("teacore.gamemode.other")) {
                String partial = args[0].toLowerCase();
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(partial)) suggestions.add(p.getName());
                }
                return suggestions;
            }
        }

        return suggestions;
    }
}
