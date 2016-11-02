package dip;

import com.sun.deploy.panel.JSmartTextArea;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;


public class Vision extends JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Vision() {

        initUI();
    }
	JLabel infoLabel, statusLabel;
	
	private void refresh() {
		infoLabel.setText("<html><h2>Info Section</h2>" + "File Name:" + selected.getName() + "<br>Data Model: " + img.getType() + "<br>"
   				+ img.getColorModel() + "<br>"
   				+ "Image Size: " + img.getWidth() + '*' + img.getHeight() + "</html>");
		statusLabel.setIcon(new ImageIcon(img));
	}
	
    private void initUI() {
        createMenuBar();
        
        setTitle("DIP tools");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        setLayout(new GridLayout(2, 1));
        infoLabel = new JLabel("", JLabel.CENTER);
        statusLabel = new JLabel("",JLabel.CENTER);
        statusLabel.setSize(800,800);
        add(statusLabel);
        add(infoLabel);
    }
    File selected;
    BufferedImage img;
    private void createMenuBar() {

        JMenuBar menubar = new JMenuBar();
        
        JMenu file = new JMenu("File");
        JMenu Op = new JMenu("Operation");
        
        JMenuItem openImage = new JMenuItem("Open Image");
        openImage.addActionListener((ActionEvent event) -> {
        	JFileChooser fc = new JFileChooser();
        	File workingDirectory = new File(System.getProperty("user.dir"));
        	fc.setCurrentDirectory(workingDirectory);
        	fc.setAcceptAllFileFilterUsed(false);
        	FileFilter filter = new ImageFilter();
        	fc.addChoosableFileFilter(filter);
        	int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
            	selected = fc.getSelectedFile();
            	try {
            	   img = ImageIO.read(selected);
            	   refresh();
               } catch(IOException e) {}
            }
        });
        file.add(openImage);
        
        JMenuItem saveImage = new JMenuItem("Save Image");
        saveImage.addActionListener((ActionEvent event) -> {
        	JFileChooser fc = new JFileChooser();
        	File workingDirectory = new File(System.getProperty("user.dir"));
        	fc.setCurrentDirectory(workingDirectory);
        	fc.setAcceptAllFileFilterUsed(false);
        	FileFilter filter = new ImageFilter();
        	fc.addChoosableFileFilter(filter);
        	int returnVal = fc.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
            	if (infoLabel.getText() == "") {
	        		infoLabel.setText("No Image Warning! Won't Do Anything.");
	        	} else {
	            	selected = fc.getSelectedFile();
	            	try {
	            	   ImageIO.write(img, "png", selected);
	            	   refresh();
	               } catch(IOException e) {}
	        	}
            }
        });
        file.add(saveImage);
        
        
        JMenuItem scale = new JMenuItem("Scale Image");
        scale.addActionListener((ActionEvent event)-> {
        	JTextField Width = new JTextField();
        	JTextField Height = new JTextField();
        	Object[] message = {
        	    "Width:", Width,
        	    "Height:", Height
        	};
        	int option = JOptionPane.showConfirmDialog(this, message, "Input", JOptionPane.OK_CANCEL_OPTION);
        	if (option == JOptionPane.OK_OPTION) {
        		if (infoLabel.getText() == "") {
	        		infoLabel.setText("No Image Warning! Won't Do Anything.");
	        	} else {
	        		img = Processor.scale(img, 
							Integer.parseInt(Width.getText()), 
							Integer.parseInt(Height.getText()));
	        		refresh();
	        	}
        	}
        });
        Op.add(scale);
        
        JMenuItem quantize = new JMenuItem("Quantize Image");
		quantize.addActionListener((ActionEvent event)-> {
			JTextField dep = new JTextField();
			Object[] message = {
					"Color Level:", dep
			};
			int option = JOptionPane.showConfirmDialog(this, message, "Input", JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.OK_OPTION) {
				if (infoLabel.getText() == "") {
					infoLabel.setText("No Image Warning! Won't Do Anything.");
				} else {
					img = Processor.quantize(img,
							Integer.parseInt(dep.getText()));
					refresh();
				}
			}
		});
		Op.add(quantize);

		JMenuItem equalize = new JMenuItem("Equalize Image");
		equalize.addActionListener((ActionEvent event)-> {

			if (infoLabel.getText() == "") {
				infoLabel.setText("No Image Warning! Won't Do Anything.");
			} else {
				img = Processor.equalize(img);
				refresh();
			}
		});
		Op.add(equalize);

		JMenuItem conv = new JMenuItem("Convolution");
		conv.addActionListener((ActionEvent event)-> {
			JTextArea mat = new JTextArea();
			JScrollPane pane = new JScrollPane(mat);
			UIManager.put("OptionPane.minimumSize",new Dimension(500,500));
			Object[] message = {
					"Matrix:", pane
			};
			int option = JOptionPane.showConfirmDialog(this, message, "Input", JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.OK_OPTION) {
				if (infoLabel.getText() == "") {
					infoLabel.setText("No Image Warning! Won't Do Anything.");
				} else {
					String input = mat.getText();
					int[] arr = Arrays.stream(input.split(" |\\n")).map(String::trim).mapToInt(Integer::parseInt).toArray();
					int fm = arr[0];
					int fn = arr[1];
					int[] line = Arrays.copyOfRange(arr, 2, arr.length);
					int[][] f = new int[fm][fn];
					for (int i = 0; i < fm; i ++) {
						for (int j = 0; j < fn; j ++)
							f[i][j] = line[i * fn + j];
					}
					System.out.println(fm + " " + fn);
					for (int i = 0; i < fm; i ++) {
						for (int j = 0; j < fn; j++)
							System.out.print(f[i][j]);
						System.out.println();
					}
					img = Processor.conv(img, fm, fn, f);
					refresh();
				}
			}
		});
		Op.add(conv);
        
        menubar.add(file);
        menubar.add(Op);
        
        setJMenuBar(menubar);
    }
    
    public static void main(String[] args) {
    	EventQueue.invokeLater(() -> {
            Vision vi = new Vision();
            vi.setVisible(true);
        });
        
    }
}
