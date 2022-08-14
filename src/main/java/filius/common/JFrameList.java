/*
 * JFrameList.java
 *
 * Purpose:
 * Singleton used to coherently handle several JFrame windows, one being considered the master of the others.
 * 
 * Use:
 * call JFrameList.getInstance().addMain(...) to assign the main application's JFrame (the call must placed before the JFrame becomes visible)
 * call JFrameList.getInstance().add(...) for each other JFrame (each call must placed before the corresponding JFrame becomes visible)
 * 
 * When the master JFrame is iconified, the other JFrames are automatically hidden. Only the master JFrame's icon is visible in the taskbar.
 * When the master JFrame is deiconified, the other JFrames are automatically restored. Their Z-order and iconification states are restored too.
 * 
 * When the window of a JFrame (other than the master) is closed, the JFrame is automatically removed from the list.
 * 
 * call JFrameList.getInstance().hideAll() to hide all JFrames except the master
 * call JFrameList.getInstance().restoreAll() to show all JFrames previously hidden with hideAll(). Their Z-order and iconification states are restored too.
 * call JFrameList.getInstance().putMasterInTheBackground() to put the master JFrame behind all other JFrame windows.
 */

package filius.gui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import javax.swing.JFrame;

import filius.gui.nachrichtensicht.AbstractPacketsAnalyzerDialog;

public class JFrameList {
	
	private static JFrameList frameList = null;
	
	private JFrame mainFrame = null;	
	private ArrayList<JFrame> frames = new ArrayList<JFrame>();
	private ArrayList<JFrame> closedFrames = new ArrayList<JFrame>();
	
	// frozen is true between a call to hideAll() and restoreAll() to prevent  
	// any change to the list (notably to avoid unwanted feedback effect)
	private boolean frozen = false;
	
	private JFrameList() {
		super();
	}
	
	/*
	 * returns the instance of the singleton 
	 */
	public static JFrameList getInstance() {
		if (frameList == null) {
			frameList = new JFrameList();
		}
		return frameList;
	}
	
	public void addMain(JFrame f) {
		if (frozen || !(f instanceof JFrame) || (mainFrame != null)) return;
		
		mainFrame = f;

		f.addWindowListener(new WindowListener() {
			
			public void windowIconified(WindowEvent e) {
				JFrameList.this.masterIconified();
			}
			public void windowDeiconified(WindowEvent e) {
				JFrameList.this.masterDeiconified();
			}
			
			public void windowOpened(WindowEvent e) {}
			public void windowActivated(WindowEvent e) {}
			public void windowDeactivated(WindowEvent e) {}
			public void windowClosing(WindowEvent e) {}
			public void windowClosed(WindowEvent e) {}
		});
	}	
	
	public void add(JFrame f) {
		if (frozen || !(f instanceof JFrame) || (frames.indexOf(f) > -1)) return;
		
		frames.add(0,f);
		
		f.addWindowFocusListener(new WindowFocusListener() {
			public void windowGainedFocus(WindowEvent e) {
				JFrameList.this.showFrame((JFrame) e.getSource());
		    }
			public void windowLostFocus(WindowEvent e) {}
		});
		
		f.addWindowListener(new WindowListener() {
			
			public void windowIconified(WindowEvent e) {
				JFrameList.this.putFrameAtBottom((JFrame) e.getSource());
			}
			public void windowDeiconified(WindowEvent e) {
				JFrameList.this.putFrameOnTop((JFrame) e.getSource());
			}
			public void windowClosing(WindowEvent e) {				
				JFrameList.this.hideFrame((JFrame) e.getSource());
			}
			public void windowClosed(WindowEvent e) {
				JFrameList.this.removeFrame((JFrame) e.getSource());
			}	
			
			public void windowOpened(WindowEvent e) {}
			public void windowActivated(WindowEvent e) {}
			public void windowDeactivated(WindowEvent e) {}					
		});
	}	
	
	private void showFrame(JFrame f) {
    	if (frozen) return;
		if (frames.remove(f)) frames.add(0,f);
		else if (closedFrames.remove(f)) frames.add(0,f);
	}
	
	private void hideFrame(JFrame f) {
    	if (frozen) return;
    	frames.remove(f);
    	closedFrames.add(f);
	}
	
	private void removeFrame(JFrame f) {
    	if (! frames.remove(f)) closedFrames.add(f);
	}
	
	private void putFrameOnTop(JFrame f) {
		if (frozen) return;
		if (frames.remove(f)) frames.add(0,f);
	}
	
    private void putFrameAtBottom(JFrame f) {
    	if (frozen) return;
    	if (frames.remove(f)) frames.add(f);
	}
    
    private void masterIconified() {
    	if (frozen) return;
    	
    	hideAll();
    	
    	frozen = false;
    }
    
    private void masterDeiconified() {
    	if (frozen) return;
    	frozen = true;
    	
    	restoreAll();   	
    }
    
    public void hideAll() {
    	if (frozen) return;
    	frozen = true;
    	
    	for (JFrame f : frames) { 		      
    		f.setVisible(false);		
        }    	
    }
    
    private void setFramesVisible() {
    	for (int i = frames.size()-1; i>=0; i--) { 		    		
    		JFrame frame = frames.get(i);    		
    		if (frame instanceof AbstractPacketsAnalyzerDialog) {
    			// The packets analyzer dialog is only shown if it's not empty
    			if (((AbstractPacketsAnalyzerDialog)frame).isEmpty()) continue;
    		}
    		frames.get(i).setVisible(true);			
        }
    }
    
    public void restoreAll() {
    	if (! frozen) return;    	    	

    	setFramesVisible();    	
    	frozen = false;
    }
    
    public void putMasterInTheBackground() {
    	if (frozen || mainFrame == null) return;
    	
    	setFramesVisible();
    }
    
    public void closeAll() {
    	
        // Just hiding the windows (for the moment)
    	for (JFrame f : frames) { 		      
    		f.setVisible(false);		
        }  	
    	    	
//    	javax.swing.JOptionPane.showMessageDialog(null,frames.size()); 
//    	int count = frames.size() - 1;
//    	while (count >= 0) {
//    		JFrame f = frames.get(count); 
//    		if (f != mainFrame) f.dispose();
//    		count--;
//    	}
    	
    	
//    	for (JFrame f : frames) { 		      
//    		f.dispatchEvent(new WindowEvent(f, WindowEvent.WINDOW_CLOSING));		
//        }   
//    	Iterator<JFrame> i = frames.iterator();
//    	while (i.hasNext()) {
//    		JFrame f = i.next(); 
//    		if (f != mainFrame) f.dispatchEvent(new WindowEvent(f, WindowEvent.WINDOW_CLOSING));	
//    	}
    	//frames.clear();
    }
}

