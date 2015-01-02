package farrael.fr.chat.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author atesin@gmail.com
 * Modified by Farrael
 */
public class StringArray {

	private String lineSeparator = "\n";
	private int pageHeight = 20;
	private String[] lines;

	public String fieldSeparator = "`";
	public String fillSpace = " ";
	public String thinSpace = ".";
	public int pageWidth = 53;
	public boolean useThinSpace = true;
	public boolean monospace = true;
	public ArrayList<Integer> tabs = new ArrayList<Integer>(Arrays.asList(10,20,30,40));
	private int numPages;

	private Map<Integer, String> chraracters = new HashMap<Integer, String>(){ 
		private static final long serialVersionUID = 1L; {
			put(7, "@~");
			put(6, "#$%&+-/0123456789=?ABCDEFGHJKLMNOPQRSTUVWXYZ\\^_abcdeghjmnopqrsuvwxyzñÑáéóúü");
			put(5, "\"()*<>fk{}");
			put(4, " I[]t");
			put(3, "'`lí");
			put(2, "!.,:;i|");
			put(-6, "§");
		}};

		public StringArray(String text){
			lines = text.split(lineSeparator);
			numPages = (int) Math.ceil((double)lines.length / (double)pageHeight);

			if(lines != null){
				int row = 0;

				for(String line : lines){
					String[] tab = line.split(fieldSeparator);
					if(tab.length > row) row = tab.length;
				}
				ArrayList<Integer> tab = new ArrayList<Integer>();
				for(int i = 1; i <= row; i++)
					tab.add( (60/row)*i );

				this.tabs = tab;
			}
		}

		public StringArray(String text, int chatHeigth){
			lines = text.split(lineSeparator);
			this.pageHeight = chatHeigth;
			numPages = (int) Math.ceil((double)lines.length / (double)pageHeight);
		}

		public StringArray(String text, String lineSeparator){
			lines = text.split(lineSeparator);
			this.lineSeparator = lineSeparator;
			numPages = (int) Math.ceil((double)lines.length / (double)pageHeight);
		}

		public StringArray(String text, int chatHeigth, String lineSeparator){
			lines = text.split(lineSeparator);
			this.pageHeight = chatHeigth;
			this.lineSeparator = lineSeparator;
			numPages = (int) Math.ceil((double)lines.length / (double)pageHeight);
		}

		public int getNumPages(){
			return numPages;
		}

		public int getNumLines(){
			return lines.length;
		}

		/** just for comfort */
		public StringArray setTabs(int[] tabs){
			ArrayList<Integer> tabs2 = new ArrayList<Integer>();
			for (int i: tabs) tabs2.add(i);
			this.tabs = tabs2;
			return this;
		}

		/**
		 *  Appends chars with its width to be checked too
		 *  @param chars a list of the chars, as a string (not all works, make tests)
		 *  @param wid the horizontal space each char ocuppies including its separation, in pixels
		 */
		public void addChars(String chars, int wid){
			if (!chraracters.containsKey(wid)) chraracters.put(wid, "");
			chraracters.get(wid).concat(chars);
		}

		/**
		 * @param page desired page number (0=all-in-one), considering preconfigured adjusts
		 * @return desired page text
		 */
		public String getPage(int page){
			return getPage(page, this.monospace);
		}

		/**
		 * @param page desired page number (0=all-in-one), considering preconfigured adjusts
		 * @param monospace true if fonts are fixed width (e.g.: console)
		 * @return desired page text
		 */
		public String getPage(int page, boolean monospace){

			int chatWidthPx = (monospace)? pageWidth: pageWidth * 6;
			int fromLine = (--page) * pageHeight;
			int toLine = (fromLine + pageHeight > lines.length)? lines.length: fromLine + pageHeight;
			if (page < 0){
				fromLine = 0;
				toLine = lines.length;
			}

			String tabLines = "";
			String tabLine;
			String line;
			String fields[];
			String field;
			int fieldLen;
			int toLen;
			int colWid;

			for (int linePos = fromLine; linePos < toLine; ++linePos){
				line = lines[linePos];
				tabLine = "";
				fields = line.split(fieldSeparator);
				for (int fieldPos = 0; fieldPos < fields.length; ++fieldPos){
					field = fields[fieldPos];
					fieldLen = pxLength(field, monospace);
					toLen = (fieldPos > tabs.size()-1)? chatWidthPx: (tabs.get(fieldPos) * (monospace? 1: 6));
					colWid = toLen - pxLength(tabLine, monospace);
					if (fieldLen > colWid) tabLine += pxSubStr(field, colWid, monospace);
					else tabLine += field;
					if (toLen >= chatWidthPx){
						if (pxLength(tabLine+fillSpace, monospace) > chatWidthPx) break;
						tabLine += fillSpace;
						continue;
					}
					if (!monospace && useThinSpace && pxLength(tabLine, monospace) % 4 > 1) tabLine += thinSpace;

					while (pxLength(tabLine, monospace) < toLen) tabLine += fillSpace;
				}
				tabLines += ((linePos == fromLine)? "": lineSeparator) + tabLine;
			}
			return tabLines;
		}

		/**
		 * @param str string to be checked
		 * @param monospace true if fixed width fonts will be used
		 * @return string width in pixels
		 */
		private int pxLength(String str, boolean monospace){

			if (monospace) return 2*(str.replace("§", "").length()) - str.length();

			int len = 0;
			for (int strPos = 0; strPos < str.length(); ++strPos){
				for (int px: chraracters.keySet()){
					if (chraracters.get(px).indexOf(str.charAt(strPos)) >= 0){
						len += px;
						break;
					}
				}
			}
			return len;
		}

		/**
		 * 
		 * @param str input string
		 * @param len desired string length in pixels or in chars if monospace (exclusive)
		 * @param monospace true if fonts with fixed width (console)
		 * @return stripped string
		 */
		private String pxSubStr(String str, int len, boolean monospace){

			int len2 = str.length();

			while (len2 > 0){
				if (pxLength(str, monospace) <= len) return str;
				--len2;
				str = str.substring(0, len2);
			}
			return str;
		}

		void sortByFields(int... args){
			if (args.length == 0) return;

			ArrayList<String> tempArray = new ArrayList<String>();
			String tempLines;
			String[] tempFields;

			for (int i = 0; i < lines.length; ++i){
				tempFields = lines[i].split(fieldSeparator);
				tempLines = "";
				for (int by: args) tempLines += fillSpace+tempFields[by];
				tempArray.add(tempLines+fieldSeparator+i);
			}
			Collections.sort(tempArray);

			tempLines = "";
			for (String line: tempArray){
				tempLines += (tempLines.length() == 0)? "": lineSeparator;
				tempLines += lines[Integer.parseInt(line.substring(line.indexOf(fieldSeparator)+1))];
			}
			lines = tempLines.split(lineSeparator);
		}

		void sortByNumField(int by, boolean asc){
			ArrayList<String> tempArray = new ArrayList<String>();
			String tempLines = "000000";
			String[] tempFields;

			for (int i = 0; i < lines.length; ++i){
				tempFields = lines[i].split(fieldSeparator);
				tempArray.add(tempLines.substring(tempFields[by].length())+tempFields[by]+fieldSeparator+i);
			}

			if (asc) Collections.sort(tempArray);
			else Collections.sort(tempArray, Collections.reverseOrder());

			tempLines = "";
			for (String line: tempArray){
				tempLines += (tempLines.length() == 0)? "": lineSeparator;
				tempLines += lines[Integer.parseInt(line.substring(line.indexOf(fieldSeparator)+1))];
			}

			lines = tempLines.split(lineSeparator);
		}
}