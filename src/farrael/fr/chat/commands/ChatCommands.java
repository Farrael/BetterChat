package farrael.fr.chat.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import farrael.fr.chat.Chat;
import farrael.fr.chat.managers.ConfigManager;
import farrael.fr.chat.managers.FileManager.FileType;
import farrael.fr.chat.storage.Configuration;

public class ChatCommands implements CommandExecutor {
	Chat plugin = Chat.instance;

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if((sender instanceof Player) && 
				!plugin.hasPermission((Player)sender, "chat.admin"))
			return plugin.sendPluginMessage(sender, ConfigManager.permission, true);

		if(args.length < 1)
			return plugin.sendPluginMessage(sender, "Tapez /chat help pour les informations sur les commandes.", false);

		String arg = args[0].toString();
		if(arg.equalsIgnoreCase("help")){			
			sender.sendMessage(ChatColor.BLUE + "Chat help [1/1]");
			sender.sendMessage(ChatColor.BLUE + "/chat reload : Reload la configuration.");
			sender.sendMessage(ChatColor.BLUE + "/chat on/off : Active ou désactive le plugin.");

		} else if(arg.equalsIgnoreCase("reload")){			
			plugin.fileManager.getFile(FileType.CONFIG).update();
			plugin.configManager.reload(FileType.CONFIG, true);
			plugin.sendPluginMessage(sender, "Config reloaded.", false);

		} else if(arg.equalsIgnoreCase("on") || arg.equalsIgnoreCase("off")){
			Boolean bol = false;
			if(arg.equalsIgnoreCase("on"))
				bol = true;

			Configuration.ENABLE = bol;
			plugin.fileManager.setData(FileType.CONFIG, "enable", bol);
			plugin.sendPluginMessage(sender, "Vous venez " + (bol ? "d'activer" : "de désactiver") + " le plugin.", false);
		
		} else {
			sender.sendMessage(ChatColor.GOLD + "•---• " + ChatColor.BLUE + plugin.getName() + ChatColor.GOLD + " •---•");
			sender.sendMessage("Description : " + plugin.getDescription().getDescription());
			sender.sendMessage("Version : " + plugin.getDescription().getVersion());
			sender.sendMessage("=================");
			sender.sendMessage("Devlopped by Farrael");
		}

		return true;
	}

}
