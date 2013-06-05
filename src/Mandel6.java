/*<applet code="Mandel6.class" width=297 height=297></applet>*/
import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;

public final class Mandel6 extends Applet implements MouseListener, MouseMotionListener, KeyListener, Runnable {
  private int maxCount = 1000; // maximum number of iterations
  private int fractal = 0; // current fractal
  private boolean smooth = false; // smoothing state
  private boolean antialias = false; // antialias state
  private boolean toDrag = false; // dragging state
  private boolean rect = true; // zooming or moving mode for dragging
  private Color[][] colors; // palettes
  private int pal = 0; // current palette
  // julia set variables
  private boolean julia = false;
  private double juliaX = 0.56667, juliaY = -0.5;
  private boolean preview = false;
  private double previewX, previewY;
  private boolean toDrawAll = true;
  private boolean toDrawPreview = false;
  // currently visible relative window dimensions
  private double viewX = 0.0;
  private double viewY = 0.0;
  private double zoom = 1.0;

  private Image image; // offscreen image for double buffering
  private Graphics graphics; // offscreen graphics for the offscreen image
  private int width, height; // current screen width and height
  private Image previewImage;
  private Graphics previewGraphics;
  private Image bufferImage;
  private Graphics bufferGraphics;

  private Thread thread = null;

  private int mouseX, mouseY; // mouse position when the button was pressed
  private int dragX, dragY; // current mouse position during dragging

  private static final int[][][] colpal = { // palette colors
    { {12, 0, 10, 20}, {12, 50, 100, 240}, {12, 20, 3, 26}, {12, 230, 60, 20},
      {12, 25, 10, 9}, {12, 230, 170, 0}, {12, 20, 40, 10}, {12, 0, 100, 0},
      {12, 5, 10, 10}, {12, 210, 70, 30}, {12, 90, 0, 50}, {12, 180, 90, 120},
      {12, 0, 20, 40}, {12, 30, 70, 200} },
    { {10, 70, 0, 20}, {10, 100, 0, 100}, {14, 255, 0, 0}, {10, 255, 200, 0} },
    { {8, 40, 70, 10}, {9, 40, 170, 10}, {6, 100, 255, 70}, {8, 255, 255, 255} },
    { {12, 0, 0, 64}, {12, 0, 0, 255}, {10, 0, 255, 255}, {12, 128, 255, 255}, {14, 64, 128, 255} },
    { {16, 0, 0, 0}, {32, 255, 255, 255} },
  };

  public void init() {
    addMouseListener(this);
    addMouseMotionListener(this);
    addKeyListener(this);
    // initialize color palettes
    colors = new Color[colpal.length][];
    for (int p = 0; p < colpal.length; p++) { // process all palettes
      int n = 0;
      for (int i = 0; i < colpal[p].length; i++) // get the number of all colors
        n += colpal[p][i][0];
      colors[p] = new Color[n]; // allocate pallete
      n = 0;
      for (int i = 0; i < colpal[p].length; i++) { // interpolate all colors
        int[] c1 = colpal[p][i]; // first referential color
        int[] c2 = colpal[p][(i + 1) % colpal[p].length]; // second ref. color
        for (int j = 0; j < c1[0]; j++) // linear interpolation of RGB values
          colors[p][n + j] = new Color(
              (c1[1] * (c1[0] - 1 - j) + c2[1] * j) / (c1[0] - 1),
              (c1[2] * (c1[0] - 1 - j) + c2[2] * j) / (c1[0] - 1),
              (c1[3] * (c1[0] - 1 - j) + c2[3] * j) / (c1[0] - 1));
        n += c1[0];
      }
    }
    thread = null;
  }

  public void start() {
    redraw(false);
  }

  public void destroy() {
    Thread t = thread;
    thread = null;
    t.interrupt();
  }

  public void run() {
    while (thread != null) {
      while (draw() || Thread.interrupted());
      synchronized (this) {
        try {
          wait();
        }
        catch (InterruptedException e) {}
      }
    }
  }

  private void redraw(boolean preview) {
    markDraw(preview);
    if (thread != null && thread.isAlive()) {
      thread.interrupt();
    }
    else {
      thread = new Thread(this);
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }
  }

  private synchronized boolean toDraw(boolean preview) {
    boolean f = preview ? toDrawPreview : toDrawAll;
    if (preview)
      toDrawPreview = false;
    else
      toDrawAll = false;
    return f;
  }

  private synchronized void markDraw(boolean preview) {
    if (preview)
      toDrawPreview = true;
    else
      toDrawAll = true;
  }

  private synchronized Color getColor(int i) {
    int palSize = colors[pal].length;
    if (i + palSize < 0)
      i = Math.max(0, i + palSize);
    return colors[pal][(i + palSize) % palSize];
  }

  private synchronized void nextPalette() {
    pal = (pal + 1) % colors.length;
  }

  private static final int[][] rows = {
    {0, 16, 8}, {8, 16, 8}, {4, 16, 4}, {12, 16, 4},
    {2, 16, 2}, {10, 16, 2}, {6, 16, 2}, {14, 16, 2},
    {1, 16, 1}, {9, 16, 1}, {5, 16, 1}, {13, 16, 1},
    {3, 16, 1}, {11, 16, 1}, {7, 16, 1}, {15, 16, 1},
  };

  private boolean draw() {
    if (drawPreview())
      return true;
    if (!toDraw(false))
      return false;
    Dimension size = getSize();
    // create offscreen buffer for double buffering
    if (image == null || size.width != width || size.height != height) {
      width = size.width;
      height = size.height;
      image = createImage(width, height);
      graphics = image.getGraphics();
      previewImage = createImage(width / 4, height / 4);
      previewGraphics = previewImage.getGraphics();
      bufferImage = createImage(width, height);
      bufferGraphics = bufferImage.getGraphics();
    }
    // fractal image pre-drawing
    double r = zoom / Math.min(width, height);
    double sx = width > height ? 2.0 * r * (width - height) : 0.0;
    double sy = height > width ? 2.0 * r * (height - width) : 0.0;
    for (int y = 0; y < height + 4; y += 8) {
      if ((drawPreview() || Thread.interrupted()) && toDrawAll)
        return true;
      for (int x = 0; x < width + 4; x += 8) {
        double dx = 4.0 * (x * r + viewX) - 2.0 - sx;
        double dy = -4.0 * (y * r + viewY) + 2.0 + sy;
        Color color = color(dx, dy);
        graphics.setColor(color);
        graphics.fillRect(x - 4, y - 4, 8, 8);
      }
    }
    repaint();
    // fractal image drawing
    for (int row = 0; row < rows.length; row++) {
      for (int y = rows[row][0]; y < height; y += rows[row][1]) {
        if ((drawPreview() || Thread.interrupted()) && toDrawAll)
          return true;
        for (int x = 0; x < width; x++) {
          double dx = 4.0 * (x * r + viewX) - 2.0 - sx;
          double dy = -4.0 * (y * r + viewY) + 2.0 + sy;
          Color color = color(dx, dy);
          // computation of average color for antialiasing
          if (antialias) {
            Color c1 = color(dx - 0.25 * r, dy - 0.25 * r);
            Color c2 = color(dx + 0.25 * r, dy - 0.25 * r);
            Color c3 = color(dx + 0.25 * r, dy + 0.25 * r);
            Color c4 = color(dx - 0.25 * r, dy + 0.25 * r);
            int red = (color.getRed() + c1.getRed() + c2.getRed() + c3.getRed() + c4.getRed()) / 5;
            int green = (color.getGreen() + c1.getGreen() + c2.getGreen() + c3.getGreen() + c4.getGreen()) / 5;
            int blue = (color.getBlue() + c1.getBlue() + c2.getBlue() + c3.getBlue() + c4.getBlue()) / 5;
            color = new Color(red, green, blue);
          }
          graphics.setColor(color);
          graphics.fillRect(x, y - rows[row][2] / 2, 1, rows[row][2]);
        }
      }
      repaint();
    }
    return toDraw(false);
  }

  private boolean drawPreview() {
    if (julia || !preview || !toDraw(true))
      return false;
    boolean interrupted = false;
    // fractal image pre-drawing
    int width = this.width / 4;
    int height = this.height / 4;
    double r = 1.0 / Math.min(width, height);
    double sx = width > height ? 2.0 * r * (width - height) : 0.0;
    double sy = height > width ? 2.0 * r * (height - width) : 0.0;
    for (int y = 0; y < height + 2; y += 4) {
      if (Thread.interrupted()) {
        if (toDrawPreview)
          return true;
        interrupted = true;
      }
      for (int x = 0; x < width + 2; x += 4) {
        double dx = 4.0 * x * r - 2.0 - sx;
        double dy = -4.0 * y * r + 2.0 + sy;
        Color color = getColor(zfun(dx, dy, previewX, previewY) / 256);
        previewGraphics.setColor(color);
        previewGraphics.fillRect(x - 2, y - 2, 4, 4);
      }
    }
    repaint();
    if ((interrupted || Thread.interrupted()) && (toDrawPreview || toDrawAll))
      return true;
    // fractal image drawing
    for (int y = 0; y < height; y++) {
      if (Thread.interrupted()) {
        if (toDrawPreview)
          return true;
        interrupted = true;
      }
      for (int x = 0; x < width; x++) {
        double dx = 4.0 * x * r - 2.0 - sx;
        double dy = -4.0 * y * r + 2.0 + sy;
        Color color = getColor(zfun(dx, dy, previewX, previewY) / 256);
        previewGraphics.setColor(color);
        previewGraphics.fillRect(x, y, 1, 1);
      }
    }
    repaint();
    return interrupted;
  }

  // Computes a color for a given point
  private Color color(double x, double y) {
    int count = julia ? zfun(x, y, juliaX, juliaY): zfun(0.0, 0.0, x, y);
    Color color = getColor(count / 256);
    if (smooth) {
      Color color2 = getColor(count / 256 - 1);
      int k1 = count % 256;
      int k2 = 255 - k1;
      int red = (k1 * color.getRed() + k2 * color2.getRed()) / 255;
      int green = (k1 * color.getGreen() + k2 * color2.getGreen()) / 255;
      int blue = (k1 * color.getBlue() + k2 * color2.getBlue()) / 255;
      color = new Color(red, green, blue);
    }
    return color;
  }

  private int zfun(double zr, double zi, double cr, double ci) {
    switch (fractal) {
     case 1:
      return phoenix(zr, zi, cr, ci);
     default:
      return mandel(zr, zi, cr, ci);
    }
  }

  // Computes a value for a given complex number
  private int mandel(double zr, double zi, double cr, double ci) {
    double pr = zr*zr, pi = zi*zi;
    double zm = 0.0;
    int count = 0;
    while (pr + pi < 4.0 && count < maxCount) {
      zm = pr + pi;
      zi = 2.0*zr*zi + ci;
      zr = pr - pi + cr;
      pr = zr*zr;
      pi = zi*zi;
      count++;
    }
    if (count == 0 || count == maxCount)
      return 0;
    zm += 0.000000001;
    return 256 * count + (smooth ? (int)(255.0 * Math.log(4.0 / zm) / Math.log((pr + pi) / zm)) : 0);
  }

  // Computes a value for a given complex number
  private int phoenix(double zr, double zi, double cr, double ci) {
    double pr = zr*zr, pi = zi*zi;
    double sr = 0.0, si = 0.0;
    double zm = 0.0;
    int count = 0;
    while (pr + pi < 4.0 && count < maxCount) {
      zm = pr + pi;
      pr = pr - pi + ci*sr + cr;
      pi = 2.0*zr*zi + ci*si;
      sr = zr;
      si = zi;
      zr = pr;
      zi = pi;
      pr = zr*zr;
      pi = zi*zi;
      count++;
    }
    if (count == 0 || count == maxCount)
      return 0;
    zm += 0.000000001;
    return 256 * count + (smooth ? (int)(255.0 * Math.log(4.0 / zm) / Math.log((pr + pi) / zm)) : 0);
  }

  // To prevent background clearing for each paint()
  public void update(Graphics g) {
    paint(g);
  }

  public void paint(Graphics g) {
    if (image == null) // nothing to show
      return;
    Dimension size = getSize();
    if (size.width != width || size.height != height) {
      redraw(false);
      return;
    }
    bufferGraphics.drawImage(image, 0, 0, null);
    if (preview) {
      if (!julia)
        bufferGraphics.drawImage(previewImage, 0, 0, null);
      bufferGraphics.setColor(Color.white);
      bufferGraphics.drawString("x:" + previewX + "  y:" + previewY, 0, height - 5);
    }
    g.drawImage(bufferImage, 0, 0, null);
    // select-rectangle or offset-line drawing
    if (toDrag) {
      g.setColor(Color.black);
      g.setXORMode(Color.white);
      if (rect) {
        int x = Math.min(mouseX, dragX);
        int y = Math.min(mouseY, dragY);
        double w = mouseX + dragX - 2 * x;
        double h = mouseY + dragY - 2 * y;
        double r = Math.max(w / width, h / height);
        g.drawRect(x, y, (int)(width * r), (int)(height * r));
      }
      else
        g.drawLine(mouseX, mouseY, dragX, dragY);
    }
  }

  // methods from MouseListener interface

  public void mousePressed(MouseEvent e) {
    mouseX = dragX = e.getX();
    mouseY = dragY = e.getY();
    toDrag = true;
  }

  public void mouseReleased(MouseEvent e) {
    toDrag = false;
    int x = e.getX();
    int y = e.getY();
    if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) { // LMB
      double r = zoom / Math.min(width, height); // relative pixel size
      if (preview) { // julia
        julia = !julia;
        double sx = width > height ? width - height : 0.0;
        double sy = height > width ? height - width : 0.0;
        juliaX = 4.0 * (r * (mouseX - sx / 2.0) + viewX) - 2.0;
        juliaY = -4.0 * (r * (mouseY - sy / 2.0) + viewY) + 2.0;
        redraw(false); // recompute and repaint
      }
      else if (!rect) { // moved
        viewX += (mouseX - x) * r;
        viewY += (mouseY - y) * r;
        redraw(false); // recompute and repaint
      }
      else if (x != mouseX || y != mouseY) { // zoom in
        int mx = Math.min(x, mouseX);
        int my = Math.min(y, mouseY);
        double sx = width > height ? width - height : 0.0;
        double sy = height > width ? height - width : 0.0;
        viewX += r * (mx - sx / 2.0);
        viewY += r * (my - sy / 2.0);
        double w = x + mouseX - 2 * mx;
        double h = y + mouseY - 2 * my;
        double zoom0 = zoom;
        zoom *= Math.max(w / width, h / height);
        viewX += r / zoom0 * zoom * sx / 2.0;
        viewY += r / zoom0 * zoom * sy / 2.0;
        redraw(false); // recompute and repaint
      }
    }
    else if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) { // RMB
      if (maxCount < 1000000000);
      maxCount += maxCount / 4; // increase the number of iterations by 1/4
      redraw(false); // recompute and repaint
    }
  }

  public void mouseClicked(MouseEvent e) {} // not used
  public void mouseEntered(MouseEvent e) {} // not used
  public void mouseExited(MouseEvent e) {} // not used

  // methods from MouseMotionListener interface

  public void mouseDragged(MouseEvent e) {
    if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) { // LMB drag
      dragX = e.getX();
      dragY = e.getY();
      repaint(); // only repaint - no recomputation
    }
  }

  public void mouseMoved(MouseEvent e) {
    if (preview) {
      int x = e.getX();
      int y = e.getY();
      double r = zoom / Math.min(width, height); // relative pixel size
      double sx = width > height ? width - height : 0.0;
      double sy = height > width ? height - width : 0.0;
      previewX = 4.0 * (r * (x - sx / 2.0) + viewX) - 2.0;
      previewY = -4.0 * (r * (y - sy / 2.0) + viewY) + 2.0;
      if (julia)
        repaint();
      else
        redraw(true);
    }
  }

  // methods from KeyListener interface

  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) { // init
      maxCount = 192;
      viewX = viewY = 0.0;
      zoom = 1.0;
      redraw(false); // recompute and repaint
    }
    else if (e.getKeyCode() == KeyEvent.VK_SPACE) { // next fractal
      fractal = (fractal + 1) % 2;
      viewX = viewY = 0.0;
      zoom = 1.0;
      julia = false;
      if (fractal == 1) {
        juliaX = 0.56667;
        juliaY = -0.5;
        julia = true;
      }
      redraw(false); // recompute and repaint
    }
    else if (e.getKeyCode() == KeyEvent.VK_I) { // zoom in
      viewX += 0.25 * zoom;
      viewY += 0.25 * zoom;
      zoom *= 0.5;
      redraw(false); // recompute and repaint
    }
    else if (e.getKeyCode() == KeyEvent.VK_O) { // zoom out
      viewX -= 0.5 * zoom;
      viewY -= 0.5 * zoom;
      zoom *= 2.0;
      redraw(false); // recompute and repaint
    }
    else if (e.getKeyCode() == KeyEvent.VK_J) { // julia
      julia = !julia;
      redraw(false); // recompute and repaint
    }
    else if (e.getKeyCode() == KeyEvent.VK_P) { // next palette
      nextPalette();
      redraw(false); // recompute and repaint
    }
    else if (e.getKeyCode() == KeyEvent.VK_S) { // smoothing
      smooth = !smooth;
      redraw(false); // recompute and repaint
    }
    else if (e.getKeyCode() == KeyEvent.VK_A) { // antialiasing
      antialias = !antialias;
      redraw(false); // recompute and repaint
    }
    else if (e.getKeyCode() == KeyEvent.VK_SHIFT) { // move mode
      rect = false; // offset line (not selecting rectangle)
      if (toDrag) // repaint only when dragging is performed
        repaint(); // only repaint - no recomputation
    }
    else if (e.getKeyCode() == KeyEvent.VK_CONTROL) { // preview mode
      if (!preview) {
        preview = true;
        redraw(true);
      }
    }
  }

  public void keyReleased(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_SHIFT) { // zoom mode
      rect = true; // selecting rectangle (not offset line)
      if (toDrag) // repaint only when dragging is performed
        repaint(); // only repaint - no recomputation
    }
    else if (e.getKeyCode() == KeyEvent.VK_CONTROL) { // preview mode
      preview = false;
      repaint();
    }
  }

  public void keyTyped(KeyEvent e) {} // not used

  public static void main(String[] args) {
    Frame frame = new Frame("Fractal Viewer");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    Applet applet = new Mandel6();
    frame.add(applet, BorderLayout.CENTER);
    frame.setSize(301, 301);
    frame.show();
    applet.init();
    applet.start();
  }
}
