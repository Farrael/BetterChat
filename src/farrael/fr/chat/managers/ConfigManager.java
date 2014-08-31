package farrael.fr.chat.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import farrael.fr.chat.Chat;
import farrael.fr.chat.managers.FileManager.FileType;

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
		this.plugin.join_display			= (boolean) this.fileManager.getData(type, "join-display", false);
		this.fileManager.setComment(type, "join-display", false, "Affiche un message lors de la connexion.");
		this.plugin.join_message 			= (String) this.fileManager.getData(type, "join-message", "%player% vient de se connecter.");
		this.fileManager.setComment(type, "join-message", true, "Message lors de la connexion.");
		
		this.plugin.leave_display			= (boolean) this.fileManager.getData(type, "leave-display", false);
		this.fileManager.setComment(type, "leave-display", true, "Affiche un message lors de la deconnexion.");
		this.plugin.leave_message			= (String) this.fileManager.getData(type, "leave-message", "%player% vient de se deconnecter.");
		this.fileManager.setComment(type, "leave-message", true, "Message lors de la deconnexion.");
		
		this.plugin.first_message_display	= (boolean) this.fileManager.getData(type, "first-message-display", false);
		this.fileManager.setComment(type, "first-message-display", true, "Affiche un message lors de la de la premiere connexion.");
		this.plugin.first_message_broadcast	= (String) this.fileManager.getData(type, "first-message-broadcast", "Bienvenue a %player% sur le serveur.");
		this.fileManager.setComment(type, "first-message-broadcast", true, "Message lors de la premiere connexion ( server ).");
		this.plugin.first_message_player	= (String) this.fileManager.getData(type, "first-message-player", "Bienvenue %player% sur le serveur %server%.");
		this.fileManager.setComment(type, "first-message-player", true, "Message lors de la premiere connexion ( player only ).");
		
		this.plugin.chat_format	= (String) this.fileManager.getData(type, "chat-format", "%player% &f: %message%");
		this.fileManager.setComment(type, "chat-format", true, "Message lors de la premiere connection ( player only ).");
		
		this.plugin.player_format	= (String) this.fileManager.getData(type, "player-format", "%player%");
		this.fileManager.setComment(type, "player-format", true, "Message d'affichage du joueur.");
				
		this.plugin.player_tab				= (boolean) this.fileManager.getData(type, "player-tab", true);
		this.fileManager.setComment(type, "player-tab", true, "Modifie la couleur d'un joueur dans le menu tabulation.");
		
		this.plugin.player_click			= (boolean) this.fileManager.getData(type, "player-click", true);
		this.fileManager.setComment(type, "player-click", true, "Active le wisph lors du clique sur le nom d\'un joueur.");
		
		this.plugin.player_hover			= (boolean) this.fileManager.getData(type, "player-hover", true);
		this.fileManager.setComment(type, "player-hover", true, "Affiche un message lors du survole du nom d\'un joueur.");
		this.plugin.player_hover_text		= (String) this.fileManager.getData(type, "player-hover-message", "&9%time%");
		this.fileManager.setComment(type, "player-hover-message", true, "Message lors du survole du nom d\'un joueur.");
		
		this.plugin.enable				= (boolean) this.fileManager.getData(type, "enable", true);
		this.fileManager.setComment(type, "enable", true, "Active ou desactive le plugin.");
		
		this.debug 						= (boolean) this.fileManager.getData(type, "console-Debug", false);
		this.fileManager.setComment(type, "console-Debug", true, "Display debug message in the console.");
	}
	public void debug(String string){
		if(this.debug)
			Bukkit.getConsoleSender().sendMessage("[" + plugin.getName() + "] " + ChatColor.BLUE + string);
	}
}
