package farrael.fr.chat.classes;

public class clickableText{
	private JsonPart part;
	private String	text;

	public clickableText(JsonPart instance){
		this.part = instance;
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
	public JsonPart close(){
		this.part.addToString(this.text);
		return this.part;
	}
}