import java.awt.*;
import java.awt.event.*; //for ActionListener
import java.net.*; //Socket, InetAddress
import java.io.*; //IOException
import javax.swing.*;
import java.util.*; //Scanner Formatter Random
import java.util.concurrent.*; //ExecutorService Executors
public class SnakeLadderClient extends JFrame implements ActionListener, Runnable{

	private SLPanel sl;
	private JButton roll;
	private int die;
	private int n;
	private int score[] = {0,0};
	private JTextArea displayArea;
	private Socket connection;
	private Scanner input;
	private Formatter output;
	private String SLHost;
	private String myToken;
	private boolean myTurn;
	private final String tokens[] = {"Red.png","Blue.png"};
	
	public SnakeLadderClient(String host){
		super("Snakes and Ladders");
		SLHost = host;
		setMinimumSize(new Dimension(850,730));
		setSize(900,740);
		sl = new SLPanel(0);
		add(sl,BorderLayout.CENTER);
		JPanel bottom = new JPanel();
		bottom.setBackground(Color.darkGray);
		roll = new JButton("ROLL DIE");
		bottom.add(roll,BorderLayout.CENTER);
		displayArea = new JTextArea(2,30); // set up JTextArea
		displayArea.setEditable(false);
		bottom.add(new JScrollPane(displayArea),BorderLayout.SOUTH);
		roll.addActionListener(this);
		add(bottom,BorderLayout.SOUTH);
		//sl.requestFocus();
		setVisible(true);
		startClient();
	}
	
	public void startClient(){
		try{
			connection = new Socket(InetAddress.getByName(SLHost),12345);//Creates a stream socket and connects it to the specified port number at the specified IP address.
			input = new Scanner(connection.getInputStream());// Returns an input stream for this socket.
			output = new Formatter(connection.getOutputStream());// Returns an output stream for this socket.
		}
		catch(IOException e){
			e.printStackTrace();
		}
		ExecutorService worker = Executors.newFixedThreadPool(1); //Creates a thread pool that reuses a fixed number of threads operating off a shared unbounded queue.
		worker.execute(this); 	
	}
	public void run() {
		n = input.nextInt();
		myToken = tokens[n];
		SwingUtilities.invokeLater(
				new Runnable(){
					public void run(){
						roll.setText("Player "+(n+1));
					}
				}
		);
		myTurn = (myToken.equals(tokens[0]));
		while(true){
			if(input.hasNext())
				processMessage(input.nextLine());
		}	
	}
	
	private void processMessage(String msg){
		if(msg.equals("Valid move.")){
			int score = input.nextInt();
			input.nextLine();
			setScore(n,score);
			displayMessage("Your turn over. Please wait.\n");
		}
		else if(msg.equals("Opponent moved")){
			int score = input.nextInt();
			input.nextLine();
			setScore((n+1)%2,score);
			displayMessage("Opponent moved. Your Turn\n");
			myTurn=true;
		}
		else if(msg.equals("Six")){
			displayMessage("Six scored, roll again\n");
			repaint();
			int score = input.nextInt();
			input.nextLine();
			setScore(n,score);
			myTurn=true;
		}
		else
			displayMessage(msg+"\n");
	}
	
	private void setScore(final int i, final int s) {
		SwingUtilities.invokeLater(
				new Runnable(){
					public void run(){
						score[i] = s;
						repaint();
						if(s == 100){
							JOptionPane.showMessageDialog(null,"Player" +(i+1)+ "WON", "GAME OVER",JOptionPane.PLAIN_MESSAGE);
							System.exit(1);
						}
					}
				}
		);
	}

	private void displayMessage( final String messageToDisplay ){
		SwingUtilities.invokeLater(
				new Runnable(){
					public void run(){
						displayArea.append(messageToDisplay);
					}
				}
		);
	}
	public void actionPerformed(ActionEvent e) {
		if(myTurn){
			Random rnd = new Random();
			int i = Math.abs(rnd.nextInt() % 6);
			die=i;
			output.format("%d\n", i+1);
			output.flush();
			myTurn=false;
		}
	}
	
	public class SLPanel extends JPanel {
		final int X[]={25,89,153,217,281,345,409,473,537,601,
						601,537,473,409,345,281,217,153,89,25,
						25,89,153,217,281,345,409,473,537,601,
						601,537,473,409,345,281,217,153,89,25,
						25,89,153,217,281,345,409,473,537,601,
						601,537,473,409,345,281,217,153,89,25,
						25,89,153,217,281,345,409,473,537,601,
						601,537,473,409,345,281,217,153,89,25,
						25,89,153,217,281,345,409,473,537,601,
						601,537,473,409,345,281,217,153,89,25};
		final int Y[]={600,600,600,600,600,600,600,600,600,600,
						536,536,536,536,536,536,536,536,536,536,
						472,472,472,472,472,472,472,472,472,472,
						408,408,408,408,408,408,408,408,408,408,
						344,344,344,344,344,344,344,344,344,344,
						280,280,280,280,280,280,280,280,280,280,
						216,216,216,216,216,216,216,216,216,216,
						152,152,152,152,152,152,152,152,152,152,
						88,88,88,88,88,88,88,88,88,88,
						24,24,24,24,24,24,24,24,24,24};
		public SLPanel(int x){
			die=x;
		}
		public void paintComponent(Graphics g) {
			setBackground(Color.cyan);
			//FontMetrics fm=g.getFontMetrics();
			Color color[] = {Color.red,Color.green,Color.blue,Color.orange};
			int x=10,y=10;
			for(int i=0; i<10; i++){
				for(int j=0;j<10;j++){
					g.setColor(color[(i+j)%4]); 
					g.fillRect(x+(i*64), y+(j*64), 64, 64);
				}
			}
			Graphics2D g2 = (Graphics2D) g;
			g2.setStroke(new BasicStroke(3));
			g2.setColor(Color.BLACK);
			x=y=10;
			for(int i=0;i<=10;i++){
					g2.drawLine(x+(i*64),10,x+(i*64),647);
			}
			for(int i=0;i<=10;i++){
				g2.drawLine(10,y+(i*64),647,y+(i*64));
			}
			String image[] = {"die1.png","die2.png","die3.png","die4.png","die5.png","die6.png"};
			Image s,s1,s2,s3,s4,l1,l2,l3,l4,d,t1,t2;
			s = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/Snake6.png"));
		    if(s != null) g.drawImage(s, 20, 350, this);
		    s1 = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/Snake7.png"));
		    if(s1 != null) g.drawImage(s1,150,20, this);
		    s2 = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/Snake5.png"));
		    if(s2 != null) g.drawImage(s2, 420, 150, this);
		    s3 = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/Snake3.png"));
		    if(s3 != null) g.drawImage(s3, 420, 460, this);
		    s4 = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/Snake1.png"));
		    if(s4 != null) g.drawImage(s4, 120, 220, this);
		    l1 = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/Ladder1.png"));
		    if(l1 != null) g.drawImage(l1, 300, 240, this);
		    l2 = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/Ladder3.png"));
		    if(l2 != null) g.drawImage(l2, -25, 80, this);
		    l3 = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/Ladder1.png"));
		    if(l3 != null) g.drawImage(l3, 430, -80, this);
		    l4 = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/Ladder4.png"));
		    if(l4 != null) g.drawImage(l4, 190, 50, this);
		    
		    g.setColor(Color.WHITE);
		    x=y=20;
		    int n = 100;
		    for(int i=0; i<10; i+=2){
				for(int j=0;j<10;j++){
					g.drawString(""+n--,y+(j*64),x+(i*64));
				}
				n-=10;
			}
		    x=20;
		    y=596;
		    n=1;
		    for(int i=0; i<10; i+=2){
				for(int j=0;j<10;j++){
					g.drawString(n<10 ? "0"+n++ : ""+n++,x+(j*64),y-(i*64));
				}
				n+=10;
			}
			g.drawString("HOME",25,42);
			g.drawString("START",20,623);
			
			d = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/" +image[die]));
		    if(d != null) g.drawImage(d, 700, 200, this);
		    
		    Font f=new Font("Serif",Font.BOLD,25);
		    g.setFont(f);
		    g.setColor(Color.RED);
	    	g.drawString("Player1: "+score[0], 700, 370);
	    	g.setColor(Color.BLUE);
	    	g.drawString("Player2: "+score[1], 700, 400);
		    
		    if(score[0]>0){
		    	t1 = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/Red.png"));
			    if(t1 != null) g.drawImage(t1, X[score[0]-1],Y[score[0]-1], this);
		    }
		    if(score[1]>0){
		    	t2 = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/Blue.png"));
			    if(t2 != null) g.drawImage(t2,X[score[1]-1], Y[score[1]-1]+20, this);
		    }
		}
	}
}