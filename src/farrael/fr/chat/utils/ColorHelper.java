package farrael.fr.chat.utils;

import org.bukkit.ChatColor;

public class ColorHelper {

	/**
	 * Return the last color used in string with symbole
	 * @param altColorChar - Color symbole
	 * @param string - String to parse
	 */
	public static ChatColor getColorFromString(char altColorChar, String string) {
		ChatColor result = ChatColor.WHITE;
		char[] b = string.toCharArray();
		int length = b.length;

		ChatColor color = null;
		for (int i = 0; i < (length - 1); i++) {
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
}
