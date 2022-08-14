/*
 *  This EventListener interface aims at replacing the observer 
 *  used in previous versions of Filius.
 *  
 */

package filius.software.system;

public interface FiliusFileListener {	
	
	// Error codes 
    public static enum ChangeType {
    	NAME,    	
    	TYPE,
    	CONTENT
   	}
	
	void onChange (ChangeType ct);
}
