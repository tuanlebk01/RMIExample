/**
 * This class represents the object client for a distributed object of class CallbackServerImpl, which implements the remote interface CallbackServerInterface. 
 * It also accepts callback from the server.
 */

import java.util.*;
import java.rmi.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;

public class rmiClient 
{
	public static String username, password, validation_code, url;
	//public static Vector sessionList;
	public static ChatFrame chat;

	public static int session_id;
	public static String session_key;

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
			byte sd[] = k.getBytes(); // must be 8 characters
			SecretKeySpec key = new SecretKeySpec(sd, "DES");

			Cipher d = Cipher.getInstance("DES");
			d.init(Cipher.ENCRYPT_MODE, key);

			byte dn[] = d.doFinal(m.getBytes("UTF8"));

			return dn;
		}
		catch(Exception e)
		{
			System.out.println("Exception in rmiClinet.encryption: " + e.getMessage());
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
			System.out.println("Exception in in rmiClinet.decryption: " + e.getMessage());
		}

		return null;
	}

	/*public static void create_session(int session_id, String session_key, ChatFrame win)
	{
		Vector session = new Vector();

		session.addElement(session_id);
		session.addElement(session_key);
		session.addElement(win);

		sessionList.addElement(session);
	}*/

	public static void main(String args[]) 
	{
		url = "//localhost/sims";

		LoginFrame win = new LoginFrame();
		win.setVisible(true);

		//chat = new ChatFrame(session_id, session_key);
	}
}
