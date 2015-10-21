/* This class implements the remote interface rmiClientInterface. */

import java.rmi.*;
import java.rmi.server.*;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import java.util.*;

public class rmiClientImpl extends UnicastRemoteObject implements rmiClientInterface 
{
	public rmiClientImpl() throws RemoteException 
	{
		super();
	}

	public boolean recive(String usr, int session_id, byte[] m, String h)
	{
		try
		{
			String plaintext = new String(rmiClient.decryption(m, rmiClient.chat.session_key), "UTF8");
			String message_hash = new String(rmiClient.hash((rmiClient.chat.session_key+usr+plaintext+rmiClient.chat.session_key).getBytes()), "UTF8");

			if(message_hash.equals(h))
			{
				rmiClient.chat.chat_text.append(usr + ": " + plaintext + "\n");

				return true;
			}
			else
				rmiClient.chat.chat_text.append("You get invalid message from " + usr + "\n");
		}
		catch(Exception e)
		{
			JOptionPane.showMessageDialog(null, e.toString(), "Exception in rmiClientImpl.recive", JOptionPane.ERROR_MESSAGE);
		}

		return false;
	}	

	public boolean invite(String usr, int session_id, String h) throws RemoteException
	{
		try
		{
			String correct_hash = new String(rmiClient.hash((rmiClient.validation_code + usr + session_id + rmiClient.validation_code).getBytes()), "UTF8");

			if(h.equals(correct_hash))
			{
				Object[] options = {"Accept", "Ignore"};

				if(JOptionPane.showOptionDialog(null, usr + " wants to invite you to a session", "Invitation to session", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null) == JOptionPane.YES_OPTION)
				{
					rmiServerInterface rmiServer = (rmiServerInterface)Naming.lookup(rmiClient.url);
					byte[] r0 = rmiClient.encryption(Integer.toString(session_id), rmiClient.validation_code);

					String session_key = rmiServer.accept(rmiClient.username, session_id, r0);

					byte[] r = rmiClient.encryption(session_key, rmiClient.validation_code);

					if(rmiServer.join(rmiClient.username, session_id, r))
					{
						ChatFrame w = new ChatFrame(session_id, session_key);
						w.setVisible(true);
					}
					else
						JOptionPane.showMessageDialog(null, "Sorry, can't join to the session !", "", JOptionPane.WARNING_MESSAGE);
				}
			}
		}
		catch(Exception e)
		{
			JOptionPane.showMessageDialog(null, e.toString(), "Exception in rmiClientImpl.invite", JOptionPane.ERROR_MESSAGE);
		}

		return false;
	}	
}
