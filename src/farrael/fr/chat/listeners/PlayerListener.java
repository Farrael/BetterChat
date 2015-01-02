package farrael.fr.chat.listeners;

import java.util.Arrays;
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

public class PlayerListener implements Listener{
	Chat chat = Chat.instance;

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		if(!Configuration.ENABLE) return;

		setChatName(event.getPlayer());

		event.setJoinMessage("");
		if(!event.getPlayer().hasPlayedBefore() && Configuration.FIRST_MESSAGE_DISPLAY){
			List<Player> c =  Arrays.asList(Bukkit.getOnlinePlayers());
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

	@SuppressWarnings("deprecation")
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

	public static void setChatName(Player player){
		if(player == null) return;
		String prefix = getPrefix(player);
		String suffix = getSuffix(player);

		if(prefix != ""){
			String color = getColor(prefix);
			if(color != "")
				player.setMetadata("color", new FixedMetadataValue(Chat.instance, color));
			player.setPlayerListName(color + player.getName());
		}

		player.setMetadata("prefix", new FixedMetadataValue(Chat.instance, ChatColor.translateAlternateColorCodes('&', prefix)));
		player.setMetadata("suffix", new FixedMetadataValue(Chat.instance, ChatColor.translateAlternateColorCodes('&', suffix)));
	}

	public static String getPrefix(Player player){
		if(Chat.instance.useGroupManager){
			AnjoPermissionsHandler handler = Chat.instance.groupManager.getWorldsHolder().getWorldPermissions(player);
			return handler != null ? nonNull(handler.getUserPrefix(player.getName())) : "";
		}
		if(Chat.instance.usePermissionEx){
			PermissionUser user = PermissionsEx.getUser(player);
			return user != null ? nonNull(user.getPrefix(player.getWorld().getName())) : "";
		}
		return "";
	}

	public static String getSuffix(Player player){
		if(Chat.instance.useGroupManager){
			AnjoPermissionsHandler handler = Chat.instance.groupManager.getWorldsHolder().getWorldPermissions(player);
			return handler != null ? nonNull(handler.getUserSuffix(player.getName())) : "";
		}
		if(Chat.instance.usePermissionEx){
			PermissionUser user = PermissionsEx.getUser(player);
			return user != null ? nonNull(user.getSuffix(player.getWorld().getName())) : "";
		}
		return "";
	}

	public static String getColor(String prefix){
		int index = prefix.lastIndexOf('&');
		if(index > -1)
			return ChatColor.translateAlternateColorCodes('&', prefix.substring(index, index+2));
		return "";
	}

	public static String nonNull(String value){
		return value != null ? value : "";
	}
}
