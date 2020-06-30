/*
 *  This EventListener interface aims at replacing the observer 
 *  used in previous versions of Filius.
 *  
 */
package filius.hardware;

public interface CableActiveListener {	
	
	/** 
     * <b>onActiveChange</b> is called by the cable to notify a change of 
     * its active status. active is true when data flows through the cable.
     * 
     * @param active boolean reflecting the active status
     */
	public void onActiveChange (boolean active);
}
