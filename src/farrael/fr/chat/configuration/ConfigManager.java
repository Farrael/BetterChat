package farrael.fr.chat.configuration;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import farrael.fr.chat.Chat;
import farrael.fr.chat.configuration.FileManager.FileType;

public class ConfigManager {

	private boolean 		loaded 			= false;
	private boolean			debug			= false;
	private FileManager 	fileManager		= null;
	private Chat 			plugin			= null;

	public static String 	permission 		= ChatColor.RED + "Vous n'avez pas la permission pour effectuer cela.";

	public ConfigManager(Chat plugin, FileManager fileManager){
		this.plugin 		= plugin;
		this.fileManager 	= fileManager;
	}

	/**
	 * Return FileManager used.
	 */
	public FileManager getFileManager(){
		return this.fileManager;
	}

	/**
	 * Is config load successful.
	 * @return True if finished without error.
	 */
	public boolean isLoaded(){
		return this.loaded;
	}

	/**
	 * Load config from FileManager
	 */
	public boolean load(boolean reload){
		for(FileType type : this.fileManager.getFileList())
			this.loadType(type, reload);
		this.loaded = true;
		return true;
	}

	/**
	 * Load file by Type
	 * @param type
	 */
	private void loadType(FileType type, boolean reload){
		long startTime = System.currentTimeMillis();
		switch(type){
		case CONFIG:
			this.loadConfig(type);
			break;
		case USER:
			this.loadUser(type);
			break;
		default:
			break;
		}
		long endTime = System.currentTimeMillis();
		debug("Loading " + type.toString().toLowerCase() + " took " + (endTime-startTime) + "ms.");
	}

	/**
	 * Reload configuration
	 */
	public boolean reload(FileType file, boolean verbose){
		if(!this.isLoaded()) return false;

		try{
			if(file == null){
				this.loaded = false;
				this.load(true);
			} else {
				this.loadType(file, true);
			}
		} catch(Exception e){
			Bukkit.getConsoleSender().sendMessage("[" + plugin.getName() + "] " + ChatColor.RED + e);
			if(verbose)
				e.printStackTrace();
			return false;
		}

		return true;
	}

	/*****************************************************************/
	/*                  Private loading function                     */ 
	/*****************************************************************/
	private void loadConfig(FileType type){
		// Get value.
		Configuration.JOIN_DISPLAY = (boolean) this.fileManager.getData(type, "join-display", false);
		Configuration.JOIN_MESSAGE = (String) this.fileManager.getData(type, "join-message", "%player% vient de se connecter.");
		Configuration.LEAVE_DISPLAY = (boolean) this.fileManager.getData(type, "leave-display", false);
		Configuration.LEAVE_MESSAGE = (String) this.fileManager.getData(type, "leave-message", "%player% vient de se deconnecter.");
		Configuration.FIRST_MESSAGE_DISPLAY = (boolean) this.fileManager.getData(type, "first-message-display", false);
		Configuration.FIRST_MESSAGE_BROADCAST = (String) this.fileManager.getData(type, "first-message-broadcast", "Bienvenue a %player% sur le serveur.");
		Configuration.FIRST_MESSAGE_PLAYER = (String) this.fileManager.getData(type, "first-message-player", "Bienvenue %player% sur le serveur %server%.");
		Configuration.CHAT_FORMAT = (String) this.fileManager.getData(type, "chat-format", "%player% &f: %message%");
		Configuration.WHISP_TO_FORMAT = (String) this.fileManager.getData(type, "whisp-to-format", "&7[&6A %color%%player%&7] &f%message%");
		Configuration.WHISP_FROM_FORMAT = (String) this.fileManager.getData(type, "whisp-from-format", "&7[&6De %color%%player%&7] &f%message%");
		Configuration.WHISP_SPY_FORMAT = (String) this.fileManager.getData(type, "whisp-spy-format", "&7[&6De %color%%player% &6a %color%%target%&7] &f%message%");
		Configuration.PLAYER_COLOR = (boolean) this.fileManager.getData(type, "player-color", true);
		Configuration.CONSOLE_CHAT = (boolean) this.fileManager.getData(type, "console-chat", true);
		Configuration.PLAYER_TAB = (boolean) this.fileManager.getData(type, "player-tab", true);
		Configuration.PLAYER_CLICK = (boolean) this.fileManager.getData(type, "player-click", true);
		Configuration.PLAYER_CLICK_MESSAGE = (String) this.fileManager.getData(type, "player-click-message", "/w %player% ");
		Configuration.PLAYER_HOVER = (boolean) this.fileManager.getData(type, "player-hover", true);
		Configuration.PLAYER_HOVER_MESSAGE	= (String) this.fileManager.getData(type, "player-hover-message", "&9%time%");
		Configuration.ENABLE = (boolean) this.fileManager.getData(type, "enable", true);

		this.debug = (boolean) this.fileManager.getData(type, "console-Debug", false);

		// Set commentaries.
		this.fileManager.setComment(type, "join-display", false, "Affiche un message lors de la connexion.");
		this.fileManager.setComment(type, "join-message", true, "Message lors de la connexion.");
		this.fileManager.setComment(type, "leave-display", true, "Affiche un message lors de la deconnexion.");
		this.fileManager.setComment(type, "leave-message", true, "Message lors de la deconnexion.");
		this.fileManager.setComment(type, "first-message-display", true, "Affiche un message lors de la de la premiere connexion.");
		this.fileManager.setComment(type, "first-message-broadcast", true, "Message lors de la premiere connexion ( server ).");
		this.fileManager.setComment(type, "first-message-player", true, "Message lors de la premiere connexion ( player only ).");
		this.fileManager.setComment(type, "chat-format", true, "Format des messages dans le chat.");
		this.fileManager.setComment(type, "whisp-to-format", true, "Format lors d'envois de message privee");
		this.fileManager.setComment(type, "whisp-from-format", true, "Format lors de la reception de message privee");
		this.fileManager.setComment(type, "whisp-spy-format", true, "Format lors de l'interception de message privee");
		this.fileManager.setComment(type, "player-color", true, "Change le display name avec la couleur du prefix.");
		this.fileManager.setComment(type, "console-chat", true, "Affiche les message dans la console.");
		this.fileManager.setComment(type, "player-tab", true, "Modifie la couleur d'un joueur dans le menu tabulation.");
		this.fileManager.setComment(type, "player-click", true, "Active le clique sur le nom d\'un joueur.");
		this.fileManager.setComment(type, "player-click-message", true, "Message lors du clique sur le nom d\'un joueur.");
		this.fileManager.setComment(type, "player-hover", true, "Active le survole du nom d\'un joueur.");
		this.fileManager.setComment(type, "player-hover-message", true, "Message lors du survole du nom d\'un joueur.");
		this.fileManager.setComment(type, "enable", true, "Active ou desactive le plugin.");
		this.fileManager.setComment(type, "console-Debug", true, "Display debug message in the console.");

		// Save file.
		this.fileManager.saveFile(type);
	}

	private void loadUser(FileType type){
		for(String uuid : this.fileManager.getSection(type).getKeys(false))
			plugin.spy.add(UUID.fromString(uuid));
	}

	/**
	 * Print debug message to console
	 * @param string - Debug message
	 */
	public void debug(String string){
		if(this.debug)
			Bukkit.getConsoleSender().sendMessage("[" + plugin.getName() + "] " + ChatColor.BLUE + string);
	}
}
