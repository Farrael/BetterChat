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
import farrael.fr.chat.utils.StringArray;

public class ChatCommands implements CommandExecutor {
	Chat plugin = Chat.getInstance();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if((sender instanceof Player) && 
				!plugin.hasPermission((Player)sender, "chat.admin"))
			return plugin.sendPluginMessage(sender, ConfigManager.permission, true);

		if(args.length < 1) {
			StringBuilder test = new StringBuilder();
			test.append(ChatColor.GOLD + "# " + ChatColor.AQUA + "Description : `" + ChatColor.GREEN + plugin.getDescription().getDescription() + "\n");
			test.append(ChatColor.GOLD + "# " + ChatColor.AQUA + "Version : `" + ChatColor.GREEN + plugin.getDescription().getVersion() + "\n");
			test.append(ChatColor.GOLD + "# \n");
			test.append(ChatColor.GOLD + "# " + ChatColor.AQUA + "Contributor : `"  + ChatColor.GREEN + "Farrael");

			StringArray array = new StringArray(test.toString()).setTabs(new int[]{17});
			sender.sendMessage(ChatColor.GOLD + "\n#--------[" + ChatColor.BLUE + plugin.getName() + ChatColor.GOLD + "]--------#");
			sender.sendMessage(array.getPage(0));
			sender.sendMessage(ChatColor.GOLD + "#--------------------------#\n");

			return true;
		}

		String arg = args[0].toString();
		if(arg.equalsIgnoreCase("help")){			
			sender.sendMessage(ChatColor.GOLD + "\n#----- " + ChatColor.GREEN + "Aide [1/1]" + ChatColor.GOLD + " -----#");
			sender.sendMessage(plugin.getUsage("/chat reload : Reload la configuration."));
			sender.sendMessage(plugin.getUsage("/chat [on/off] : Active ou désactive le plugin."));

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
			plugin.sendPluginMessage(sender, "Tapez /chat help pour les informations sur les commandes.", false);
		}

		return true;
	}

}
