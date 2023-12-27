package filius.software.nat;

import java.util.Date;

/** Diese Klasse stellt die Einträge für die NAT-Tabelle da
 */
public class NatEntry {
	
	private InetAddress address;
    private NatType natType;
	private Date lastUpdate;
	
	public InetAddress getInetAddress(){
		return address;
	}
	
	public void setInetAddress(InetAddress address) {
		this.address = address;
	}
	
	public Date getLastUpdate() {
		return lastUpdate;
	}
	
	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	
    public NatType getNatType() {
        return natType;
    }

    public void setNatType(NatType natType) {
        this.natType = natType;
    }

    @Override
    public String toString() {
        return address +" (" + lastUpdate + "; " + natType + ")"; //neu
    }
}