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
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;

import activeMQInterface.Client;
import activeMQInterface.Consumer;
import activeMQInterface.Producer;

/**
 * ActiveMQClient implements the Client interface to provide a GUI for creating
 * producers, consumers, and utilizing their methods.
 */
@SuppressWarnings("serial")
public class ActiveMQClient extends JFrame implements Client {
	// JFrame, JPanel, and Layout
	private JFrame frame = null;
	private JPanel cards = null;
	private CardLayout cardLayout = null;

	// Defaults
	private String defaultURL = "tcp://localhost";
	private String defaultURL2 = "tcp://clondaq6.jlab.org";
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
	private JDialogCustom jd = null;
	private JButton destinationType = new JButton("Use: Topics");
	private JButton destinationCreate = null;
	private JComboBox<String> destinationSelect = null;
	private JButton dataType = null;
	private JButton send = null;
	private JButton receive = null;
	private JButton refresh = null;
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
	private DestinationSource ds = null;
	private Set<ActiveMQTopic> topics = null;
	private Set<ActiveMQQueue> queues = null;
	private DefaultComboBoxModel<String> model = null;
	private ActiveMQConsumer activeMQConsumer = null;
	private ActiveMQProducer activeMQProducer = null;

	public void insertData(Object[] data) {
		((DefaultTableModel) table.getModel()).addRow(data);
	}

	public void setExtent(int extent) {
		this.extent = extent;
	}

	public void setMaximum(int maximum) {
		this.maximum = maximum;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public void addComponentToPane(Container pane) {

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
					connect();
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

		JMenu editMenu = new JMenu("Edit");
		JMenuItem clear = editMenu.add("Clear");
		menuBar.add(editMenu);

		card2.add(menuBar, BorderLayout.NORTH);

		// Contents
		JPanel contents = new JPanel(new GridLayout());
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
		table.setRowHeight(20);

		table.getColumnModel().getColumn(0).setPreferredWidth(100);
		table.getColumnModel().getColumn(1).setPreferredWidth(400);
		table.removeColumn(table.getColumnModel().getColumn(2));

		scrollR = new JScrollPane(table);
		left.add(scrollR);

		sendText.setPreferredSize(new Dimension(400, 75));
		left.add(sendText);

		JPanel right = new JPanel(new GridBagLayout());

		// Create the constraints
		GridBagConstraints constraints2 = new GridBagConstraints();
		constraints2.insets = new Insets(20, 0, 20, 0);

		// Add components
		constraints2.ipadx = 75;
		constraints2.ipady = 6;

		constraints2.gridx = 1;
		constraints2.gridy = 0;
		constraints2.fill = GridBagConstraints.HORIZONTAL;
		right.add(destinationType, constraints2);

		constraints2.gridy = 1;
		destinationCreate = new JButton("Create Destination");
		right.add(destinationCreate, constraints2);

		constraints2.gridy = 2;
		destinationSelect = new JComboBox<String>();
		destinationSelect.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXX");
		right.add(destinationSelect, constraints2);

		constraints2.gridy = 3;
		dataType = new JButton("Message Type: Stream");
		right.add(dataType, constraints2);

		constraints2.gridy = 4;
		send = new JButton("Send");
		right.add(send, constraints2);

		constraints2.gridy = 5;
		receive = new JButton("Receive: Off");
		right.add(receive, constraints2);

		constraints2.gridy = 6;
		refresh = new JButton("Refresh");
		right.add(refresh, constraints2);

		constraints2.gridy = 7;
		disconnect = new JButton("Disconnect");
		right.add(disconnect, constraints2);

		contents.add(left);
		contents.add(right);

		// LISTENERS
		// MENU
		// Provides instructions for program behavior
		usage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String general = "--GENERAL--\n"
						+ "All of the known Destinations will be displayed in the ComboBox.  If there are none, press the \"Create Destination\" button and enter a name for one.  Note that an actual Destination on the server will not be created until a message is sent or a activeMQConsumer is set to listen to the Destination (the Receive button is set to On).  Additionally, the program may not show any Destinations even though the server has at least one.  In this case, press the \"Refresh\" button to force the client to check the server again.  Pressing the Use button will change whether a Topic or Queue is used for the Destination.  Lastly, one can press the \"Disconnect\" button to disconnect from the server and to reconnect to a different server.";

				String dataTypes = "--MESSAGE TYPES--\n"
						+ "Text -> This will send a String of all text in the text box in the form of a TextMessage.\n\n"
						+ "Stream (One Type) -> First enter the primitive data type that will be sent.  The type can be either a byte, short, int, long, char, float, double, or bool (boolean).  Next, enter the amount of data to be sent followed by the data.  If the amount of data provided is less than the length, then the empty data will be filled in with the data type's equivalent of zero.\n\n"
						+ "Stream (Mixed Type) -> This follows the same rules as stream messages of one type, except mix (mixed) is entered instead of the primitive type followed by the size of the data.  The primitive type is then written in front of each piece of data to specify what type the data should be.\n\n"
						+ "Byte -> This follows the same rules as a StreamMessage; however, the data received is represented by a byte array with one byte at the beginning of the message to designate the type.  Mixed type messages are not used, since they would just be a redundant version of a mixed type StreamMessage.  ByteMessages are meant to be faster due to less bytes being used to identify the type of data in the message.\n\n"
						+ "    *Examples*\n      int 6 4 3 23 6 3 298\n          -> [4, 3, 23, 6, 3, 298]\n\n      int 4 3 2\n          -> [3, 2, 0, 0]\n\n      bool 4 true false 89fj TruE\n          -> [true, false, false, true]\n\n      char 3 f 8 a\n          -> [f, 8, a]\n\n      mix 5 int 4 bool true char f byte 2 double 32.38\n          -> [4, true, f, 2, 32.38]\n\n      mix 3 int 1 long 9283928\n          -> [1, 9283928, null]";

				displayMessageDialog(general + "\n\n\n" + dataTypes, "How to Use");
			}
		});

		// Removes all of the rows in the table
		clear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				modelR.setRowCount(0);
			}
		});

		// TABLE
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

		// Scrolls to the bottom of the table if the scroll bar is at the bottom
		table.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				if (extent == maximum - value) {
					table.scrollRectToVisible(table.getCellRect(table.getRowCount() - 1, 0, true));
				}
			}
		});

		// BOXES/BUTTONS
		// Listens for changes in selecting a destination from the JComboBox and
		// disconnects if a different destination is selected
		destinationSelect.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				int state = e.getStateChange();
				if (state == ItemEvent.SELECTED) {
					if (receive.getText().equals("Receive: On")) {
						receive.doClick();
					}
					if (activeMQProducer != null && activeMQProducer.isConnected()) {
						try {
							activeMQProducer.disconnect();
						} catch (JMSException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			}
		});

		// Allows the user to decide whether to use Topics or Queues
		destinationType.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (destinationType.getText().equals("Use: Topics")) {
					destinationType.setText("Use: Queues");
				} else {
					destinationType.setText("Use: Topics");
				}
				if (receive.getText().equals("Receive: On")) {
					receive.doClick();
				}
				if (activeMQProducer != null && activeMQProducer.isConnected()) {
					try {
						activeMQProducer.disconnect();
					} catch (JMSException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				refresh.doClick();
			}
		});

		// Creates a new Topic or Queue
		destinationCreate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					jd = new JDialogCustom(frame);
					jd.setLocationRelativeTo(null);
					jd.pack();
					jd.setVisible(true);
					if (jd.getValidatedText() != null) {
						model.addElement(jd.getValidatedText());
						session.createTopic(jd.getValidatedText());
						destinationSelect.setSelectedItem(jd.getValidatedText());
					}
				} catch (Exception err) {
					String error = "Caught while creating Destination:\n\n" + err + "\n";
					error += err.getStackTrace();
					displayMessageDialog(error, "Error");
				}
			}
		});

		// Allows the user to decide which message type to send
		dataType.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (dataType.getText().equals("Message Type: Stream")) {
					dataType.setText("Message Type: Byte");
				} else if (dataType.getText().equals("Message Type: Byte")) {
					dataType.setText("Message Type: Text");
				} else if (dataType.getText().equals("Message Type: Text")) {
					dataType.setText("Message Type: Stream");
				}
			}
		});

		// Sends a StreamMessage, BytesMessage, or TextMessage
		send.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (destinationSelect.getSelectedItem() != null) {
					try {
						if (activeMQProducer == null || !activeMQProducer.isConnected()) {
							if (destinationType.getText().equals("Use: Topics")) {
								activeMQProducer = (ActiveMQProducer) createProducer(addressText,
										clientID.getText() + " - ActiveMQProducer",
										destinationSelect.getSelectedItem().toString(), true);
							} else {
								activeMQProducer = (ActiveMQProducer) createProducer(addressText,
										clientID.getText() + " - ActiveMQProducer",
										destinationSelect.getSelectedItem().toString(), false);
							}
							activeMQProducer.run();
						}

						if (dataType.getText().equals("Message Type: Text")) {
							activeMQProducer.sendTextMessage(sendText.getText());
						} else if (dataType.getText().equals("Message Type: Stream")) {
							// Parse the text to decide what type of primitives
							// to send
							String[] array = sendText.getText().split("\\s+");
							String type = array[0].toLowerCase();

							if (!type.equals("byte") && !type.equals("short") && !type.equals("int")
									&& !type.equals("long") && !type.equals("char") && !type.equals("float")
									&& !type.equals("double") && !type.equals("boolean") && !type.equals("bool")
									&& !type.equals("mixed") && !type.equals("mix")) {
								displayMessageDialog(
										"\"" + type + "\" is not a valid primitive type.\n"
												+ "Please enter the type of the array as either a primitive type: byte, short, int, long, char, float, double, or bool (boolean), or as a mixed type: mix (mixed).",
										"Error");
							} else {
								// Parse primitives here and check that all
								// types match
								int size = Integer.parseInt(array[1]);

								if (type.equals("boolean") || type.equals("bool")) {
									boolean[] arrayN = new boolean[size];
									for (int i = 0; i < array.length - 2; i++) {
										arrayN[i] = Boolean.parseBoolean(array[i + 2]);
									}
									activeMQProducer.sendBooleanStreamMessage(size, arrayN);
								} else if (type.equals("byte")) {
									byte[] arrayN = new byte[size];
									for (int i = 0; i < array.length - 2; i++) {
										arrayN[i] = Byte.parseByte(array[i + 2]);
									}
									activeMQProducer.sendByteStreamMessage(size, arrayN);
								} else if (type.equals("char")) {
									char[] arrayN = new char[size];
									for (int i = 0; i < array.length - 2; i++) {
										arrayN[i] = array[i + 2].charAt(0);
									}
									activeMQProducer.sendCharStreamMessage(size, arrayN);
								} else if (type.equals("short")) {
									short[] arrayN = new short[size];
									for (int i = 0; i < array.length - 2; i++) {
										arrayN[i] = Short.parseShort(array[i + 2]);
									}
									activeMQProducer.sendShortStreamMessage(size, arrayN);
								} else if (type.equals("int")) {
									int[] arrayN = new int[size];
									for (int i = 0; i < array.length - 2; i++) {
										arrayN[i] = Integer.parseInt(array[i + 2]);
									}
									activeMQProducer.sendIntStreamMessage(size, arrayN);
								} else if (type.equals("long")) {
									long[] arrayN = new long[size];
									for (int i = 0; i < array.length - 2; i++) {
										arrayN[i] = Long.parseLong(array[i + 2]);
									}
									activeMQProducer.sendLongStreamMessage(size, arrayN);
								} else if (type.equals("double")) {
									double[] arrayN = new double[size];
									for (int i = 0; i < array.length - 2; i++) {
										arrayN[i] = Double.parseDouble(array[i + 2]);
									}
									activeMQProducer.sendDoubleStreamMessage(size, arrayN);
								} else if (type.equals("float")) {
									float[] arrayN = new float[size];
									for (int i = 0; i < array.length - 2; i++) {
										arrayN[i] = Float.parseFloat(array[i + 2]);
									}
									activeMQProducer.sendFloatStreamMessage(size, arrayN);
								} else if (type.equals("mixed") || type.equals("mix")) {
									Object[] arrayN = new Object[size];
									String typeMix = "";
									int adjustment = 0;
									for (int i = 0; i < array.length - 2; i++) {
										if (i % 2 == 0) {
											typeMix = array[i + 2];
											adjustment++;
										} else {
											if (typeMix.equals("byte")) {
												arrayN[i - adjustment] = Byte.parseByte(array[i + 2]);
											} else if (typeMix.equals("short")) {
												arrayN[i - adjustment] = Short.parseShort(array[i + 2]);
											} else if (typeMix.equals("int")) {
												arrayN[i - adjustment] = Integer.parseInt(array[i + 2]);
											} else if (typeMix.equals("long")) {
												arrayN[i - adjustment] = Long.parseLong(array[i + 2]);
											} else if (typeMix.equals("char")) {
												arrayN[i - adjustment] = "" + array[i + 2].charAt(0);
											} else if (typeMix.equals("float")) {
												arrayN[i - adjustment] = Float.parseFloat(array[i + 2]);
											} else if (typeMix.equals("double")) {
												arrayN[i - adjustment] = Double.parseDouble(array[i + 2]);
											} else if (typeMix.equals("boolean") || typeMix.equalsIgnoreCase("bool")) {
												arrayN[i - adjustment] = Boolean.parseBoolean(array[i + 2]);
											}
										}
									}
									activeMQProducer.sendMixedStreamMessage(size, arrayN);
								}
							}
						} else if (dataType.getText().equals("Message Type: Byte")) {
							// Parse the text to decide what type of primitives
							// to send
							String[] array = sendText.getText().split("\\s+");
							String type = array[0].toLowerCase();

							if (!type.equals("byte") && !type.equals("short") && !type.equals("int")
									&& !type.equals("long") && !type.equals("char") && !type.equals("float")
									&& !type.equals("double") && !type.equals("boolean") && !type.equals("bool")
									&& !type.equals("mixed") && !type.equals("mix")) {
								displayMessageDialog(
										"\"" + type + "\" is not a valid primitive type.\n"
												+ "Please enter the type of the array as either a primitive type: byte, short, int, long, char, float, double, or bool (boolean), or as a mixed type: mix (mixed).",
										"Error");
							} else {
								// Parse primitives here and check that all
								// types match
								int size = Integer.parseInt(array[1]);

								if (type.equals("boolean") || type.equals("bool")) {
									boolean[] arrayN = new boolean[size];
									for (int i = 0; i < array.length - 2; i++) {
										arrayN[i] = Boolean.parseBoolean(array[i + 2]);
									}
									activeMQProducer.sendBooleanBytesMessage(size, arrayN);
								} else if (type.equals("byte")) {
									byte[] arrayN = new byte[size];
									for (int i = 0; i < array.length - 2; i++) {
										arrayN[i] = Byte.parseByte(array[i + 2]);
									}
									activeMQProducer.sendByteBytesMessage(size, arrayN);
								} else if (type.equals("char")) {
									char[] arrayN = new char[size];
									for (int i = 0; i < array.length - 2; i++) {
										arrayN[i] = array[i + 2].charAt(0);
									}
									activeMQProducer.sendCharBytesMessage(size, arrayN);
								} else if (type.equals("short")) {
									short[] arrayN = new short[size];
									for (int i = 0; i < array.length - 2; i++) {
										arrayN[i] = Short.parseShort(array[i + 2]);
									}
									activeMQProducer.sendShortBytesMessage(size, arrayN);
								} else if (type.equals("int")) {
									int[] arrayN = new int[size];
									for (int i = 0; i < array.length - 2; i++) {
										arrayN[i] = Integer.parseInt(array[i + 2]);
									}
									activeMQProducer.sendIntBytesMessage(size, arrayN);
								} else if (type.equals("long")) {
									long[] arrayN = new long[size];
									for (int i = 0; i < array.length - 2; i++) {
										arrayN[i] = Long.parseLong(array[i + 2]);
									}
									activeMQProducer.sendLongBytesMessage(size, arrayN);
								} else if (type.equals("double")) {
									double[] arrayN = new double[size];
									for (int i = 0; i < array.length - 2; i++) {
										arrayN[i] = Double.parseDouble(array[i + 2]);
									}
									activeMQProducer.sendDoubleBytesMessage(size, arrayN);
								} else if (type.equals("float")) {
									float[] arrayN = new float[size];
									for (int i = 0; i < array.length - 2; i++) {
										arrayN[i] = Float.parseFloat(array[i + 2]);
									}
									activeMQProducer.sendFloatBytesMessage(size, arrayN);
								} else if (type.equals("mixed") || type.equals("mix")) {
									displayMessageDialog(
											"Mixed messages are not supported for a BytesMessage.\n\nThis would require data to be attached to a BytesMessage to specify what type of data is being sent, which would result in a redundant method that performs identical to using a mixed message with StreamMessage.",
											"Warning");
								}
							}
						}
					} catch (Exception err) {
						String error = "Caught while sending message:\n\n" + err + "\n";
						error += err.getStackTrace();
						displayMessageDialog(error, "Error");
					}
				} else {
					displayMessageDialog("No Topic or Queue is selected. Please create or select a Topic or Queue.",
							"Requirements");
				}
			}
		});

		// Opens a new ActiveMQConsumer thread to receive messages or closes and
		// open one
		receive.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Thread receives from Topics until turned off
				if (receive.getText().equals("Receive: On")) {
					receive.setText("Receive: Off");

					try {
						activeMQConsumer.disconnect();
					} catch (Exception err) {
						String error = "Caught while closing ActiveMQConsumer:\n\n" + err + "\n";
						error += err.getStackTrace();
						displayMessageDialog(error, "Error");
					}

				} else {
					if (destinationSelect.getSelectedItem() != null) {
						receive.setText("Receive: On");

						if (destinationType.getText().equals("Use: Topics")) {
							activeMQConsumer = (ActiveMQConsumer) createConsumer(addressText,
									clientID.getText() + " - ActiveMQConsumer",
									destinationSelect.getSelectedItem().toString(), clientID.getText());
						} else {
							activeMQConsumer = (ActiveMQConsumer) createConsumer(addressText,
									clientID.getText() + " - ActiveMQConsumer",
									destinationSelect.getSelectedItem().toString());
						}

						activeMQConsumer.run();
					} else {
						displayMessageDialog(
								"No Topic or Queue is selected.  Please create or select a Topic or Queue.",
								"Requirements");
					}
				}
			}
		});

		// Refreshes the list of available destinations
		refresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				// BrokerService broker = new BrokerService();
				// try {
				// broker.start();
				// System.out.println(Arrays.toString(broker.getAdminView().getInactiveDurableTopicSubscribers()));
				// broker.stop();
				// } catch (Exception e1) {
				// // TODO Auto-generated catch block
				// e1.printStackTrace();
				// }

				// Make sure to retain the previous choice if it isn't removed
				String previous = (String) model.getSelectedItem();
				updateDestinations();
				for (int i = 0; i < destinationSelect.getItemCount(); i++) {
					if (destinationSelect.getItemAt(i).equals(previous)) {
						destinationSelect.setSelectedItem(previous);
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
		frame = new JFrame("ActiveMQ Client");
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);

		// Create and set up the content pane.
		ActiveMQClient activeMQClient = new ActiveMQClient();
		activeMQClient.addComponentToPane(frame.getContentPane());

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

	public void updateDestinations() {
		model = new DefaultComboBoxModel<String>();
		if (destinationType.getText().equals("Use: Topics")) {
			for (ActiveMQTopic topic : topics) {
				try {
					destinationSelect.addItem(topic.getTopicName());
					model.addElement(topic.getTopicName());
				} catch (Exception err) {
					String error = "Caught while updating Topics:\n\n" + err + "\n";
					error += err.getStackTrace();
					displayMessageDialog(error, "Error");
				}
			}
		} else {
			for (ActiveMQQueue queue : queues) {
				try {
					destinationSelect.addItem(queue.getQueueName());
					model.addElement(queue.getQueueName());
				} catch (Exception err) {
					String error = "Caught while updating Queues:\n\n" + err + "\n";
					error += err.getStackTrace();
					displayMessageDialog(error, "Error");
				}
			}
		}
		destinationSelect.setModel(model);
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
				new ActiveMQClient().createAndDisplayGUI();
			}
		});
	}

	public int getVerticalScrollBarValue() {
		return scrollR.getVerticalScrollBar().getModel().getValue();
	}

	public int getVerticalScrollBarMaximum() {
		return scrollR.getVerticalScrollBar().getModel().getMaximum();
	}

	public int getVerticalScrollBarExtent() {
		return scrollR.getVerticalScrollBar().getModel().getExtent();
	}

	@Override
	public Producer createProducer(String address, String clientID, String destinationName, boolean useTopics) {
		return new ActiveMQProducer(address, clientID, destinationSelect.getSelectedItem().toString(), useTopics);
	}

	@Override
	public Consumer createConsumer(String address, String clientID, String destinationName, String subscriptionName) {
		return new ActiveMQConsumer(address, clientID, destinationName, subscriptionName, ActiveMQClient.this);
	}

	@Override
	public Consumer createConsumer(String address, String clientID, String destinationName) {
		return new ActiveMQConsumer(address, clientID, destinationName, ActiveMQClient.this);
	}

	@Override
	public void connect() {
		try {
			// Initialize ConnectionFactory
			connectionFactory = new ActiveMQConnectionFactory(addressText);

			// Initialize and Start Connection
			connection = (ActiveMQConnection) connectionFactory.createConnection();
			connection.setClientID("");
			connection.start();

			// Initialize Session
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Get all Destinations
			ds = connection.getDestinationSource();
			topics = ds.getTopics();
			queues = ds.getQueues();

			updateDestinations();

			// Display ActiveMQConsumer and ActiveMQProducer settings
			cardLayout.show(cards, "2");

		} catch (Exception err) {
			String error = "Caught while connecting:\n\n" + err + "\n";
			error += err.getStackTrace();
			displayMessageDialog(error, "Error");
		}
	}

	@Override
	public void disconnect() {
		try {
			if (receive != null && receive.getText() == "Receive: On") {
				receive.doClick();
			}
			if (activeMQConsumer != null) {
				activeMQConsumer.disconnect();
			}
			if (activeMQProducer != null) {
				activeMQProducer.disconnect();
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
}