import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import java.sql.*; 

class LoginFrame extends JFrame
{
	private JTextField _username = new JTextField(5);	  
	private JTextField _password = new JPasswordField(5); //JPasswordField

	public LoginFrame()
	{		
		setResizable(false);
							
		JPanel content = new JPanel(new FlowLayout());	
		JButton login = new JButton("Login");

		content.add(new JLabel("Username"));
		content.add(_username);

		content.add(new JLabel("Password"));
		content.add(_password);

		content.add(login);

		login.addActionListener(new login());

		setContentPane(content);
		pack(); // Layout components.
		setTitle("Login");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null); // Center window.
	}

	class login implements ActionListener
	{		 
		public void actionPerformed(ActionEvent ae)
		{
			try
			{
				String usr = _username.getText();
				String pwd = _password.getText(); //getPassword().toString();

				rmiServerInterface rmiServer = (rmiServerInterface)Naming.lookup(rmiClient.url);

				byte[] encrypted_code = rmiServer.code(usr);
				byte[] decrypted_code = rmiClient.decryption(encrypted_code, pwd);
				String utd_code = new String(decrypted_code, "UTF8");

				byte[] en_vc = rmiClient.encryption(utd_code, pwd);

				rmiClientInterface rmiClientObject = new rmiClientImpl();
				
				if(rmiServer.login(usr, en_vc, rmiClientObject))
				{
					rmiClient.username = usr;
					rmiClient.password = pwd;
					rmiClient.validation_code = new String(decrypted_code, "UTF8");

					byte[] r = rmiClient.encryption(utd_code, utd_code);

					Vector session = rmiServer.session(usr, r);

					int session_id = (Integer)session.elementAt(0);
					String session_key = (String)session.elementAt(1);

					if (rmiServer.join(rmiClient.username, session_id, rmiClient.encryption(session_key, utd_code)))
					{	
						ChatFrame chat = new ChatFrame(session_id, session_key);
						//rmiClient.create_session(session_id, session_key, chat);

						/*Vector item = new Vector();

						item.addElement(session_id);
						item.addElement(session_key);
						item.addElement(chat);

						rmiClient.sessionList.addElement(item);*/

						rmiClient.chat = chat;
						setVisible(false);
						rmiClient.chat.setVisible(true);
						/*ChatFrame w = (ChatFrame)rmiClient.sessionList.elementAt(0);
						w.setVisible(true);*/
						//chat.setVisible(true);
					}
					else
						JOptionPane.showMessageDialog(null, "Sorry, create session !", "", JOptionPane.WARNING_MESSAGE);
				}
				else
					JOptionPane.showMessageDialog(null, "Sorry, failure login !", "", JOptionPane.WARNING_MESSAGE);
			}
			catch(Exception e)
			{
				JOptionPane.showMessageDialog(null, e.toString(), "Exception in LoginFrame.login", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
