import java.awt.*;
import java.awt.event.*;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.advisory.DestinationSource;
import org.apache.activemq.command.ActiveMQTopic;

@SuppressWarnings("serial")
public class Client extends JFrame {
	// JFrame, JPanel, and Layout
	private JFrame frame = null;
	private JPanel cards = null;
	private CardLayout cardLayout = null;

	// Defaults
	private String defaultURL = "tcp://localhost";
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
	private TextArea sendText = new TextArea();
	private JDialogCustom jd = null;;
	private JButton topicCreate = null;
	private JComboBox<String> topicSelect = null;
	private JButton dataType = null;
	private JButton send = null;
	private JButton receive = null;
	private JButton refresh = null;
	private JButton disconnect = null;

	// Connection Parameters
	private String addressText = null;
	private ActiveMQConnectionFactory connectionFactory = null;
	private ActiveMQConnection connection = null;
	private Session session = null;
	private DestinationSource ds = null;
	private Set<ActiveMQTopic> topics = null;
	private DefaultComboBoxModel<String> model = null;
	private Consumer consumer = null;
	private Producer producer = null;

	public JTable getTable() {
		return table;
	}

	public void addComponentToPane(Container pane) {

		// CARD 1 START
		JPanel card1 = new JPanel(new GridBagLayout());

		// Create the constraints
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(10, 10, 10, 10);

		// Add components
		constraints.anchor = GridBagConstraints.WEST;

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

		// Connects to a given server
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
				// If the parameters are valid, try connecting
				else {
					try {
						// Initialize ConnectionFactory
						connectionFactory = new ActiveMQConnectionFactory(addressText);

						// Initialize and Start Connection
						connection = (ActiveMQConnection) connectionFactory.createConnection();
						connection.setClientID("");
						connection.start();

						// Initialize Session
						session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

						// Get all Topic Destinations
						ds = connection.getDestinationSource();
						topics = ds.getTopics();

						updateTopics();

						// Display Consumer and Producer settings
						cardLayout.show(cards, "2");

					} catch (Exception err) {
						String error = "Caught while connecting:\n\n" + err + "\n";
						error += err.getStackTrace();
						displayMessageDialog(error, "Error");
					}
				}
			}
		});
		// CARD 1 END

		// CARD 2 START
		JPanel card2 = new JPanel(new BorderLayout());

		// Menu
		JMenuBar menuBar = new JMenuBar();
		JMenu helpMenu = new JMenu("Help");
		JMenuItem usage = helpMenu.add("How to Use");
		menuBar.add(helpMenu);
		card2.add(menuBar, BorderLayout.NORTH);

		// Contents
		JPanel contents = new JPanel(new FlowLayout());
		card2.add(contents);

		JPanel left = new JPanel();
		left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

		DefaultTableModel modelR = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				// Makes cells not editable so double clicking will open a view
				return false;
			}
		};
		modelR.addColumn("Type");
		modelR.addColumn("Data");
		modelR.addColumn("Actual Data");
		table = new JTable(modelR);
		table.setPreferredScrollableViewportSize(new Dimension(400, 500));
		table.setFillsViewportHeight(true);

		table.getColumnModel().getColumn(0).setPreferredWidth(100);
		table.getColumnModel().getColumn(1).setPreferredWidth(400);
		table.removeColumn(table.getColumnModel().getColumn(2));

		scrollR = new JScrollPane(table);
		left.add(scrollR);

		sendText.setPreferredSize(new Dimension(400, 50));
		left.add(sendText);

		JPanel right = new JPanel(new GridBagLayout());

		// Create the constraints
		GridBagConstraints constraints2 = new GridBagConstraints();
		constraints2.insets = new Insets(20, 10, 20, 10);

		// Add components
		constraints2.anchor = GridBagConstraints.CENTER;

		constraints2.gridx = 1;
		constraints2.gridy = 0;
		constraints2.fill = GridBagConstraints.HORIZONTAL;
		topicCreate = new JButton("Create Topic");
		right.add(topicCreate, constraints2);

		constraints2.gridy = 1;

		topicSelect = new JComboBox<String>();
		topicSelect.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXX");
		right.add(topicSelect, constraints2);

		constraints2.gridy = 2;
		dataType = new JButton("Data Type: Array");
		right.add(dataType, constraints2);

		constraints2.gridy = 3;
		send = new JButton("Send");
		right.add(send, constraints2);

		constraints2.gridy = 4;
		receive = new JButton("Receive: Off");
		right.add(receive, constraints2);

		constraints2.gridy = 5;
		refresh = new JButton("Refresh");
		right.add(refresh, constraints2);

		constraints2.gridy = 6;
		disconnect = new JButton("Disconnect");
		right.add(disconnect, constraints2);

		contents.add(left);
		contents.add(right);

		// ActionListeners
		usage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String general = "--GENERAL--\n"
						+ "All of the known Topics will be displayed in the ComboBox.  If there are none, press the \"Create Topic\" button and enter a name for one.  Note that an actual topic on the server will not be created until a message is sent or a consumer is set to listen to the Topic (the Receive button is set to On).  Additionally, the program may not show any Topics even though the server has at least one.  In this case, press the \"Refresh\" button to force the client to check the server again.  Lastly, one can press the \"Disconnect\" button to disconnect from the server and to reconnect to a different server.";

				String dataTypes = "--DATA TYPES--\n"
						+ "String: This will send a String of all text in the text box in the form of a TextMessage.\n\n"
						+ "Array:  First enter the primitive data type that will be sent.  The type can be either a byte, short, int, long, char, float, double, or bool (boolean).  Next, enter the amount of data to be sent followed by the data.  If the amount of data provided is less than the length, then the empty data will be filled in with the data type's equivalent of zero.\n\n"
						+ "    *Examples*\n      int 6 4 3 23 6 3 298             -> [4, 3, 23, 6, 3, 298]\n      int 4 3 2                                 -> [3, 2, 0, 0]\n      bool 4 true false 89fj TruE -> [true, false, false, true]\n      char 3 f 8 a                            -> [f, 8, a]";

				displayMessageDialog(general + "\n\n\n" + dataTypes, "How to Use");
			}
		});

		// Shows a window of the data for a row that is double-clicked
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

		// Creates a new Topic
		topicCreate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					jd = new JDialogCustom(frame, null);
					jd.setLocationRelativeTo(null);
					jd.pack();
					jd.setVisible(true);
					if (jd.getValidatedText() != null) {
						model.addElement(jd.getValidatedText());
						session.createTopic(jd.getValidatedText());
					}
				} catch (Exception err) {
					String error = "Caught while creating Topic:\n\n" + err + "\n";
					error += err.getStackTrace();
					displayMessageDialog(error, "Error");
				}
			}
		});

		dataType.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (dataType.getText().equals("Data Type: Array")) {
					dataType.setText("Data Type: String");
				} else {
					dataType.setText("Data Type: Array");
				}
			}
		});

		// Sends a TextMessage or an ObjectMessage based on the parameters
		send.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (topicSelect.getSelectedItem() != null) {
					try {
						producer = new Producer(addressText, clientID.getText() + " - Producer",
								topicSelect.getSelectedItem().toString());
						producer.run();

						if (dataType.getText().equals("Data Type: String")) {
							producer.sendTextMessage(sendText.getText());
						} else {
							// Parse the text to decide what type of primitives
							// to send
							String[] array = sendText.getText().split("\\s+");
							String type = array[0].toLowerCase();

							if (!type.equals("byte") && !type.equals("short") && !type.equals("int")
									&& !type.equals("long") && !type.equals("char") && !type.equals("float")
									&& !type.equals("double") && !type.equals("boolean") && !type.equals("bool")) {
								displayMessageDialog(
										"\"" + type + "\" is not a valid primitive type.\n"
												+ "Please enter the type of the array as either byte, short, int, long, char, float, double, or bool (boolean).",
										"Error");
							} else {
								// Parse primitives here and check that all
								// types match
								if (type.equals("byte")) {
									byte[] arrayN = new byte[array.length - 2];
									for (int i = 0; i < array.length - 2; i++) {
										arrayN[i] = Byte.parseByte(array[i + 2]);
									}
									producer.sendByteStream(Integer.parseInt(array[1]), arrayN);
								} else if (type.equals("short")) {
									short[] arrayN = new short[array.length - 2];
									for (int i = 0; i < array.length - 2; i++) {
										arrayN[i] = Short.parseShort(array[i + 2]);
									}
									producer.sendShortStream(Integer.parseInt(array[1]), arrayN);
								} else if (type.equals("int")) {
									int[] arrayN = new int[array.length - 2];
									for (int i = 0; i < array.length - 2; i++) {
										arrayN[i] = Integer.parseInt(array[i + 2]);
									}
									producer.sendIntStream(Integer.parseInt(array[1]), arrayN);
								} else if (type.equals("long")) {
									long[] arrayN = new long[array.length - 2];
									for (int i = 0; i < array.length - 2; i++) {
										arrayN[i] = Long.parseLong(array[i + 2]);
									}
									producer.sendLongStream(Integer.parseInt(array[1]), arrayN);
								} else if (type.equals("char")) {
									char[] arrayN = new char[array.length - 2];
									for (int i = 0; i < array.length - 2; i++) {
										arrayN[i] = array[i + 2].charAt(0);
									}
									producer.sendCharStream(Integer.parseInt(array[1]), arrayN);
								} else if (type.equals("float")) {
									float[] arrayN = new float[array.length - 2];
									for (int i = 0; i < array.length - 2; i++) {
										arrayN[i] = Float.parseFloat(array[i + 2]);
									}
									producer.sendFloatStream(Integer.parseInt(array[1]), arrayN);
								} else if (type.equals("double")) {
									double[] arrayN = new double[array.length - 2];
									for (int i = 0; i < array.length - 2; i++) {
										arrayN[i] = Double.parseDouble(array[i + 2]);
									}
									producer.sendDoubleStream(Integer.parseInt(array[1]), arrayN);
								} else if (type.equals("boolean") || array[0].equals("bool")) {
									boolean[] arrayN = new boolean[array.length - 2];
									for (int i = 0; i < array.length - 2; i++) {
										arrayN[i] = Boolean.parseBoolean(array[i + 2]);
									}
									producer.sendBooleanStream(Integer.parseInt(array[1]), arrayN);
								}
							}

						}

						producer.closeConnection();
					} catch (Exception err) {
						try {
							producer.closeConnection();
						} catch (JMSException e1) {
							String error = "Caught while closing Producer connection:\n\n" + err + "\n";
							error += err.getStackTrace();
							displayMessageDialog(error, "Error");
						}
						String error = "Caught while sending message:\n\n" + err + "\n";
						error += err.getStackTrace();
						displayMessageDialog(error, "Error");
					}
				} else {
					displayMessageDialog("No Topic is selected. Please create or select a Topic.", "Requirements");
				}

				// checkScroll();
			}
		});

		// Opens a new consumer thread to receive messages
		receive.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Thread receives from Topics until turned off
				if (receive.getText().equals("Receive: On")) {
					receive.setText("Receive: Off");

					try {
						consumer.closeConnection();
					} catch (Exception err) {
						String error = "Caught while closing Consumer:\n\n" + err + "\n";
						error += err.getStackTrace();
						displayMessageDialog(error, "Error");
					}

				} else {
					if (topicSelect.getSelectedItem() != null) {
						receive.setText("Receive: On");

						consumer = new Consumer(addressText, clientID.getText() + " - Consumer",
								topicSelect.getSelectedItem().toString(), clientID.getText(), Client.this);
						consumer.run();
					} else {
						displayMessageDialog("No Topic is selected.  Please create or select a Topic.", "Requirements");
					}
				}
			}
		});

		// Refreshes the list of available topics
		refresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Make sure to retain the previous choice if it isn't removed
				String previous = (String) model.getSelectedItem();
				updateTopics();
				for (int i = 0; i < topicSelect.getItemCount(); i++) {
					if (topicSelect.getItemAt(i).equals(previous)) {
						topicSelect.setSelectedItem(previous);
					}
				}
			}
		});

		// Disconnects from the server
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

	private void createAndDisplayGUI() {
		// Create and set up the window.
		frame = new JFrame("Client");
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);

		// Create and set up the content pane.
		Client client = new Client();
		client.addComponentToPane(frame.getContentPane());

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

	public void updateTopics() {
		model = new DefaultComboBoxModel<String>();
		for (ActiveMQTopic topic : topics) {
			try {
				topicSelect.addItem(topic.getTopicName());
				model.addElement(topic.getTopicName());
			} catch (Exception err) {
				String error = "Caught while updating Topics:\n\n" + err + "\n";
				error += err.getStackTrace();
				displayMessageDialog(error, "Error");
			}
		}
		topicSelect.setModel(model);
	}

	public void disconnect() {
		try {
			if (receive != null && receive.getText() == "Receive: On") {
				receive.doClick();
			}
			if (consumer != null) {
				consumer.closeConnection();
			}
			if (producer != null) {
				producer.closeConnection();
			}
			if (connection != null) {
				connection.close();
			}
		} catch (Exception err) {
			String error = "Caught while closing:\n\n" + err + "\n";
			error += err.getStackTrace();
			displayMessageDialog(error, "Error");
		}
	}

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
				new Client().createAndDisplayGUI();
			}
		});
	}

	// public void checkScroll() {
	// int value = scrollR.getVerticalScrollBar().getModel().getValue();
	// int maximum = scrollR.getVerticalScrollBar().getModel().getMaximum();
	// int extent = scrollR.getVerticalScrollBar().getModel().getExtent();
	//
	// if (extent == maximum - value) {
	// table.scrollRectToVisible(table.getCellRect(table.getRowCount() - 1, 0,
	// true));
	// }
	// }
}