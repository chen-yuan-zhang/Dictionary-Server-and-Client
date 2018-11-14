/*
   The University of Melbourne
   School of Computing and Information Systems
   Author: Chenyuan Zhang
   Student ID: 815901
*/


import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import org.json.simple.JSONObject;

public class Server {

	private static final int DEFAULT_PORT_NUM = 4444; // Default Port Number
	private static final int PORT_NUM_UPPERBOUND = 4999;
	private static final int PORT_NUM_LOWERBOUND = 1024;
	//private static final String DEFAULT_DICT_FILE = "dict.dat";

	private Scanner keyboard;
	private int portNum;
	//String dictFile;
	private ServerSocket serverSocket;
	private int clientNum;

	public Server() {
		keyboard = new Scanner(System.in);

		/*
		while (true) {
			dictFile = getDictFile(keyboard);

			try {
				readDictFile(dictFile);
				break;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Fail to read the dictionary file! Please try again!");
			}
		}
		

		System.out
				.println(Thread.currentThread().getName() + " - DictServer run on dictionary file '" + dictFile + "'.");
		*/

		while (true) {
			portNum = getPortNum(keyboard);

			try {
				serverSocket = new ServerSocket(portNum);
				break;
			} catch (Exception e) {
				System.out.println("Fail to open server! Please try again!");
			}
		}

		System.out.println(
				Thread.currentThread().getName() + " - GameServer listening on port " + portNum + " for a connection");
		clientNum = 0;
		//SaveThread saveThread = new SaveThread(this);
		//saveThread.start();
	}

	/*
	private String getDictFile(Scanner terminal) {
		System.out.print("Please enter the dictionary file(default file is '" + DEFAULT_DICT_FILE + "'): ");
		String input = terminal.nextLine();
		if (input.equals(""))
			return DEFAULT_DICT_FILE;

		return input;
	}
	

	@SuppressWarnings("unchecked")
	private void readDictFile(String filename) throws Exception {
		try {
			ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(filename));
			ServerState.getInstance().setWordList((ArrayList<JSONObject>) inputStream.readObject());
			inputStream.close();

		} catch (FileNotFoundException e) {
			// No existing file
			System.out.println("No existing file. A new dictionary file '" + filename + "' will be created.");
			ServerState.getInstance().setWordList(new ArrayList<JSONObject>());
		}
	}
	*/

	private int getPortNum(Scanner terminal) {
		while (true) {
			System.out.print("Please enter the port number(default port number is " + DEFAULT_PORT_NUM + "): ");
			String input = terminal.nextLine();
			if (input.equals(""))
				return DEFAULT_PORT_NUM;

			try {
				int inputNum = Integer.parseInt(input);
				if ((inputNum <= PORT_NUM_UPPERBOUND) && (inputNum >= PORT_NUM_LOWERBOUND)) {
					return inputNum;
				}
				System.out.println(
						"Please enter a valid port number(" + PORT_NUM_LOWERBOUND + "-" + PORT_NUM_UPPERBOUND + ").");
				System.out.println();
			} catch (NumberFormatException e) {
				System.out.println("Please enter a number.");
				System.out.println();
			}
		}
	}

	public static void main(String[] args) {

		Server s = new Server();

		try {

			while (true) {
				Socket clientSocket;

				clientSocket = s.serverSocket.accept();

				s.clientNum++;

				System.out
						.println(Thread.currentThread().getName() + " - Client " + s.clientNum + " conection accepted");

				ClientConnection clientConnection = new ClientConnection(clientSocket, s.clientNum);
				clientConnection.setName("Thread" + s.clientNum);
				clientConnection.start();

				ServerState.getInstance().clientConnected(clientConnection);
				
				//Gamelobby log information
				/*
				for (ClientConnection client : ServerState.getInstance().getConnectedClients()) {
					client.write("[Client" + s.clientNum + "]: New user login in...");
				}
				*/
				

				System.out.println(
						ServerState.getInstance().getConnectedClients().size() + " clients are connecting now.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (!s.serverSocket.isClosed()) {
				try {
					s.serverSocket.close();

					/*
					try {
						ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(s.dictFile));
						outputStream.writeObject(ServerState.getInstance().getWordList());
						outputStream.close();
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("Fail to save the dictionary file!");
					}
					*/

				} catch (Exception e) {
					e.printStackTrace();
					System.exit(0);
				}
			}
		}
	}
}
