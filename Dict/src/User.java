import org.json.simple.JSONObject;

public class User {
	private ClientConnection connection;
	private String userName;
	private Game currentGame;
	private boolean isReady;
	
	public User(ClientConnection connection, String userName){
		this.connection = connection;
		this.userName = userName;
		currentGame = null;
		isReady = false;
	}
	
	public void changeReadyState() {
		isReady = !isReady;
	}
		
	public ClientConnection getConnection() {
		return connection;
	}
	
	public String getUsername() {
		return userName;
	}

	public void setGame(Game game) {
		// TODO Auto-generated method stub
		currentGame = game;
	}
	
	public Game getGame() {
		return currentGame;
	}
	
	public JSONObject getJSONObject(){
		JSONObject result = new JSONObject();
		result.put("Name",userName);
		result.put("IsReady", isReady);
		if (currentGame==null)
			result.put("Game","Idle");
		else
			result.put("Game","Game" + currentGame.getGameID());
		
		return result;
	}
}
