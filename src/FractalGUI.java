 
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/*
 * 
 */

class ComplexNumber {
	
	private double real;
	private double imaginary;
	
	/**
	 * Creates a new complex number with the given real and imaginary values.
	 * @param real The real value.
	 * @param imaginary The imaginary value.
	 */
	public ComplexNumber(double real, double imaginary) {
		this.real = real;
		this.imaginary = imaginary;
	}
	
	/**
	 * Returns the real part of this complex number.
	 * @return The real part.
	 */
	public double getRealPart() {
		return real;
	}
	
	/**
	 * Returns the imaginary part of this complex number.
	 * @return The imaginary part.
	 */
	public double getImaginaryPart() {
		return imaginary;
	}
	
	/**
	 * Squares this complex number.
	 */
	public void square() {
		double temp = real;
		real = (real * real) - (imaginary * imaginary);
		imaginary = 2 * temp * imaginary;
	}
	
	/**
	 * Changes the real and imaginary parts of this complex number into their absolute values.
	 */
	public void absolute() { // This method is used when calculating the BurningShip fractal.
		if (real < 0)
			real *= -1;
		if (imaginary < 0)
			imaginary *= -1;
	}
	
	/**
	 * Returns the modulus squared of this complex number.
	 * @return The value of the complex number's modulus squared.
	 */
	public double modulusSquared() {
		return (imaginary * imaginary) + (real * real);
	}
	
	/**
	 * Adds this complex number to the complex number passed as the parameter.
	 * @param d The complex number to add.
	 */
	public void add(ComplexNumber d) {
		real = real + d.getRealPart();
		imaginary = imaginary + d.getImaginaryPart();
	}
	
}

/*
 * 
 */

class Fractal {
	
	protected ComplexNumber z;
	protected ComplexNumber c;
	
	/**
	 * Constructs a new Fractal with the given constant real x and imaginary y values.
	 * @param cx The real value.
	 * @param cy The imaginary value.
	 */
	public Fractal(double cx, double cy) {
		c = new ComplexNumber(cx, cy);
	}
	
	/**
	 * Calculates the fractal formula set by returning the number of iterations before the set diverges.
	 * @param max The maximum number of iterations.
	 * @return The number of iterations before the set diverges.
	 */
	public int calculate(int max) {
		return 0;
	}
	
}

/*
 * 
 */

class MandelbrotSet extends Fractal {
	
	public MandelbrotSet(double cx, double cy) {
		super(cx, cy);
		z = new ComplexNumber(0, 0);
	}

	@Override
	public int calculate(int max) {
		int i = 0;
		while (z.modulusSquared() < 4.0 && i < max) {
			z.square();
			z.add(c);
			i++;
		}
		
		return max - i;
	}
	
}

/*
 * 
 */

class BurningShip extends Fractal {

	public BurningShip(double cx, double cy) {
		super(cx, cy);
		z = new ComplexNumber(0, 0);
	}

	@Override
	public int calculate(int max) {
		int i = 0;
		while (z.modulusSquared() < 4.0 && i < max) {
			z.absolute();
			z.square();
			z.add(c);
			i++;
		}
		
		return max - i;
	}
	
}

/*
 * 
 */

class JuliaSet extends Fractal {
	
	public JuliaSet(double cx, double cy, double dx, double dy) {
		super(cx, cy);
		z = new ComplexNumber(dx, dy);
	}
	
	public int calculate(int fractalIndex, int max) {
		int i = 0;
		switch (fractalIndex) {
			case 0:
				while (z.modulusSquared() < 4.0 && i < max) {
					z.square();
					z.add(c);
					i++;
				}
			break;
			case 1:
				while (z.modulusSquared() < 4.0 && i < max) {
					z.absolute();
					z.square();
					z.add(c);
					i++;
				}
			break;
			default:
				while (z.modulusSquared() < 4.0 && i < max) {
					z.square();
					z.add(c);
					i++;
				}
			break;
		}
		
		return max - i;
	}
	
}

/*
 * 
 */

class FractalDrawer {
	
	public static double minX;
	public static double maxX;
	public static double minY;
	public static double maxY;
	public static int maxIterations;
	
	private static ComplexNumber userSelectedPoint;
	
	private JPanel panel;
	private BufferedImage image;
	
	/**
	 * Constructs a new FractalDrawer using the given panel to draw a fractal.
	 * @param p The panel on which to draw the fractal.
	 */
	public FractalDrawer(JPanel p) {
		panel = p;
		minX = DisplayPanel.PreferencesPanel.getMinX();
		maxX = DisplayPanel.PreferencesPanel.getMaxX();
		minY = DisplayPanel.PreferencesPanel.getMinY();
		maxY = DisplayPanel.PreferencesPanel.getMaxY();
		maxIterations = DisplayPanel.PreferencesPanel.getMaxIterations();
	}
	
	/**
	 * Sets the user selected point with the required x and y value.
	 * @param x The real part of the user selected complex number.
	 * @param y The real imaginary of the user selected complex number.
	 */
	public void setUserSelectedPoint(double x, double y) {
		userSelectedPoint = new ComplexNumber(x, y);
	}
	
	/**
	 * Gets the x coordinate relative to the current fractal image.
	 * @param x The x coordinate the of the point clicked.
	 * @return The x coordinate relative to the current fractal image.
	 */
	public double getRelativeX(int x) {
		return (((maxX - minX) / 700) * x) - maxX;
	}
	
	/**
	 * Gets the y coordinate relative to the current fractal image.
	 * @param y The y coordinate the of the point clicked.
	 * @return The y coordinate relative to the current fractal image.
	 */
	public double getRelativeY(int y) {
		return (((maxY - minY) / 550) * y) - maxY;
	}
	
	/**
	 * Resizes and scales the image when zooming using the values passed in as parameters.
	 * @param x1 The x location of the zoom square.
	 * @param y1 The y location of the zoom square.
	 * @param width The width of the zoom square.
	 * @param height The height of the zoom square.
	 */
	public void setZoom(int x1, int y1, int width, int height) {
		image = new BufferedImage(700, 550, BufferedImage.TYPE_INT_RGB);
		double tx1 = minX;
		double tx2 = maxX;
		double ty1 = minY;
		double ty2 = maxY;
		minX = tx2 - ((x1 + width) * (tx2 - tx1) / image.getWidth());
		maxX = tx2 - (x1 * (tx2 - tx1) / image.getWidth());
		minY = ty2 - ((y1 + height) * (ty2 - ty1) / image.getHeight());
		maxY = ty2 - (y1 * (ty2 - ty1) / image.getHeight());
	}
	
	/**
	 * Draws a new fratcal image according the fractal index.
	 * @param index 0 - Mandelbrot Set.
	 * 				1 - BurningShip.
	 * @return The fractal image.
	 */
	public BufferedImage drawFractal(int index) {
		image = new BufferedImage(700, 550, BufferedImage.TYPE_INT_RGB);
		ExecutorService pool = Executors.newCachedThreadPool();
		int noOfSections = image.getWidth() / 2;
		switch (index) {
			case 0:
				for (int currentSection = 1; currentSection <= noOfSections; currentSection++) {
					pool.execute(new MandelbrotSetSectionDrawer(currentSection)); // Multiple threads are created to draw each section.
				}
			break;
			case 1:
				for (int currentSection = 1; currentSection <= noOfSections; currentSection++) {
					pool.execute(new BurningShipDrawer(currentSection));
				}
			break;
			default:
				for (int currentSection = 1; currentSection <= noOfSections; currentSection++) {
					pool.execute(new MandelbrotSetSectionDrawer(currentSection));
				}
			break;
		}
		pool.shutdown();
		return image;
	}
	
	/**
	 * Draws a new Julia fractal image.
	 * @param index The main fractal index to which the Julia image will be accorded to.
	 * @return The Julia image.
	 */
	public BufferedImage drawJulia(int index) {
		image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
		new JuliaSetDrawer().draw(index);
		return image;
	}
	
	abstract class SectionDrawer implements Runnable {
		
		protected int s;
		
		/**
		 * Constructs a new drawer for the fractal image which draws the section number specified in the argument.
		 * @param sectionNumber The section number.
		 */
		public SectionDrawer(int sectionNumber) {
			s = sectionNumber;
		}
		
		@Override
		public abstract void run();
		
	}
	
	class MandelbrotSetSectionDrawer extends SectionDrawer {
		
		public MandelbrotSetSectionDrawer(int sectionNumber) {
			super(sectionNumber);
		}

		@Override
		public void run() {
			Graphics graphics = image.createGraphics(); // creates a new graphics object for each section.
			for (int x = 2 * (s - 1); x <= (2 * s); x++) { // loop through the pixels horizontally.
				double cx = getRelativeX(x);
				for (int y = 0; y < image.getHeight(); y++) { // loop through the pixels vertically.
					double cy = getRelativeY(y);
					Fractal mandel = new MandelbrotSet(cx, cy); // create a new Mandelbrot set.
					int result = mandel.calculate(maxIterations); // calculate.
					graphics.setColor(Color.getHSBColor(result/300.0F, result/50.0F, result/1.0F)); // choose a colour based on the 
					graphics.drawLine(x, y, x, y);
				}
				panel.repaint();
	    	}
		}
		
	}
	
	class BurningShipDrawer extends SectionDrawer {
		
		public BurningShipDrawer(int sectionNumber) {
			super(sectionNumber);
		}
		
		@Override
		public void run() {
			Graphics graphics = image.createGraphics();
			for (int x = 2 * (s - 1); x <= (2 * s); x++) {
				double cx = (((maxX - minX) / image.getWidth()) * x) - maxX;
				for (int y = 0; y < image.getHeight(); y++) {
					double cy = (((maxY - minY) / image.getHeight()) * y) - maxY;
					Fractal ship = new BurningShip(cx, cy);
					int result = ship.calculate(maxIterations);
					graphics.setColor(Color.getHSBColor(result/200.0F, result/50.0F, result/1.0F));
					graphics.drawLine(x, y, x, y);
				}
				panel.repaint();
	    	}
		}
		
	}
	
	class JuliaSetDrawer {
		
		/**
		 * Draws a new Julia image based on the user selected point.
		 * @param index The index of the main fractal image it is specified to.
		 */
		public void draw(int index) {
			Graphics graphics = image.createGraphics();
			for (int x = 0; x < image.getWidth(); x++) {
				double dx = ((4.0 / image.getWidth()) * x) - 2.0;
				for (int y = 0; y < image.getHeight(); y++) {
					double dy = ((3.2 / image.getHeight()) * y) - 1.6;
					JuliaSet mandel = new JuliaSet(userSelectedPoint.getRealPart(), userSelectedPoint.getImaginaryPart(), dx, dy);
					int result = mandel.calculate(index, maxIterations);
					graphics.setColor(Color.getHSBColor(result/100.0F, result/80.0F, result/99.0F));
					graphics.drawLine(x, y, x, y);
				}
				panel.repaint();
			}
		}
		
	}
	
}

/*
 * 
 */

public class FractalGUI extends JFrame {
	
	public static void main(String[] args) {
		new FractalGUI("Fractal Application");
	}
	
	/**
	 * Constructs a new GUI fractal application.
	 * @param title The title of the window.
	 */
	public FractalGUI(String title) {
		super(title);
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println("Failed to load system LookAndFeel.");
		}
		
		setContentPane(new DisplayPanel());
		
        JMenuBar menuBar = new JMenuBar(); // Menu bar
        setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("File");
        JMenuItem favItem = new JMenuItem("Favourites");
        JMenuItem exitItem = new JMenuItem("Exit");
        fileMenu.add(favItem);
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        
        favItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new FavouritesGUI("Favourites");
			}
		});
        
        exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
        
        deleteOldImages();
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize(); // center the window to the screen.
		int frameWidth = 1400;
		int frameHeight = 800;
		setBounds((int) (dim.getWidth() - frameWidth) / 2,
					(int) (dim.getHeight() - frameHeight) / 2,
						frameWidth, frameHeight);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		
	}
	
	/**
	 * Deletes old images in the docs folder to be replaced by new ones.
	 */
	public void deleteOldImages() {
		
		File directory = new File("docs/");
		directory.mkdir();
		File files[] = directory.listFiles();
		
		for (File f : files) {
			String fileName = f.getName();
			int dotIndex = fileName.lastIndexOf('.');
			int lastIndex = fileName.length();
			if (dotIndex > 0) {
				String extension = fileName.substring(dotIndex + 1, lastIndex);
				if (extension.equals("jpg")) {
					f.delete();
				}
			}
		}
		
	}

}

/*
 * 
 */

class DisplayPanel extends JPanel {
	
	private static PreferencesPanel prefPanel;
	private static MainFractalPanel mainPanel;
	private static JuliaSetPanel juliaPanel;

	private static boolean inZoomMode = false;
	
	private static int fractalIndex = 0;
	
	/**
	 * Constructs a new panel to house all other sub panels.
	 */
	public DisplayPanel() {
		
		setBackground(new Color(20, 20, 20));
		
		prefPanel = new PreferencesPanel();
		mainPanel = new MainFractalPanel(700, 550);
		juliaPanel = new JuliaSetPanel(400, 300);
		
		add(mainPanel, BorderLayout.CENTER);
		add(juliaPanel, BorderLayout.EAST);
		add(prefPanel, BorderLayout.SOUTH);
		
	}
	
	/**
	 * Draws the fractal which is currently chosen to be drawn.
	 * @param drawer The new fractal drawer object.
	 */
	public static void showChosenFractal(FractalDrawer drawer) {
		switch (fractalIndex) {
			case 0: mainPanel.setImage(drawer.drawFractal(0));
			break;
			case 1: mainPanel.setImage(drawer.drawFractal(1));
			break;
			default: mainPanel.setImage(drawer.drawFractal(0));
			break;
		}
	}
	
	static class PreferencesPanel extends JPanel {
		
		private static JLabel minXLabel = new JLabel("Min");
		private static JTextField minXField = new JTextField(8);
		private static JLabel maxXLabel = new JLabel("Max");
		private static JTextField maxXField = new JTextField(8);
		private static JLabel minYLabel = new JLabel("Min");
		private static JTextField minYField = new JTextField(8);
		private static JLabel maxYLabel = new JLabel("Max");
		private static JTextField maxYField = new JTextField(8);
		private static JTextField iterField = new JTextField(8);
		
		private static String[] fractals = {"Mandelbrot Set", "Burning Ship"};
		private static JComboBox fractalComboBox = new JComboBox(fractals);
		
		private static JButton drawButton = new JButton(" DRAW ");
		private static JButton resetButton = new JButton(" RESET ");
		private static JButton modeToggle = new JButton("Zoom Mode");
		
		/**
		 * Constructs a new panel to house all the components which will manipulate the fractal to be drawn.
		 */
		public PreferencesPanel() {
			
			setBorder(new EmptyBorder(5, 0, 0, 0));
			setBackground(new Color(200, 200, 200));
			setLayout(new FlowLayout(FlowLayout.CENTER));
			
			fractalComboBox.setBorder(BorderFactory.createEmptyBorder());
			fractalComboBox.setOpaque(false);
			fractalComboBox.setBorder(new EmptyBorder(2, 2, 2, 2));
			fractalComboBox.setMaximumRowCount(4);
			
			fractalComboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					fractalIndex = fractalComboBox.getSelectedIndex();
					FractalDrawer drawer = new FractalDrawer(mainPanel);
					showChosenFractal(drawer);
				}
			});
			
			modeToggle.setOpaque(false);
			modeToggle.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					inZoomMode = !inZoomMode;
					if (inZoomMode == true) {
						modeToggle.setText("Julia Mode");
					} else {
						modeToggle.setText("Zoom Mode");
					}
				}
			});
			
			drawButton.setOpaque(false);
			drawButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					FractalDrawer drawer = new FractalDrawer(mainPanel);
					showChosenFractal(drawer);
				}
			});
			
			resetButton.setOpaque(false);
			resetButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					resetValues();
					FractalDrawer drawer = new FractalDrawer(mainPanel);
					showChosenFractal(drawer);
				}
			});
			
			TitledBorder title;
			Border raisedBorder = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
			
			JPanel xPanel = new JPanel(); // creating smaller bordered panels for clearer layout.
			xPanel.setLayout(new FlowLayout());
			title = BorderFactory.createTitledBorder(raisedBorder, " X-Coordinates ");
			xPanel.setBorder(title);
			xPanel.setOpaque(false);
			add(xPanel);
			xPanel.add(minXLabel);
			xPanel.add(minXField);
			addGap(xPanel);
			xPanel.add(maxXLabel);
			xPanel.add(maxXField);
			addGap(this);
			
			JPanel yPanel = new JPanel();
			yPanel.setLayout(new FlowLayout());
			title = BorderFactory.createTitledBorder(raisedBorder, " Y-Coordinates ");
			yPanel.setBorder(title);
			yPanel.setOpaque(false);
			add(yPanel);
			yPanel.add(minYLabel);
			yPanel.add(minYField);
			addGap(yPanel);
			yPanel.add(maxYLabel);
			yPanel.add(maxYField);
			addGap(this);
			
			JPanel iPanel = new JPanel();
			iPanel.setLayout(new FlowLayout());
			title = BorderFactory.createTitledBorder(raisedBorder, " Iterations ");
			iPanel.setBorder(title);
			iPanel.setOpaque(false);
			add(iPanel);
			iPanel.add(iterField);
			addGap(this);
			
			JPanel fPanel = new JPanel();
			fPanel.setLayout(new FlowLayout());
			title = BorderFactory.createTitledBorder(raisedBorder, " Fractal ");
			fPanel.setBorder(title);
			fPanel.setOpaque(false);
			add(fPanel);
			fPanel.add(fractalComboBox);
			addGap(this);
			
			JPanel mPanel = new JPanel();
			mPanel.setLayout(new FlowLayout());
			title = BorderFactory.createTitledBorder(raisedBorder, " Mode ");
			mPanel.setBorder(title);
			mPanel.setOpaque(false);
			add(mPanel);
			mPanel.add(modeToggle);
			addGap(this);
			
			JPanel dPanel = new JPanel();
			dPanel.setLayout(new FlowLayout());
			title = BorderFactory.createTitledBorder(raisedBorder, " Draw ");
			dPanel.setBorder(title);
			dPanel.setOpaque(false);
			add(dPanel);
			dPanel.add(drawButton);
			dPanel.add(resetButton);
			
			resetValues();
			
		}
		
		/**
		 * Adds a gap between components.
		 * @param p The panel to add the gap in.
		 */
		public void addGap(JPanel p) {
			p.add(Box.createRigidArea(new Dimension(15, 0)));
		}
		
		/**
		 * Resets all the values to the default values.
		 */
		public void resetValues() {
			setValues(-2.0, 2.0, -1.6, 1.6, 100);
		}
		
		/**
		 * Sets the text fields with the values passed in as parameters.
		 * @param minX The minimum x value.
		 * @param maxX The maximum x value.
		 * @param minY The minimum y value.
		 * @param maxY The maximum y value.
		 * @param max The maximum number of iterations.
		 */
		public void setValues(double minX, double maxX, double minY, double maxY, int max) {
			minXField.setText(String.valueOf(round(minX)));
			maxXField.setText(String.valueOf(round(maxX)));
			minYField.setText(String.valueOf(round(minY)));
			maxYField.setText(String.valueOf(round(maxY)));
			iterField.setText(String.valueOf(max));
		}
		
		/**
		 * Gets the value in the text field.
		 * @return The value in the field.
		 */
		public static double getMinX() {
			return Double.valueOf(minXField.getText());
		}
		
		/**
		 * Gets the value in the text field.
		 * @return The value in the field.
		 */
		public static double getMaxX() {
			return Double.valueOf(maxXField.getText());
		}
		
		/**
		 * Gets the value in the text field.
		 * @return The value in the field.
		 */
		public static double getMinY() {
			return Double.valueOf(minYField.getText());
		}
		
		/**
		 * Gets the value in the text field.
		 * @return The value in the field.
		 */
		public static double getMaxY() {
			return Double.valueOf(maxYField.getText());
		}
		
		/**
		 * Gets the value in the text field.
		 * @return The value in the field.
		 */
		public static int getMaxIterations() {
			return Integer.valueOf(iterField.getText());
		}
		
		/**
		 * Rounds the value in the field to 8 decimal places.
		 * @param d The value to round.
		 * @return The rounded value.
		 */
		public double round(double d) {
			DecimalFormat decimal = new DecimalFormat("#.########");
			return Double.valueOf(decimal.format(d));
		}
		
	}
	
	/*
	 * 
	 */
	
	class MainFractalPanel extends JPanel {
		
		private Dimension dim;
		private BufferedImage image;
		private boolean dragging = false;
		private int width = 1;
		private int height = 1;
		private int x1 = 0;
		private int y1 = 0;
		
		/**
		 * Constructs a new main fractal panel to which fractals will be drawn in.
		 * @param w The width of the panel.
		 * @param h The height of the panel.
		 */
		public MainFractalPanel(int w, int h) {
			
			dim = new Dimension(w, h);
			setPreferredSize(dim);
			
			FractalDrawer drawer = new FractalDrawer(this); // draw a fractal when it first starts up.
			setImage(drawer.drawFractal(0));
			
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (!inZoomMode) {
						FractalDrawer drawer = new FractalDrawer(juliaPanel);
						double ux = prefPanel.round(drawer.getRelativeX(e.getX()));
	    				double uy = prefPanel.round(drawer.getRelativeY(e.getY()));
						drawer.setUserSelectedPoint(ux, uy);
						juliaPanel.setImage(drawer.drawJulia(fractalIndex));
						juliaPanel.repaint();
						juliaPanel.label = "Now drag..." + "     " + ux + " + " + uy + "i";
					}
				}
				@Override
				public void mousePressed(MouseEvent e) {
					dragging = true;
					if (inZoomMode) {
						x1 = e.getX();
						y1 = e.getY();
					} else {
						juliaPanel.label = "Now drag...";
						juliaPanel.repaint();
					}
				}
				@Override
				public void mouseReleased(MouseEvent e) {
					juliaPanel.label = "Click to add this shot to my favourites.";
					juliaPanel.repaint();
					dragging = false;
					if (inZoomMode && height > 1) { // checks whether the user wants to zoom and the height of the zoom box is more than one pixel.
						FractalDrawer drawer = new FractalDrawer(mainPanel);
						drawer.setZoom(x1, y1, width, height);
						showChosenFractal(drawer);
						prefPanel.setValues(FractalDrawer.minX, FractalDrawer.maxX, FractalDrawer.minY, FractalDrawer.maxY, FractalDrawer.maxIterations);
					}
					width = 1;
					height = 1;
				}
			});
			
	        addMouseMotionListener(new MouseMotionAdapter() {
	        	@Override
	    		public void mouseDragged(MouseEvent e) {
	        		dragging = true;
	    			if (inZoomMode) {
	    				width = Math.abs(x1 - e.getX());
	    				height = width * dim.height / dim.width;
	    				repaint();
	    			} else {
	    				FractalDrawer drawer = new FractalDrawer(juliaPanel);
	    				double ux = prefPanel.round(drawer.getRelativeX(e.getX()));
	    				double uy = prefPanel.round(drawer.getRelativeY(e.getY()));
						drawer.setUserSelectedPoint(ux, uy);
						juliaPanel.setImage(drawer.drawJulia(fractalIndex));
						juliaPanel.label = "Dragging..." + "     " + ux + " + " + uy + "i";
	    			}
	    		}
	        });
	        
		}
		
		/**
		 * Sets the image in the panel to the specified image.
		 * @param i The image to set.
		 */
		public void setImage(BufferedImage i) {
			image = i;
		}
		
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			g.drawImage(image, 0, 0, this);
			if (inZoomMode && dragging) {
				g.setColor(Color.white);
				g.drawRect(x1, y1, width, height);
			}
		}
		
	}
	
	/*
	 * 
	 */
	
	class JuliaSetPanel extends JPanel {
		
		private BufferedImage image;
		private int imageNumber = 0;
		private String label = "Click to add this shot to my favourites.";
		
		/**
		 * Constructs a new Julia fractal panel to which Julia images will be drawn in.
		 * @param w The width of the panel.
		 * @param h The height of the panel.
		 */
		public JuliaSetPanel(int w, int h) {
			
			Dimension dim = new Dimension(w, h);
			setPreferredSize(dim);
			
			FractalDrawer drawer = new FractalDrawer(this);
			drawer.setUserSelectedPoint(0, 0);
			setImage(drawer.drawJulia(fractalIndex));
			
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					label = "Added to favourites.";
					saveImage();
					repaint();
				}
			});
			
		}
		
		/**
		 * Sets the image in the panel to the specified image.
		 * @param i The image to set.
		 */
		public void setImage(BufferedImage i) {
			image = i;
		}
		
		/**
		 * Saves the clicked image as a favourite in a new folder called 'docs'.
		 */
		public void saveImage() {
			String fileName = "docs/julia" + imageNumber;
	        File file = new File(fileName + ".jpg");
	        try {
	            ImageIO.write(image, "jpg", file);
	        } catch(IOException e) {
	            System.out.println("Write error for " + file.getPath() + ": " + e.getMessage());
	        }
	        imageNumber++;
	        if (imageNumber == 9) // Allows only up to 9 images.
	        	imageNumber = 0;
		}
		
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			g.drawImage(image, 0, 0, this);
			g.drawString(label, 10, 20);
		}
		
	}
	
}

/*
 * 
 */

class FavouritesGUI extends JFrame {

	/**
	 * Constructs a new favourites GUI window to show all favourite Julia images.
	 * @param title
	 */
	public FavouritesGUI(String title) {
		super(title);
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println("Failed to load system LookAndFeel.");
		}
		
		setContentPane(new FavouritesPanel(600, 450));
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int frameWidth = 815;
		int frameHeight = 332;
		setBounds((int) (dim.getWidth() - frameWidth) / 2,
					(int) (dim.getHeight() - frameHeight) / 2,
						frameWidth, frameHeight);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
		
	}
	
}

/*
 * 
 */

class FavouritesPanel extends JPanel {
	
	private Dimension panel;
	private ImagePanel imagePanel;
	private ListPanel listPanel;
	private static List<BufferedImage> imageList;
	private BufferedImage previewImage;
	
	/**
	 * Constructs a new favourites panel which will house other sub panels.
	 * @param w The width of the panel.
	 * @param h The height of the panel.
	 */
	public FavouritesPanel(int w, int h) {
		
		panel = new Dimension(w, h);
		setPreferredSize(panel);
		setLayout(new GridLayout(1, 2, 3, 0));
		setBorder(new EmptyBorder(3, 3, 3, 3));
		setBackground(Color.black);
		
		imageList = new ArrayList<BufferedImage>();
		
		imagePanel = new ImagePanel();
		add(imagePanel);
		
		listPanel = new ListPanel();
		add(listPanel);
		
	}
	
	/**
	 * Gets all the favourite images and adds them to the image list.
	 */
	public void getImages() {
		
		File directory = new File("docs/");
		File files[] = directory.listFiles();
		
		for (File f : files) {
			String fileName = f.getName();
			int dotIndex = fileName.lastIndexOf('.');
			int lastIndex = fileName.length();
			if (dotIndex > 0) {
				String extension = fileName.substring(dotIndex + 1, lastIndex);
				if (extension.equals("jpg")) {
					try {
						imageList.add(ImageIO.read(f));
					} catch (IOException e) {
						System.out.println("Failed to read the file " + f);
					}
				}
			}
		}
		
	}
	
	class ImagePanel extends JPanel {
		
		/**
		 * Constructs a new panel to hold the image the user wants to see.
		 */
		public ImagePanel() {
			setBackground(Color.black);
		}
		
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			g.drawImage(previewImage, 0, 0, this);
		}
		
	}
	
	class ListPanel extends JPanel {
		
		/**
		 * Constructs a new panel to list all the images saved as favourites.
		 */
		public ListPanel() {
			GridLayout listLayout = new GridLayout(3, 0, 2, 2);
			setLayout(listLayout);
			setBackground(Color.black);
			getImages();
			showImages();
		}
		
		/**
		 * Shows the images as a list in this panel.
		 */
		public void showImages() {
			int i = 0;
			Iterator<BufferedImage> itr = imageList.iterator();
			while (itr.hasNext()) {
				JLabel image = new JLabel(new ImageIcon(itr.next()));
				image.addMouseListener(new FavouritesImageListMouseListener(i));
				add(image);
				i++;
			}
		}
		
		class FavouritesImageListMouseListener extends MouseAdapter {
			
			private int index;
			
			/**
			 * Constructs an image listener which listens to which image the user wants to see.
			 * @param i
			 */
			public FavouritesImageListMouseListener(int i) {
				index = i;
			}
			
			@Override
		    public void mousePressed(MouseEvent e) {
				previewImage = imageList.get(index);
				imagePanel.repaint();
		    }
			
		}
		
	}
	
}

/*
 * 
 */
