import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import java.sql.*; 

class ChatFrame extends JFrame
{
	public static int session_id;
	public static String session_key;
	public static JTextArea chat_text = new JTextArea(null , null , 20 , 20);
	public static JTextArea _message = new JTextArea(null , null , 20 , 20);

	public ChatFrame(int id, String key)
	{
		session_id = id;
		session_key = key;

		setBounds(500, 600, 50, 50);

		JPanel content = new JPanel(new FlowLayout());										
		JButton send = new JButton("Send");
		JButton invite = new JButton("Invite");
		JButton history = new JButton("History");

		content.add(chat_text);
		content.add(_message);
		content.add(send);
		content.add(invite);
		content.add(history);

		send.addActionListener(new sendMessage());
		invite.addActionListener(new inviteUser());
		history.addActionListener(new showHistory());

		chat_text.setEditable(false);
		send.setFocusable(true);

		chat_text.setText("");
		chat_text.append("Welcome " + rmiClient.username + " to session #" + session_id + "\n");

		setContentPane(content);
		pack();
		setTitle("SIMS [" + rmiClient.username + ", #" + session_id + "]");
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		addWindowListener(new java.awt.event.WindowAdapter() 
		{
			public void windowClosing(WindowEvent winEvt) 
			{
				try
				{
					rmiServerInterface rmiServer = (rmiServerInterface)Naming.lookup(rmiClient.url);

					byte[] r = rmiClient.encryption(session_key, rmiClient.validation_code);
					byte[] r2 = rmiClient.encryption(rmiClient.username, rmiClient.validation_code);

					if(rmiServer.leave(rmiClient.username, session_id, r))
						if(rmiServer.logout(rmiClient.username, r2))
							System.exit(0); 
				}
				catch(Exception e){}
			}
		});
	}

	class sendMessage implements ActionListener
	{		 
		public void actionPerformed(ActionEvent e)
		{
			String msg = _message.getText();

			try
			{
				rmiServerInterface rmiServer = (rmiServerInterface)Naming.lookup(rmiClient.url);

				byte[] h = rmiClient.hash((session_key+rmiClient.username+msg+session_key).getBytes());

				rmiServer.send(rmiClient.username, rmiClient.encryption(msg, session_key), session_id, new String(h, "UTF8"));
				_message.setText("");
			}
			catch(Exception exc)
			{
				JOptionPane.showMessageDialog(null, exc.toString(), "Error in ChatFrame.sendMessage: ", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	class inviteUser implements ActionListener
	{		 
		public void actionPerformed(ActionEvent e)
		{
			try
			{
				rmiServerInterface rmiServer = (rmiServerInterface)Naming.lookup(rmiClient.url);
				Vector users = rmiServer.users(rmiClient.username);

				if(users.size() > 0)
				{
					String user = (String)JOptionPane.showInputDialog(new JFrame(), "Invite one user to your session:", "Invite user ..", JOptionPane.INFORMATION_MESSAGE, null, (String[])users.toArray(new String[0]), null);

					if(user != null)
					{
						String hash = new String(rmiClient.hash((session_key + rmiClient.validation_code + user + session_key).getBytes()), "UTF8");

						rmiServer.invite(rmiClient.username, user, session_id, hash);
					}
				}
				else
					JOptionPane.showMessageDialog(null,"Sorry, There are not online users", "Invite User", JOptionPane.INFORMATION_MESSAGE);
			}
			catch(Exception exc)
			{
				JOptionPane.showMessageDialog(null, exc.toString(), "Error in ChatFrame.inviteUser: ", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	class showHistory implements ActionListener
	{		 
		public void actionPerformed(ActionEvent e)
		{
			try
			{
				rmiServerInterface rmiServer = (rmiServerInterface)Naming.lookup(rmiClient.url);

				byte[] r = rmiClient.encryption(rmiClient.username, rmiClient.validation_code);

				Vector sessions = rmiServer.sessions(rmiClient.username, r);

				if(sessions.size() > 0)
				{
					Integer session = (Integer)JOptionPane.showInputDialog(new JFrame(), "Select session number:", "History", JOptionPane.INFORMATION_MESSAGE, null, sessions.toArray(), null);

					if(session != null && session.intValue() > 0)
					{
						byte[] r2 = rmiClient.encryption(rmiClient.username, rmiClient.validation_code);

						Vector messageList = rmiServer.messages(rmiClient.username, session.intValue(), r2);

						HistoryFrame win = new HistoryFrame(messageList);
						win.setVisible(true);
					}
				}
				else
					JOptionPane.showMessageDialog(null,"There are not history", "Invite User", JOptionPane.INFORMATION_MESSAGE);
			}
			catch(Exception exc)
			{
				JOptionPane.showMessageDialog(null, exc.toString(), "Error in ChatFrame.showHistory: ", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
