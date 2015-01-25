package farrael.fr.chat.utils;

import java.util.Date;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import farrael.fr.chat.Chat;
import farrael.fr.chat.classes.JsonMessage;
import farrael.fr.chat.storage.Configuration;

public class StringHelper {

	/**
	 * Return the first/last color used in string with symbole
	 * @param altColorChar - Color symbole
	 * @param string - String to parse
	 * @param reverse - First or last color
	 */
	public static ChatColor getColorFromString(char altColorChar, String string, boolean reverse) {
		ChatColor result = ChatColor.WHITE;
		char[] b = string.toCharArray();
		int length = b.length;

		ChatColor color = null;
		int min = reverse ? (length - 1) : 0;
		int max = reverse ? 0 : (length - 1);
		int hit = reverse ? -1 : 1;
		for (int i = min; i != max; i += hit) {
			if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i+1]) > -1) {				
				color = ChatColor.getByChar(b[i+1]);
				if (color != null) {
					result = color;

					if (color.isColor() || color.equals(ChatColor.RESET)) {
						break;
					}
				}
			}
		}

		return result;
	}

	/**
	 * Create pre-formatted json
	 * @param format - Default format
	 * @param message - Message to send
	 * @param player - Player sender
	 */
	public static JsonMessage createJsonMessage(String format, String message, Player player){
		String name = player.getName();
		String date = (new java.text.SimpleDateFormat("HH:mm:ss")).format(new Date());
		Location l  = player.getLocation();
		message 	= message.replace("\\", "\\\\\\\\").replace("\"", "\\\"");

		JsonMessage json = new JsonMessage(format);

		// Format player
		json.replace("%player%").text(getDisplayName(player, Configuration.PLAYER_COLOR));
		if(Configuration.PLAYER_HOVER) json.getPart().hover(Configuration.PLAYER_HOVER_MESSAGE);
		if(Configuration.PLAYER_CLICK) json.getPart().click().chatSuggestion(Configuration.PLAYER_CLICK_MESSAGE.replace("%player%", name)).close();

		// Format variable
		json.replaceInText("%server%", Chat.getInstance().getServer().getName());
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
			json.replace("%item%").text(ChatColor.GRAY + "[" + item_name + ChatColor.GRAY + "]").color(getColorFromString(ChatColor.COLOR_CHAR, item_name, false)).tooltip(item);
		}

		return json.finish();
	}

	/**
	 * Get player display name with/without color
	 */
	public static String getDisplayName(Player player, boolean color){
		return (color ? (player.hasMetadata("color") ? player.getMetadata("color").get(0).asString() : "") : "") + player.getDisplayName() != null ? player.getDisplayName() : player.getName();
	}
}
