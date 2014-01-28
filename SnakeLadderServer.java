import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import javax.swing.*;
public class SnakeLadderServer extends JFrame
{
	private int[] score = new int[2];
	private int count=0;
	private JTextArea outputArea;
	private Player[] players;
	private ServerSocket server;
	private int currentPlayer;
	private final static int PLAYER_1 = 0;
	private final static String[] MARKS = { "1", "2" };
	private ExecutorService runGame;
	private Lock gameLock;
	private Condition otherPlayerConnected;
	private Condition otherPlayerTurn;
	public SnakeLadderServer()
	{
		super("Snakes and Ladders Server");
		runGame = Executors.newFixedThreadPool(2);
		gameLock = new ReentrantLock();
		otherPlayerConnected = gameLock.newCondition();
		otherPlayerTurn = gameLock.newCondition();
		for(int i=0; i<2; i++)
			score[i] = 0;
		players = new Player[2];
		currentPlayer = PLAYER_1;
		try{
			server = new ServerSocket(12345,2);
		}
		catch(IOException e){
			e.printStackTrace();
			System.exit(1);
		}	 
		outputArea = new JTextArea();
		outputArea.setEditable(false);
		add(new JScrollPane(outputArea), BorderLayout.CENTER);
		outputArea.setText("Server awaiting connections\n");
		setSize(300,300);
		setVisible(true);
	}
	
	public void execute()
	{
		for(int i=0; i < players.length; i++){
			try{
				players[i] = new Player(server.accept(),i);
				runGame.execute(players[i]);
			}
			catch(IOException e){
				e.printStackTrace();
				System.exit(1);
			}
		}
		gameLock.lock();
		try{
			players[PLAYER_1].setSuspended(false);
			otherPlayerConnected.signal();
		}
		finally{
			gameLock.unlock();
		}
	}
	
	private void displayMessage(final String msg)
	{
		SwingUtilities.invokeLater(
				new Runnable()
				{
					public void run()
					{
						outputArea.append(msg);
					}
				}
		);
	}
	
	public void Move(int value,int player)
	{
		while(player != currentPlayer)
		{
			gameLock.lock();
			try{
				otherPlayerTurn.await();
			}
			catch (InterruptedException e){
				e.printStackTrace();
			}
			finally{
				gameLock.unlock();
			}
		}
		current_score(value);
		if(value==6){
			players[currentPlayer].scoredSix(score[currentPlayer]);
		}
		if(value!=6){
			players[currentPlayer].myMove(score[currentPlayer]);
			currentPlayer = (currentPlayer + 1) % 2;
			players[currentPlayer].otherPlayerMoved(score[(currentPlayer+1)%2]);
			gameLock.lock();
			try{
				otherPlayerTurn.signal();
			}
			finally{
				gameLock.unlock();
			}
		}
	}
	
	private void current_score(int value) {
		if (score[currentPlayer]+value<=100)
	    {
	        if( (value!=6)&&score[currentPlayer]==0){}
	        else score[currentPlayer]+=value;
	        score[currentPlayer]=Check_ladder_snake(score[currentPlayer]);
	    }
	}
	private int Check_ladder_snake(int i) {
		switch(i){
		case 13: return 34;
		case 40: return 82;
		case 57: return 75;
		case 70: return 89;
		case 30: return 7;
		case 38: return 18;
		case 63: return 36;
		case 73: return 50;
		case 98: return 15;
		default: return i;
		}
	}
	
	public boolean isGameOver(){
		if(score[0]==100||score[1]==100)
			return true;
		return false;
	}
	
	private class Player implements Runnable
	{
		private Socket connection;
		private Scanner input;
		private Formatter output;
		private int playerNumber;
		private String mark;
		private boolean suspended = true;
		
		public Player(Socket socket, int number ){
			playerNumber = number;
			mark = MARKS[playerNumber];
			connection = socket;
			try{
				input = new Scanner(connection.getInputStream());
				output = new Formatter(connection.getOutputStream());
			}
			catch(IOException e){
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		public void scoredSix(int value) {
			output.format("Six\n");
			output.format("%d\n",value);
			output.flush();
		}

		public void otherPlayerMoved(int value){
			output.format("Opponent moved\n");
			output.format("%d\n", value);
			output.flush();
		}
	
		public void myMove(int value){
			output.format("Valid move.\n");
			output.format("%d\n", value);
			output.flush();
		}
		
		public void run()
		{
			try{
				displayMessage("Player " + mark + " connected\n");
				output.format("%d\n",playerNumber);
				output.flush();
				if (playerNumber == PLAYER_1){
					output.format("%s\n%s", "Player 1 connected","Waiting for another player\n");
					output.flush();
					gameLock.lock();
					try{
						while(suspended){
							otherPlayerConnected.await();
						}
					}
					catch(InterruptedException e){
						e.printStackTrace();
					}
					finally{
						gameLock.unlock();
					}
					output.format("Other player connected. Your move.\n");
					output.flush();
				}
				else{
					output.format("Player 2 connected, please wait\n");
					output.flush();
				}
				while(!isGameOver()){
					int location = 0;
					if(input.hasNext())
						location = input.nextInt();
					if(location==0)
						count++;
					if(count==2) System.exit(1);
					Move(location, playerNumber);
					displayMessage("\nPlayer "+mark+ ": " + location);
				}
			}
			finally{
				try{
					connection.close();
		    	}
				catch(IOException e){
					e.printStackTrace();
					System.exit(1);
		    	}
				finally{
					System.exit(1);
				}
			}
		}
		public void setSuspended(boolean status){
			suspended = status;
		}
	}
}