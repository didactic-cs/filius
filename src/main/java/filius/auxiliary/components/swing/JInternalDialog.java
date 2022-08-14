package filius.auxiliary.components.swing;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class JInternalDialog {
	
	private static int count = 0;
	

	public static boolean isVisible() {
		
		return count > 0;
	}
	
	/** 
     * Changing this global parameter seems to be the only way to customize the title icon
     * of the JOptionPane's internal dialogs. Must be called before each call. 
     */
    private static void setInternalDialogIcon(Icon icon) {
    	
    	UIManager.put("InternalFrame.icon", icon);
    	count++;
    }
    
    private static void resetInternalDialogIcon() { 
    	
    	UIManager.put("InternalFrame.icon", null);
    	count--;
    }
    
    public static void info(Component parent, Icon icon, String message, String title) {
    	
    	setInternalDialogIcon(icon);
    	JOptionPane.showInternalMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE); 
    	resetInternalDialogIcon();
    }

    public static void warning(Component parent, Icon icon, String message, String title) {

    	setInternalDialogIcon(icon);
    	JOptionPane.showInternalMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE); 
    	resetInternalDialogIcon();
    }
    
//    public static String OKCancel(Component parent, Icon icon, String message, String title) {
//
//    	setInternalDialogIcon(icon);
//    	String s = (String) JOptionPane.showInternalInputDialog(parent, message, title, JOptionPane.QUESTION_MESSAGE);  
//    	resetInternalDialogIcon();
//    	return s;
//    }
    
    public static String OKCancel(Component parent, Icon icon, String message, String title, String defaultvalue) {    	
   	
    	setInternalDialogIcon(icon);
    	String s = (String) JOptionPane.showInternalInputDialog(parent, message, title, JOptionPane.QUESTION_MESSAGE, null, null, defaultvalue);  
    	resetInternalDialogIcon();
    	return s;
    }
    
    /**
     * yesNo returns true when the user clicked on yes
     */
    public static boolean yesNo(Component parent, Icon icon, String message, String title) {
    	
    	setInternalDialogIcon(icon);
    	boolean b = (JOptionPane.showInternalConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION); 
    	resetInternalDialogIcon();
    	return b;
    }
    
    /**
     * yesNo is identical to yesNo except for the Warning icon
     */
    public static boolean yesNoWarning(Component parent, Icon icon, String message, String title) {
    	
    	setInternalDialogIcon(icon);
    	boolean b = (JOptionPane.showInternalConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION); 
    	resetInternalDialogIcon();
    	return b;
    }
    
    /**
     * yesNoCancel returns JOptionPane.YES_OPTION, JOptionPane.NO_OPTION, JOptionPane.CANCEL_OPTION
     */
    public static int yesNoCancel(Component parent, Icon icon, String message, String title) {
    	
    	setInternalDialogIcon(icon);
    	int result = JOptionPane.showInternalConfirmDialog(parent, message, title, JOptionPane.YES_NO_CANCEL_OPTION);
    	if (result == JOptionPane.CLOSED_OPTION) result = JOptionPane.CANCEL_OPTION;
    	resetInternalDialogIcon();
    	return result;
    }
}
