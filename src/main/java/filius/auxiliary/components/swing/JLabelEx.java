/*
 ** This file is part of Filius, a network construction and simulation software.
 ** 
 ** Originally created at the University of Siegen, Institute "Didactics of
 ** Informatics and E-Learning" by a students' project group:
 **     members (2006-2007): 
 **         André Asschoff, Johannes Bade, Carsten Dittich, Thomas Gerding,
 **         Nadja Haßler, Ernst Johannes Klebert, Michell Weyer
 **     supervisors:
 **         Stefan Freischlad (maintainer until 2009), Peer Stechert
 ** Project is maintained since 2010 by Christian Eibl <filius@c.fameibl.de>
 **         and Stefan Freischlad
 ** Filius is free software: you can redistribute it and/or modify
 ** it under the terms of the GNU General Public License as published by
 ** the Free Software Foundation, either version 2 of the License, or
 ** (at your option) version 3.
 ** 
 ** Filius is distributed in the hope that it will be useful,
 ** but WITHOUT ANY WARRANTY; without even the implied
 ** warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 ** PURPOSE. See the GNU General Public License for more details.
 ** 
 ** You should have received a copy of the GNU General Public License
 ** along with Filius.  If not, see <http://www.gnu.org/licenses/>.
 */
package filius.gui.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;
import javax.swing.JLabel;

/**
 * <b>JExtendedLabel</b> is a JLabel showing an icon on top of two lines
 * of text (possibly empty).<br>
 * If one of the texts is changed, the location of the image is recomputed so
 * that the icon does not move.
 */
@SuppressWarnings("serial")
public class JExtendedLabel extends JLabel {

	private final int text1dy = 16;
	private final int text2dy = 14;

	private Icon icon = null;
	private String text1;
	private String text2;
	private int compX = 0;
	private int iconX = 0;
	private int iconDx = 0;   // compX + iconDx = iconX
	private int iconWidth = 0;
	private int iconHeight = 0;
	private int text1width = 0;
	private int text1x = 0;
	private int text1y = 0;
	private int text2width = 0;
	private int text2x = 0;
	private int text2y = 0;
	private int totalWidth;
	private int totalHeight;


//	public JExtendedLabel(String text, Icon icon, int horizontalAlignment) {
//		super(null, null, JLabel.CENTER); // Horizontal alignment is forced
//		text1 = text;
//		setIcon(icon);
//	}

	public JExtendedLabel(Icon icon) {
		super(null, null, JLabel.CENTER); // Horizontal alignment is forced
		setIcon(icon);
	}

	@Override
	public void setIcon(Icon icon) {

		this.icon = icon;
		iconWidth  = (icon != null ? icon.getIconWidth() : 0); 
		iconHeight = (icon != null ? icon.getIconHeight() : 0);
		updateSize();
		super.setIcon(icon);
	}

	@Override
	public void setText(String text) {

		setText1(text);
	}

	public void setText1(String text) {

		boolean notext1 = (text == null || text.isEmpty());
		text1 = (notext1 ? null : text);
		text1width = (notext1 ? 0 : getFontMetrics(getFont()).stringWidth(text1));
		updateSize();
	}

	public void setText2(String text) {

		boolean notext2 = (text == null || text.isEmpty());
		text2 = (notext2 ? null : text);
		text2width = (notext2 ? 0 : getFontMetrics(getFont()).stringWidth(text2));
		updateSize();
	}

	public void setTexts(String text1, String text2) {

		boolean notext1 = (text1 == null || text1.isEmpty());
		this.text1 = (notext1 ? null : text1);
		text1width = (notext1 ? 0 : getFontMetrics(getFont()).stringWidth(text1));
		boolean notext2 = (text2 == null || text2.isEmpty());
		this.text2 = (notext2 ? null : text2);
		text2width = (notext2 ? 0 : getFontMetrics(getFont()).stringWidth(text2));
		updateSize();
	}

	@Override
	public int getWidth() {
		return totalWidth;
	}

	@Override
	public int getHeight() {
		return totalHeight;
	}

	private void updateSize() {

		// Width

		totalWidth = iconWidth;	    	
		if (text1width > totalWidth)  totalWidth = text1width;
		if (text2width > totalWidth)  totalWidth = text2width;      

		// Height

		int height = iconHeight;
		if (text1 != null) {
			height += getFontMetrics(getFont()).getHeight() + 5;
		}
		if (text2 != null) {
			height += getFontMetrics(getFont()).getHeight() + 5;
		}
		totalHeight = height;

		// Center Icon
		int prevIconDx = iconDx;
		iconDx = (totalWidth - iconWidth) / 2;

		// Center Texts
		text1x = (totalWidth - text1width) / 2;
		text2x = (totalWidth - text2width) / 2;

		// Vertical position of Texts
		text1y = (text1 != null ? iconHeight + text1dy : iconHeight);
		text2y = (text2 != null ? text1y + (text1 == null ? text1dy : text2dy) : text1y);

		// Height
		totalHeight = text2y;
		if (text1 != null || text2 != null )  totalHeight += getFontMetrics(getFont()).getDescent();

		// Relocate the component so that the icon does not move
		if (prevIconDx != iconDx) {
			compX = iconX - iconDx;
			super.setBounds(compX, getY(), totalWidth, totalHeight);
		}
	};

	public boolean inBounds(int x, int y) {

		return (getX() <= x) && (x <= getX() + totalWidth) && (getY() <= y) && (y <= getY() + totalHeight);
	}	    

	/**
	 * The x value sets the horizontal location of the icon
	 * If the texts become wider than the icon's width, the icon does not move
	 */
	public void setLocation(int x, int y) {     	

		iconX = x;
		compX = iconX - iconDx;
		super.setBounds(compX, y, totalWidth, totalHeight);
	}

	// Overloaded to prevent any change since the layout is fixed
	@Override
	public void setVerticalAlignment(int alignment) {}
	@Override
	public void setHorizontalAlignment(int alignment) {}
	@Override
	public void setVerticalTextPosition(int textPosition) {}
	@Override
	public void setHorizontalTextPosition(int textPosition) {}

	@Override
	protected void paintComponent(Graphics g) {

		Graphics2D g2d = (Graphics2D) g;

		// Background
		if (isOpaque()) {
			g2d.setColor(getBackground());
			g2d.fillRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
		}

		// Icon
		if (icon != null)  icon.paintIcon(this, g2d, iconDx, 0);

		// Text1
		if (text1 != null) {
			g2d.setColor(Color.black);
			g2d.drawString(text1, text1x, text1y);
		}

		// Text2
		if (text2 != null) {
			g2d.setColor(Color.black);
			g2d.drawString(text2, text2x, text2y);
		}
	}
}
