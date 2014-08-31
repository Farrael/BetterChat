package farrael.fr.chat.managers;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public class FileManager {

	private LinkedHashMap<FileType, FileData> data = new LinkedHashMap<FileType, FileData>();
	private JavaPlugin plugin;

	public enum FileType{
		CONFIG		("config.yml"	, "");

		private String file;
		private String section;
		FileType(String file, String section){
			this.file 	= file;
			this.section= section;
		}
		public String getFileName(){
			return this.file;
		}
		public String getFileSection(){
			return this.section;
		}
	}

	// Don't forget the ".yml" to the end of first column.
	public HashMap<String, String> rename = new HashMap<String, String>(){ private static final long serialVersionUID = 1L; {
		//put("door.yml", "structure.yml");
	}};

	public FileManager(JavaPlugin plugin){
		this.plugin = plugin;
		this.createFolder(plugin.getDataFolder());
	}

	/**
	 * Save ( create if not exist ) files into memory
	 * @param list - List of FileType to create
	 * @return List of FileData based on filenames
	 */
	public Set<FileData> newFiles(FileType ...origin){
		this.verify(origin);

		Set<FileData> files = new LinkedHashSet<FileData>();
		for(FileType type : origin){	
			FileData file = null;
			for(FileData data : this.data.values()){
				if(data.getName() == type.getFileName()){
					file = data;
					break;
				}
			}

			if(file == null)
				file = new FileData(type.getFileName(), plugin.getDataFolder().getAbsolutePath());

			this.data.put(type, file);
			files.add(file);
		}
		return files;
	}

	/**
	 * Verify validity of files
	 * @param types - List of FileType to create
	 * @return list of FileType to create after verification
	 */
	public void verify(FileType ... types){
		File[] files = plugin.getDataFolder().listFiles();
		Set<FileType> type = new HashSet<FileType>(Arrays.asList(types));
		for(File file : files){
			if(rename.containsKey(file.getName()))
				file.renameTo(new File(plugin.getDataFolder().getAbsolutePath(), rename.get(file.getName())));
			for(FileType t : type){
				if(t.getFileName().equalsIgnoreCase(file.getName())){
					if(!t.getFileName().equals(file.getName()))
						file.renameTo(new File(plugin.getDataFolder().getAbsolutePath(), t.getFileName()));
				}
			}
		}
	}

	/**
	 * Create dolder if not exist
	 * @param path - Folder path
	 */
	public void createFolder(File path){
		if(!path.exists())
			path.mkdir();
	}

	/**
	 * Return list of files in memory.
	 */
	public LinkedHashSet<FileType> getFileList(){
		return new LinkedHashSet<FileType>(data.keySet());
	}

	/**
	 * Return file in memory.
	 * @param type - File type
	 */
	public FileData getFile(FileType type){
		return this.data.get(type);
	}

	/**
	 * Is file in memory
	 * @param type - File type
	 */
	public boolean isFile(FileType type){
		return this.data.containsKey(type);
	}

	/**
	 * Is data in file.
	 * @param type - File type
	 * @param data - Data name
	 */
	public boolean isData(FileType type, String data){
		if(!isFile(type)) return false;
		return this.getFile(type).isData(type.getFileSection() + data);
	}

	/**
	 * Delete file from memory
	 * @param name - File name
	 * @return False if no file in memory
	 */
	public boolean delFile(FileType type){
		if(isFile(type)){
			this.data.remove(type);
			return true;
		}
		return false;
	}

	/**
	 * Set data to file and save it
	 * @param type - File name
	 * @param path - Data path
	 * @param value - Value to set
	 * @return True if error occurred
	 */
	public boolean setData(FileType type, Object path, Object value){
		if(!isFile(type)) return false;
		return this.getFile(type).setData(type.getFileSection() + path, value);
	}

	/**
	 * Set comments to file and save it
	 * @param type - File type
	 * @param path - Data path
	 * @param values - Value to set
	 * @return
	 */
	public boolean setComment(FileType type, Object path, String ...values){
		return setComment(type, path, false, values);
	}

	/**
	 * Set comments to file and save it
	 * @param type - File type
	 * @param path - Data path
	 * @param line - line before comment
	 * @param values - Value to set
	 * @return
	 */
	public boolean setComment(FileType type, Object path, boolean line, String ...values){
		if(!isFile(type)) return false;
		return this.getFile(type).setComment(type.getFileSection() + path, line, values);
	}

	/**
	 * Set data to file without save it.
	 * @param type - File type
	 * @param path - Data path
	 * @param value - Value to set
	 * @return True if error occurred
	 */
	public boolean setDataWithoutSave(FileType type, Object path, Object value){
		if(!isFile(type)) return false;
		return this.getFile(type).setDataWithoutSave(type.getFileSection() + path, value);
	}

	/**
	 * Return data, or create if not exist.
	 * @param type - File type
	 * @param path - Data path
	 * @param value - Default value
	 * @return Value of data ( default if not exist )
	 */
	public Object getData(FileType type, String path, Object value){
		if(!isFile(type)) return null;
		return this.getFile(type).getData(type.getFileSection() + path, value);
	}

	/**
	 * Return main section from FileType.
	 * @param type - FileType
	 */
	public ConfigurationSection getSection(FileType type){
		if(!isFile(type)) return null;
		String path = "";
		if(type.getFileSection() != "")
			path = type.getFileSection().substring(0, type.getFileSection().length() - 1);
		return this.getFile(type).getSection(path);
	}

	/**
	 * Return sub-section from FileType
	 * @param type - File type
	 * @param path - Path to sub-section.
	 * @return
	 */
	public ConfigurationSection getSection(FileType type, String path){
		if(!isFile(type)) return null;
		path = type.getFileSection() + path;
		return this.getFile(type).getSection(path);
	}

	/**
	 * Return if sub-section exist.
	 * @param type - File type
	 * @param section - Path to sub-section.
	 * @return
	 */
	public boolean isSection(FileType type, String section){
		if(!isFile(type)) return false;
		return this.getFile(type).isSection(type.getFileSection() + section);
	}

	/**
	 * Save file if exist.
	 * @param type - File type
	 * @return True if error occurred
	 */
	public boolean saveFile(FileType type){
		if(!isFile(type)) return false;		
		FileData file = this.getFile(type);
		file.save();
		return true;
	}

	/**
	 * Save all files in memory.
	 * @return True if error occurred
	 */
	public boolean saveAll(){
		Set<FileType> list = this.data.keySet();
		boolean success = true;
		for(FileType type : list) {
			if(!this.saveFile(type))
				success = false;
		}
		return success;
	}
}
