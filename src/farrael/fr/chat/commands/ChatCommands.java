package farrael.fr.chat.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import farrael.fr.chat.Chat;
import farrael.fr.chat.managers.ConfigManager;
import farrael.fr.chat.managers.FileManager.FileType;

public class ChatCommands implements CommandExecutor {
	Chat plugin;
	public ChatCommands(Chat scrap){
		plugin = scrap;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if(!(sender instanceof Player)) {
			plugin.logger.info("Only Players in game command supported.");
			return true;
		}

		Player player = (Player)sender;
		if(!plugin.hasPermission(player, "chat.admin")){
			player.sendMessage(ConfigManager.permission);
			return true;
		}

		if(args.length < 1){
			player.sendMessage(ChatColor.BLUE + "Tapez /chat help pour les informations sur les commandes.");
			return true;
		}

		String arg = args[0].toString();

		if(arg.equalsIgnoreCase("help")){			
			player.sendMessage(ChatColor.BLUE + "Help [1/1]");
			player.sendMessage(ChatColor.BLUE + "/chat reload : Reload la configuration.");
			player.sendMessage(ChatColor.BLUE + "/chat on/off : Active ou désactive le plugin.");
			return false;
		}

		if(arg.equalsIgnoreCase("reload")){			
			plugin.fileManager.getFile(FileType.CONFIG).update();
			plugin.configManager.reload(FileType.CONFIG, true);
			player.sendMessage(ChatColor.BLUE + "Config reloaded.");
			return false;
		}

		if(arg.equalsIgnoreCase("on") || arg.equalsIgnoreCase("off")){
			Boolean bol = false;
			if(arg.equalsIgnoreCase("on")){
				bol = true;
			}

			plugin.enable = bol;
			plugin.fileManager.setData(FileType.CONFIG, "enable", bol);
			player.sendMessage(ChatColor.BLUE + "[" + ChatColor.YELLOW + plugin.getName() + ChatColor.BLUE + "] Vous venez " + (bol ? "d'activer" : "de désactiver") + " le plugin.");
			return false;
		}

		return false;
	}

}
