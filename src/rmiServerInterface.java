/* This is a remote interface for illustrating RMI client callback. */

import java.rmi.*;
import java.util.*;

public interface rmiServerInterface extends Remote 
{
	public byte[] code(String usr) throws RemoteException;

	public boolean login(String usr, byte[] r, rmiClientInterface callbackClientObject) throws RemoteException;

	public Vector session(String usr, byte[] r) throws RemoteException;

	public boolean join(String usr, int session_id, byte[] r) throws RemoteException;

	public Vector users(String usr) throws RemoteException;

	public boolean invite(String from, String to, int session_id, String h) throws RemoteException;

	public String accept(String usr, int session_id, byte[] r) throws RemoteException;

	public boolean send(String usr, byte[] msg, int session, String h) throws RemoteException;

	public boolean leave(String usr, int session_id,byte[] r) throws RemoteException;

	public Vector sessions(String usr, byte[] r) throws RemoteException;

	public Vector messages(String usr, int session_id, byte[] r) throws RemoteException;

	public boolean delete(String usr, int msg, byte[] r) throws RemoteException;

	public boolean logout(String usr, byte[] r) throws RemoteException;
}
