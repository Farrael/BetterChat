package farrael.fr.chat;

import java.util.logging.Logger;

import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import farrael.fr.chat.commands.ChatCommands;
import farrael.fr.chat.commands.WhisperCommands;
import farrael.fr.chat.listeners.PermissionsExListener;
import farrael.fr.chat.listeners.PlayerListener;
import farrael.fr.chat.managers.ConfigManager;
import farrael.fr.chat.managers.FileManager;
import farrael.fr.chat.managers.FileManager.FileType;

public class Chat extends JavaPlugin{

	//-----------/ Static /------------//
	public static Chat 	instance;
	public static String 	label;

	//-------/ Configurations /--------//
	public FileManager	fileManager;
	public ConfigManager	configManager;

	//---------/ Permissions /---------//
	public boolean		useGroupManager;
	public boolean		usePermissionEx;
	public GroupManager 	groupManager;

	public final Logger 	logger = Bukkit.getServer().getLogger();
	@Override
	public void onEnable() {
		instance 			= this;
		label				= ChatColor.BLUE + "[" + ChatColor.YELLOW + this.getName() + ChatColor.BLUE + "] ";

		// Create config loader
		this.fileManager 	= new FileManager(this);
		this.configManager	= new ConfigManager(this, fileManager);

		// Detecte plugins
		useGroupManager = Bukkit.getServer().getPluginManager().getPlugin("GroupManager") 	!= null;
		usePermissionEx = Bukkit.getServer().getPluginManager().getPlugin("PermissionsEx") 	!= null;

		// If GroupManager and PermissionEx on same time
		if(useGroupManager && usePermissionEx){
			Bukkit.getConsoleSender().sendMessage("[" + this.getName() + "] " + ChatColor.RED + "PermissionEx and GroupManager detected.");
			Bukkit.getServer().getPluginManager().disablePlugin(this);
			return;
		}

		if(useGroupManager || usePermissionEx)
			Bukkit.getConsoleSender().sendMessage("[" + this.getName() + "] " + ChatColor.GREEN + (useGroupManager ? "GroupManager" : "PermissionEx") + " detected.");
		else
			Bukkit.getConsoleSender().sendMessage("[" + this.getName() + "] " + ChatColor.GREEN + "Zero permissions plugin detected.");

		// If GroupManager
		if(useGroupManager)
			groupManager = (GroupManager) Bukkit.getServer().getPluginManager().getPlugin("GroupManager");

		// Listeners
		registreEvents(new PlayerListener(), new PermissionsExListener());

		// Loading configuration
		this.fileManager.newFiles(FileType.CONFIG);
		this.configManager.load(false);

		// Commands Listener
		getCommand("chat").setExecutor(new ChatCommands());
		getCommand("w").setExecutor(new WhisperCommands());
	}

	/**
	 * Register list of Listeners.
	 * @param listeners
	 */
	public void registreEvents(Listener... listeners){
		for(Listener listener : listeners){
			getServer().getPluginManager().registerEvents(listener, this);
		}
	}

	/**
	 * Return player has permission node.
	 * @param player
	 * @param permission
	 */
	public boolean hasPermission(Player player, String permission){
		return player.isOp() ? true : player.hasPermission(permission);
	}

	/**
	 * Send message to player with command usage
	 * @param player
	 * @param commande
	 */
	public String getUsage(String commande){
		String[] splitted = commande.split(":");

		String cmd = splitted[0];
		String effect = splitted.length > 1 ? splitted[1] : "";

		cmd = cmd.replace("[", ChatColor.RED + "[").replace("]", "]" + ChatColor.GOLD);
		cmd = cmd.replace("<", ChatColor.GRAY + "<").replace(">", ">" + ChatColor.GOLD);

		return ChatColor.GOLD + cmd + (effect.length() > 1 ? " : " + ChatColor.GREEN + effect : "");
	}

	/**
	 * Send message with plugin label
	 * @param target - Target
	 * @param message - Message to send
	 */
	public boolean sendPluginMessage(CommandSender target, String message, boolean isError) {
		ChatColor color = ChatColor.BLUE;
		if(isError)
			color = ChatColor.RED;

		target.sendMessage(label + color + message);
		return false;
	}

	/**
	 * Return list of arguments separate with space.
	 * @param args - List of arguments
	 * @param start - Start index
	 */
	public String getArguments(String[] args, int start) {
		String result = "";
		if(start < args.length) {
			for(int i = start; i < args.length; i++)
				result += (i == start ? " " : "") + args[i];
		}

		return result;
	}
}
