import javax.swing.JFrame;
public class SnakeLadderServerTest{
	public static void main(String args[]){
		SnakeLadderServer sls = new SnakeLadderServer();
		sls.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		sls.execute();
	}
}