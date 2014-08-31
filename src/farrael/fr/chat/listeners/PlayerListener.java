package farrael.fr.chat.listeners;

import java.util.Date;

import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import farrael.fr.chat.Chat;
import farrael.fr.chat.classes.JsonMessage;

public class PlayerListener implements Listener{
	Chat chat = Chat.instance;

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		if(!chat.enable) return;

		setChatName(event.getPlayer());

		if(!event.getPlayer().hasPlayedBefore()){
			if(!chat.first_message_display) return;
			event.setJoinMessage(parseMessage(chat.first_message_broadcast, event.getPlayer()));
			event.getPlayer().sendMessage(parseMessage(chat.first_message_player, event.getPlayer()));
		} else {
			if(!chat.join_display) return;
			event.setJoinMessage(parseMessage(chat.join_message, event.getPlayer()));
		}
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event){
		if(!chat.enable || !chat.leave_display) return;
		event.setQuitMessage(parseMessage(chat.leave_message, event.getPlayer()));
	}

	@EventHandler
	public void onPlayerSpeak(AsyncPlayerChatEvent event){
		if(!chat.enable) return;

		event.setCancelled(true);
		sendChatMessage(chat.chat_format, event.getMessage(), event.getPlayer());
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

	public String parseMessage(String message, Player player){
		String date = (new java.text.SimpleDateFormat("HH:mm:ss")).format(new Date());

		message = message.replaceAll("%player%", player.getDisplayName() != null ? player.getDisplayName() : player.getName());
		message = message.replaceAll("%playername%", player.getDisplayName() != null ? player.getDisplayName() : player.getName());
		message = message.replaceAll("%server%", Bukkit.getServerName());
		message = message.replaceAll("%suffix%", player.hasMetadata("suffix") ? player.getMetadata("suffix").get(0).asString() : "");
		message = message.replaceAll("%prefix%", player.hasMetadata("prefix") ? player.getMetadata("prefix").get(0).asString() : "");
		message = message.replaceAll("%color%" , player.hasMetadata("color") ? player.getMetadata("color").get(0).asString() : "");
		message = message.replaceAll("%time%"  , date);
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public void sendChatMessage(String format, String message, Player player){
		String name = ChatColor.stripColor(player.getDisplayName() != null ? player.getDisplayName() : player.getName());
		String date = (new java.text.SimpleDateFormat("HH:mm:ss")).format(new Date());
		Location l  = player.getLocation();
		message 	= message.replace("\\", "\\\\\\\\").replace("\"", "\\\\\"");

		JsonMessage json = new JsonMessage(format);

		// Format player
		json.replace("%player%").text(parseMessage(chat.player_format, player));
		if(chat.player_hover) json.hover(chat.player_hover_text);
		if(chat.player_click) json.click().chatSuggestion("/w " + name + " ").close();
		json.then();

		// Format variable
		json.replace("%server%").text(chat.getServer().getName()).then();
		json.replace("%suffix%").text(player.hasMetadata("suffix") ? player.getMetadata("suffix").get(0).asString() : "").then();
		json.replace("%prefix%").text(player.hasMetadata("prefix") ? player.getMetadata("prefix").get(0).asString() : "").then();
		json.replace("%color%").text(player.hasMetadata("color") ? player.getMetadata("color").get(0).asString() : "").then();
		json.replaceInText("%playername%", player.getDisplayName() != null ? player.getDisplayName() : player.getName());
		json.replaceInText("%time%", date).then();

		if(player.hasPermission("chat.color"))
			json.replaceInText("%message%", message).translateColorCode('&');
		else
			json.translateColorCode('&').replaceInText("%message%", message);

		//Message format
		json.replace("%position%").text(ChatColor.GRAY + "[" + ChatColor.GOLD + "position" + ChatColor.GRAY + "]" + ChatColor.WHITE).hover(ChatColor.BLUE + name + " en [" + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ() + "]").then();
		if(json.contain("%item%")){
			ItemStack item 		= player.getItemInHand();
			String item_name 	= ChatColor.stripColor(item.getItemMeta() != null ? item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name().toLowerCase() : item.getType().name().toLowerCase()).replace("_", " " + ChatColor.GOLD);
			json.replace("%item%").text(ChatColor.GRAY + "[" + ChatColor.GOLD + item_name + ChatColor.GRAY + "]").tooltip(item).then();
		}

		//Close and Send
		json.finish().sendToAll();
	}

	public static void setChatName(Player player){
		if(player == null) return;
		String prefix = getPrefix(player);
		String suffix = getSuffix(player);

		if(prefix != ""){
			String color = getColor(prefix);
			if(color != ""){
				player.setMetadata("color", new FixedMetadataValue(Chat.instance, color));
			}
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
