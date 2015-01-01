package farrael.fr.chat.classes;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.server.v1_8_R1.NBTTagCompound;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class JsonPart {
	private String string;
	private String text;

	private ChatColor color;
	private ArrayList<ChatColor> style;

	/**
	 * Create new JsonPart empty
	 */
	public JsonPart() {
		this(null);
	}

	/**
	 * Create new JsonPart with text
	 * @param string - default text
	 */
	public JsonPart(String string) {
		this.style = new ArrayList<ChatColor>();
		this.string = "";
		this.text 	= "";
		if(string != null)
			this.text(string);
	}

	/**
	 * Write string
	 * @param message - string to write
	 */
	public JsonPart text(String message) {
		if(this.text.length() > 1)
			this.text += " ";
		this.text += message;
		return this;
	}

	/**
	 * Create hover event
	 * @param message - message to display
	 */
	public JsonPart hover(String message) {
		this.string += ",\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + message + "\"}";
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
	public JsonPart color(ChatColor color) {
		if(color == null)
			return this;

		String value 	= "";
		boolean isColor = false;
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
			isColor = true;
			break;
		}

		if(isColor)
			this.color = color;
		else {
			this.style.add(color);
			this.string += value;
		}

		return this;
	}

	/**
	 * Add list of text format
	 * @param list - List of format to add
	 */
	public JsonPart format(List<ChatColor> list) {
		for(ChatColor color : list) {
			if(color.isFormat())
				this.color(color);
		}

		return this;
	}

	/**
	 * Add display color to text
	 * @param color - color to display
	 */
	public JsonPart copyColor(JsonPart part) {
		this.color(part.color);
		for(ChatColor c : part.style)
			this.color(c);

		return this;
	}

	/**
	 * Translate given string (cf minecraft)
	 * @param value - value to translate
	 * @return
	 */
	public JsonPart translate(String value) {
		this.string += ",{\"translate\":" + value;
		return this;
	}

	/**
	 * Create item event
	 * @param item - item informations to display
	 */
	public JsonPart tooltip(ItemStack item) {
		if(item == null || item.getType() == Material.AIR){
			this.text = "";
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

		net.minecraft.server.v1_8_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
		NBTTagCompound root = nmsItem.save(new NBTTagCompound());

		this.string += ",\"hoverEvent\":{\"action\":\"show_item\",\"value\":\"" + (root.toString()).replace("\"", "") + "\"}";
		return this;
	}

	/**
	 * Contain string in text
	 * @param value - Value to search for
	 */
	public boolean contains(String value) {
		return this.getText().contains(value);
	}

	/**
	 * Replace cible by source in text
	 * @param regex - String to replace
	 * @param replacement - String to replace with
	 */
	public JsonPart replaceInText(String regex, String replacement) {
		this.text = this.text.replace(regex, replacement);
		this.string = this.string.replace(regex, replacement);
		return this;
	}

	/**
	 * Replace text with another JsonPart
	 * @param replace - String to replace
	 * @param part - JsonPart to replace with
	 * @return
	 */
	public Set<JsonPart> replaceWithPart(String replace, JsonPart part) {
		Set<JsonPart> found = new LinkedHashSet<JsonPart>();

		int index;
		while((index = text.indexOf(replace)) != -1) {
			found.add(new JsonPart(text.substring(0, index)).copyColor(this));
			text = text.substring(index + replace.length());
			found.add(part);
		}

		found.add(new JsonPart(text).copyColor(this));
		return found;
	}

	/**
	 * Translate color for all text already written with then()
	 */
	public Set<JsonPart> translateColorCode(char symbole) {
		this.text = this.text.replace("$", "\f00");
		this.text = ChatColor.translateAlternateColorCodes(symbole, this.text).replace("\f00", "$");
		this.string = this.string.replace("$", "\f00");
		this.string = ChatColor.translateAlternateColorCodes(symbole, this.string).replace("\f00", "$");

		Set<JsonPart> result = new LinkedHashSet<JsonPart>();

		int index; String before, after;
		ChatColor color = this.color;
		JsonPart json;
		ArrayList<ChatColor> format = new ArrayList<ChatColor>();
		while((index = text.indexOf(Character.toString(ChatColor.COLOR_CHAR))) != -1) {
			before = text.substring(0, index);
			after = text.substring(index + 2);

			ChatColor c = ChatColor.getByChar(text.charAt(index+1));
			if(c == null){
				text = before + after;
				continue;
			}

			json = new JsonPart(before);
			if(json.isValid())
				result.add(json.color(color).format(format).addToString(this.string));

			if(c.equals(ChatColor.RESET)) {
				color = null;
				format.clear();
			} else {
				if(c.isColor())
					color = c;
				else
					format.add(c);
			}

			text = after;
		}

		result.add(new JsonPart(text).color(color).format(format).addToString(this.string));
		return result;
	}

	/**
	 * Return written text with/without parsing
	 */
	public String getText() {
		return "{\"text\":\"" + this.text + "\"" + this.string + (this.color != null ? (",\"color\":\"" + color.name().toLowerCase() + "\"") : "")  + "}";
	}

	/**
	 * Return the color used
	 */
	public ChatColor getColor() {
		return this.color;
	}

	/**
	 * Return the format used
	 */
	public List<ChatColor> getFormat() {
		return this.style;
	}

	/**
	 * Only used by ClickableText
	 */
	public JsonPart addToString(String string) {
		this.string += string;
		return this;
	}

	/**
	 * Return if is valide
	 */
	public boolean isValid() {
		return !this.text.equals("");
	}
}
