/*
 * mandel.java
 * Mandelbrot Renderer v1.0.7 (c) 2006 Eric Dietz
 * created: 02 Apr 2006, modified: 02 Oct 2006
 * notes: mandel.txt  email: root@wrongway.org
 */
/**
 * @author Eric Dietz
 * @version 1.0.7
 */

// package
//package mandel;

// imports
import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.Random;
import java.io.File;
import javax.imageio.ImageIO;

// class definition...
public class mandel extends Applet
    implements Runnable, ActionListener, ItemListener, MouseListener,
        MouseMotionListener, KeyListener, WindowListener
{

    // members
    public static final String version = "1.0.7";
    public static final boolean DEBUG = false;
    public double xmin, xmax, ymin, ymax;
    public int maxiteration, colorrange, app_width, app_height;
    public int col_mode;
    public boolean go_trip;
    public String bmp_filename;
    private static final long serialVersionUID = -7157472954351289184L;
    private double def_xmin, def_xmax, def_ymin, def_ymax;
    private int def_maxiteration, def_colorrange, def_width, def_height;
    private int drag_x1, drag_y1, drag_x2, drag_y2;
    private int [][] arr_mandel;
    private boolean commandline_init = false;
    private boolean running;
    private boolean drag, drag_meta;
    private boolean state_change, go_render, go_gradient;
    private Dimension dim;
    private Image img_applet, img_mandel;
    private Graphics gfx_applet, gfx_mandel;
    private Random rand;
    private Color [] col;
    private TextArea console;
    private TextField txt_maxit;
    private TextField txt_cols;
    private Button but_incmax;
    private Button but_decmax;
    private Button but_trip;
    private Button but_colscheme;
    private Button but_rerender;
    private Button but_inccols;
    private Button but_deccols;
    private Button but_help;
    private Thread render_thread;
    private Frame app_frame;
    private MenuBar app_menubar;
    private Menu app_filemenu;
    private MenuItem app_fileexit;

    // constructor
    public mandel ()
    {
        app_frame = null;
        app_menubar = null;
        app_filemenu = null;
        app_fileexit = null;
    }
    // if you want to run the applet standalone...
    public static void main (String [] args)
    {
        int size_x = 800, size_y = 600;
        mandel app = new mandel();
        app.commandline_init = true;
        app.xmin = -2.0;
        app.xmax = 2.0;
        app.ymin = -2.0;
        app.ymax = 2.0;
        app.maxiteration = 1000;
        app.colorrange = 100;
        app.col_mode = 0;
        app.go_trip = false;
        app.app_width = app.def_width = 600;
        app.app_height = app.def_height = 600;
        app.bmp_filename = "";
        for (int i = 0; i < args.length; i++)
        {
            if      (args[i].startsWith("xmin="))
                app.xmin = Double.parseDouble(args[i].substring(5));
            else if (args[i].startsWith("xmax="))
                app.xmax = Double.parseDouble(args[i].substring(5));
            else if (args[i].startsWith("ymin="))
                app.ymin = Double.parseDouble(args[i].substring(5));
            else if (args[i].startsWith("ymax"))
                app.ymax = Double.parseDouble(args[i].substring(5));
            else if (args[i].startsWith("maxiteration="))
                app.maxiteration = Integer.parseInt(args[i].substring(13));
            else if (args[i].startsWith("colorrange="))
                app.colorrange = Integer.parseInt(args[i].substring(11));
            else if (args[i].startsWith("col_mode="))
                app.col_mode = Integer.parseInt(args[i].substring(9));
            else if (args[i].startsWith("go_trip="))
                app.go_trip = Integer.parseInt(args[i].substring(8))==0?false:true;
            else if (args[i].startsWith("width="))
                app.app_width = Integer.parseInt(args[i].substring(6));
            else if (args[i].startsWith("height="))
                app.app_height = Integer.parseInt(args[i].substring(7));
            else if (args[i].startsWith("bitmap="))
                app.bmp_filename = args[i].substring(7);
            else if
             (args[i].startsWith("help") || args[i].startsWith("-help") ||
              args[i].startsWith("--help") || args[i].startsWith("-h") ||
              args[i].startsWith("/?") )
            {
                System.out.print
                ("Mandelbrot Renderer by Eric Dietz\n" +
                 "Command-line options:\n" +
                 "  option=<type> - description (range) [default]\n" +
                 "  xmin=<number> - set left boundary (-2.0-2.0) [-2.0]\n" +
                 "  xmax=<number> - set right boundary (-2.0-2.0) [2.0]\n" +
                 "  ymin=<number> - set bottom boundary (-2.0-2.0) [-2.0]\n" +
                 "  ymax=<number> - set top boundary (-2.0-2.0) [2.0]\n" +
                 "  maxiteration=<number> - set iterative bailout threshold (10-20000) [1000]\n" +
                 "  colorrange=<number> - set number of colors (1-32000) [100]\n" +
                 "  col_mode=<number> - set color palette mode (0-2) [0]\n" +
                 "  go_trip=<boolean> - set color cycling (0,1) [0]\n" +
                 "  width=<number> - set image width [800]\n" +
                 "  height=<number> - set image height [600]\n" +
                 "  bitmap=<filename> - write output to file *\n" +
                 "  help, -help, --help, -h, /? - view this message and exit\n" +
                 "* If you use the bitmap= option, then you will need to specify xmin, xmax,\n" +
                 "ymin, ymax, width and height at the command-line, as bitmap= causes the program\n" +
                 "to simply render the image and quit.  Also, height and width are ignored except\n" +
                 "when used with the bitmap option.\n" +
                 "Examples:\n" +
                 " java mandel col_mode=1 go_trip=1\n" +
                 " java mandel xmin=-2.0 xmax=1.0 ymin=-1.5 ymax=1.5 width=1024 height=768 bitmap=mandel.bmp\n" +
                 "have fun!\n"
                );
                System.exit(0);
            }
        }
        if (app.bmp_filename.equals(""))
        {
            app.app_width = app.def_width;
            app.app_height = app.def_height;
        }
        app.app_frame = new Frame("Mandelbrot Renderer");
        app.app_menubar = new MenuBar();
        app.app_filemenu = new Menu("File");
        app.app_fileexit = new MenuItem("Exit");
        app.app_frame.setMenuBar(app.app_menubar);
        app.app_menubar.add(app.app_filemenu);
        app.app_filemenu.add(app.app_fileexit);
        app.app_fileexit.addActionListener(app);
        app.setSize(size_x, size_y);
        app.app_frame.add(app, BorderLayout.CENTER);
        app.app_frame.setSize(size_x+10, size_y+50);
        //app.app_frame.pack();
        app.app_frame.addWindowListener(app);
        //app.app_frame.setIconImage(Toolkit.getDefaultToolkit().getImage("icon.gif"));
        //app.app_frame.setResizable(false);
        app.app_frame.setVisible(true);
        app.init();
        app.repaint();
    }
    // Applet.init override
    public void init ()
    {
        setLayout(null);
        def_xmin = -2.0;
        def_xmax = 2.0;
        def_ymin = -2.0;
        def_ymax = 2.0;
        def_maxiteration = 1000;
        def_colorrange = 100;
        def_width = 600;
        def_height = 600;
        if (!commandline_init)
        {
            try { xmin = Double.parseDouble(getParameter("xmin")); }
            catch (Exception e) { xmin = def_xmin; }
            try { xmax = Double.parseDouble(getParameter("xmax")); }
            catch (Exception e) { xmax = def_xmax; }
            try { ymin = Double.parseDouble(getParameter("ymin")); }
            catch (Exception e) { ymin = def_ymin; }
            try { ymax = Double.parseDouble(getParameter("ymax")); }
            catch (Exception e) { ymax = def_ymax; }
            try { maxiteration = Integer.parseInt(getParameter("maxiteration")); }
            catch (Exception e) { maxiteration = def_maxiteration; }
            try { colorrange = Integer.parseInt(getParameter("colorrange")); }
            catch (Exception e) { colorrange = def_colorrange; }
            try { col_mode = Integer.parseInt(getParameter("col_mode")); }
            catch (Exception e) { col_mode = 0; }
            try { go_trip = Integer.parseInt(getParameter("go_trip"))==0?false:true; }
            catch (Exception e) { go_trip = false; }
            app_width = def_width;
            app_height = def_height;
            bmp_filename = "";
        }
        if (xmin > xmax) { double d = xmin; xmin = xmax; xmax = d; }
        if (ymin > ymax) { double d = ymin; ymin = ymax; ymax = d; }
        colorrange = colorrange>32000?32000:colorrange<1?1:colorrange;
        maxiteration = maxiteration>20000?20000:maxiteration<10?10:maxiteration;
        col_mode = col_mode>2?2:col_mode<0?0:col_mode;
        def_xmin = xmin;
        def_xmax = xmax;
        def_ymin = ymin;
        def_ymax = ymax;
        def_maxiteration = maxiteration;
        def_colorrange = colorrange;
        dim = getSize();
        drag_x1 = 0; drag_y1 = 0;
        drag_x2 = 0; drag_y2 = 0;
        arr_mandel = new int [app_width][app_height];
        for (int i = 0; i < app_width; i++)
            for (int j = 0; j < app_height; j++)
                arr_mandel[i][j] = 0;
        running = true; drag = false; drag_meta = false;
        state_change = false; go_render = false; go_gradient = false;
        img_applet = createImage(dim.width, dim.height);
        gfx_applet = img_applet.getGraphics();
        img_mandel = createImage(app_width, app_height);
        gfx_mandel = img_mandel.getGraphics();
        rand = new Random();
        console = new TextArea("", 0, 0,
          DEBUG?TextArea.SCROLLBARS_VERTICAL_ONLY:TextArea.SCROLLBARS_NONE );
        console.setBounds(app_width, 0, dim.width-app_width, app_height-90);
        console.setEditable(false);
        add(console);
        gradient_setup();
        txt_maxit = new TextField(""+maxiteration, 10);
        txt_maxit.setBounds(app_width+69, app_height-44, 64, 20);
        txt_maxit.setEditable(true);
        add(txt_maxit);
        txt_cols = new TextField(""+colorrange, 10);
        txt_cols.setBounds(app_width+69, app_height-22, 64, 20);
        txt_cols.setEditable(true);
        add(txt_cols);
        but_incmax = new Button("+ maxiter");
        but_incmax.setBounds(app_width+135, app_height-44, 65, 20);
        but_incmax.setEnabled(true);
        but_incmax.addActionListener(this);
        add(but_incmax);
        but_decmax = new Button("maxiter -");
        but_decmax.setBounds(app_width+2, app_height-44, 65, 20);
        but_decmax.setEnabled(true);
        but_decmax.addActionListener(this);
        add(but_decmax);
        but_trip = new Button("trip!");
        but_trip.setBounds(app_width+135, app_height-66, 65, 20);
        but_trip.setEnabled(true);
        but_trip.addActionListener(this);
        add(but_trip);
        but_colscheme = new Button("color scheme");
        but_colscheme.setBounds(app_width+2, app_height-66, 131, 20);
        but_colscheme.setEnabled(true);
        but_colscheme.addActionListener(this);
        add(but_colscheme);
        but_rerender = new Button("abort");
        but_rerender.setBounds(app_width+2, app_height-88, 131, 20);
        but_rerender.setEnabled(true);
        but_rerender.addActionListener(this);
        add(but_rerender);
        but_inccols = new Button("+ colors");
        but_inccols.setBounds(app_width+135, app_height-22, 65, 20);
        but_inccols.setEnabled(true);
        but_inccols.addActionListener(this);
        add(but_inccols);
        but_deccols = new Button("colors -");
        but_deccols.setBounds(app_width+2, app_height-22, 65, 20);
        but_deccols.setEnabled(true);
        but_deccols.addActionListener(this);
        add(but_deccols);
        but_help = new Button("help");
        but_help.setBounds(app_width+135, app_height-88, 65, 20);
        but_help.setEnabled(true);
        but_help.addActionListener(this);
        add(but_help);
        render_thread = new Thread(this);   // the rendering thread...
        render_thread.start();              // get it going...
        render_mandel();
        if (go_trip) { go_trip = false; do_trip(); }
        addMouseListener(this);
        addMouseMotionListener(this);
    }
    // Applet.destroy override
    public void destroy ()
    {
    }
    // Applet.start override
    public void start ()
    {
    }
    // Applet.stop override
    public void stop ()
    {
    }
    // Applet.getAppletInfo override
    public String getAppletInfo ()
    {
        return "Mandelbrot renderer v"+version+" by Eric Dietz";
    }
    // Applet.getParameterInfo override
    public String [][] getParameterInfo ()
    {
        String [][] info =
        {{ "xmin",  "double",  "left boundary (-2.0-2.0)"   },
         { "xmax",  "double",  "right boundary (-2.0-2.0)"  },
         { "ymin",  "double",  "bottom boundary (-2.0-2.0)" },
         { "ymax",  "double",  "top boundary (-2.0-2.0)"    },
         { "maxiteration", "int", "iterative bailout threshold (10-20000)" },
         { "colorrange",   "int", "number of colors (1-32000)" },
         { "col_mode",     "int", "color palette mode (0-2)" },
         { "go_trip",      "int", "color cycling (0,1)" }};
        return info;
    }
    // Container.paint override
    public void paint (Graphics g)
    {
        drawscreen(gfx_applet);
        g.drawImage(img_applet, 0, 0, this);
    }
    // Container.update override
    public void update (Graphics g)
    {
        paint(g);
    }
    // Runnable.run implementation
    public void run ()
    {
        Object o = Thread.currentThread();
        if      (o == render_thread) render_loop();
    }
    // ActionListener.actionPerformed implementation
    public void actionPerformed (ActionEvent ae)
    {
        Object o = ae.getSource();
        if      (o == but_incmax) do_incmax();
        else if (o == but_decmax) do_decmax();
        else if (o == but_trip) do_trip();
        else if (o == but_colscheme) do_colscheme();
        else if (o == but_rerender) do_rerender();
        else if (o == but_inccols) do_inccols();
        else if (o == but_deccols) do_deccols();
        else if (o == but_help) do_help();
        else if (o == app_fileexit) do_quit();
    }
    // ItemListener.itemStateChanged implementation
    public void itemStateChanged (ItemEvent ie)
    {
        // Object o = ie.getSource();
    }
    // MouseListener.mouseEntered implementation
    public void mouseEntered (MouseEvent me)
    {
    }
    // MouseListener.mouseExited implementation
    public void mouseExited (MouseEvent me)
    {
    }
    // MouseListener.mouseClicked implementation
    public void mouseClicked (MouseEvent me)
    {
    }
    // MouseListener.mousePressed implementation
    public void mousePressed (MouseEvent me)
    {
        drag_x1 = me.getX();
        drag_y1 = me.getY();
        if
        (drag_x1 >= 0 && drag_x1 < app_width &&
         drag_y1 >= 0 && drag_y1 < app_height )
        {
            drag_x2 = drag_x1;
            drag_y2 = drag_y1;
            if (!me.isMetaDown())
            {
                drag = true;
                drag_meta = false;
            }
            else
            {
                drag = false;
                drag_meta = true;
            }
            repaint();
        }
    }
    // MouseListener.mouseReleased implementation
    public void mouseReleased (MouseEvent me)
    {
        drag_x2 = me.getX();
        drag_y2 = me.getY();
        if (drag_meta)
        {
            drag = false;
            drag_meta = false;
            if (Math.abs(drag_x2-drag_x1) < 20 && Math.abs(drag_y2-drag_y1) < 20)
            {
                if (xmax - xmin >= 4.0 && ymax - ymin >= 4.0)
                {
                    // zoomed out pretty far; reset view
                    xmin = def_xmin;
                    ymin = def_ymin;
                    xmax = def_xmax;
                    ymax = def_ymax;
                }
                else
                {
                    // zoom out on current position 10fold
                    double x1, y1, x2, y2;
                    double factor = 0.1, mindelta = 1.0E-15;
                    x1 = (xmax - xmin) / 2 + xmin - (xmax - xmin) / (2 * factor);
                    y1 = (ymax - ymin) / 2 + ymin - (ymax - ymin) / (2 * factor);
                    x2 = (xmax - xmin) / 2 + xmin + (xmax - xmin) / (2 * factor);
                    y2 = (ymax - ymin) / 2 + ymin + (ymax - ymin) / (2 * factor);
                    xmin = x1;
                    ymin = y1;
                    xmax = x2;
                    ymax = y2;
                    if (xmin > xmax) { double d = xmin; xmin = xmax; xmax = d; }
                    if (ymin > ymax) { double d = ymin; ymin = ymax; ymax = d; }
                    if (xmax - xmin < mindelta || ymax - ymin < mindelta)
                    {
                        xmax = xmin + mindelta;
                        ymax = ymin + mindelta;
                    }
                }
                render_mandel();
            }
        }
        if (drag)
        {
            drag = false;
            drag_meta = false;
            if (Math.abs(drag_x2-drag_x1) < 4 && Math.abs(drag_y2-drag_y1) < 4)
            {
                double x1, y1, x2, y2;
                double factor = 10.0;
                x1 = drag_x1 * (xmax - xmin) / app_width +
                     xmin - (xmax - xmin) / (2 * factor);
                y1 = (app_height - drag_y1 - 1) * (ymax - ymin) / app_height +
                     ymin - (ymax - ymin) / (2 * factor);
                x2 = drag_x1 * (xmax - xmin) / app_width +
                     xmin + (xmax - xmin) / (2 * factor);
                y2 = (app_height - drag_y1 - 1) * (ymax - ymin) / app_height +
                     ymin + (ymax - ymin) / (2 * factor);
                xmin = x1;
                ymin = y1;
                xmax = x2;
                ymax = y2;
            }
            else
            {
                int r =
                    (int)(Math.sqrt(((drag_x2-drag_x1)*(drag_x2-drag_x1)+
                                     (drag_y2-drag_y1)*(drag_y2-drag_y1))/2));
                int tx1 = drag_x1 - r, ty1 = app_height - (drag_y1 - r) - 1;
                int tx2 = drag_x1 + r, ty2 = app_height - (drag_y1 + r) - 1;
                double x1, y1, x2, y2;
                x1 = tx1 * (xmax - xmin) / app_width + xmin;
                y1 = ty1 * (ymax - ymin) / app_height + ymin;
                x2 = tx2 * (xmax - xmin) / app_width + xmin;
                y2 = ty2 * (ymax - ymin) / app_height + ymin;
                xmin = x1;
                ymin = y1;
                xmax = x2;
                ymax = y2;
            }
            if (xmin > xmax) { double d = xmin; xmin = xmax; xmax = d; }
            if (ymin > ymax) { double d = ymin; ymin = ymax; ymax = d; }
            render_mandel();
        }
    }
    // MouseMotionListener.mouseDragged implementation
    public void mouseDragged (MouseEvent me)
    {
        if (drag || drag_meta)
        {
            drag_x2 = me.getX();
            drag_y2 = me.getY();
            repaint();
        }
    }
    // MouseMotionListener.mouseMoved implementation
    public void mouseMoved (MouseEvent me)
    {
    }
    // KeyListener.keyPressed implementation
    public void keyPressed (KeyEvent ke)
    {
    }
    // KeyListener.keyReleased implementation
    public void keyReleased (KeyEvent ke)
    {
    }
    // KeyListener.keyTyped implementation
    public void keyTyped (KeyEvent ke)
    {
    }
    // WindowListener.windowOpened implementation
    public void windowOpened (WindowEvent we)
    {
    }
    // WindowListener.windowClosed implementation
    public void windowClosed (WindowEvent we)
    {
    }
    // WindowListener.windowActivated implementation
    public void windowActivated (WindowEvent we)
    {
    }
    // WindowListener.windowDeactivated implementation
    public void windowDeactivated (WindowEvent we)
    {
    }
    // WindowListener.windowIconified implementation
    public void windowIconified (WindowEvent we)
    {
    }
    // WindowListener.windowDeiconified implementation
    public void windowDeiconified (WindowEvent we)
    {
    }
    // WindowListener.windowClosing implementation
    public void windowClosing (WindowEvent we)
    {
        do_quit();
    }

    // exit the program!
    public void do_quit ()
    {
        System.exit(0);
    }

    // print things to the text screen...
    public void printf (String s)
    {
        console.append(s);
    }

    // clear the console text...
    public void clearscreen ()
    {
        console.setText("");
    }

    // increase maxiterations and rerender...
    private void do_incmax ()
    {
        if (maxiteration < 20000-100) maxiteration += 100;
        txt_maxit.setText(""+maxiteration);
        render_mandel();
    }

    // decrease maxiterations and rerender...
    private void do_decmax ()
    {
        if (maxiteration > 100) maxiteration -= 100;
        txt_maxit.setText(""+maxiteration);
        render_mandel();
    }

    // increase colorrange and rerender...
    private void do_inccols ()
    {
        if (colorrange < 32000-100) colorrange += 100;
        txt_cols.setText(""+colorrange);
        render_gradient(true);
    }

    // decrease colorrange and rerender...
    private void do_deccols ()
    {
        if (colorrange > 100) colorrange -= 100;
        txt_cols.setText(""+colorrange);
        render_gradient(true);
    }

    // start the tripping routine...
    private void do_trip ()
    {
        if (go_trip)
        {
            go_trip = false;
            but_trip.setLabel("trip");
        }
        else
        {
            go_trip = true;
            but_trip.setLabel("don't trip");
        }
    }

    // re-order the color scheme...
    private void do_colscheme ()
    {
        col_mode++;
        if (col_mode >= 3) col_mode -= 3;
        render_gradient(false);
    }

    // show some help info...
    private void do_help ()
    {
        if (DEBUG) { printf("**********\n**********\n"); return; }
        printf(
         "----------\n" +
         "This program renders the Mandelbrot set, which is a mathematic " +
         "fractal derived from the Julia set.  The controls should hopefully be " +
         "simple: Left click to zoom in 10x, Right click (ctrl+click on Mac) to " +
         "zoom out 10x.  To get a more precise viewing window, left click on " +
         "the center of the desired area, then drag the mouse away from the " +
         "center to set the size of the new window (which will be framed by the " +
         "yellow box).  If you accidently click, right click and drag to " +
         "cancel.  This applet (c) 2006 Eric Dietz.  Cheers!\n"
        );
        String info =
         "Java version: " + System.getProperty("java.version") +
         " from " + System.getProperty("java.vendor") +
         ", running on " + System.getProperty("os.name") +
         " (" + System.getProperty("os.version") +
         ", " + System.getProperty("os.arch") +
         ")\n";
        printf(info);
    }

    // process Abort command...
    private void do_rerender ()
    {
        if (go_render)
        {
            go_render = false;
            repaint();
        }
        else
        {
            //render_mandel();
            render_gradient(true);
        }
    }

    // sync textboxes with internal settings...
    private void sync_textboxes ()
    {
        int maxit = Integer.parseInt(txt_maxit.getText());
        if (maxit != maxiteration)
        {
            if (maxit > 0) maxiteration = maxit;
            maxiteration = maxiteration>20000?20000:maxiteration<10?10:maxiteration;
            txt_maxit.setText(""+maxiteration);
        }
        int colrange = Integer.parseInt(txt_cols.getText());
        if (colrange != colorrange)
        {
            if (colrange > 0) colorrange = colrange;
            colorrange = colorrange>32000?32000:colorrange<1?1:colorrange;
            txt_cols.setText(""+colorrange);
        }
    }

    // re-setup the gradient
    private void render_gradient (boolean recalc)
    {
        go_render = false;
        sync_textboxes();
        try { Thread.sleep(50); }
        catch (Exception e) { }
        go_gradient = true;
        if (recalc) go_render = true;
    }

    // Control the rendering and color oscillating thread...
    private void render_loop ()
    {
        int old_maxiteration = maxiteration, old_colorrange = colorrange;
        int step = 0;
        double old_xmin = xmin, old_xmax = xmax, old_ymin = ymin, old_ymax = ymax;
        while (running)
        {
            if (go_gradient || old_colorrange != colorrange)
            {
                boolean recalc = go_render;
                gradient_setup();
                if (recalc)
                    go_render = true;
                else
                {
                    render_again();
                    mandel_info();
                }
                go_gradient = false;
            }
            if
            (go_render ||
             old_maxiteration != maxiteration || old_colorrange != colorrange ||
             old_xmin != xmin || old_xmax != xmax ||
             old_ymin != ymin || old_ymax != ymax )
            {
                if (col_mode < 2)
                {
                    step = colorrange / 100;
                    step = step>100?100:step<1?1:step;
                }
                else
                    step = 1;
                go_render = true;
                if (DEBUG) printf("calling mandelbrot()\n");
                mandelbrot();
                go_render = false;
                old_maxiteration = maxiteration;
                old_colorrange = colorrange;
                old_xmin = xmin; old_xmax = xmax; old_ymin = ymin; old_ymax = ymax;
            }
            if (go_trip && !state_change)
            {
                /*
                Color c = col[colorrange-1];
                for (int i = colorrange-1; i > 0 && !state_change; i--)
                    col[i] = col[i-1];
                col[0] = c;
                */
                for (int i = 0; i < app_height && !state_change; i++)
                {
                    for (int j = 0; j < app_width && !state_change; j++)
                    {
                        if (arr_mandel[i][j] != -1)
                        {
                            arr_mandel[i][j] += step;
                            if (arr_mandel[i][j] >= colorrange)
                                arr_mandel[i][j] -= colorrange;
                        }
                    }
                }
                if (DEBUG) printf("[");
                render_again();
            }
            try { Thread.sleep(10); }
            catch (Exception e) { }
        }
        render_thread = null;
    }

    // render_again - to rerender w/o recalculating
    private void render_again ()
    {
        if (DEBUG) printf("]");
        // If gradient_setup resets the size of colorrange, this will mess up,
        // therefore it's in a try{}.  Apparently gradient_setup's
        // state_change=true;Thread.sleep(50); wasn't enough to prevent this :<
        try {
            Color c;
            for (int i = 0; i < app_height && !state_change; i++)
            {
                for (int j = 0; j < app_width && !state_change; j++)
                {
                    if (arr_mandel[j][i] == -1)
                        c = Color.black;
                    else
                        c = col[arr_mandel[j][i]];
                    gfx_mandel.setColor(c);
                    gfx_mandel.drawLine(j, i, j, i);
                }
            }
        }   
        catch (ArrayIndexOutOfBoundsException e) {
            // nada
        }
        repaint();
    }

    // draw the screen
    private void drawscreen (Graphics g)
    {
        if (gfx_mandel == null) return;
        g.clearRect(0, 0, dim.width, dim.height);
        g.drawImage(img_mandel, 0, 0, this);
        if (drag)
        {
            int x1 = drag_x1, y1 = drag_y1, x2 = drag_x2, y2 = drag_y2;
            int r = (int)Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
            if (Math.abs(drag_x2-drag_x1) < 4 && Math.abs(drag_y2-drag_y1) < 4)
            {
                r = (int)(app_height / (10 * Math.sqrt(2)));
                x2 = x1; y2 = y1;
            }
            int r2 = (int)(r/Math.sqrt(2));
            g.setColor(Color.red);
            g.drawOval(x1 - r, y1 - r, r*2, r*2);
            g.drawLine(x1, y1, x2, y2);
            g.drawLine(x1, y1, x1 + r2, y1 + r2);
            g.setColor(Color.yellow);
            g.drawRect(x1-r2,y1-r2,r2*2,r2*2);
        }
        else if
        (drag_meta &&
         Math.abs(drag_x2-drag_x1) < 20 && Math.abs(drag_y2-drag_y1) < 20 )
        {
            int x = app_width / 2, y = app_height / 2;
            int r = app_width / 3;
            g.setColor(Color.yellow);
            g.drawLine(x-r, y-r, x+r, y+r);
            g.drawLine(x-r, y+r, x+r, y-r);
            g.drawLine(x-r, y-r, x-r, y-r+50);
            g.drawLine(x-r, y-r, x-r+50, y-r);
            g.drawLine(x-r, y+r, x-r, y+r-50);
            g.drawLine(x-r, y+r, x-r+50, y+r);
            g.drawLine(x+r, y-r, x+r, y-r+50);
            g.drawLine(x+r, y-r, x+r-50, y-r);
            g.drawLine(x+r, y+r, x+r, y+r-50);
            g.drawLine(x+r, y+r, x+r-50, y+r);
            g.drawOval(x-r/2, y-r/2, r, r);
        }
    }

    // display info about the mandel
    private void mandel_info ()
    {
        if (DEBUG) {
            printf("---maxit="+maxiteration+",cols="+colorrange+",rand="+col_mode+"---\n");
            return;
        }
        clearscreen();
        printf("Mandelbrot renderer v"+version+"\n");
        printf("by Eric Dietz\n\n");
        printf("("+xmin+" < x < "+xmax+")\n\n");
        printf("("+ymin+" < y < "+ymax+")\n\n");
        printf("dx = "+(xmax-xmin)+"\n");
        printf("dy = "+(ymax-ymin)+"\n\n");
        printf("maxiters="+maxiteration+
               ", colors="+colorrange+
               ", colormode="+col_mode+
               "\n\n");
        if (!bmp_filename.equals(""))
            printf("Saved "+app_width+"x"+app_height+" bitmap to "+bmp_filename+"\n\n");
    }

    // send the signal to the render_thread to render the mandel
    private void render_mandel()
    {
        if (DEBUG) printf("render_");
        go_render = false;
        state_change = true;
        sync_textboxes();
        try { Thread.sleep(50); }
        catch (Exception e) { }
        go_render = true;
        if (DEBUG) printf("_mandel()\n");
    }
  
    // set up our color palette
    private void gradient_setup ()
    {
        if (DEBUG) printf("gradient_setup() start\n");
        go_render = false;
        state_change = true;
        if (!DEBUG) printf("\nRefreshing...\n");
        // to avoid exceptions arrayindexoutofbounds thrown by trip_loop when
        // recalculating the gradient, set state_change=true; and Thread.sleep(50);
        col = new Color [colorrange];
        if (col_mode == 0 || col_mode == 1)
        {
            // revamed version for nicer "random" palettes...
            int [][] grad;
            if (col_mode == 1) /* Colors RANDOM GRADIENT */
            {
                int p = rand.nextInt(17) + 4;
                grad = new int [p][3];
                for (int i = 0; i < p; i++)
                    for (int j = 0; j < 3; j++)
                        grad[i][j] = rand.nextInt(256);
            }
            else /* Standard RedGreenBlueBlack Gradient */
            {

                // set up the color palette: a gradient of points from the grad[][]
                // array of RGB values.
                int [][] RGB = // our color gradient points: ROYGBV
                {{ 255 , 0 , 0 },{ 0 , 255 , 0 },{ 0 , 0 , 255 },{ 1 , 1 , 1 }};
                //{{ 255, 0  , 0   },{ 255, 128, 0   },{ 255, 255, 0   },
                // { 0  , 255, 0   },{ 0  , 0  , 255 },{ 255, 0  , 255 }};

                grad = RGB;
            }
            int tot = grad.length, sec = colorrange / tot, cur = 0;
            int a = 0, b = 0, c = 0;
            if (colorrange < tot)
            { tot = colorrange; sec = 1; } 
            for (int i = 0; i < tot; i++)
            {
                for (int j = 0; j < sec; j++)
                {
                    a = (grad[i==tot-1?0:i+1][0]*j/sec+grad[i][0]*(sec-j)/sec);
                    b = (grad[i==tot-1?0:i+1][1]*j/sec+grad[i][1]*(sec-j)/sec);
                    c = (grad[i==tot-1?0:i+1][2]*j/sec+grad[i][2]*(sec-j)/sec);
                    if (a < 0) a = 0; else if (a > 255) a = 255;
                    if (b < 0) b = 0; else if (b > 255) b = 255;
                    if (c < 0) c = 0; else if (c > 255) c = 255;
                    col[cur] = new Color(a, b, c);
                    cur++;
                }
            }
            for (int i = cur; i < colorrange; i++)
                col[i] = new Color(a, b, c); // <-- meh, to deal with remainders...
        }
        else if (col_mode == 2) /* Colors TOTALLY RANDOM */
        {
            for (int i = 0; i < colorrange; i++)
                col[i] = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
        }
        state_change = false;
        if (DEBUG) printf("gradient_setup() end\n");
    }

    // render the mandelbrot set...
    private void mandelbrot ()
    {
        if (DEBUG) printf("mandelbrot() start\n");
        int i, j, iteration, colrange = colorrange;
        double x, y, x0, y0, x2, y2, xratio, yratio;
        Color c;
        state_change = true;
        but_rerender.setLabel("abort");
        if (!DEBUG) {
            printf("\n*** RENDERING ***\n");
            printf("(press abort to cancel)\n");
        }
        // correct aspect ratio if necessary...
        if (xmax-xmin > ymax-ymin)
            ymax = ymin + (xmax - xmin);
        else if (ymax-ymin > xmax-xmin)
            xmax = xmin + (ymax - ymin);
        xratio = (xmax - xmin) / app_width;
        yratio = (ymax - ymin) / app_height;
        try {
            for (i = 0; i < app_height && go_render; i++)
            {
                for (j = 0; j < app_width && go_render; j++)
                {
                    x0 = j * xratio + xmin;
                    y0 = (app_height - i - 1) * yratio + ymin;
                    x = x0;
                    y = y0;
                    x2 = x*x;
                    y2 = y*y;
                    iteration = 0;
                    while (x2 + y2 < (2*2) && iteration < maxiteration)
                    {
                        y = 2*x*y+y0;
                        x = x2 - y2 + x0;
                        x2 = x*x;
                        y2 = y*y;
                        iteration++;
                    }
                    if (iteration == maxiteration)
                    {
                        arr_mandel[j][i] = -1;
                        c = Color.black;
                    }
                    else
                    {
                        arr_mandel[j][i] = (iteration % colrange);
                        c = col[arr_mandel[j][i]];
                    }
                    gfx_mandel.setColor(c);
                    gfx_mandel.drawLine(j, i, j, i);
                }
                if (i % 20 == 0) repaint();
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            // nada
        }
        if (!bmp_filename.equals(""))
        {
            printf("Saving bitmap...\n");
            save_bitmap(bmp_filename, img_mandel, app_width, app_height);
        }
        mandel_info();
        repaint();
        but_rerender.setLabel("rerender");
        state_change = false;
        if (DEBUG) printf("mandelbrot() end\n");
    }

    // save bitmap to file and exit
    public void save_bitmap (String filename, Image image, int w, int h)
    {
        try {
            File fn = new File(filename);
            RenderedImage im = (RenderedImage)image;
            ImageIO.write(im , "bmp", fn);
        }
        catch (Exception e) {
            System.out.println("Error writing bitmap "+filename+": "+e);
        }
        System.out.println("Saved "+app_width+"x"+app_height+" bitmap to "+bmp_filename);
        do_quit();
    }

}

// end of code
