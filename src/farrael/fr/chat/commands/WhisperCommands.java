package farrael.fr.chat.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import farrael.fr.chat.Chat;
import farrael.fr.chat.classes.JsonMessage;
import farrael.fr.chat.configuration.ConfigManager;
import farrael.fr.chat.configuration.Configuration;
import farrael.fr.chat.utils.StringHelper;

public class WhisperCommands implements CommandExecutor {
	Chat plugin = Chat.getInstance();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player))
			return plugin.sendPluginMessage(sender, "Only Players in game command supported.", true);

		Player player = (Player)sender;
		Player target;
		String message;

		if(!plugin.hasPermission(player, "chat.whisp"))
			return plugin.sendPluginMessage(sender, ConfigManager.permission, true);

		if(cmd.getLabel().equalsIgnoreCase("w")) {
			if(args.length < 2) {
				player.sendMessage(ChatColor.RED + "Il manque des arguments...\n" + plugin.getUsage("/w [name] [message]"));
				return false;
			}

			target 	= Bukkit.getPlayerExact(args[0].toString());
			message = plugin.getArguments(args, 1);
		} else {
			if(args.length < 1) {
				player.sendMessage(ChatColor.RED + "Il manque des arguments...\n" + plugin.getUsage("/r [message]"));
				return false;
			}

			if(!plugin.whisper.containsKey(player.getUniqueId())){
				player.sendMessage(ChatColor.RED + "Vous n'avez personne à qui répondre.");
				return false;
			}

			target 	= Bukkit.getPlayer(plugin.whisper.get(player.getUniqueId()));
			message = plugin.getArguments(args, 0);
		}

		if(target == null || !target.isOnline())
			return plugin.sendPluginMessage(sender, "Le joueur " + args[0] + " n'est pas connecté.", true);

		plugin.whisper.put(target.getUniqueId(), player.getUniqueId());
		JsonMessage json;

		json = StringHelper.createJsonMessage(Configuration.WHISP_TO_FORMAT, message, player);
		StringHelper.parsePlayer(json, target, "%target%").send(player);

		json = StringHelper.createJsonMessage(Configuration.WHISP_FROM_FORMAT, message, player);
		StringHelper.parsePlayer(json, target, "%target%").send(target);

		//Send Consol Message
		if(Configuration.CONSOLE_CHAT)
			Bukkit.getConsoleSender().sendMessage(StringHelper.getPlayerName(player, false, Configuration.PLAYER_COLOR) + ChatColor.GRAY + " to " + StringHelper.getPlayerName(target, false, Configuration.PLAYER_COLOR) + ChatColor.GRAY + " : " + message);

		if(!plugin.spy.isEmpty()) {
			json = StringHelper.createJsonMessage(Configuration.WHISP_SPY_FORMAT, message, player).replaceInText("%target%", StringHelper.getPlayerName(target, false, true));
			for(UUID uuid : plugin.spy){
				Player spy = Bukkit.getPlayer(uuid);
				if(spy != null && spy.isOnline() && spy.getUniqueId() != target.getUniqueId() && spy.getUniqueId() != player.getUniqueId())
					json.send(spy);
			}
		}

		return false;
	}

}
