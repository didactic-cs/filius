package filius.auxiliary.components.swing;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.border.Border;

@SuppressWarnings("serial")
public class JButtonEx extends JButton {
	
	
	public JButtonEx() {
		
		setBorder(new RoundedBorder(10));
	}
	
    private static class RoundedBorder implements Border {

        private int radius;
        

        RoundedBorder(int radius) {
            this.radius = radius;
        }


        public Insets getBorderInsets(Component c) {
        	
            return new Insets(this.radius+1, this.radius+1, this.radius+2, this.radius);
        }


        public boolean isBorderOpaque() {
        	
            return true;
        }


        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        	
            g.drawRoundRect(x, y, width-1, height-1, radius, radius);
        }
    }
}
