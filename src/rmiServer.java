/* This class represents the object server for a distributed object of class Callback, which implements the remote interface CallbackInterface. */

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.net.*;
import java.io.*;

public class rmiServer	
{
	public static rmiModel db;

	public static void main(String args[]) 
	{
		try
		{		 
			startRegistry(1099);

			rmiServerImpl rmiServerObject = new rmiServerImpl();

			Naming.rebind("rmi://localhost/sims", rmiServerObject);
			System.out.println("RMI Callback Server ready ..");

			rmiModel db = new rmiModel("jdbc:mysql://localhost/sims", "root", "");
		}
		catch (Exception e) 
		{
			System.out.println("Exception in rmiServer.main: " + e);
		}
	}

	// This method starts a RMI registry on the localhost, if it does not already exists at the specified port number.

	private static void startRegistry(int RMIPortNum) throws RemoteException
	{
		try 
		{
			Registry registry = LocateRegistry.getRegistry(RMIPortNum);
			registry.list();

			// This call will throw an exception if the registry does not already exist
		}
		catch (RemoteException e) 
		{ 
			// No valid registry at that port.
			Registry registry = LocateRegistry.createRegistry(RMIPortNum);
		}
	}

}
