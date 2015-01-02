package farrael.fr.chat.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import farrael.fr.chat.Chat;
import farrael.fr.chat.storage.Configuration;
import farrael.fr.chat.utils.StringHelper;

public class WhisperCommands implements CommandExecutor {
	Chat plugin = Chat.instance;

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player))
			return plugin.sendPluginMessage(sender, "Only Players in game command supported.", true);

		Player player = (Player)sender;
		if(args.length < 2)
			return plugin.getUsage(player, "/w [name] [message]");

		Player target 	= Bukkit.getPlayerExact(args[0].toString());
		String message 	= args[1].toString();
		if(target == null)
			return plugin.sendPluginMessage(sender, "Le joueur " + args[0] + " n'est pas connecté.", true);

		StringHelper.createJsonMessage(Configuration.WHISP_TO_FORMAT, message, target).replaceInText("%target%", StringHelper.getDisplayName(target, true)).send(player);
		StringHelper.createJsonMessage(Configuration.WHISP_FROM_FORMAT, message, player).replaceInText("%target%", StringHelper.getDisplayName(player, true)).send(target);
		return false;
	}

}
