/* This is a remote interface for illustrating RMI client rmi. */

import java.rmi.*;

public interface rmiClientInterface extends java.rmi.Remote
{
	public boolean recive(String usr, int session_id, byte[] msg, String hash) throws RemoteException;
	public boolean invite(String usr, int session_id, String h) throws RemoteException;
}
