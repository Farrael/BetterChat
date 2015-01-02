package farrael.fr.chat.classes;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.minecraft.server.v1_8_R1.ChatSerializer;
import net.minecraft.server.v1_8_R1.PacketPlayOutChat;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class JsonMessage {

	private String		string 	= "";
	private boolean 	closed 	= false;

	private String 		replace;
	private JsonPart 	replace_temp;

	private List<JsonPart> part = new LinkedList<JsonPart>();
	private int actual_part 	= -1;

	/**
	 * Create empty JsonMessage
	 */
	public JsonMessage(){}

	/**
	 * Create formatted JsonMessage
	 */
	public JsonMessage(String format){
		this.text(format);
	}

	/**
	 * Create texte node
	 * @param message
	 */
	public JsonPart text(String message) {
		this.apply();
		this.actual_part++;
		this.part.add(new JsonPart(message));
		if(this.actual_part > 0)
			this.part.get(actual_part).copyColor(this.part.get(actual_part - 1));

		return this.getPart();
	}

	/**
	 * Return actual texte node
	 */
	public JsonPart getPart() {
		return this.isReplacement() ? this.replace_temp : this.part.get(actual_part);
	}

	/**
	 * Return if message contains value
	 * @param value
	 */
	public boolean contains(String value){
		for(JsonPart part : this.part)
			if(part.contains(value))
				return true;
		return false;
	}

	/**
	 * Return message to string
	 * @return
	 */
	public String getText(){
		this.apply();

		String string = "";
		for(JsonPart part : this.part)
			string += part.getText() + ",";

		if(this.part.size() > 0)
			string = string.substring(0, string.length() - 1);

		return "{\"text\":\"\"" + ",\"extra\":[" + string + "]}";
	}

	/**
	 * Return if message finished
	 */
	public boolean isValid() {
		return this.closed;
	}

	/**
	 * Return if replacement enabled
	 */
	public boolean isReplacement() {
		return this.replace != null;
	}

	/**
	 * Replace in text with event
	 * @param regex - String to replace
	 */
	public JsonPart replace(String regex){
		this.apply();

		this.replace = regex;
		return this.replace_temp = new JsonPart();
	}

	/**
	 * Replace cible by source in text
	 */
	public JsonMessage replaceInText(String regex, String replacement){
		this.closed = false;

		for(int i = 0; i < this.part.size(); i++)
			this.part.get(i).replaceInText(regex, replacement);

		return this;
	}

	/**
	 * Apply replacement
	 */
	public JsonMessage apply() {
		if(this.isReplacement() && this.replace_temp.isValid()) {
			List<JsonPart> result = new LinkedList<JsonPart>();
			for(int i = 0; i < this.part.size(); i++) {
				if(this.part.get(i).contains(this.replace)) {
					Set<JsonPart> parts = this.part.get(i).replaceWithPart(this.replace, this.replace_temp);
					for(JsonPart part : parts)
						if(part.isValid())
							result.add(part);
				} else
					result.add(this.part.get(i));
			}

			this.part = result;
			this.actual_part = (result.size() - 1);
		}

		this.replace 		= null;
		this.replace_temp 	= null;
		this.closed			= false;
		return this;
	}

	/**
	 * Translate symbole to color code
	 * @param symbole
	 */
	public JsonMessage translateColorCode(char symbole) {
		this.apply();

		List<JsonPart> result = new LinkedList<JsonPart>();
		JsonPart last = null;
		for(JsonPart part : this.part) { 
			if(last != null)
				part.copyColor(last);
			result.addAll(part.translateColorCode(symbole));
			last = result.get(result.size() - 1);
		}
		this.part = result;
		this.actual_part = (result.size() - 1);
		return this;
	}

	/**
	 * Close the JsonMessage
	 */
	public JsonMessage finish() {
		this.string = this.getText();
		this.closed = true;
		return this;
	}

	/**
	 * Return the JsonMessage packet to send
	 */
	public PacketPlayOutChat getPacket(){
		if(!this.isValid())
			this.finish();
		return new PacketPlayOutChat(ChatSerializer.a(this.string));
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
	public void sendToList(List<Player> players){
		for (Player p: players)
			this.send(p);
	}

	/**
	 * Send to all players
	 */
	@SuppressWarnings("deprecation")
	public void sendToAll(){
		for (Player p: Bukkit.getOnlinePlayers())
			this.send(p);
	}
}
