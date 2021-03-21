package filius.gui.dokusicht;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.MenuSelectionManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

// Implements a color selector
@SuppressWarnings("serial")
public class JDocColorSelector extends JMenu {		
	
	private int sampleWidth = 30;
	private int sampleHeight = 30;
	
	protected  Border standardBorder = new CompoundBorder(new MatteBorder(1, 1, 1, 1, getBackground()), 
            											  new MatteBorder(1, 1, 1, 1, Color.DARK_GRAY));
	protected  Border selectedBorder = new MatteBorder(3, 3, 3, 3, new Color(0.48f, 0.73f, 0.98f)); // Light blue border
	protected  Border activeBorder   = new MatteBorder(2, 2, 2, 2, Color.BLACK);
	
    private Color color;

	public JDocColorSelector(String name, Color[] colors, int lineCount, Color selectedColor) {
		super(name);

		JPanel p = new JPanel();
		p.setBorder(new EmptyBorder(5, 5, 5, 5));
		p.setLayout(new GridLayout(lineCount, 1));		
		
		for (int i = 0; i < colors.length; i++) {
			boolean sameColor = (colors[i].getRGB() == selectedColor.getRGB());
			ColorPane pn = new ColorPane(colors[i], sameColor);
			p.add(pn);
		}
				
		add(p);
	}

	public Color getColor() {		
		return color;
	}

	public void triggerMenuResponse(Color color) {
		this.color = color;
		MenuSelectionManager.defaultManager().clearSelectedPath();
		fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,	getActionCommand()));
	}

	private class ColorPane extends JPanel implements MouseListener {		
		
		boolean selected;

		public ColorPane(Color c, boolean selected) {
			setBackground(c);
			this.selected = selected;
			setBorder(selected ? selectedBorder : standardBorder);
			addMouseListener(this);
		}

		public Dimension getPreferredSize() {
			return new Dimension(sampleWidth, sampleHeight);
		}

		public Dimension getMaximumSize() {
			return getPreferredSize();
		}

		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		public void mousePressed(MouseEvent e) {}

		public void mouseClicked(MouseEvent e) {}

		public void mouseReleased(MouseEvent e) {
			triggerMenuResponse(getBackground());
		}

		public void mouseEntered(MouseEvent e) {
			setBorder(activeBorder);
		}

		public void mouseExited(MouseEvent e) {			
			setBorder(selected ? selectedBorder : standardBorder);
		}
	}
}

