package filius.software.web;

import java.util.Scanner;

public class IniStrings {
	
	private int valueCount = 0;	
	private String[] sections;
	private String[] keys;
	private String[] values;
	private int enumIndex = -1;
	private int enumStart = -1;
	private String enumeratedSection = "";
	
	
	public IniStrings(String text) {
		
		parseText(text);
	}
	
	private void parseText(String text) {
		
		// Number of lines (some may be empty)
		int count = (int) text.chars().filter(ch -> ch == '\n').count() + 1;
		
		// Create three temporary parallel arrays with enough rows
		sections = new String[count];
		keys = new String[count];
		values = new String[count];
		
		// Fill the arrays
		String currentSection = "";
		Scanner scanner = new Scanner(text);
		int i = 0;
		while (scanner.hasNextLine() && i < count) {
			
			String line = scanner.nextLine().trim();
			
			if (!line.isEmpty() && line.charAt(0) != ';') {
				
				String[] parts = line.split("=");
				
				if (parts.length == 1) {
					// Could be a new section
					if (line.startsWith("[") && line.endsWith("]")) {
						currentSection = line.substring(1, line.length() - 1);
						
					} // Could be an empty value
					else if (line.endsWith("=")) {
						sections[i] = currentSection;
						keys[i] = parts[0];
						values[i] = "";
						i++;
						
					} // Ignore the line 
					else {}
						
				} else {
					// It is supposed that '=' appears only once per line
					sections[i] = currentSection;
					keys[i] = parts[0];
					values[i] = parts[1];
					i++;
				}	
			}			
		}
		valueCount = i;
		scanner.close();
	}
	
	public String getValue(String section, String key) {
		
		int i = 0;
		while (i < valueCount && (!sections[i].equals(section)) || !keys[i].equals(key))  i++;		
		if (i < valueCount)  return values[i];
		else                 return "";
	}
	
	public int getSectionCount(String section) {

		int i = 0;
		while (i < valueCount && (!sections[i].equals(section)))  i++;
		if (i >= valueCount)  return 0;
		
		int j = i;
		while (j < valueCount && (sections[j].equals(section)))  j++;
		
		return j - i;
	}

    public boolean beginEnumSection(String section) {
    	
    	int i = 0;
    	while (i < valueCount && !sections[i].equals(section))  i++;
    	if (i < valueCount) {
    		enumStart = i;
    		enumIndex = i-1;
    		enumeratedSection = section;
    		return true;
    	} else {
    		enumStart = -1;
    		enumeratedSection = "";
    		return false;
    	}
    }
    
    public boolean enumHasNext() {
    	
    	if (enumStart == -1)  return false;
    	
    	enumIndex++;
    	if (enumIndex >= valueCount)  {
    		enumStart = -1;
    		return false;
    	}
    	
    	return (sections[enumIndex].equals(enumeratedSection));
    }
    
    public String enumGetKey() {
    	
    	if (enumIndex < 0 || enumIndex >= valueCount)  return "";
    	
    	return keys[enumIndex];
    }
    
    public String enumGetValue() {
    	
    	if (enumIndex < 0 || enumIndex >= valueCount)  return "";
    	
    	return values[enumIndex];
    }
}
