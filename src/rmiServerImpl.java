/* This class implements the remote interface CallbackServerInterface. */

import java.rmi.*;
import java.rmi.server.*;
import java.sql.*;
import java.util.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class rmiServerImpl extends UnicastRemoteObject implements rmiServerInterface 
{
	private Vector clientList;

	public rmiServerImpl() throws RemoteException 
	{
		super();
		clientList = new Vector();
	}

	public static String random_value()
	{
		java.util.Date now = new java.util.Date();

		long tmp = now.getTime();
		String r = Long.toString(tmp);

		return r.substring(2, 10);
	}

	public static byte[] hash(byte[] msg)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(msg);
			byte[] h = md.digest();

			return h;
		}
		catch(Exception e)
		{
			System.out.println("Exception in in rmiClinet.hash: " + e.getMessage());
		}

		return null;
	}

	public static byte[] encryption(String m, String k)
	{
		try
		{
			byte sd[] = k.getBytes("UTF8"); // must be 8 characters
			SecretKeySpec key = new SecretKeySpec(sd, "DES");

			Cipher d = Cipher.getInstance("DES");
			d.init(Cipher.ENCRYPT_MODE, key);

			byte dn[] = d.doFinal(m.getBytes("UTF8"));

			return dn;
		}
		catch(Exception e)
		{
			System.out.println("Exception in rmiServerImpl.encryption: " + e.getMessage());
		}

		return null;
	}

	public static byte[] decryption(byte[] m, String k)
	{
		try
		{
			byte sd[] = k.getBytes("UTF8"); // must be 8 characters
			SecretKeySpec key = new SecretKeySpec(sd, "DES");

			Cipher d = Cipher.getInstance("DES");
			d.init(Cipher.DECRYPT_MODE, key);

			byte dn[] = d.doFinal(m);

			return dn;

		}
		catch(Exception e)
		{
			System.out.println("Exception in rmiServerImpl.decryption: " + e.getMessage());
		}

		return null;
	}

	public byte[] code(String usr) throws RemoteException
	{
		try
		{
			String validation_code = random_value();
			String user_password = rmiServer.db.getUserPassword(usr);

			if(user_password != null)
			{
				byte[] r = encryption(validation_code , user_password);

				if (rmiServer.db.setValidationCode(usr, validation_code))
					return r;
			}
		}
		catch (Exception e) 
		{
			System.out.println("Exception in rmiServerImpl.code: " + e.getMessage());
		}
		
		return null;		
	}

	public boolean login(String usr, byte[] r, rmiClientInterface callbackClientObject) throws RemoteException
	{
		try
		{
			String validation_code = rmiServer.db.getValidationCode(usr);
			String user_password = rmiServer.db.getUserPassword(usr);
			byte[] encrypted_code = encryption(validation_code, user_password);
			String decrypted_code = new String(decryption(r, user_password), "UTF8");

			byte[] h1 = r;
			byte[] h2 = hash(validation_code.getBytes());//Arrays.equals(h1, h2)

			if(decrypted_code.equals(validation_code))
			{
				if (!(clientList.contains(usr))) 
				{
					Vector client = new Vector();

					client.addElement(usr);
					client.addElement(callbackClientObject);
					client.addElement(new Vector());

					clientList.addElement(client);
					System.out.println(usr + " is online");

					return true;
				}
			}
			
		}
		catch(Exception e)
		{
			System.out.println("Exception in rmiServerImpl.login: " + e.getMessage());			
		}

		return false;
	}

	public synchronized Vector session(String usr, byte[] r) throws RemoteException 
	{
		try
		{
			String secret_key = random_value();
			String validation_code = rmiServer.db.getValidationCode(usr);
			String user_password = rmiServer.db.getUserPassword(usr);
			String code = new String(decryption(r, validation_code), "UTF8");

			if(code.equals(rmiServer.db.getValidationCode(usr)))
			{
				int session_id = rmiServer.db.setSession(secret_key);

				Vector session = new Vector();
				session.addElement(session_id);
				session.addElement(secret_key);

				return session;
			}
			
		}
		catch(Exception e)
		{
			System.out.println("Exception in rmiServerImpl.session: " + e.getMessage());			
		}

		return null;
	}

	public boolean join(String usr, int session_id, byte[] r) throws RemoteException
	{
		try
		{
			String validation_code = rmiServer.db.getValidationCode(usr);
			String session_key = rmiServer.db.getSessionKey(session_id);
			String code = new String(decryption(r, validation_code), "UTF8");

			if(code.equals(session_key))
			{
				for (int i = 0; i < clientList.size(); i++)
				{
					Vector client = (Vector)clientList.elementAt(i);

					if(client.elementAt(0).equals(usr))
					{
						Vector sessionList = (Vector)(client.elementAt(2));

						sessionList.addElement(session_id);

						return true;
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception in rmiServerImpl.session: " + e.getMessage());			
		}

		return false;
	}

	public Vector users(String usr) throws RemoteException
	{
		Vector online = new Vector();

		for(int i = 0; i < clientList.size(); i++)
		{
			Vector client = (Vector)clientList.elementAt(i);
			String username = (String)client.elementAt(0);

			if(!username.equals(usr))
				online.addElement(client.elementAt(0));
		}

		return online;
	}

	public boolean invite(String from, String to, int session_id, String h) throws RemoteException
	{
		try
		{
			String validation_code = rmiServer.db.getValidationCode(from);
			String session_key = rmiServer.db.getSessionKey(session_id);
			String correct_hash = new String(hash((session_key + validation_code + to + session_key).getBytes()), "UTF8");

			if(h.equals(correct_hash))
			{
				rmiServer.db.addInvite(to, session_id);

				for(int i = 0; i < clientList.size(); i++)
				{
					Vector client = (Vector)clientList.elementAt(i);

					String user = (String)client.elementAt(0);

					if(user.equals(to))
					{
						validation_code = rmiServer.db.getValidationCode(to);
						String h2 = new String(hash((validation_code + from + session_id + validation_code).getBytes()), "UTF8");
						
						if(((rmiClientInterface)client.elementAt(1)).invite(from, session_id, h2))	
							return true;
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception in rmiServerImpl.invite: " + e.getMessage());			
		}

		return false;
	}

	public String accept(String usr, int session_id, byte[] r) throws RemoteException
	{
		try
		{
			String validation_code = rmiServer.db.getValidationCode(usr);
			String user_password = rmiServer.db.getUserPassword(usr);
			String code = new String(decryption(r, validation_code), "UTF8");

			if(code.equals((Integer.toString(session_id))))
			{
				if (rmiServer.db.isInvited(usr, session_id))
				{
					if(rmiServer.db.removeInvitation(usr, session_id))
						return rmiServer.db.getSessionKey(session_id);
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception in rmiServerImpl.accept: " + e.getMessage());			
		}

		return null;
	}

	public boolean send(String usr, byte[] msg, int session, String h) throws RemoteException
	{
		try
		{
			String session_key = rmiServer.db.getSessionKey(session);
			String message = new String(decryption(msg, session_key), "UTF8");

			rmiServer.db.setMessage(usr, session, message);

			for (int i = 0; i < clientList.size(); i++)
			{
				Vector client = (Vector)clientList.elementAt(i);

				if(((Vector)client.elementAt(2)).contains(session))
				{
					rmiClientInterface nextClient = (rmiClientInterface)client.elementAt(1);

					nextClient.recive(usr, session, msg, h);
				}
			}

			return true;
		}
		catch (Exception e) 
		{
			System.out.println("Exception in rmiServerImpl.send: " + e.getMessage());
		}
		
		return false;

	}

	public boolean leave(String usr, int session_id, byte[] r) throws RemoteException
	{
		try
		{
			String validation_code = rmiServer.db.getValidationCode(usr);
			String session_key = rmiServer.db.getSessionKey(session_id);
			String code = new String(decryption(r, validation_code), "UTF8");

			if(code.equals(session_key))
			{
				for (int i = 0; i < clientList.size(); i++)
				{
					Vector client = (Vector)clientList.elementAt(i);

					if(client.elementAt(0).equals(usr))
					{
						Vector sessionList = (Vector)(client.elementAt(2));

						for (int j = 0; j < sessionList.size(); j++)
						{
							Vector session = (Vector)sessionList.elementAt(j);

							if(session.elementAt(j).equals(session_id))
							{
								session.removeElementAt(j);
								return true;
							}
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception in rmiServerImpl.leave: " + e.getMessage());
		}

		return false;
	} 

	public Vector sessions(String usr, byte[] r) throws RemoteException
	{
		try
		{
			String validation_code = rmiServer.db.getValidationCode(usr);
			String code = new String(decryption(r, validation_code), "UTF8");

			if(code.equals(usr))
			{
				Vector history = rmiServer.db.getSessions(usr);
				Vector sessionList = new Vector();

				for (int i = 0; i < history.size(); i++)
				{
					Vector session = (Vector)history.elementAt(i);

					sessionList.addElement(session.elementAt(0));
				}

				return sessionList;
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception in rmiServerImpl.sessions: " + e.getMessage());
		}	

		return null;
	}

	public Vector messages(String usr, int session_id, byte[] r) throws RemoteException
	{
		try
		{
			String validation_code = rmiServer.db.getValidationCode(usr);
			String code = new String(decryption(r, validation_code), "UTF8");

			if(code.equals(usr))
			{
				Vector messagesList = rmiServer.db.getMessages(usr, session_id);	

				return messagesList;
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception in rmiServerImpl.messages: " + e.getMessage());
		}

		return null;
	}

	public boolean delete(String usr, int msg, byte[] r) throws RemoteException
	{
		try
		{
			String validation_code = rmiServer.db.getValidationCode(usr);
			String code = new String(decryption(r, validation_code), "UTF8");

			if(code.equals(Integer.toString(msg)))
			{
				if(rmiServer.db.removeMessage(msg, usr))
					return true;
				else
					return false;
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception in rmiServerImpl.delete: " + e.getMessage());
		}

		return false;
	}

	public synchronized boolean logout(String usr, byte[] r) throws RemoteException
	{
		try
		{
			String validation_code = rmiServer.db.getValidationCode(usr);
			String code = new String(decryption(r, validation_code), "UTF8");

			if(code.equals(usr))
				if (clientList.removeElement(usr)) 
					System.out.println(usr + " is offline");
		}
		catch(Exception e)
		{
			System.out.println("Exception in rmiServerImpl.delete: " + e.getMessage());
		}

		return false;
	} 
}
