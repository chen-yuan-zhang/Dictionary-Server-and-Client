/*
   The University of Melbourne
   School of Computing and Information Systems
   Author: Chenyuan Zhang
   Student ID: 815901
*/


import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.awt.event.ActionEvent;
import javax.swing.JTextPane;

import org.json.simple.JSONObject;

import javax.swing.JLabel;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.*;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JScrollBar;
import java.awt.Color;

public class Dictionary_Client {

	private JFrame frame;
	private static final String SPELL_PATTERN = "^[a-zA-Z]+$";
	private static Socket socket = null;
	private static BufferedReader reader = null;
	private static BufferedWriter writer = null;
	private MessageListener ml = null;
	private static String serverAddress;
	private static String portNumber;

	
	public static synchronized BufferedReader connectToServer() throws Exception{
		socket = new Socket(serverAddress, Integer.parseInt(portNumber));
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
		writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
		return reader;
	}
	/**
	 * Launch the application.
	 */

	public static class Login {

		private JFrame frmLogin;
		private JTextField server;
		private JTextField port;

		/**
		 * 
		 * 
		 * /** Create the application.
		 */
		public Login() {
			initialize();
		}

		/**
		 * Initialize the contents of the frame.
		 */
		private void initialize() {
			frmLogin = new JFrame();
			frmLogin.setTitle("Login");
			frmLogin.setBounds(100, 100, 450, 300);
			frmLogin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frmLogin.getContentPane().setLayout(null);

			server = new JTextField();
			server.setBounds(224, 50, 126, 35);
			frmLogin.getContentPane().add(server);
			server.setColumns(10);

			port = new JTextField();
			port.setBounds(224, 119, 126, 35);
			frmLogin.getContentPane().add(port);
			port.setColumns(10);

			JLabel lblNewLabel = new JLabel("Server Address");
			lblNewLabel.setBounds(21, 53, 168, 29);
			frmLogin.getContentPane().add(lblNewLabel);

			JLabel lblPortNumber = new JLabel("Port Number");
			lblPortNumber.setBounds(21, 122, 132, 29);
			frmLogin.getContentPane().add(lblPortNumber);

			JButton btnLogin = new JButton("Login");
			btnLogin.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					serverAddress = server.getText();
					portNumber = port.getText();
					if (serverAddress.equals(""))
						JOptionPane.showMessageDialog(null, "Please enter the server address!", "Error", JOptionPane.ERROR_MESSAGE);
					else if (portNumber.equals(""))
						JOptionPane.showMessageDialog(null, "Please enter the port number!", "Error", JOptionPane.ERROR_MESSAGE);
					else {
						try {
							connectToServer();
							Dictionary_Client window = new Dictionary_Client();
							window.frame.setVisible(true);
				
						} catch (Exception e) {
							e.printStackTrace();
							JOptionPane.showMessageDialog(null, "Fail to connect the server! Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
							try {
								socket.close();
							} catch (Exception ee) {

							}
							Login newloginwindow = new Login();
							newloginwindow.frmLogin.setVisible(true);
						}
						finally {
							frmLogin.dispose();
							
						}
					}
				}
			});
			btnLogin.setBounds(107, 175, 153, 37);
			frmLogin.getContentPane().add(btnLogin);
		}
	}

	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Login loginwindow = new Login();
					loginwindow.frmLogin.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Dictionary_Client() {
		initialize();
	}

	private boolean isLegal(String spell) {
		return Pattern.matches(SPELL_PATTERN, spell);
	}

	/**
	 * Initialize the contents of the frame.
	 * @wbp.parser.entryPoint
	 */
	private void initialize() {
		frame = new JFrame();
		try {
			frame.setTitle(reader.readLine());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		frame.setBounds(100, 100, 743, 553);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(391, 77, 284, 340);
		frame.getContentPane().add(scrollPane);

		JTextPane textPane = new JTextPane();
		scrollPane.setViewportView(textPane);

		ml = new MessageListener(textPane, frame, reader);
		ml.start();

		JButton btnNewButton = new JButton("Add a new word");

		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String spell = JOptionPane.showInputDialog(null, "Add a new word");
				if (!(spell == null)) {
					if (isLegal(spell)) {

						JSONObject info = new JSONObject();
						info.put("Operation", "addWord");

						JSONObject newWord = new JSONObject();
						newWord.put("Spelling", spell);

						ArrayList<String> meanings = new ArrayList<String>();

						meanings.add(JOptionPane.showInputDialog(null, "Add a meaning"));

						while (true) {
							if (!(JOptionPane.showConfirmDialog(null, "Do you want to add more meanings?",
									"More meanings?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION))
								break;
							;
							meanings.add(JOptionPane.showInputDialog(null, "Add a meaning"));
						}
						newWord.put("Meanings", meanings);
						info.put("Item", newWord);
						try {
							writer.write(info.toJSONString() + "\r\n");
							writer.flush();
						} catch (Exception ee) {
							JOptionPane.showMessageDialog(null, "Fail to connect the server!", "Error", JOptionPane.ERROR_MESSAGE);
						}

					} else {
						JOptionPane.showMessageDialog(null, "Not a legal word!");
					}

				}
			}
		});
		btnNewButton.setBounds(79, 77, 201, 37);
		frame.getContentPane().add(btnNewButton);

		JButton btnRemoveAWord = new JButton("Remove a word");
		btnRemoveAWord.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String spell = JOptionPane.showInputDialog(null, "Remove a word");
				if (!(spell == null)) {
					if (isLegal(spell)) {
						JSONObject info = new JSONObject();
						info.put("Operation", "delWord");
						info.put("Item", spell);
						try {
							writer.write(info.toJSONString() + "\r\n");
							writer.flush();
						} catch (Exception ee) {
							JOptionPane.showMessageDialog(null, "Fail to connect the server!", "Error", JOptionPane.ERROR_MESSAGE);

						}
					} else {
						JOptionPane.showMessageDialog(null, "Not a legal word!");
					}
				}

			}
		});
		btnRemoveAWord.setBounds(79, 189, 189, 37);
		frame.getContentPane().add(btnRemoveAWord);

		JButton btnNewButton_1 = new JButton("Query a word");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String spell = JOptionPane.showInputDialog(null, "Query a word");
				if (!(spell == null)) {
					if (isLegal(spell)) {
						JSONObject info = new JSONObject();
						info.put("Operation", "queryWord");
						info.put("Item", spell);
						try {
							writer.write(info.toJSONString() + "\r\n");
							writer.flush();
						} catch (Exception ee) {
							JOptionPane.showMessageDialog(null, "Fail to connect the server!", "Error", JOptionPane.ERROR_MESSAGE);

						}
					} else {
						JOptionPane.showMessageDialog(null, "Not a legal word!");
					}
				}
			}
		});
		btnNewButton_1.setBounds(79, 293, 177, 37);
		frame.getContentPane().add(btnNewButton_1);

		JLabel lblInformationWindow = new JLabel("Information Window");
		lblInformationWindow.setFont(new Font("Arial", Font.PLAIN, 24));
		lblInformationWindow.setBounds(391, 21, 233, 54);
		frame.getContentPane().add(lblInformationWindow);
		
		JButton backToLogin = new JButton("Back to Login");
		backToLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				frame.dispose();
				
				try {
					ml.closeThread();
					socket.close();
				} catch (Exception eee) {
					eee.printStackTrace();
				}
				
				Login newloginwindow = new Login();
				newloginwindow.frmLogin.setVisible(true);
			}
		});
		backToLogin.setBackground(Color.ORANGE);
		backToLogin.setForeground(Color.RED);
		backToLogin.setBounds(54, 380, 214, 81);
		frame.getContentPane().add(backToLogin);
		
		
	}

	public JFrame getFrame() {
		return frame;
	}
}
