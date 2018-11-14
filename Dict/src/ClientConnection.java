/*
   The University of Melbourne
   School of Computing and Information Systems
   Author: Chenyuan Zhang
   Student ID: 815901
*/
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.simple.JSONObject;

public class ClientConnection extends Thread {

	private Socket clientSocket;
	private BufferedReader reader;
	private BufferedWriter writer;
	private User user;
	// This queue holds messages sent by the client or messages intended for the
	// client from other threads
	private BlockingQueue<Command> commandQueue;
	private int clientNum;

	public ClientConnection(Socket clientSocket, int clientNum) {
		try {
			this.clientSocket = clientSocket;
			this.clientNum = clientNum;
			reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
			writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
			JSONObject welcomeInfo = new JSONObject();
			welcomeInfo.put("Type","Welcome");
			write(welcomeInfo);
			commandQueue = new LinkedBlockingQueue<Command>();
			user = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public User getUser() {
		return user;
	}

	@Override
	public void run() {

		try {

			// Start the client message reader thread. It 'listens' for any
			// incoming messages from the client's socket input stream and places
			// them in a queue (producer)
			ClientCommandReader messageReader = new ClientCommandReader(reader, commandQueue);
			messageReader.setName(this.getName() + "Reader");
			messageReader.start();

			System.out.println(Thread.currentThread().getName() + " - Processing client " + clientNum + "  commands");

			// Monitor the queue to process any incoming messages (consumer)
			while (true) {

				// This method blocks until there is something to take from the queue
				// (when the messageReader receives a message and places it on the queue
				// or when another thread places a message on this client's queue)
				Command cmd = commandQueue.take();
				// process the message
				// If the message is "exit" and from a thread then
				// it means the client has closed the connection and we must
				// close the socket and update the server state
				// (See what the message reader does when it reads the end of stream
				// from the client socket)
				if (cmd.isExit()) {
					break;
				}

				if (cmd.isFromClient()) {
					JSONObject command = cmd.getCommand();
					switch ((String) command.get("Type")) {
											
					case "Game":
						ServerState.getInstance().findGame((int) command.get("GameID")).getCommandQueue()
								.add((JSONObject) command.get("Command"));
						break;
					
					case "NewUser":
						String username = (String) command.get("Name");
						boolean flag = true;
						for(User user: ServerState.getInstance().getUserList()) {
							if(user.getUsername().equals(username)) {
								flag = false;
								JSONObject warning = new JSONObject();
								warning.put("Type","Warning");
								warning.put("Info","Username already exists!");
								write(warning);
								break;
							}
						}
						if (flag) {
							user = new User(this, username);
							ServerState.getInstance().newUser(user);
						}
						break;
						
					case "NewGame":
						ServerState.getInstance().createNewGame(user);
						break;
						
					case "EnterGame":
						ServerState.getInstance().enterGame(user, (int)command.get("GameID"));
						break;
						
					case "LeaveGame":
						ServerState.getInstance().leaveGame(user);
					
					case "Ready":
						ServerState.getInstance().ready(user);
					
					default:
						break;
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				clientSocket.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
			System.out.println(Thread.currentThread().getName() + " - Client " + clientNum + " disconnected");
			ServerState.getInstance().clientDisconnected(this);
			/*
			List<ClientConnection> connectedClients = ServerState.getInstance().getConnectedClients();
			for (ClientConnection client : connectedClients) {
				try {
					client.write("[Client" + clientNum + "]: Log off...");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			*/
		}
	}

	public BlockingQueue<Command> getCommandQueue() {
		return commandQueue;
	}

	public int getClientNum() {
		return clientNum;
	}

	public void write(JSONObject cmd) throws Exception {
		writer.write(cmd.toJSONString() + "\r\n");
		writer.flush();
		System.out.println(Thread.currentThread().getName() + " - Message sent to client " + clientNum);

	}
}
