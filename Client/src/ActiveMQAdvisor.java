import java.awt.*;
import java.awt.event.*;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ConnectionInfo;
import org.apache.activemq.command.RemoveInfo;

/**
 * ActiveMQAdvisor implements receives information from advisory Topics to
 * gather connection data from the broker.
 */
@SuppressWarnings("serial")
public class ActiveMQAdvisor extends JFrame implements MessageListener {
	// JFrame, JPanel, and Layout
	private JFrame frame = null;
	private JPanel cards = null;
	private CardLayout cardLayout = null;

	// Defaults
	private String defaultURL = "tcp://localhost";
	// private String defaultURL = "tcp://clondaq6.jlab.org";
	private int defaultPort = 61616;

	// GUI Components - card1
	private JLabel labelAddress = new JLabel("Enter Address: ");
	private JTextField address = new JTextFieldHint(defaultURL, 20);
	private JLabel labelPort = new JLabel("Enter Port: ");
	private JTextField port = new JTextFieldHint("" + defaultPort, 20);
	private JLabel labelClientID = new JLabel("Enter ClientID: ");
	private JTextField clientID = new JTextFieldHint("Default: Computer ID", 20);
	private JButton connect = new JButton("Connect");

	// GUI Components - card2
	private JTable table = null;
	private JScrollPane scrollR = null;
	private JButton request = null;
	private JButton disconnect = null;

	// Table Scroll Parameters
	private int extent = 0;
	private int maximum = 0;
	private int value = 0;

	// Connection Parameters
	private String addressText = null;
	private ActiveMQConnectionFactory connectionFactory = null;
	private ActiveMQConnection connection = null;
	private Session session = null;
	private Destination advisoryDestination = null;
	private MessageConsumer consumerConnection = null;
	private MessageConsumer consumerAdvisor = null;
	private MessageProducer producerAdvisor = null;

	private TimeKeeper time = null;

	/**
	 * This method inserts data into the table.
	 * 
	 * @param data
	 *            - an Object array of four other pieces of data, usually
	 *            represented as four Strings.
	 */
	public void insertData(Object[] data) {
		((DefaultTableModel) table.getModel()).addRow(data);
	}

	/**
	 * This method saves the current extent of the scrollbar in a variable.
	 * 
	 * @param extent
	 *            - an int representation of the scrollbars extent.
	 */
	public void setExtent(int extent) {
		this.extent = extent;
	}

	/**
	 * This method saves the current maximum of the scrollbar in a variable.
	 * 
	 * @param maximum
	 *            - an int representation of the scrollbars maximum.
	 */
	public void setMaximum(int maximum) {
		this.maximum = maximum;
	}

	/**
	 * This method saves the current value of the scrollbar in a variable.
	 * 
	 * @param value
	 *            - an int representation of the scrollbars value.
	 */
	public void setValue(int value) {
		this.value = value;
	}

	/**
	 * Adds components to panes for the GUI.
	 * 
	 * @param pane
	 *            The Container to add to the GUI to display all of the added
	 *            JFrame objects.
	 */
	private void addComponentToPane(Container pane) {

		// CARD 1 START
		JPanel card1 = new JPanel(new GridBagLayout());

		// Create the constraints
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(10, 10, 10, 10);

		// Add components
		constraints.ipadx = 24;
		constraints.ipady = 6;

		constraints.gridx = 0;
		constraints.gridy = 0;
		card1.add(labelAddress, constraints);

		constraints.gridx = 1;
		card1.add(address, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		card1.add(labelPort, constraints);

		constraints.gridx = 1;
		card1.add(port, constraints);

		constraints.gridx = 0;
		constraints.gridy = 2;
		card1.add(labelClientID, constraints);

		constraints.gridx = 1;
		card1.add(clientID, constraints);

		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.gridwidth = 0;
		constraints.anchor = GridBagConstraints.CENTER;
		card1.add(connect, constraints);

		/**
		 * This listener connects to a given server.
		 */
		connect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean requests = false;
				String requestsMessage = "";

				// If the address isn't empty, set it to the new value
				if (!address.getText().isEmpty()) {
					addressText = address.getText() + ":";
				} else {
					addressText = defaultURL + ":";
				}

				// If the port isn't a valid number, create request
				try {
					if (!port.getText().isEmpty()) {
						addressText += Integer.parseInt(port.getText());
					} else {
						addressText += defaultPort;
					}
				} catch (Exception err) {
					requestsMessage += "Please enter a number for the port.\n";
					requests = true;
				}

				// If the clientID is empty, create request
				if (clientID.getText().isEmpty()) {
					requestsMessage += "Please enter a unique ClientID.\n";
					requests = true;
				}

				// If the parameters aren't valid, display requests
				if (requests) {
					displayMessageDialog(requestsMessage, "Requirements");
				}
				// If the parameters are valid, try connecting.
				else {
					connect();
				}
			}
		});
		// CARD 1 END

		// CARD 2 START
		JPanel card2 = new JPanel();
		card2.setLayout(new BoxLayout(card2, BoxLayout.Y_AXIS));

		// Menu
		JMenuBar menuBar = new JMenuBar();

		JMenu helpMenu = new JMenu("Help");
		JMenuItem usage = helpMenu.add("How to Use");
		menuBar.add(helpMenu);

		JMenu editMenu = new JMenu("Edit");
		JMenuItem clear = editMenu.add("Clear");
		menuBar.add(editMenu);

		menuBar.add(Box.createHorizontalGlue(), Box.LEFT_ALIGNMENT);

		card2.add(menuBar);

		DefaultTableModel modelR = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				// Makes cells not editable so double clicking will open a view
				return false;
			}
		};
		modelR.addColumn("Name");
		modelR.addColumn("Remote Address");
		modelR.addColumn("Time");
		modelR.addColumn("Actual Data");
		table = new JTable(modelR);
		table.setPreferredScrollableViewportSize(new Dimension(800, 500));
		table.setFillsViewportHeight(true);
		table.setRowHeight(20);

		table.removeColumn(table.getColumnModel().getColumn(3));

		scrollR = new JScrollPane(table);
		card2.add(scrollR);

		JPanel buttons = new JPanel(new GridBagLayout());

		// Create the constraints
		GridBagConstraints constraints2 = new GridBagConstraints();
		constraints2.insets = new Insets(20, 0, 20, 0);

		// Add components
		constraints2.fill = GridBagConstraints.HORIZONTAL;
		constraints2.ipadx = 75;
		constraints2.ipady = 6;

		constraints2.gridx = 0;
		constraints2.gridy = 0;
		request = new JButton("Request");
		buttons.add(request, constraints2);

		constraints2.gridy = 1;
		disconnect = new JButton("Disconnect");
		buttons.add(disconnect, constraints2);

		card2.add(buttons);

		// MENU
		/**
		 * This listener provides instructions for how to use the program.
		 */
		usage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				displayMessageDialog(
						"This program receives messages from an advisory topic on the server for the purpose of displaying connection information.  Once connected, information should be shown automatically.  In order to receive information from before this program is started, it needs to run with the a client ID before the information is sent to the broker, since the client ID needs to be subscribed to have messages saved while this program is not active.",
						"How to Use");
			}
		});

		/**
		 * this listener removes all of the rows in the table.
		 */
		clear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				modelR.setRowCount(0);
			}
		});

		// TABLE
		/**
		 * This listener shows a window of the data for a row that is
		 * double-clicked.
		 */
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int row = table.getSelectedRow();
					String data = "";
					String type = (String) table.getModel().getValueAt(row, 0);

					// data += table.getModel().getValueAt(row,
					// 1).replaceAll("(\r\n|\n)", "<br/>");
					data += table.getModel().getValueAt(row, 1);

					displayMessageDialog(data, type);
				}
			}
		});

		/**
		 * This adapter scrolls to the bottom of the table if the scroll bar is
		 * at the bottom.
		 */
		table.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				if (extent == maximum - value) {
					table.scrollRectToVisible(table.getCellRect(table.getRowCount() - 1, 0, true));
				}
			}
		});

		// BOXES/BUTTONS
		/**
		 * This listener sends a request to an advisory topic to get the time
		 * data from a client.
		 */
		request.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					checkInsertTime(connection.getClientID(), time.getBaseTime());
					BytesMessage message = session.createBytesMessage();
					message.writeByte((byte) 1);
					producerAdvisor.send(message);
				} catch (JMSException err) {
					String error = "Caught requesting time:\n\n" + err + "\n";
					error += err.getStackTrace();
					displayMessageDialog(error, "Error");
				}
			}
		});

		/**
		 * This listener disconnects from the server.
		 */
		disconnect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				disconnect();
				cardLayout.show(cards, "1");
			}
		});
		// CARD 2 END

		// Create the panel that contains the "cards".
		cardLayout = new CardLayout();
		cards = new JPanel(cardLayout);
		cards.add(card1, "1");
		cards.add(card2, "2");

		pane.add(cards, BorderLayout.CENTER);
	}

	/**
	 * This method creates a GUI and makes it visible for user interaction.
	 */
	private void createAndDisplayGUI() {
		// Create and set up the window.
		frame = new JFrame("ActiveMQ Advisor");
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);

		// Create and set up the content pane.
		ActiveMQAdvisor activeMQAdvisor = new ActiveMQAdvisor();
		activeMQAdvisor.addComponentToPane(frame.getContentPane());

		// Display the window.
		frame.pack();
		frame.getContentPane().requestFocusInWindow();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		// Window Listener
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				disconnect();
			}
		});
	}

	/**
	 * Displays a JDialog with a JTextPane and JScrollPane for compatibility
	 * with large amounts of text.
	 * 
	 * @param message
	 *            - the message to display.
	 * @param title
	 *            - the title of the window.
	 */
	protected void displayMessageDialog(String message, String title) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JTextPane textArea = new JTextPane();
				textArea.setText(message);
				textArea.setEditable(false);
				textArea.setCaretPosition(0);
				JScrollPane scrollPane = new JScrollPane(textArea);
				scrollPane.setPreferredSize(new Dimension(500, 250));

				JOptionPane.showMessageDialog(new JLabel(), scrollPane, title, JOptionPane.PLAIN_MESSAGE);
			}
		});
	}

	public static void main(String[] args) throws JMSException {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new ActiveMQAdvisor().createAndDisplayGUI();
			}
		});
	}

	/**
	 * This method returns the current position of the thumb within the entire
	 * length of the scrollbar.
	 * 
	 * @return - an int value to represent the value of the scrollbar.
	 */
	public int getVerticalScrollBarValue() {
		return scrollR.getVerticalScrollBar().getModel().getValue();
	}

	/**
	 * This method returns the maximum length of the scrollbar.
	 * 
	 * @return - an int value to represent the maximum of the scrollbar.
	 */
	public int getVerticalScrollBarMaximum() {
		return scrollR.getVerticalScrollBar().getModel().getMaximum();
	}

	/**
	 * This method returns the length of the visible scrollbar region.
	 * 
	 * @return - an int value to represent the extent of the scrollbar.
	 */
	public int getVerticalScrollBarExtent() {
		return scrollR.getVerticalScrollBar().getModel().getExtent();
	}

	/**
	 * This method creates a connection to the server for monitoring purposes.
	 */
	public void connect() {
		try {
			// Initialize ConnectionFactory
			connectionFactory = new ActiveMQConnectionFactory(addressText);

			// Initialize Connection
			connection = (ActiveMQConnection) connectionFactory.createConnection();
			connection.setClientID("");

			// Initialize Session
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			advisoryDestination = session.createTopic("ActiveMQ.Advisory.Connection");
			consumerConnection = session.createConsumer(advisoryDestination);
			consumerConnection.setMessageListener(this);

			advisoryDestination = session.createTopic("ActiveMQ.Advisory.Advisor");
			consumerAdvisor = session.createConsumer(advisoryDestination);
			consumerAdvisor.setMessageListener(this);

			producerAdvisor = session.createProducer(advisoryDestination);

			// Start Connection
			connection.start();
			time = new TimeKeeper();

			// Display advisory data
			cardLayout.show(cards, "2");
		} catch (Exception err) {
			String error = "Caught while connecting:\n\n" + err + "\n";
			error += err.getStackTrace();
			displayMessageDialog(error, "Error");
		}
	}

	/**
	 * This method closes all connections to a server for a given client.
	 */
	public void disconnect() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (Exception err) {
			String error = "Caught while closing:\n\n" + err + "\n";
			error += err.getStackTrace();
			displayMessageDialog(error, "Error");
		}
	}

	/**
	 * A method from an implementation of MessageListener to receive and
	 * evaluate various JMS messages.
	 * 
	 * @param message
	 *            - the message that is received from the server.
	 */
	@Override
	public void onMessage(Message message) {
		if (message instanceof ActiveMQMessage) {

			ActiveMQMessage activeMQMessage = (ActiveMQMessage) message;
			if (activeMQMessage.getDataStructure() instanceof ConnectionInfo) {
				// Inserts the data into the table from the connection message
				// advisory topic
				ConnectionInfo infoConnect = (ConnectionInfo) activeMQMessage.getDataStructure();
				insertData(new Object[] { infoConnect.getClientId(), infoConnect.getClientIp(), "", activeMQMessage });
				request.doClick();
			} else if (activeMQMessage.getDataStructure() instanceof RemoveInfo) {
				// Remove the data from the table from the connection removed
				// message from the advisory topic
				RemoveInfo infoRemove = (RemoveInfo) activeMQMessage.getDataStructure();
				for (int i = 0; i < table.getModel().getRowCount(); i++) {
					ActiveMQMessage infoTable = (ActiveMQMessage) table.getModel().getValueAt(i, 3);
					ConnectionInfo infoConnect = (ConnectionInfo) infoTable.getDataStructure();

					// The connectionId of the connect message from the advisory
					// topic is the same as the objectId from the remove message
					// from the advisory topic.

					// System.out.println(infoRemove.getObjectId());
					// System.out.println(infoConnect.getConnectionId());

					if (infoRemove.getObjectId() == infoConnect.getConnectionId()) {
						((DefaultTableModel) table.getModel()).removeRow(i);
					}
				}
			} else if (activeMQMessage instanceof TextMessage) {
				try {
					TextMessage textMessage = (TextMessage) message;
					String text = textMessage.getText();

					int space = text.indexOf(' ');
					String time = text.substring(0, space);
					String clientID = text.substring(space + 1);

					// System.out.println(time);
					// System.out.println(clientID);

					checkInsertTime(clientID, time);
				} catch (JMSException err) {
					String error = "Caught while closing:\n\n" + err + "\n";
					error += err.getStackTrace();
					displayMessageDialog(error, "Error");
				}

			}
		}
	}

	/**
	 * Inserts a time value for a given clientID if it is connected to the
	 * server (in the table).
	 * 
	 * @param clientID
	 *            - the String representation of a clientID that could be on the
	 *            server.
	 * @param time
	 *            - a String representation of the time.
	 */
	public void checkInsertTime(String clientID, String time) {
		for (int i = 0; i < table.getModel().getRowCount(); i++) {
			String tableID = (String) table.getModel().getValueAt(i, 0);

			if (tableID.equals(clientID)) {
				table.getModel().setValueAt(time, i, 2);
			}
		}
	}
}