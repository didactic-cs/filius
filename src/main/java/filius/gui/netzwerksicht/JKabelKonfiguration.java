package filius.gui.netzwerksicht;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.Box.Filler;

import filius.hardware.Cable;
import filius.hardware.Hardware;
import filius.hardware.NetworkInterface;
import filius.hardware.Port;
import filius.hardware.knoten.Host;
import filius.hardware.knoten.Modem;
import filius.hardware.knoten.Node;
import filius.hardware.knoten.Switch;
import filius.hardware.knoten.Vermittlungsrechner;
import filius.rahmenprogramm.I18n;


public class JKabelKonfiguration extends JKonfiguration implements I18n {

	private static final long serialVersionUID = 1L;
	
	private Port[] ports;
	private JLabel[][] labels;
	private JLabel param1, param2, param3;

	protected JKabelKonfiguration() {
		super();
	}
	
	public void setHardwares(Cable cable) {
		
		if (ports == null) {
			ports = new Port[2];			
		}
		
		ports[0] = cable.getPorts()[0];
		ports[1] = cable.getPorts()[1];
		
		// If there is only one switch, display it last
		if (ports[0].getOwner() instanceof Modem && !(ports[1].getOwner() instanceof Switch)) {
			Port tmp = ports[1];
			ports[1] = ports[0];
			ports[0] = tmp;
		}

		// If there is only one switch, display it last
		if (ports[0].getOwner() instanceof Switch && !(ports[1].getOwner() instanceof Switch)) {
			Port tmp = ports[1];
			ports[1] = ports[0];
			ports[0] = tmp;
		}
		
		// If there is only one host, display it first
		if (!(ports[0].getOwner() instanceof Host) && ports[1].getOwner() instanceof Host) {
			Port tmp = ports[1];
			ports[1] = ports[0];
			ports[0] = tmp;
		}
		
		//hardware instanceof Host, Modem, Switch, Vermittlungsrechner
		
		updateDisplay();
	}
	
	private void updateDisplay() {
         
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
				labels[i][0].setText(h.getName());	
				NetworkInterface ni = ports[i].getNIC();
				labels[i][1].setText(ni.getMac());			
				labels[i][2].setText(ni.getIp());
				labels[i][3].setText(ni.getSubnetMask());
				
			} else if (node instanceof Switch) {
				Switch s = (Switch) node;	
				labels[i][0].setText(s.getName());		
				labels[i][1].setText(messages.getString("jkabelkonfiguration_msg2")+" "+String.valueOf(ports[i].getIndex()+1));
				labels[i][2].setText(" ");
				labels[i][3].setText(" ");				
				
			} else if (node instanceof Vermittlungsrechner) {
				Vermittlungsrechner r = (Vermittlungsrechner) node;
				labels[i][0].setText(r.getName());
				NetworkInterface ni = ports[i].getNIC();
				labels[i][1].setText(ni.getMac());			
				labels[i][2].setText(ni.getIp());
				labels[i][3].setText(ni.getSubnetMask());
				
			} else if (node instanceof Modem) {
				Modem m = (Modem) node;		
				labels[i][0].setText(m.getName());
				labels[i][1].setText(" ");
				labels[i][2].setText(" ");
				labels[i][3].setText(" ");
			}			
		}
	}
	
	private Filler vFiller(int height) {
		return new Filler(new Dimension(0,height), new Dimension(0,height), new Dimension(1,height));
	}

	@Override
	protected void initAttributEingabeBox(Box box, Box rightBox) {
		
		final int TITLE_WIDTH = 80;
		final int NAME_WIDTH  = 150;
		final int DATA_WIDTH  = 300; 		
		final int VSPACING = 10;
		
		// Initialize the array 
		// (Because this method is called before the constructor returns!)
		labels = new JLabel[2][4];
		
		// Extremities
		Box bx1 = Box.createVerticalBox();	
		
		JLabel padLabel = new JLabel(" ");
		bx1.add(padLabel);
		
		bx1.add(vFiller(VSPACING));
		
		JLabel label1 = new JLabel(messages.getString("jkabelkonfiguration_msg1")+" 1");	// Extremity
		bx1.add(label1);
		
		bx1.add(vFiller(VSPACING));
		
		JLabel label2 = new JLabel(messages.getString("jkabelkonfiguration_msg1")+" 2");	// Extremity
		bx1.add(label2);
		
		// Names
		Box bx2 = Box.createVerticalBox();
		
		JLabel label0 = new JLabel(messages.getString("jhostkonfiguration_msg1"));  // Name
		bx2.add(label0);
		
		bx2.add(vFiller(VSPACING));
		
		labels[0][0] = new JLabel();		
		Font ft = labels[0][0].getFont();
		labels[0][0].setFont(ft.deriveFont(ft.getStyle() & ~Font.BOLD));	
		bx2.add(labels[0][0]);
		
		bx2.add(vFiller(VSPACING));
		
		labels[1][0] = new JLabel();		
		ft = labels[1][0].getFont();
		labels[1][0].setFont(ft.deriveFont(ft.getStyle() & ~Font.BOLD));	
		bx2.add(labels[1][0]);
		
		// Param1
		Box bx3 = Box.createVerticalBox();
		
		param1 = new JLabel();
		bx3.add(param1);
		
		bx3.add(vFiller(VSPACING));
		
		labels[0][1] = new JLabel();		
		ft = labels[0][1].getFont();
		labels[0][1].setFont(ft.deriveFont(ft.getStyle() & ~Font.BOLD));	
		bx3.add(labels[0][1]);
		
		bx3.add(vFiller(VSPACING));
		
		labels[1][1] = new JLabel();		
		ft = labels[1][1].getFont();
		labels[1][1].setFont(ft.deriveFont(ft.getStyle() & ~Font.BOLD));	
		bx3.add(labels[1][1]);
		
		// Param2
		Box bx4 = Box.createVerticalBox();

		param2 = new JLabel();
		bx4.add(param2);
		
		bx4.add(vFiller(VSPACING));

		labels[0][2] = new JLabel();		
		ft = labels[0][2].getFont();
		labels[0][2].setFont(ft.deriveFont(ft.getStyle() & ~Font.BOLD));	
		bx4.add(labels[0][2]);
		
		bx4.add(vFiller(VSPACING));
		
		labels[1][2] = new JLabel();		
		ft = labels[1][2].getFont();
		labels[1][2].setFont(ft.deriveFont(ft.getStyle() & ~Font.BOLD));	
		bx4.add(labels[1][2]);
		
		// Param2
		Box bx5 = Box.createVerticalBox();

		param3 = new JLabel();
		bx5.add(param3);
		
		bx5.add(vFiller(VSPACING));

		labels[0][3] = new JLabel();		
		ft = labels[0][3].getFont();
		labels[0][3].setFont(ft.deriveFont(ft.getStyle() & ~Font.BOLD));	
		bx5.add(labels[0][3]);
		
		bx5.add(vFiller(VSPACING));
		
		labels[1][3] = new JLabel();		
		ft = labels[1][3].getFont();
		labels[1][3].setFont(ft.deriveFont(ft.getStyle() & ~Font.BOLD));	
		bx5.add(labels[1][3]);

		
		// Main wrapper		
		Box bx = Box.createHorizontalBox();
        bx.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        bx.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        
        bx.add(Box.createHorizontalStrut(5)); 
        bx.add(bx1);
        bx.add(Box.createHorizontalStrut(20)); 
        bx.add(bx2);
        bx.add(Box.createHorizontalStrut(20)); 
        bx.add(bx3);
        bx.add(Box.createHorizontalStrut(20)); 
        bx.add(bx4); 
        bx.add(Box.createHorizontalStrut(20)); 
        bx.add(bx5); 
        box.add(bx, BorderLayout.NORTH);	
        box.add(Box.createVerticalStrut(120));		
	}

	@Override
	public void updateAttribute() {
	}

}