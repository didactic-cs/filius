package filius.gui.netzwerksicht;

import java.awt.Color;

import javax.swing.JLabel;

public class GUIFooterPanel extends GUIMainArea {

    private static final long serialVersionUID = 1L;
    private JLabel footer;

    public GUIFooterPanel(int width, int height, String text) {
        setLayout(null);
        setOpaque(false);
        setSize(width, height);

        footer = new JLabel(text);
        footer.setForeground(Color.lightGray);
        footer.setSize(footer.getFontMetrics(footer.getFont()).stringWidth(text) + 20,
                footer.getFontMetrics(footer.getFont()).getHeight());

        add(footer);
    }

    public void updateViewport() {
        removeAll();
        add(footer);
    }

    public void setFooterPos(int x, int y) {
        footer.setBounds(x, y, footer.getWidth(), footer.getHeight());
    }
}
