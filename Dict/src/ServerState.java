/*
   The University of Melbourne
   School of Computing and Information Systems
   Author: Chenyuan Zhang
   Student ID: 815901
*/


import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ServerState {

	private static ServerState instance;
	private ArrayList<ClientConnection> connectedClients;
	private ArrayList<Game> gameList;
	private ArrayList<User> userList;
	private int gameID;

	private ServerState() {
		connectedClients = new ArrayList<ClientConnection>();
		gameList = new ArrayList<Game>();
		userList = new ArrayList<User>();
		gameID = 0;
	}

	public static synchronized ServerState getInstance() {
		if (instance == null) {
			instance = new ServerState();
		}
		return instance;
	}
	
	public synchronized void createNewGame(User user) {
		gameID++;
		Game newGame = new Game(user, gameID);
		gameList.add(newGame);
		user.setGame(newGame);
		newGame.updateGameView();
		updateLobby();
	}
	
	public synchronized void enterGame(User user, int gameID) {
		Game g = findGame(gameID);
		g.newUserEnter(user);
		user.setGame(g);
		g.updateGameView();
		updateLobby();
	}
	
	public synchronized void ready(User user) {
		user.changeReadyState();
		user.getGame().updateGameView();
	}
	
	public synchronized void leaveGame(User user) {
		Game g = user.getGame();
		g.userLeave(user);
		user.setGame(null);
		if(g.getNumOfObserver()==0) {
			deleteGame(g);
		}
		else {
			g.updateGameView();
		}
	}

	public synchronized void deleteGame(Game game) {
		gameList.remove(game);
		updateLobby();
	}

	public synchronized void clientConnected(ClientConnection client) {
		connectedClients.add(client);
	}

	public synchronized void clientDisconnected(ClientConnection client) {
		connectedClients.remove(client);
		userLogout(client.getUser());
	}

	public synchronized ArrayList<ClientConnection> getConnectedClients() {
		return connectedClients;
	}
	
	public synchronized void newUser(User user) {
		userList.add(user);
		updateLobby();
	}
	
	public synchronized void userLogout(User user) {
		if(userList.remove(user)) {
			updateLobby();
		}
	}
	
	private void updateLobby() {
		JSONObject update=new JSONObject();
		update.put("Type","UpdateLobby");
		
		JSONArray userArray = new JSONArray();
		for(int i=0;i<userList.size();i++) {
			userArray.add(userList.get(i).getJSONObject());
		}
		update.put("UserList", userArray);
		
		JSONArray gameArray = new JSONArray();
		for(int i=0;i<gameList.size();i++) {
			JSONObject gameObject = new JSONObject();
			Game g = gameList.get(i);
			gameObject.put("GameID",g.getGameID());
			gameObject.put("NumOfPlayer", g.getNumOfPlayer());
			gameObject.put("NumOfObserver", g.getNumOfObserver());
			gameObject.put("GameState", g.getGameState());
			gameArray.add(gameObject);
		}
		update.put("GameList", gameList);
		
		broadcast(update);
	}
	

	public synchronized ArrayList<Game> getGameList() {
		return gameList;
	}

	public synchronized ArrayList<User> getUserList(){
		return userList;
	}

	public Game findGame(int gameID) {
		// TODO Auto-generated method stub
		for(Game g: gameList) {
			if (g.getGameID()==gameID)
				return g;
		}
		return null;
	}

	private void broadcast(JSONObject cmd) {
		// TODO Auto-generated method stub
		for(ClientConnection client: connectedClients) {
			try {
				client.write(cmd);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
