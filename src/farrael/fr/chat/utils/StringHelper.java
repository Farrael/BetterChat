package farrael.fr.chat.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import farrael.fr.chat.Chat;
import farrael.fr.chat.classes.JsonMessage;
import farrael.fr.chat.configuration.Configuration;

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
		message 	= message.replace("\\", "\\\\").replace("\"", "\\\"");

		// Default/Player format
		JsonMessage json = parsePlayer(new JsonMessage(format), player, "%player%");

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

		// Urls parsing
		List<String> urls = extractUrls(json.getText());
		if(!urls.isEmpty()){
			for(int i = 0; i < urls.size(); i++){
				json.replace(urls.get(i)).text("[Lien]").color(ChatColor.ITALIC).color(ChatColor.GRAY).hover(ChatColor.DARK_PURPLE + urls.get(i)).click().openlink(urls.get(i)).close();
			}
		}

		return json.finish();
	}

	/**
	 * Parse player name, using configuration
	 * @param json - JsonMessage to change
	 * @param player - Player informations
	 * @param string - String to replace
	 * @param color - Using color
	 */
	public static JsonMessage parsePlayer(JsonMessage json, Player player, String string){
		json.replace(string).text(getPlayerName(player, true, Configuration.PLAYER_COLOR));
		if(Configuration.PLAYER_HOVER) json.getPart().hover(Configuration.PLAYER_HOVER_MESSAGE);
		if(Configuration.PLAYER_CLICK) json.getPart().click().chatSuggestion(Configuration.PLAYER_CLICK_MESSAGE.replace("%player%", player.getName())).close();
		json.getPart().replaceInText("%time%", (new java.text.SimpleDateFormat("HH:mm:ss")).format(new Date()));
		return json.translateColorCode('&');
	}

	/**
	 * Get player display name with/without color
	 */
	public static String getPlayerName(Player player, boolean display, boolean color){
		String name = (display ? (player.getDisplayName() != null ? player.getDisplayName() : player.getName()) : player.getName());
		return (color ? (player.hasMetadata("color") ? player.getMetadata("color").get(0).asString() : "") : "") + name;
	}

	/**
	 * Returns a list with all links contained in the input
	 */
	public static List<String> extractUrls(String text) {
		List<String> containedUrls = new ArrayList<String>();
		String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
		Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
		Matcher urlMatcher = pattern.matcher(text);

		while (urlMatcher.find()) {
			containedUrls.add(text.substring(urlMatcher.start(0),
					urlMatcher.end(0)));
		}

		return containedUrls;
	}
}
