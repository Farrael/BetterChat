package farrael.fr.chat.listeners;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import farrael.fr.chat.Chat;
import farrael.fr.chat.storage.Configuration;
import farrael.fr.chat.utils.StringHelper;

@SuppressWarnings("deprecation")
public class PlayerListener implements Listener{
	Chat chat = Chat.getInstance();


	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		if(!Configuration.ENABLE) return;

		setChatName(event.getPlayer());
		event.setJoinMessage("");
		if(!event.getPlayer().hasPlayedBefore() && Configuration.FIRST_MESSAGE_DISPLAY){
			List<Player> c = new LinkedList<Player>(Arrays.asList(Bukkit.getOnlinePlayers()));
			c.remove(event.getPlayer());

			StringHelper.createJsonMessage(Configuration.FIRST_MESSAGE_BROADCAST, "", event.getPlayer()).sendToList(c);
			StringHelper.createJsonMessage(Configuration.FIRST_MESSAGE_PLAYER, "", event.getPlayer()).send(event.getPlayer());
		} else {
			if(!Configuration.JOIN_DISPLAY) return;
			StringHelper.createJsonMessage(Configuration.JOIN_MESSAGE, "", event.getPlayer()).sendToAll();
		}
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event){
		if(!Configuration.ENABLE) return;

		event.setQuitMessage("");
		if(Configuration.LEAVE_DISPLAY)
			StringHelper.createJsonMessage(Configuration.LEAVE_MESSAGE, "", event.getPlayer()).sendToAll();
	}

	@EventHandler
	public void onPlayerSpeak(AsyncPlayerChatEvent event){
		if(!Configuration.ENABLE) return;

		event.setCancelled(true);
		StringHelper.createJsonMessage(Configuration.CHAT_FORMAT, event.getMessage(), event.getPlayer()).sendToAll();

		//Send Consol Message
		if(Configuration.CONSOLE_CHAT)
			Bukkit.getConsoleSender().sendMessage(StringHelper.getDisplayName(event.getPlayer(), Configuration.PLAYER_COLOR) + " : " + event.getMessage());
	}

	@EventHandler
	public void onCommands(final PlayerCommandPreprocessEvent event){
		Bukkit.getScheduler().runTaskLater(chat, new BukkitRunnable(){
			public void run(){
				if(event.getMessage().startsWith("/mang")){
					for(Player player : Bukkit.getOnlinePlayers()){
						PlayerListener.setChatName(player);
					}
				}
				if(event.getMessage().startsWith("/manu")){
					String[] message 	= event.getMessage().split(" ");
					PlayerListener.setChatName(Bukkit.getPlayer(message[1]));
				}
			}
		}, 5L);
	}

	/**
	 * Save player suffix and prefix in metadata and set PlayerListName.
	 * @param player
	 */
	public static void setChatName(Player player){
		if(player == null) return;
		String prefix = getPrefix(player);
		String suffix = getSuffix(player);

		if(prefix != ""){
			String color = getColor(prefix);
			if(color != "")
				player.setMetadata("color", new FixedMetadataValue(Chat.getInstance(), color));
			player.setPlayerListName(color + player.getName());
		}

		player.setMetadata("prefix", new FixedMetadataValue(Chat.getInstance(), ChatColor.translateAlternateColorCodes('&', prefix)));
		player.setMetadata("suffix", new FixedMetadataValue(Chat.getInstance(), ChatColor.translateAlternateColorCodes('&', suffix)));
	}

	/**
	 * Return player prefix
	 * @param player
	 */
	public static String getPrefix(Player player){
		if(Chat.getInstance().useGroupManager){
			AnjoPermissionsHandler handler = Chat.getInstance().groupManager.getWorldsHolder().getWorldPermissions(player);
			return handler != null ? nonNull(handler.getUserPrefix(player.getName())) : "";
		}
		if(Chat.getInstance().usePermissionEx){
			PermissionUser user = PermissionsEx.getUser(player);
			return user != null ? nonNull(user.getPrefix(player.getWorld().getName())) : "";
		}
		return "";
	}

	/**
	 * Return player suffix
	 * @param player
	 */
	public static String getSuffix(Player player){
		if(Chat.getInstance().useGroupManager){
			AnjoPermissionsHandler handler = Chat.getInstance().groupManager.getWorldsHolder().getWorldPermissions(player);
			return handler != null ? nonNull(handler.getUserSuffix(player.getName())) : "";
		}
		if(Chat.getInstance().usePermissionEx){
			PermissionUser user = PermissionsEx.getUser(player);
			return user != null ? nonNull(user.getSuffix(player.getWorld().getName())) : "";
		}
		return "";
	}

	/**
	 * Return last color with '&'
	 * @param prefix
	 */
	public static String getColor(String prefix){
		int index = prefix.lastIndexOf('&');
		if(index > -1)
			return ChatColor.translateAlternateColorCodes('&', prefix.substring(index, index+2));
		return "";
	}

	/**
	 * Return empty string if null.
	 * @param value
	 */
	public static String nonNull(String value){
		return value != null ? value : "";
	}
}
