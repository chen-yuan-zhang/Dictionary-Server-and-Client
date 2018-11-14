/*
   The University of Melbourne
   School of Computing and Information Systems
   Author: Chenyuan Zhang
   Student ID: 815901
*/

import org.json.simple.JSONObject;

public class Command {
	private boolean isFromClient;
	private boolean isExit;
	private JSONObject command;
	
	public Command(boolean isFromClient, boolean isExit, JSONObject command) {
		this.isFromClient = isFromClient;
		this.command = command;		
		this.isExit = isExit;
	}
	
	
	@SuppressWarnings("finally")
	public JSONObject executeCommand() {
		String reply=null;
		JSONObject replyCommand = new JSONObject();
		int flag=1;

		try {
			switch ((String) command.get("Type")) {
			
			case "Username":
				String username = (String) command.get("Name");
				for(User user: ServerState.getInstance().getUserList()) {
					if(user.getUsername().equals(username)) {
						reply = "Username Already Exists. Please choose an unique username";
						replyCommand.put("Type", "Warning");
						replyCommand.put("Info", reply);
						flag = 0;
						break;
					}
				}
				if (flag!=0) {
					replyCommand.put("Type", "Newuser");
					replyCommand.put("Name", username);
				}
				break;
				
			case "Game":
				ServerState.getInstance().findGame((int) command.get("GameID")).getCommandQueue()
						.add((JSONObject) command.get("Command"));
				break;
			
			default:
				break;
			}
		}catch(Exception e) {
			reply = "Not a valid command!";
		}finally {
			replyCommand.put("Info", reply);
			replyCommand.put("Type", flag);
			return replyCommand;
		}
	}
	
	public boolean isFromClient() {
		return isFromClient;
	}
	
	public boolean isExit() {
		return isExit;
	}
	
	public JSONObject getCommand() {
		return command;
	}
}
