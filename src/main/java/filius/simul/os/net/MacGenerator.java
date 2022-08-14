package filius.system.net.mac;

import java.util.Random;
import java.util.Vector;

public class MacGenerator {
	
	private static MacGenerator instance = null;
    private Vector<String> macAddresses = new Vector<String>();
        
    
    public static MacGenerator getInstance() {
    	
        if (instance == null) {
            instance = new MacGenerator();
        }
        return instance;        
    }
	
	/**
     * Automatic generation of random MAC address
     */
    public String getFreeMac() {
    	
        Random r = new Random();
        String[] mac;
        String newMac;

        mac = new String[6];
        for (int i = 0; i < mac.length; i++) {
            mac[i] = Integer.toHexString(r.nextInt(255));
            if (mac[i].length() == 1)  mac[i] = "0" + mac[i];
        }
        newMac = mac[0] + ":" + mac[1] + ":" + mac[2] + ":" + mac[3] + ":" + mac[4] + ":" + mac[5];
                 
        return (isValidMac(newMac) ? newMac : getFreeMac());
    }

    /** 
     * Add a MAC address to the list
     */
    public void addMac(String mac) {
    	
        macAddresses.addElement(mac);
    }

    /**
     * Check if the MAC address is available and valid.
     */
    private boolean isValidMac(String mac) {    	

    	for (String m: macAddresses) {
    		if (mac.equals(m))  return false;
    	}
        if (mac.equals("FF:FF:FF:FF:FF:FF"))  return false;

        return true;
    }
}
