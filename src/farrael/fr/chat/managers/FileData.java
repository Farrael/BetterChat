package farrael.fr.chat.managers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class FileData {

	private String name;
	private String path;

	private File file 				= null;
	private FileConfiguration conf 	= null;

	private int comments = 0;

	private boolean isFolder = false;

	public FileData(String name, String path){
		this.name = name;
		this.path = path;

		if(new File(path, name).exists()){
			if(!name.contains("."))
				this.isFolder = true;
			this.file = new File(path, name);
		} else {
			File Create = new File(path, name);
			if(!name.contains(".")){
				try {Create.mkdir();} catch (Exception e) {e.printStackTrace();}
				this.isFolder = true;
			} else {
				try {Create.createNewFile();} catch (Exception e) {e.printStackTrace();}
			}
			this.file = Create;
		}

		if(!this.isFolder){
			this.conf = new YamlConfiguration();
			try {
				this.conf.loadFromString(this.getContent());
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
	}

	public FileData(File file){
		this.name = file.getName();
		this.path = file.getPath();

		if(file.exists()){
			if(!name.contains("."))
				this.isFolder = true;
			this.file = file;
		} else {
			if(!name.contains(".")){
				try {file.mkdir();} catch (Exception e) {e.printStackTrace();}
				this.isFolder = true;
			} else {
				try {file.createNewFile();} catch (Exception e) {e.printStackTrace();}
			}
			this.file = file;
		}

		if(!this.isFolder){
			this.conf = new YamlConfiguration();
			try {
				this.conf.loadFromString(this.getContent());
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
	}

	public File getFile(){
		return this.file;
	}

	public FileConfiguration getConf(){
		return this.conf;
	}

	public String getPath(){
		return this.path;
	}

	public String getName(){
		return this.name;
	}

	public boolean isComments(){
		return this.comments > 0;
	}

	public boolean isFolder(){
		return this.isFolder;
	}

	public File[] getFiles(final String name){
		if(!this.isFolder) return new File[]{};
		FilenameFilter filter = new FilenameFilter(){
			public boolean accept(File file, String fileName) {
				return fileName.contains(name);
			}
		};
		return this.file.listFiles(filter);
	}

	public File[] getFiles(){
		if(!this.isFolder) return new File[]{};
		return this.file.listFiles();
	}

	public Set<FileData> getFilesData(String name){
		if(!this.isFolder) return new HashSet<FileData>();
		File[] file_list;
		if(name != null)
			file_list = this.getFiles(name);
		else
			file_list = this.getFiles();

		Set<FileData> file_data = new HashSet<FileData>();
		for(File file : file_list){
			file_data.add(new FileData(file));
		}
		return file_data;
	}

	public Set<FileData> getFilesData(){
		return this.getFilesData(null);
	}

	public ConfigurationSection getSection(String path){
		if(this.getConf().isConfigurationSection(path))
			return this.getConf().getConfigurationSection(path);
		else
			return this.getConf().createSection(path);
	}

	public void setPath(String path){
		this.path = path;
	}

	public void setName(String name){
		this.name = name;
	}

	public boolean setComment(String path, boolean line, String ...values){
		String path_comment = "";
		if(path.contains(".")){
			String[] list = path.split("\\.");
			for(int i = 0; i < (list.length - 1); i++){
				path_comment += list[i] + ".";
			}
		}

		if(this.conf.contains(path_comment + "_COMMENT_" + this.comments))
			this.conf.set(path_comment + "_COMMENT_" + this.comments, null);

		if(line)
			values[0] = values[0] + "{/}";

		for(String value : values){
			this.setData(path_comment + "_COMMENT_" + this.comments, " " + value);
			this.comments++;
		}

		if(this.isSection(path)){
			Object old = this.eraseData(path);
			this.setData(path, old);
		}

		return this.save();
	}

	public boolean setData(String data, Object value){
		this.getConf().set(data, value);
		return this.save();
	}

	public boolean setDataWithoutSave(String data, Object value){
		this.getConf().set(data, value);
		return true;
	}

	public boolean isData(String data){
		return this.getConf().contains(data);
	}

	public boolean isSection(String data){
		return this.getConf().contains(data);
	}

	public Object getData(String data, Object value){
		if(this.conf.contains(data)){
			return this.conf.get(data);
		} else {
			this.setData(data, value);
			return value;
		}
	}

	public Object eraseData(String path){
		Object old = this.conf.get(path);
		this.conf.set(path, null);
		return old;
	}

	public void update(){
		File file = new File(this.path, this.name);
		if(file.exists()){
			this.file = file;
		} else {
			try {file.createNewFile();} catch (IOException e) {e.printStackTrace();}
			this.file = file;
		}
		this.comments = 0;
		this.conf = new YamlConfiguration();
		try {
			this.conf.loadFromString(this.getContent());
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	public boolean save(){
		if(this.comments <= 0){
			try {
				this.conf.save(this.file);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		} else {
			try {
				this.saveConfig();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	/*****************************************************************/
	/*                 Private Save/Load Function                    */ 
	/*****************************************************************/
	private String getContent(){
		try {
			String 			addLine;
			String 			currentLine;
			int comment 			= 0;
			StringBuilder 	builder	= new StringBuilder();
			BufferedReader 	reader 	= new BufferedReader(new InputStreamReader(new FileInputStream(this.file), "UTF-8"));

			while((currentLine = reader.readLine()) != null) {
				if(currentLine.contains("#")) {
					addLine = currentLine.replaceFirst("#", "_COMMENT_" + comment + ":");
					builder.append(addLine + "\n");
					comment++;
				} else {
					builder.append(currentLine + "\n");
				}
			}
			reader.close();

			return builder.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private String getConfigStream() {
		String string   = this.conf.saveToString();
		int headerLine 	= 0;

		String[] lines 			= string.split("\n");
		StringBuilder config 	= new StringBuilder("");

		for(String line : lines) {
			if(line.contains("_COMMENT_")) {
				String comment = line.replaceFirst("_COMMENT_", "#");
				comment = comment.replace(comment.substring(comment.indexOf("#") + 1, comment.indexOf(":") + 1), "");

				if(comment.contains("# +-")) {
					if(headerLine == 0) {
						config.append(comment + "\n");

						headerLine = 1;
					} else if(headerLine == 1) {
						config.append(comment + "\n\n");
						headerLine = 0;
					}
				} else {
					String normalComment;
					if(comment.contains("# ' "))
						normalComment = comment.substring(0, comment.length() - 1).replaceFirst("# ' ", "# ");
					else
						normalComment = comment;

					boolean lineSpacer = normalComment.contains("{/}");

					if(lineSpacer){
						normalComment = normalComment.replace("{/}", "");
						config.append("\n" + normalComment + "\n");
					} else
						config.append(normalComment + "\n");
				}
			} else {
				config.append(line + "\n");
			}
		}
		return config.toString();
	}

	private void saveConfig() {
		String configuration = this.getConfigStream();

		try {
			Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.file), "UTF-8"));
			writer.write(configuration);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
