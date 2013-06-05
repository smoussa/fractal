
import java.applet.Applet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JFrame;

@SuppressWarnings("serial")
public class Bouncing extends Applet implements Runnable {
	
	private int xpos = 0;
	private int ypos = 0;
	private int xdirection = 1;
	private int ydirection = 1;
	private int time = 3;
	
	private JFrame frame = new JFrame(); // creates the window
	private Dimension dim = new Dimension(500, 400); // the dimension of the screen
	private Image image; // the image that will contain everything that has been drawn on bufferGraphics
	private Graphics bufferGraphics; // will be using this instead of the standard screen graphics
	private Thread thread = new Thread(this); // thread is used to perform actions over and over again
	
	public void init() {
		
		// sets the properties of the applet
		frame.setSize(dim);
		this.setSize(dim);
		this.setBackground(Color.BLACK);
		
		// creates an off screen image to draw on and everything is drawn on by bufferGraphics
		image = createImage(dim.width, dim.height);
		bufferGraphics = image.getGraphics();
		thread.start();
		
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		
		bufferGraphics.clearRect(0, 0, dim.width, dim.width); // clears the rectangle
		
		bufferGraphics.setFont(new Font("Trebucht MS",Font.BOLD,16)); // draws the following objects
		bufferGraphics.setColor(Color.red);
		
		//bufferGraphics.drawString("MOVING TEXT", xpos, ypos);
		bufferGraphics.fillArc(xpos, ypos, 30, 30, 0, 360); // Here is a circle
		
		// the following code changes the position the object moving on the screen
		if (xpos > dim.width || xpos < 0) {
			xdirection *= -1;
		}
		if (ypos > dim.height || ypos < 0) {
			ydirection *= -1;
		}
		xpos += xdirection;
		ypos += ydirection;
		
		// then draws image
		g.drawImage(image, 0, 0, this); 
		
	}
	
	public void update(Graphics g) { 
         paint(g);
    } 

	@Override
	public void run() {
		
		// the thread is used to change the speed of the moving object
		while (thread != null){
			repaint();
			try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
			}
		}
		
	}
	
}
