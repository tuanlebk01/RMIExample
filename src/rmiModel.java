import java.sql.*;
import java.util.*;

public class rmiModel
{
	public static String db_url, db_username, db_password;

	public rmiModel(String db, String usr, String pwd)
	{
		db_url = db;
		db_username = usr;
		db_password = pwd;
	}

	public static int getUserId(String username)
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection con = DriverManager.getConnection(db_url, db_username, db_password);

			PreparedStatement pst = con.prepareStatement("select user_id from users where user_username = ?");
			pst.setString(1, username);
			ResultSet rs = pst.executeQuery();

			if (rs.next())
				return rs.getInt(1);
		}
		catch (Exception e) 
		{
			System.out.println("Exception in rmiModel.getUserId: " + e.getMessage());
		}

		return -1;
	}

	public static String getUserPassword(String username)
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection con = DriverManager.getConnection(db_url, db_username, db_password);

			PreparedStatement pst = con.prepareStatement("select user_password from users where user_username = ?");
			pst.setString(1, username);
			ResultSet rs = pst.executeQuery();

			if (rs.next())
				return rs.getString(1);
		}
		catch (Exception e) 
		{
			System.out.println("Exception in rmiModel.getUserPassword: " + e.getMessage());
		}

		return null;
	}

	public static boolean setValidationCode(String username, String validation_code)
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection con = DriverManager.getConnection(db_url, db_username, db_password);

			java.util.Date now = new java.util.Date();

			PreparedStatement pst = con.prepareStatement("update users set user_validation = ? where user_username = ?");
			pst.setString(1, validation_code);
			pst.setString(2, username);
			int result = pst.executeUpdate();

			if (result > 0)
				return true;
		}
		catch (Exception e) 
		{
			System.out.println("Exception in rmiModel.setValidationCode: " + e.getMessage());
		}

		return false;
	}

	public static String getValidationCode(String username)
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection con = DriverManager.getConnection(db_url, db_username, db_password);

			PreparedStatement pst = con.prepareStatement("select user_validation from users where user_username = ?");
			pst.setString(1, username);
			ResultSet rs = pst.executeQuery();

			if(rs.next())
				return rs.getString(1);
			
		}
		catch(Exception e)
		{
			System.out.println("Exception in rmiModel.getValidationCode: " + e.getMessage());
		}

		return null;
	}

	public static int setSession(String session_key)
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection con = DriverManager.getConnection(db_url, db_username, db_password);

			java.util.Date now = new java.util.Date();

			Statement st = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
			st.executeUpdate("insert into sessions(session_date, session_key) values("+now.getTime()+", '"+session_key+"')",Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = st.getGeneratedKeys();

			if (rs.next())
				return rs.getInt(1); // session_id
		}
		catch (Exception e) 
		{
			System.out.println("Exception in rmiModel.getUserId: " + e.getMessage());
		}

		return -1;
	}

	public static String getSessionKey(int session_id)
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection con = DriverManager.getConnection(db_url, db_username, db_password);

			PreparedStatement pst = con.prepareStatement("select session_key from sessions where session_id = ?");
			pst.setInt(1, session_id);
			ResultSet rs = pst.executeQuery();

			if(rs.next())
				return rs.getString(1);
			
		}
		catch(Exception e)
		{
			System.out.println("Exception in rmiModel.getValidationCode: " + e.getMessage());
		}

		return null;
	}

	public static boolean addInvite(String username, int session_id)
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection con = DriverManager.getConnection(db_url, db_username, db_password);

			int user_id = getUserId(username);

			if (user_id > 0)
			{
				PreparedStatement pst = con.prepareStatement("insert into invitations(user_id, session_id) values(?, ?)");
				pst.setInt(1, getUserId(username));
				pst.setInt(2, session_id);
				int result = pst.executeUpdate();

				if (result > 0)
					return true;
			}
		}
		catch (Exception e) 
		{
			System.out.println(e.getMessage());
		}

		return false;
	} 

	public static boolean isInvited(String username, int session_id)
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection con = DriverManager.getConnection(db_url, db_username, db_password);

			int user_id = getUserId(username);

			PreparedStatement pst = con.prepareStatement("select * from invitations where user_id = ? and session_id = ?");
			pst.setInt(1, user_id);
			pst.setInt(2, session_id);
			ResultSet rs = pst.executeQuery();

			if(rs.next())
				return true;
			
		}
		catch(Exception e)
		{
			System.out.println("Exception in rmiModel.isInvited: " + e.getMessage());
		}

		return false;
	}

	public static boolean removeInvitation(String username, int session_id)
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection con = DriverManager.getConnection(db_url, db_username, db_password);

			int user_id = getUserId(username);

			if (user_id > 0)
			{
				PreparedStatement pst = con.prepareStatement("delete from invitations where user_id = ? and session_id = ?");
				pst.setInt(1, user_id);
				pst.setInt(2, session_id);
				int result = pst.executeUpdate();

				if (result > 0)
					return true;
			}
		}
		catch (Exception e) 
		{
			System.out.println("Exception in rmiModel.removeInvitation: " + e.getMessage());
		}

		return false;
	} 

	public static boolean setMessage(String username, int session_id, String message)
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection con = DriverManager.getConnection(db_url, db_username, db_password);

			int user_id = getUserId(username);

			if (user_id > 0)
			{
				java.util.Date now = new java.util.Date();

				PreparedStatement pst = con.prepareStatement("insert into messages(message_sender, message_time, message_during, message_content) values(?, ?, ?, ?)");
				pst.setInt(1, user_id);
				pst.setLong(2, now.getTime());
				pst.setInt(3, session_id);
				pst.setString(4, message);
				int result = pst.executeUpdate();

				if (result > 0)
					return true;
			}
		}
		catch (Exception e) 
		{
			System.out.println("Exception in rmiModel.setMessage: " + e.getMessage());
		}

		return false;
	} 

	public static Vector getSessions(String username)
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection con = DriverManager.getConnection(db_url, db_username, db_password);

			int user_id = getUserId(username);
			
			PreparedStatement pst = con.prepareStatement("select DISTINCT session_id, session_date from sessions, messages, users where message_during = session_id and message_sender = ?");
			pst.setInt(1, user_id);
			ResultSet rs = pst.executeQuery();

			Vector sessionList = new Vector();
			
			while(rs.next())
			{
				Vector session = new Vector();

				session.addElement(new Integer(rs.getInt(1)));
				//session.addElement(rs.getLong(2));

				sessionList.addElement(session);
			}

			return sessionList;
			
		}
		catch(Exception e)
		{
			System.out.println("Exception in rmiModel.getSessions: " + e.getMessage());
		}

		return null;
	}

	public static Vector getMessages(String username, int session_id)
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection con = DriverManager.getConnection(db_url, db_username, db_password);

			int user_id = getUserId(username);
			
			PreparedStatement pst = con.prepareStatement("select message_id, message_content, message_time from messages where message_sender = ? and message_during = ?");
			pst.setInt(1, user_id);
			pst.setInt(2, session_id);
			ResultSet rs = pst.executeQuery();

			Vector messageList = new Vector();
			
			while(rs.next())
			{
				Vector message = new Vector();

				message.addElement(new Integer(rs.getInt(1)));
				message.addElement(rs.getString(2));
				message.addElement(rs.getLong(3));

				messageList.addElement(message);
			}

			return messageList;
			
		}
		catch(Exception e)
		{
			System.out.println("Exception in rmiModel.getMessages: " + e.getMessage());
		}

		return null;
	}

	public static boolean removeMessage(int message_id, String username)
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection con = DriverManager.getConnection(db_url, db_username, db_password);

			java.util.Date now = new java.util.Date();

			PreparedStatement pst = con.prepareStatement("delete from messages where message_id = ? and message_sender = ?");
			pst.setInt(1, message_id);
			pst.setInt(2, getUserId(username));
			int result = pst.executeUpdate();

			if (result > 0)
				return true;
		}
		catch (Exception e) 
		{
			System.out.println("Exception in rmiModel.removeMessage: " + e.getMessage());
		}

		return false;
	}
}
