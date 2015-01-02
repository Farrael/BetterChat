package farrael.fr.chat.listeners;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
import farrael.fr.chat.storage.Configuration;
import farrael.fr.chat.utils.ColorHelper;

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

			createJsonMessage(Configuration.FIRST_MESSAGE_BROADCAST, "", event.getPlayer()).sendToList(c);
			createJsonMessage(Configuration.FIRST_MESSAGE_PLAYER, "", event.getPlayer()).send(event.getPlayer());
		} else {
			if(!Configuration.JOIN_DISPLAY) return;
			createJsonMessage(Configuration.JOIN_MESSAGE, "", event.getPlayer()).sendToAll();
		}
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event){
		if(!Configuration.ENABLE) return;

		event.setQuitMessage("");
		if(Configuration.LEAVE_DISPLAY)
			createJsonMessage(Configuration.LEAVE_MESSAGE, "", event.getPlayer()).sendToAll();
	}

	@EventHandler
	public void onPlayerSpeak(AsyncPlayerChatEvent event){
		if(!Configuration.ENABLE) return;

		event.setCancelled(true);
		createJsonMessage(Configuration.CHAT_FORMAT, event.getMessage(), event.getPlayer()).sendToAll();

		//Send Consol Message
		if(Configuration.CONSOLE_CHAT)
			Bukkit.getConsoleSender().sendMessage(getDisplayName(event.getPlayer(), Configuration.PLAYER_COLOR) + " : " + event.getMessage());
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

	public JsonMessage createJsonMessage(String format, String message, Player player){
		String name = player.getName();
		String date = (new java.text.SimpleDateFormat("HH:mm:ss")).format(new Date());
		Location l  = player.getLocation();
		message 	= message.replace("\\", "\\\\\\\\").replace("\"", "\\\\\"");

		JsonMessage json = new JsonMessage(format);

		// Format player
		json.replace("%player%").text(getDisplayName(player, Configuration.PLAYER_COLOR));
		if(Configuration.PLAYER_HOVER) json.getPart().hover(Configuration.PLAYER_HOVER_MESSAGE);
		if(Configuration.PLAYER_CLICK) json.getPart().click().chatSuggestion(Configuration.PLAYER_CLICK_MESSAGE.replace("%player%", name)).close();

		// Format variable
		json.replaceInText("%server%", chat.getServer().getName());
		json.replaceInText("%suffix%", player.hasMetadata("suffix") ? player.getMetadata("suffix").get(0).asString() : "");
		json.replaceInText("%prefix%", player.hasMetadata("prefix") ? player.getMetadata("prefix").get(0).asString() : "");
		json.replaceInText("%color%" , player.hasMetadata("color") ? player.getMetadata("color").get(0).asString() : "");
		json.replaceInText("%playername%", player.getDisplayName() != null ? player.getDisplayName() : player.getName());

		if(player.hasPermission("chat.color"))
			json.replaceInText("%message%", message).translateColorCode('&');
		else
			json.translateColorCode('&').replaceInText("%message%", message);

		//Message format
		json.replaceInText("%time%", date);
		json.replace("%position%").text(ChatColor.GRAY + "[" + ChatColor.GOLD + "position" + ChatColor.GRAY + "]" + ChatColor.WHITE).hover(ChatColor.BLUE + name + " en [" + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ() + "]");
		if(json.contains("%item%")){
			ItemStack item 		= player.getItemInHand();
			String item_name 	= (item.getItemMeta() != null ? item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name().toLowerCase() : item.getType().name().toLowerCase());
			json.replace("%item%").text(ChatColor.GRAY + "[" + item_name + ChatColor.GRAY + "]").color(ColorHelper.getColorFromString(ChatColor.COLOR_CHAR, item_name, false)).tooltip(item);
		}

		return json.finish();
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

	public static String getDisplayName(Player player, boolean color){
		return (color ? (player.hasMetadata("color") ? player.getMetadata("color").get(0).asString() : "") : "") + player.getDisplayName() != null ? player.getDisplayName() : player.getName();
	}

}
