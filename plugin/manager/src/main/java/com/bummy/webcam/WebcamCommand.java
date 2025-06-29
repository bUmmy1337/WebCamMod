package com.bummy.webcam;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.List;

public class WebcamCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("webcam.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                WebcamManager.getInstance().reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "WebcamManager configuration reloaded!");
                sender.sendMessage(ChatColor.GRAY + "Debug mode: " + 
                    (WebcamManager.getInstance().getConfig().getBoolean("debug", false) ? "ENABLED" : "DISABLED"));
                sender.sendMessage(ChatColor.GRAY + "Max distance: " + 
                    WebcamManager.getInstance().getMaxDistance() + " blocks");
                break;
                
            case "status":
                sender.sendMessage(ChatColor.GOLD + "=== WebcamManager Status ===");
                sender.sendMessage(ChatColor.GRAY + "Debug mode: " + 
                    (WebcamManager.getInstance().isDebugMode() ? ChatColor.RED + "ENABLED" : ChatColor.GREEN + "DISABLED"));
                sender.sendMessage(ChatColor.GRAY + "Max distance: " + 
                    ChatColor.WHITE + WebcamManager.getInstance().getMaxDistance() + " blocks");
                sender.sendMessage(ChatColor.GRAY + "Max frame size: " + 
                    ChatColor.WHITE + WebcamManager.getInstance().getMaxFrameSize() + " bytes");
                sender.sendMessage(ChatColor.GRAY + "Log interval: " + 
                    ChatColor.WHITE + WebcamManager.getInstance().getLogInterval() + " ms");
                break;
                
            case "debug":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /webcam debug <on|off>");
                    return true;
                }
                
                boolean enable = args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("true");
                WebcamManager.getInstance().getConfig().set("debug", enable);
                WebcamManager.getInstance().saveConfig();
                
                sender.sendMessage(ChatColor.GREEN + "Debug mode " + 
                    (enable ? "enabled" : "disabled") + "!");
                sender.sendMessage(ChatColor.YELLOW + "Restart the server for changes to take effect.");
                break;
                
            default:
                sendHelp(sender);
                break;
        }
        
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== WebcamManager Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/webcam status" + ChatColor.GRAY + " - Show plugin status");
        sender.sendMessage(ChatColor.YELLOW + "/webcam reload" + ChatColor.GRAY + " - Reload configuration");
        sender.sendMessage(ChatColor.YELLOW + "/webcam debug <on|off>" + ChatColor.GRAY + " - Toggle debug mode");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("status", "reload", "debug");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("debug")) {
            return Arrays.asList("on", "off");
        }
        return null;
    }
}