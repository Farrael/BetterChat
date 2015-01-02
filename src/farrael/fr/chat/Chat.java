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
import farrael.fr.chat.listeners.PermissionsExListener;
import farrael.fr.chat.listeners.PlayerListener;
import farrael.fr.chat.managers.ConfigManager;
import farrael.fr.chat.managers.FileManager;
import farrael.fr.chat.managers.FileManager.FileType;

public class Chat extends JavaPlugin{

	//-------------/ Variables /-------------//
	public static Chat 		instance;
	public static String	label;

	public FileManager		fileManager;
	public ConfigManager	configManager;

	public boolean			enable;

	public boolean			join_display;
	public String 			join_message;

	public boolean			leave_display;
	public String 			leave_message;

	public boolean			first_message_display;
	public String 			first_message_player;
	public String 			first_message_broadcast;

	public String 			chat_format;
	public boolean 			player_color;
	public boolean 			console_chat;

	public boolean			player_tab;
	public boolean			player_click;
	public boolean			player_hover;
	public String			player_hover_text;

	public boolean			useGroupManager;
	public boolean			usePermissionEx;

	public GroupManager 	groupManager;

	public final Logger 	logger = Bukkit.getServer().getLogger();

	@Override
	public void onEnable() {
		instance 			= this;
		label				= ChatColor.BLUE + "[" + ChatColor.YELLOW + this.getName() + ChatColor.BLUE + "]";

		// Create config loader
		this.fileManager 	= new FileManager(this);
		this.configManager	= new ConfigManager(this, fileManager);
		this.enable 		= true;

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
	public boolean getUsage(Player player, String commande){
		player.sendMessage(ChatColor.RED + "Utilisation :");
		player.sendMessage(ChatColor.RED + commande);
		return true;
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
		return true;
	}
}
