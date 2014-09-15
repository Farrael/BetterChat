package farrael.fr.chat.classes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_7_R3.ChatSerializer;
import net.minecraft.server.v1_7_R3.NBTTagCompound;
import net.minecraft.server.v1_7_R3.PacketPlayOutChat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class JsonMessage {

	private String  text;
	private String 	replace;
	private String 	temp = "";
	private boolean closed;

	/**
	 * Create empty JsonMessage
	 */
	public JsonMessage(){
		this.text   = "{\"text\":\"" + "" + "\"" + ",\"extra\":[";
		this.closed = false;
	}

	/**
	 * Create JsonMessage with default text
	 * @param string - default text
	 */
	public JsonMessage(String string){
		this.text   = "{\"text\":\"" + "" + "\"" + ",\"extra\":[" + "{\"text\":\"" + string + "\"}";
		this.closed = false;
	}

	/**
	 * Write string
	 * @param message - string to write
	 */
	public JsonMessage text(String message) {
		this.temp = this.temp + (",{\"text\":\"" + message + "\"");
		return this;
	}

	/**
	 * Create hover event
	 * @param message - message to display
	 */
	public JsonMessage hover(String message) {
		this.temp = this.temp + (",\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + message + "\"}");
		return this;
	}

	/**
	 * Create click event
	 * @return clickableText setup
	 */
	public clickableText click() {
		return new clickableText(this);
	}

	/**
	 * Add display color to text
	 * @param color - color to display
	 */
	public JsonMessage color(ChatColor color){
		String value = "";
		switch(color){
		case STRIKETHROUGH:
			value = (",\"strikethrough\":true");
			break;
		case BOLD:
			value = (",\"bold\":true");
			break;
		case UNDERLINE:
			value = (",\"underlined\":true");
			break;
		case ITALIC:
			value = (",\"italic\":true");
			break;
		case MAGIC:
			value = (",\"obfuscated\":true");
			break;
		default:
			value = (",\"color\":\"" + color.name().toLowerCase() + "\"");
			break;
		}
		this.temp = this.temp + value;
		return this;
	}

	/**
	 * Translate given string (cf minecraft)
	 * @param value - value to translate
	 * @return
	 */
	public JsonMessage translate(String value){
		this.temp = this.temp + (",{\"translate\":" + value);
		return this;
	}

	/**
	 * Create item event
	 * @param item - item informations to display
	 */
	public JsonMessage tooltip(ItemStack item){
		if(item == null || item.getType() == Material.AIR){
			this.temp = "";
			return this;
		}

		//Lore and Name
		if(item.hasItemMeta()){
			item = item.clone();
			ItemMeta meta = item.getItemMeta();
			
			if(meta.hasDisplayName())
				meta.setDisplayName(meta.getDisplayName().replaceAll(",", ";"));
			
			if(meta.hasLore()){
				List<String> lore = new ArrayList<String>();
				for(String line : meta.getLore())
					lore.add(line.replaceAll(",", ";"));
				meta.setLore(lore);
			}
			item.setItemMeta(meta);
		}

		net.minecraft.server.v1_7_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
		NBTTagCompound root = nmsItem.save(new NBTTagCompound());
		
		this.temp = this.temp + (",\"hoverEvent\":{\"action\":\"show_item\",\"value\":\"" + (root.toString()).replace("\"", "") + "\"}");
		return this;
	}

	/**
	 * Seperate texte without write it (use for replace)
	 */
	public JsonMessage seperate(){
		if(this.temp != "")
			this.temp = this.temp + "}";
		return this;
	}

	/**
	 * Close the text
	 */
	public JsonMessage then(){
		if(this.temp != ""){
			this.temp = this.temp + "}";
			if(this.replace != null){
				this.text = this.text.replaceAll(this.replace, "\"}" + this.temp + ",{\"text\":\"");
				this.replace = null;
			} else
				this.text = this.text + this.temp;
			this.closed = false;
			this.temp 	= "";
		}
		return this;
	}

	/**
	 * Contain string in text
	 * @param value - Value to search for
	 */
	public boolean contain(String value){
		return this.text.contains(value);
	}

	/**
	 * Replace in text with event
	 * @param regex - String to replace
	 */
	public JsonMessage replace(String regex){
		this.replace = regex;
		return this;
	}

	/**
	 * Replace cible by source in text
	 */
	public JsonMessage replaceInText(String regex, String replacement){
		this.text = this.text.replaceAll(regex, replacement);
		return this;
	}

	/**
	 * Translate color for all text already written with then()
	 */
	public JsonMessage translateColorCode(char symbole){
		this.text = this.text.replace("$", "\f00");
		this.text = ChatColor.translateAlternateColorCodes(symbole, this.text).replace("\f00", "$");
		return this;
	}

	/**
	 * Close the JsonMessage
	 */
	public JsonMessage finish() {
		if(!this.temp.equals(""))
			this.then();

		this.text = text + "]}";
		this.closed = true;
		return this;
	}

	/**
	 * Return the JsonMessage packet to send
	 */
	public PacketPlayOutChat getPacket(){
		if(!this.closed)
			this.finish();
		return new PacketPlayOutChat(ChatSerializer.a(this.text));
	}

	/**
	 * Return written text
	 */
	public String getText(){
		return this.text;
	}

	/**
	 * Send to a player
	 * @param player - player to send
	 */
	public void send(Player player) {
		((CraftPlayer)player).getHandle().playerConnection.sendPacket(this.getPacket());
	}

	/**
	 * Send to a list of players
	 * @param players - list of players to send
	 */
	public void sendToList(Player[] players){
		for (Player p: players)
			this.send(p);
	}

	/**
	 * Send to all players
	 */
	public void sendToAll(){
		for (Player p: Bukkit.getOnlinePlayers())
			this.send(p);
	}

	////////////////////////////////////////////////////////////
	//                Clickable Text Writter                  //
	////////////////////////////////////////////////////////////
	public class clickableText{
		private JsonMessage json;
		private String		text;

		public clickableText(JsonMessage instance){
			this.json = instance;
			this.text = ",\"clickEvent\":";
		}

		/**
		 * Open a link on click
		 * @param url - url to open
		 */
		public clickableText openlink(String url){
			this.text = this.text + "{\"action\":\"open_url\",\"value\":\"" + url + "\"}";
			return this;
		}

		/**
		 * Run command on click
		 * @param command - command to run
		 */
		public clickableText runCommand(String command){
			this.text = this.text + "{\"action\":\"run_command\",\"value\":\"" + command + "\"}";
			return this;
		}

		/**
		 * Write string in chat on click
		 * @param chat - string to write in chat
		 * @return
		 */
		public clickableText chatSuggestion(String chat){
			this.text = this.text + "{\"action\":\"suggest_command\",\"value\":\"" + chat + "\"}";
			return this;
		}

		/**
		 * Open a file on click
		 * @param file - file to open
		 */
		public clickableText openFile(String file){
			this.text = this.text + "{\"action\":\"open_file\",\"value\":\"" + file + "\"}";
			return this;
		}

		/**
		 * Separator for multiple actions
		 */
		public clickableText then(){
			this.text = this.text + ",";
			return this;
		}

		/**
		 * Close the click event
		 * @return
		 */
		public JsonMessage close(){
			this.json.temp = this.json.temp + this.text;
			return json;
		}
	}

	// Get last chat color
	public static ChatColor getLastColors(String input) {
        ChatColor result = ChatColor.GRAY;
        int length = input.length();

        // Search backwards from the end as it is faster
        for (int index = length - 1; index > -1; index--) {
            char section = input.charAt(index);
            if (section == ChatColor.COLOR_CHAR && index < length - 1) {
                char c = input.charAt(index + 1);
                ChatColor color = ChatColor.getByChar(c);

                if (color != null) {
                    result = color;

                    // Once we find a color or reset we can stop searching
                    if (color.isColor() || color.equals(ChatColor.RESET)) {
                        break;
                    }
                }
            }
        }

        return result;
    }
}
