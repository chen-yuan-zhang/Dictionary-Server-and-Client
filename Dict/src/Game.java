import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Game extends Thread{

	private ArrayList<User> playerList;
	private ArrayList<User> observerList;
	private int gameID; 
	private User currentPlayer;
	private boolean gameState;	//True: playing; False: waiting
	private BlockingQueue<JSONObject> commandQueue;
	
	public void run(){
		
	}
	
	public Game(User client, int gameID) {
		playerList = new ArrayList<User>();
		playerList.add(client);
		observerList = new ArrayList<User>();
		observerList.add(client);
		this.gameID = gameID;
		gameState = false;
		commandQueue = new LinkedBlockingQueue<JSONObject>();
	}
	
	public void newUserEnter(User client) {
		if (!gameState) {
			playerList.add(client);
			observerList.add(client);
		}
		else {
			observerList.add(client);
		}
		
	}
	

	
	public void userLeave(User user) {
		// TODO Auto-generated method stub
		playerList.remove(user);
		observerList.remove(user);
		
	}
	
	
	public void broadcastToPlayer(JSONObject cmd) {
		for (User client : playerList) {
			try {
				client.getConnection().write(cmd);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void getGameState() {
		
	}
	
	public void updateGameView() {
		JSONObject update = new JSONObject();
		update.put("Type","UpdateGameView");
		
		JSONArray playerArray = new JSONArray();
		for(int i=0;i<playerList.size();i++) {
			playerArray.add(playerList.get(i).getJSONObject());
		}
		update.put("PlayerList", playerArray);
		
		JSONArray observerArray = new JSONArray();
		for(int i=0;i<observerList.size();i++) {
			observerArray.add(observerList.get(i).getJSONObject());
		}
		update.put("ObserverList", observerArray);
		broadcastToObserver(update);
	}
	
	public void broadcastToObserver(JSONObject cmd) {
		for (User client : observerList) {
			try {
				client.getConnection().write(cmd);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public int getGameID() {
		return gameID;
	}
	
	public int getNumOfPlayer() {
		return playerList.size();
	}
	
	public int getNumOfObserver() {
		return observerList.size();
	}


	public BlockingQueue<JSONObject> getCommandQueue() {
		// TODO Auto-generated method stub
		return commandQueue;
	}

}
