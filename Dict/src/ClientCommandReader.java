/*
   The University of Melbourne
   School of Computing and Information Systems
   Author: Chenyuan Zhang
   Student ID: 815901
*/

import java.io.BufferedReader;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ClientCommandReader extends Thread {

	private BufferedReader reader; 
	private BlockingQueue<Command> commandQueue;
	
	public ClientCommandReader(BufferedReader reader, BlockingQueue<Command> commandQueue) {
		this.reader = reader;
		this.commandQueue = commandQueue;
	}
	
	@Override
	//This thread reads messages from the client's socket input stream
	public void run() {
		try {
			
			System.out.println(Thread.currentThread().getName() 
					+ " - Reading messages from client connection");
			
			String clientCmd = null;
			JSONParser parser = new JSONParser();
			while ((clientCmd = reader.readLine())!=null) {
				System.out.println(Thread.currentThread().getName() 
						+ " - Message from client received: " + clientCmd);
				//place the message in the queue for the client connection thread to process
				JSONObject command = (JSONObject) parser.parse(clientCmd);
				Command cmd = new Command(true, false, command);
				commandQueue.add(cmd);
			}
			
			//If the end of the stream was reached, the client closed the connection
			//Put the exit message in the queue to allow the client connection thread to 
			//close the socket
			Command exit = new Command(false, true, null);
			commandQueue.add(exit);
			
		} catch (SocketException e) {
			//In some platforms like windows, when the end of stream is reached, instead
			//of returning null, the readLine method throws a SocketException, so 
			//do whatever you do when the while loop ends here as well
			Command exit = new Command(false, true, null);
			commandQueue.add(exit);		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
