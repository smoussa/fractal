import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.List;
import java.util.Random;

public class AnagramGUIThreaded {

    public static void main(String[] args){

        SwingUtilities.invokeLater ( 
			       new Runnable () {
				     public void run() {
           AnagramFrame window = new AnagramFrame("Anagrams"); 
           window.init(); }
				 }
				  );
    }
}

class AnagramFrame extends JFrame {

    public AnagramFrame(String title){
	super(title);
    }

    void init(){

	Container panel = this.getContentPane();
	panel.setLayout(new FlowLayout());
	JTextField input =  new JTextField(8);
	JTextArea output = new JTextArea(20,20);
	JScrollPane sp = new JScrollPane(output);
	JButton anagram = new JButton("Get anagrams");
	panel.add(input);
	panel.add(anagram);
	panel.add(sp);
	ActionListener rl = new AnagramListener(input,output);
	anagram.addActionListener( rl );
	input.addActionListener( rl );

	this.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE) ;
        this.setSize(300,800);
       	this.setVisible(true);
    }
}


class AnagramListener implements ActionListener {

    JTextField input;
    JTextArea output;
    
    
    public AnagramListener(JTextField input, JTextArea output){
	this.input = input;
	this.output = output;

    }

    public void actionPerformed(ActionEvent e){

	final String s;
	s = input.getText();    
        Random r = new Random();

       class AnagramSwingWorker extends SwingWorker<String,String> {

         int size = s.length();
         char[] chars = new char[size];
	 StringBuffer sb = new StringBuffer();

         public String doInBackground(){ 
	     for (int j=0; j < size; j++){
		 chars[j] = s.charAt(j);
	     }
             doAnagram(size);
	     return ("");
         }

	 private void doAnagram(int rsize){
	     if (rsize==1){ return; }
	     for (int i=0; i < rsize ; i++){
		 doAnagram(rsize-1);
		 if (rsize==2){
		     for (int j=0; j < size; j++){
			 sb.append(chars[j]);
		     }
		     sb.append("\n");
		     publish(sb.toString());
		     sb.delete(0,size+1);
		     try {Thread.sleep(100);} catch (InterruptedException ie){}
		 }
		 rotate(rsize);
	     }
	 }
       

	 private void rotate(int rsize){
	     int i;
	     int pos = size - rsize;
	     char tmp = chars[pos];
	     for (i=pos + 1; i < size ; i++){
		 chars[i-1] = chars[i];
	     }
	     chars[i-1] = tmp;
	 }

	 protected void process(List<String> cs){
	     for (String c : cs) {
	     output.append(c);
	     }
	 }

	 protected void done(){
	     output.append("\nAll done for word " + s + "\n");
	 }

       }
       
   
	final SwingWorker sw = new AnagramSwingWorker();
	sw.execute();
	output.setBackground(new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256)));
    }

       
}



