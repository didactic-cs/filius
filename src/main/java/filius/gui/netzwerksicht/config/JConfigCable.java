package filius.gui.netzwerksicht.config;

import javax.swing.JLabel;

import filius.gui.GUIHelper;
import filius.gui.JBackgroundPanel;
import filius.hardware.Cable;
import filius.hardware.NetworkInterface;
import filius.hardware.Port;
import filius.hardware.knoten.Host;
import filius.hardware.knoten.Modem;
import filius.hardware.knoten.Node;
import filius.hardware.knoten.Router;
import filius.hardware.knoten.Switch;
import filius.rahmenprogramm.I18n;

@SuppressWarnings("serial")
public class JConfigCable extends JConfigPanel implements I18n {
	
	private GUIHelper gui;
	private JLabel[][] labels;
	private JLabel     param1, param2, param3;
	
	
	protected void initMainPanel(JBackgroundPanel mainPanel) {
		
		gui = new GUIHelper();
		
		// Initialize the array of labels
		// (Because this method is called before the constructor returns!)
		labels = new JLabel[2][4];
		
		// Column 1 Extremities		
		
		gui.addLabel(messages.getString("jkabelkonfiguration_msg1")+" 1", mainPanel, 50, 36, 80, 15);
				
		gui.addLabel(messages.getString("jkabelkonfiguration_msg1")+" 2", mainPanel, 50, 60, 80, 15);	
		
		// Column 2 Name
		
		gui.addLabel(messages.getString("jhostkonfiguration_msg1"), mainPanel, 150, 12, 120, 15);	
		
		labels[0][0] = gui.addLabel("", mainPanel, 150, 36, 120, 15);	
		gui.setBoldFont(labels[0][0], false);
		
		labels[1][0] = gui.addLabel("", mainPanel, 150, 60, 120, 15);			
		gui.setBoldFont(labels[1][0], false);
		
		// Column 3 MAC/Port		 
		
		param1 = gui.addLabel("", mainPanel, 290, 12, 130, 15);	
		
		labels[0][1] = gui.addLabel("", mainPanel, 290, 36, 130, 15);	
		gui.setBoldFont(labels[0][1], false);
		
		labels[1][1] = gui.addLabel("", mainPanel, 290, 60, 130, 15);			
		gui.setBoldFont(labels[1][1], false);
		
		// Column 4	IP

		param2 = gui.addLabel("", mainPanel, 445, 12, 100, 15);	

		labels[0][2] = gui.addLabel("", mainPanel, 445, 36, 100, 15);	
		gui.setBoldFont(labels[0][2], false);

		labels[1][2] = gui.addLabel("", mainPanel, 445, 60, 100, 15);			
		gui.setBoldFont(labels[1][2], false);
		
		// Column 5	Masque

		param3 = gui.addLabel("", mainPanel, 555, 12, 100, 15);	

		labels[0][3] = gui.addLabel("", mainPanel, 555, 36, 100, 15);	
		gui.setBoldFont(labels[0][3], false);

		labels[1][3] = gui.addLabel("", mainPanel, 555, 60, 100, 15);			
		gui.setBoldFont(labels[1][3], false);
	}		
	
	private void swapPorts(Port[] ports) {
		
		Port tmp = ports[1];
		ports[1] = ports[0];
		ports[0] = tmp;
	}
	
	private void setLabels(int i, String name, String mac, String ip, String mask) {
		
		labels[i][0].setText(name);				
		labels[i][1].setText(mac);			
		labels[i][2].setText(ip);
		labels[i][3].setText(mask);
	}

	@Override
	public void updateDisplayedValues() {		
		
		Port[] ports = ((Cable)getHardware()).getPorts();
		
		// Reorder the ports to get this order of display: host, router, modem, and switch
		
		// Show the modem last (except if connected to a switch)
		if (ports[0].getOwner() instanceof Modem && !(ports[1].getOwner() instanceof Switch)) {
			swapPorts(ports);
		}

		// If there is only one switch, display it last
		if (ports[0].getOwner() instanceof Switch && !(ports[1].getOwner() instanceof Switch)) {
			swapPorts(ports);
		}
		
		// If there is only one host, display it first
		if (!(ports[0].getOwner() instanceof Host) && ports[1].getOwner() instanceof Host) {
			swapPorts(ports);
		}
		
		// Update the headers
        
		int switchCount = 0;
		if (ports[0].getOwner() instanceof Switch) switchCount = 2; 
		else if (ports[1].getOwner() instanceof Switch) switchCount = 1;
		
		// Update the column captions according to the hardware types
		if (switchCount < 2) {
			if (switchCount == 0) {
				param1.setText(messages.getString("jhostkonfiguration_msg9"));  // MAC 
			} else {
				param1.setText(messages.getString("jhostkonfiguration_msg9")+" / "+messages.getString("jkabelkonfiguration_msg2"));   // MAC / Port
			}
			param2.setText(messages.getString("jhostkonfiguration_msg3"));   // IP
			param3.setText(messages.getString("jhostkonfiguration_msg4"));   // Mask
		} else {
			param1.setText(messages.getString("jkabelkonfiguration_msg2"));  // Port
			param2.setText(" ");  
			param3.setText(" ");  
		}
		
		for (int i = 0; i<2; i++) {
			
			Node node = ports[i].getOwner();
			
			if (node instanceof Host) {
				Host h = (Host) node;			
				NetworkInterface ni = ports[i].getNIC();
				setLabels(i, h.getName(), ni.getMac(), ni.getIp(), ni.getSubnetMask());
				
			} else if (node instanceof Switch) {
				Switch s = (Switch) node;	
				setLabels(i, s.getName(), messages.getString("jkabelkonfiguration_msg2")+" "+String.valueOf(ports[i].getIndex()+1), "", "");	
				
			} else if (node instanceof Router) {
				Router r = (Router) node;
				NetworkInterface ni = ports[i].getNIC();
				int portIndex = r.getPortIndex(ni) + 1;
				setLabels(i, r.getName(), ni.getMac()+" ("+String.valueOf(portIndex)+")", ni.getIp(), ni.getSubnetMask());
				
			} else if (node instanceof Modem) {
				Modem m = (Modem) node;		
				setLabels(i, m.getName(), "", "", "");
			}			
		}
	}

}