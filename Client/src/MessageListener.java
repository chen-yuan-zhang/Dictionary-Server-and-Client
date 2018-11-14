/*
   The University of Melbourne
   School of Computing and Information Systems
   Author: Chenyuan Zhang
   Student ID: 815901
*/

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;


public class MessageListener extends Thread {

	private boolean flag;
	private JTextPane textpane;
	private JFrame frame;
	private static final String HIGHLIGHT_PATTERN = "^.Private.*$";
	private BufferedReader reader;
	
	public MessageListener(JTextPane textpane ,JFrame frame, BufferedReader reader) {
		this.textpane = textpane;
		this.frame = frame;
		flag = true;
		this.reader = reader;
	}
	
	public void closeThread() {
		flag = false;
	}
	
	
	@Override
	public void run() {
		
		while (flag) {
			try {
				
				Style highlight = textpane.addStyle("hl", null);
				StyleConstants.setForeground(highlight, Color.RED);
				
				String msg = null;
				textpane.getDocument().insertString(textpane.getDocument().getLength(), "Connecting...\r\n", null);
				textpane.setCaretPosition(textpane.getDocument().getLength());
				//Read messages from the server while the end of the stream is not reached
				while((msg = reader.readLine()) != null) {
					//Print the messages to the console
					if (Pattern.matches(HIGHLIGHT_PATTERN, msg))
						textpane.getDocument().insertString(textpane.getDocument().getLength(), msg+"\r\n", textpane.getStyle("hl"));
					else
						textpane.getDocument().insertString(textpane.getDocument().getLength(), msg+"\r\n", null);
					
					textpane.setCaretPosition(textpane.getDocument().getLength());
				}
			} catch (SocketException e) {
				try {
					textpane.getDocument().insertString(textpane.getDocument().getLength(), "Connection failed!!!Wait 5 seconds to reconnect...\r\n", textpane.getStyle("hl"));
				} catch (BadLocationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				textpane.setCaretPosition(textpane.getDocument().getLength());
				
				try {
					sleep(5000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				if (flag) {
					try {
						reader = Dictionary_Client.connectToServer();
						try {
							frame.setTitle(reader.readLine());
						} catch (IOException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						
					}
				}
				
				

			
			} catch (Exception e) {
				e.printStackTrace();
				break;
			} 
		}
		
		
	}
}
