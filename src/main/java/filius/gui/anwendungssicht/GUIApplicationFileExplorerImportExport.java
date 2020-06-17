package filius.gui.anwendungssicht;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.MouseInputAdapter;

import filius.Main;
import filius.software.lokal.FileExplorer;

public class GUIApplicationFileExplorerImportDlg {
	
//    private String file;
//    private String path;
//    private JInternalFrame fileImportFrame;
	
	
//	public void importFile() {
//        fileImportFrame = new JInternalFrame(messages.getString("fileexplorer_msg12"));
//
//        ImageIcon image = new ImageIcon(getClass().getResource("/gfx/desktop/icon_fileimporter.png"));
//        image.setImage(image.getImage().getScaledInstance(16, 16, Image.SCALE_AREA_AVERAGING));
//        setFrameIcon(image);
//
//        backPanel = new JPanel(new BorderLayout());
//
//        final JTextArea outputField = new JTextArea("");
//        outputField.setEditable(false);
//        outputField.setSize(new Dimension(300, 80));
//        JLabel fileLabel = new JLabel(messages.getString("fileexplorer_msg13"));
//
//        final JTextField inputField = new JTextField("");
//        inputField.setSize(new Dimension(150, 30));
//        inputField.setEditable(false);
//
//        final JTextField renameField = new JTextField("");
//        renameField.setSize(new Dimension(150, 30));
//        JLabel renameLabel = new JLabel(messages.getString("fileexplorer_msg9"));
//
//        JButton fileButton = new JButton(messages.getString("fileexplorer_msg14"));
//        fileButton.setSize(new Dimension(100, 30));
//        fileButton.addMouseListener(new MouseInputAdapter() {
//            @Override
//            public void mousePressed(MouseEvent e) {
//                JFileChooser fc = new JFileChooser();
//                if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
//                    file = fc.getSelectedFile().getName();
//                    path = fc.getSelectedFile().getParent();
//                    if (!path.endsWith(System.getProperty("file.separator")))
//                        path += System.getProperty("file.separator");
//                }
//
//                if (file != null) {
//                    inputField.setText(path + file);
//                    renameField.setText(file);
//                }
//
//                try {
//                    fileImportFrame.setSelected(true);
//                } catch (PropertyVetoException e1) {
//                    e1.printStackTrace(Main.debug);
//                }
//            }
//        });
//
//        Box importBox = Box.createHorizontalBox();
//        importBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//        importBox.add(fileLabel);
//        importBox.add(Box.createHorizontalStrut(5));
//        importBox.add(inputField);
//        importBox.add(Box.createHorizontalStrut(5));
//        importBox.add(fileButton);
//
//        Box middleBox = Box.createHorizontalBox();
//
//        middleBox.add(renameLabel);
//        middleBox.add(Box.createHorizontalStrut(5));
//        middleBox.add(renameField);
//        middleBox.add(Box.createHorizontalStrut(5));
//
//        Box upperBox = Box.createVerticalBox();
//        upperBox.add(importBox);
//        upperBox.add(middleBox);
//
//        backPanel.add(upperBox, BorderLayout.NORTH);
//
//        JButton importButton = new JButton(messages.getString("fileexplorer_msg15"));
//        importButton.setSize(new Dimension(100, 30));
//        importButton.addMouseListener(new MouseInputAdapter() {
//            @Override
//            public void mousePressed(MouseEvent z) {
//                if (inputField.getText().equals("") || renameField.getText().equals("")) {
//                    outputField.setText(messages.getString("fileexplorer_msg16"));
//                } else {
//
//                    if (currentDir == null) {
//                        outputField.setText(messages.getString("fileexplorer_msg17"));
//                    } else {
//                        outputField.setText(((FileExplorer) holeAnwendung()).addFile(path, file, currentDir,
//                                renameField.getText()));
//                        refresh();
//                    }
//                }
//            }
//        });
//
//        Box lowerBox = Box.createHorizontalBox();
//        lowerBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//        lowerBox.add(importButton);
//
//        backPanel.add(outputField, BorderLayout.CENTER);
//        backPanel.add(lowerBox, BorderLayout.SOUTH);
//
//        fileImportFrame.getContentPane().add(backPanel);
//
//        fileImportFrame.setClosable(true);
//        fileImportFrame.setResizable(false);
//        fileImportFrame.setBounds(30, 80, 350, 200);
//        fileImportFrame.setVisible(true);
//
//        addFrame(fileImportFrame);
//        try {
//            fileImportFrame.setSelected(true);
//        } catch (PropertyVetoException e1) {
//            e1.printStackTrace(Main.debug);
//        }
//    }


}
