/*
 *  This EventListener interface aims at replacing the observer 
 *  used in previous versions of Filius.
 *  
 */
package filius.hardware.knoten;

public interface ModemConnectedListener {	
	
	/** 
     * <b>onModemActiveChange</b> is called by the modem to notify a change of 
     * its connectivity status with another modem. active is true when the modem
     * is connected to another modem.
     * 
     * @param active boolean reflecting the connection status
     */
	public void onModemConnectedChange (boolean connected);
}
