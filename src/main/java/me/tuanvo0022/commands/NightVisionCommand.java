package me.tuanvo0022.commands;

import me.tuanvo0022.Main;
import me.tuanvo0022.utils.MessageUtil;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

public class NightVisionCommand extends Command {
    private final Main plugin;
    
    public NightVisionCommand(Main plugin) {
        super("nightvision");
        this.plugin = plugin;
        this.setAliases(Arrays.asList("nv"));
    }
    
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, "error_player_only");
            return true;
        }
        
        if (!player.hasPermission("teacore.nightvision")) {
            MessageUtil.sendMessage(player, "error_permission");
            return true;
        }
        
        if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            MessageUtil.sendMessage(player, "nightvision_disabled");
        } else {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false));
            MessageUtil.sendMessage(player, "nightvision_enabled");
        }

        return true;
    }
}
