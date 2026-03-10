package me.tuanvo0022.papi;

import me.tuanvo0022.Main;
import me.tuanvo0022.utils.SmallCapsUtil;
import me.tuanvo0022.database.DatabaseManager.ToggleType;
import me.tuanvo0022.discord.link.LinkedAccount;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.serbob.donutworth.api.util.WorthUtil;
import com.zeltuv.teams.api.ZelTeamsAPI;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class TeaCoreExpansion extends PlaceholderExpansion {

    private final Main plugin;

    public TeaCoreExpansion(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "teacore";
    }

    @Override
    public String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String placeholder) {
        if (placeholder == null) return null;
        placeholder = placeholder.toLowerCase();

        switch (placeholder) {

            case "hidetoggle": {
                Boolean v = plugin.getDatabaseManager()
                        .getCachedToggle(offlinePlayer.getUniqueId(), ToggleType.HIDE);
                boolean toggle = v != null ? v : ToggleType.HIDE.defaultValue;
                return toggle ? "ON" : "OFF";
            }

            case "worthtoggle": {
                if (offlinePlayer.isOnline()) {
                    Player player = offlinePlayer.getPlayer();
                    if (player != null) {
                        boolean toggle = WorthUtil.hasWorthLoreEnabled(player);
                        return toggle ? "ON" : "OFF";
                    }
                }
                return "OFF";
            }

            case "msgtoggle": {
                Boolean v = plugin.getDatabaseManager()
                        .getCachedToggle(offlinePlayer.getUniqueId(), ToggleType.MESSAGE);
                boolean toggle = v != null ? v : ToggleType.MESSAGE.defaultValue;
                return toggle ? "ON" : "OFF";
            }

            case "nightvisiontoggle": {
                if (offlinePlayer.isOnline()) {
                    Player player = offlinePlayer.getPlayer();
                    if (player != null && player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                        return "ON";
                    }
                }
                return "OFF";
            }

            case "paytoggle": {
                Boolean v = plugin.getDatabaseManager()
                        .getCachedToggle(offlinePlayer.getUniqueId(), ToggleType.PAY);
                boolean toggle = v != null ? v : ToggleType.PAY.defaultValue;
                return toggle ? "ON" : "OFF";
            }

            case "phantomtoggle": {
                Boolean v = plugin.getDatabaseManager()
                        .getCachedToggle(offlinePlayer.getUniqueId(), ToggleType.PHANTOM);
                boolean toggle = v != null ? v : ToggleType.PHANTOM.defaultValue;
                return toggle ? "ON" : "OFF";
            }

            case "mobspawntoggle": {
                Boolean v = plugin.getDatabaseManager()
                        .getCachedToggle(offlinePlayer.getUniqueId(), ToggleType.MOBSPAWN);
                boolean toggle = v != null ? v : ToggleType.MOBSPAWN.defaultValue;
                return toggle ? "ON" : "OFF";
            }

            case "teamchattoggle": {
                boolean toggle = ZelTeamsAPI.getInstance()
                        .getTeamManager()
                        .getTeamChatToggle()
                        .contains(offlinePlayer);
                return toggle ? "ON" : "OFF";
            }

            case "chattoggle": {
                Boolean v = plugin.getDatabaseManager()
                        .getCachedToggle(offlinePlayer.getUniqueId(), ToggleType.CHAT);
                boolean toggle = v != null ? v : ToggleType.CHAT.defaultValue;
                return toggle ? "ON" : "OFF";
            }

            case "playername": {
                return SmallCapsUtil.convert(offlinePlayer.getName());
            }

            case "discord_id": {
                LinkedAccount acc = plugin.getDatabaseManager()
                        .getCachedAccount(offlinePlayer.getUniqueId());

                return acc != null
                        ? String.valueOf(acc.getDiscordId())
                        : "N/A";
            }

            case "discord_linked": {
                return plugin.getDatabaseManager()
                        .getCachedAccount(offlinePlayer.getUniqueId()) != null
                        ? "YES"
                        : "NO";
            }

            default:
                return null;
        }
    }
}
