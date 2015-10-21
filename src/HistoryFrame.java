import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.table.DefaultTableModel;
import java.util.*;

import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import java.sql.*; 

public class HistoryFrame extends JFrame
{
	public static JTable table;
	public static DefaultTableModel model;
	public static Vector messageList;

	public HistoryFrame(Vector messages)
	{
		model = new DefaultTableModel();
		table = new JTable(model);
		messageList = messages;

		model.addColumn("#");
		model.addColumn("Message");
		model.addColumn("Date");

		for(int i = 0; i < messageList.size(); i++)
		{
			Vector message = (Vector)messageList.elementAt(i);

			model.addRow(message);
		}

		JButton delete = new JButton("Delete");
		JPanel jp = new JPanel();
		JScrollPane jsp = new JScrollPane(table);
		Container c = getContentPane();

		jp.add(delete);

		c.add(jsp, BorderLayout.CENTER);
		c.add(jp, BorderLayout.SOUTH);

		delete.addActionListener(new deleteMessage());

		setTitle("History");
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		pack();
		setSize(600, 300);
		show();
	}

	class deleteMessage implements ActionListener
	{		 
		public void actionPerformed(ActionEvent e)
		{
			try
			{
				int index = table.getSelectedRow();

				if (index > -1)
				{
					rmiServerInterface rmiServer = (rmiServerInterface)Naming.lookup(rmiClient.url);
					Vector message = (Vector)messageList.elementAt(index);
					Integer message_id = (Integer)message.elementAt(0);

					if(JOptionPane.showOptionDialog(null, "Are you sure you want to delete this message ?", "Confirm delete", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Yes", "No"}, "No") == JOptionPane.YES_OPTION)
					{
						byte[] r = rmiClient.encryption(Integer.toString(message_id), rmiClient.validation_code);

						if(rmiServer.delete(rmiClient.username, message_id, r))
						{
							messageList.removeElement(index);
							model.removeRow(index);
						}
					}
				}
				else
					JOptionPane.showMessageDialog(null, "Select message to delete it !", "Delete message", JOptionPane.INFORMATION_MESSAGE);
			}
			catch(Exception exc)
			{
				JOptionPane.showMessageDialog(null, exc.toString(), "Error in HistoryFrame.deleteMessage", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
