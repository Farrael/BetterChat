package farrael.fr.chat.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import farrael.fr.chat.Chat;

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

		target.sendMessage("[From " + player.getDisplayName() + "] " + message);
		player.sendMessage("[To " + target.getDisplayName() + "] " + message);
		return false;
	}

}
