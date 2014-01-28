import javax.swing.JFrame;
public class SnakeLadderClientTest {
	public static void main(String args[]){
		SnakeLadderClient slc = new SnakeLadderClient("127.0.0.1");
		slc.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}