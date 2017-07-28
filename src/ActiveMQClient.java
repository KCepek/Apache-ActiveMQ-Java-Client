import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.Set;

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
public class ActiveMQClient extends JFrame implements Client, MessageListener, Runnable {

	// Threading
	static Scanner in;

	// Connection Parameters
	private String address = null;
	private ActiveMQConnectionFactory connectionFactory = null;
	private ActiveMQConnection connection = null;
	private Session session = null;
	private DestinationSource ds = null;
	private Set<ActiveMQTopic> topicsSet = null;
	private Set<ActiveMQQueue> queuesSet = null;
	private MessageConsumer messageConsumer = null;
	private MessageProducer messageProducer = null;
	private ActiveMQConsumer activeMQConsumer = null;
	private ActiveMQProducer activeMQProducer = null;

	private TimeKeeper time = null;

	/**
	 * This method sends the statistics for all of the Client's open
	 * connections, i.e. its Consumers and Producers.
	 * 
	 * @return a String array representing all of the connection info available
	 *         (no passwords).
	 * 
	 * @throws JMSException
	 *             a JMSException will be thrown given that the connection to
	 *             the server is not open.
	 */
	public String[] getAdvisorStats() throws JMSException {
		String[] stats = new String[3];
		stats[0] = time.getBaseTime() + " " + connection.getClientID();
		if (activeMQConsumer != null && activeMQConsumer.isConnected()) {
			stats[1] = activeMQConsumer.getBaseTime() + " " + activeMQConsumer.getID();
		}
		if (activeMQProducer != null) {
			stats[2] = activeMQProducer.getBaseTime() + " " + activeMQProducer.getID();
		}

		return stats;
	}

	public static void main(String[] args) throws JMSException {
		// in = new BufferedReader(new InputStreamReader(System.in));
		in = new Scanner(System.in);

		// Create a new thread to handle the input
		Thread t1 = new Thread(new ActiveMQClient());
		t1.start();
	}

	@Override
	public void run() {
		String word = "";
		Scanner line = null;

		outerLoop: while (true) {
			String test = in.nextLine();
			line = new Scanner(test);

			// Parse through all of the arguments to allow for multiple
			// arguments to be sent at once, e.g., creating a producer and using
			// it to send messages all in one line.
			while (line.hasNext()) {
				try {
					word = line.next();
					// Connect the Client to the server
					// connect tcp://localhost:61616
					if (word.toLowerCase().equals("connect")) {
						address = line.next();
						connect(address);
						System.out.println("CLIENT CONNECTED TO: " + address + "\n");
					}
					// Disconnect the Client from the server
					// disconnect
					else if (word.toLowerCase().equals("disconnect")) {
						disconnect();
						System.out.println("CLIENT DISCONNECTED FROM: " + address + "\n");
					} else if (word.toLowerCase().equals("sendtext") || word.toLowerCase().equals("sendt")) {
						line.useDelimiter("\"");
						line.next();
						String data = line.next();
						// System.out.println("S|" + data + "|E");
						line.reset();

						if (activeMQProducer == null || !activeMQProducer.isConnected()) {
							System.out.println("A Producer is not active.  Please create one to send messages.\n");
						} else {
							sendTextData(data);
						}

					} else if (word.toLowerCase().equals("sendstream") || word.toLowerCase().equals("sends")) {
						line.useDelimiter("\"");
						line.next();
						String data = line.next();
						// System.out.println("S|" + data + "|E");
						line.reset();

						if (activeMQProducer == null || !activeMQProducer.isConnected()) {
							System.out.println("A Producer is not active.  Please create one to send messages.\n");
						} else {
							sendStreamData(data);
						}

					} else if (word.toLowerCase().equals("sendbytes") || word.toLowerCase().equals("sendb")) {
						line.useDelimiter("\"");
						line.next();
						String data = line.next();
						// System.out.println("S|" + data + "|E");
						line.reset();

						if (activeMQProducer == null || !activeMQProducer.isConnected()) {
							System.out.println("A Producer is not active.  Please create one to send messages.\n");
						} else {
							sendBytesData(data);
						}
					}
					// Create a nondurable Consumer that listens to Queues
					// ccon clientID destinationName
					else if (word.toLowerCase().equals("ccon")) {
						String server = line.next();
						String clientID = line.next() + " - ActiveMQConsumer";
						String destinationName = line.next();
						if (activeMQConsumer == null || !activeMQConsumer.isConnected()) {

							activeMQConsumer = (ActiveMQConsumer) createConsumer(server,
									clientID, destinationName);
							activeMQConsumer.run();
							if (activeMQConsumer != null && activeMQConsumer.isConnected()) {
								System.out.println("CONSUMER CONNECTED: \n\t" + String.format("%-13s", "Server:")
										+ server + "\n\t" + String.format("%-13s", "Client ID:") + clientID + "\n\t"
										+ String.format("%-13s", "Destination:") + destinationName + "\n\t"
										+ String.format("%-13s", "Durable:") + false + "\n");
							}
						} else {
							System.out.println("The Consumer has already been created.\n");
						}
					}
					// Create a durable Consumer that listens to Topics
					// ccond clinetID destinationName
					else if (word.toLowerCase().equals("ccond")) {
						String server = line.next();
						String clientID = line.next() + " - ActiveMQConsumer";
						String destinationName = line.next();
						if (activeMQConsumer == null || !activeMQConsumer.isConnected()) {

							activeMQConsumer = (ActiveMQConsumer) createConsumer(server,
									clientID, destinationName, clientID);
							activeMQConsumer.run();
							if (activeMQConsumer != null && activeMQConsumer.isConnected()) {
								System.out.println("CONSUMER CONNECTED: \n\t" + String.format("%-13s", "Server:")
										+ server + "\n\t" + String.format("%-13s", "Client ID:") + clientID + "\n\t"
										+ String.format("%-13s", "Destination:") + destinationName + "\n\t"
										+ String.format("%-13s", "Durable:") + true + "\n");
							}
						} else {
							System.out.println("The Consumer has already been created.\n");
						}
					}

					// Create a Producer
					// cpro clientID destinationName useTopics
					else if (word.toLowerCase().equals("cpro")) {
						String server = line.next();
						String clientID = line.next() + " - ActiveMQProducer";
						String destinationName = line.next();
						boolean useTopics = Boolean.valueOf(line.next());
						if (activeMQProducer == null || !activeMQProducer.isConnected()) {

							activeMQProducer = (ActiveMQProducer) createProducer(server,
									clientID, destinationName, useTopics);
							activeMQProducer.run();
							if (activeMQProducer != null && activeMQProducer.isConnected()) {
								System.out.println("PRODUCER CONNECTED: \n\t" + String.format("%-13s", "Server:")
										+ server + "\n\t" + String.format("%-13s", "Client ID:") + clientID + "\n\t"
										+ String.format("%-13s", "Destination:") + destinationName + "\n\t"
										+ String.format("%-13s", "Use Topics:") + useTopics + "\n");
							}
						} else {
							System.out.println("The Producer has already been created.\n");
						}
					}

					// Disconnect the Consumer if it exists
					else if (word.toLowerCase().equals("dcon")) {
						disconnectConsumer();
					}
					// Disconnect the Producer if it exists
					else if (word.toLowerCase().equals("dpro")) {
						disconnectProducer();
					}
					// List all Destinations
					else if (word.toLowerCase().equals("ldes")) {
						String[] topics = listTopics();
						String[] queues = listQueues();

						if (topics.length == 0) {
							System.out.println("TOPICS: EMPTY");
						} else {
							System.out.print("TOPICS:");
							for (String topic : topics) {
								System.out.print("\n\t" + topic);
							}
							System.out.println();
							System.out.println();
						}

						if (queues.length == 0) {
							System.out.println("QUEUES: EMPTY");
						} else {
							System.out.print("QUEUES:");
							for (String queue : queues) {
								System.out.print("\n\t" + queue);
							}
							System.out.println();
							System.out.println();
						}
					}
					// Display all of the commands
					else if (word.toLowerCase().equals("help")) {
						System.out.println("COMMANDS:");
						System.out.println(getCommandInfo());
					}
					// Quit the program
					else if (word.toLowerCase().equals("quit")) {
						System.out.println("Quitting...\n");
						disconnect();
						break outerLoop;
					}
				} catch (Exception err) {
					err.printStackTrace();
				}
			}
		}
	}

	@Override
	public String[] listQueues() throws JMSException {
		ds = connection.getDestinationSource();
		queuesSet = ds.getQueues();

		int i = 0;
		String[] queues = new String[queuesSet.size()];

		for (ActiveMQQueue queue : queuesSet) {
			queues[i] = queue.getQueueName();
			i++;
		}

		return queues;
	}

	@Override
	public String[] listTopics() throws JMSException {
		ds = connection.getDestinationSource();
		topicsSet = ds.getTopics();

		int i = 0;
		String[] topics = new String[topicsSet.size()];

		for (ActiveMQTopic topic : topicsSet) {
			topics[i] = topic.getTopicName();
			i++;
		}

		return topics;
	}

	@Override
	public String getCommandInfo() {
		return "connect [address]\n    connects the client to a server, which allows for Consumers and Producers to be created on the same server"
				+ "\n\ndisconnect\n    disconnects the client from a server, including any open Consumers or Producers"
				+ "\n\nccon [server clientID destinationName]\n    connects a nondurable Consumer given a client ID and destination name (Queue) if the client is connected to a server"
				+ "\n\nccond [server clientID destinationName]\n    connects a durable Consumer given a client ID and destination name (Topic) if the client is connected to a server"
				+ "\n\ncpro [server clientID destinationName useTopics]\n    connects a Producer given a client ID, destination name, and boolean value for whether to use Topics or Queues if the client is connected to a server"
				+ "\n\ndcon\n    disconnects the Consumer if it has been instantiated and is running"
				+ "\n\ndpro\n    disconnects the Producer if it has been instantiated and is running"
				+ "\n\nldes\n    displays all of the accessible destinations on a server"
				+ "\n\nquit\n    closes all of the client's connections and ends the program"
				+ "\n\nsendtext | sendt [\"text\"]\n    This will send a String of all text in the text box in the form of a TextMessage."
				+ "\n\nsendstream | sends [\"stream info\"]\n    One Type: First enter the primitive data type that will be sent.  The type can be either a byte, short, int, long, char, float, double, or bool (boolean).  Next, enter the amount of data to be sent followed by the data.  If the amount of data provided is less than the length, then the empty data will be filled in with the data type's equivalent of zero.\n    Mixed Type: This follows the same rules as stream messages of one type, except mix (mixed) is entered instead of the primitive type followed by the size of the data.  The primitive type is then written in front of each piece of data to specify what type the data should be."
				+ "\n\nsendbytes | sendb [\"bytes info\"]\n    This follows the same rules as sendstream; however, the data received is represented by a byte array with one byte at the beginning of the message to designate the type.  Mixed type messages are not used, since they would just be a redundant version of a mixed type StreamMessage.  ByteMessages are meant to be faster due to less bytes being used to identify the type of data in the message."
				+ "\n\n    Examples\n      sendstream \"int 6 4 3 23 6 3 298\"\n          -> [4, 3, 23, 6, 3, 298]\n\n      sendbytes \"int 4 3 2\"\n          -> [3, 2, 0, 0]\n\n      sendbytes \"bool 4 true false 89fj TruE\"\n          -> [true, false, false, true]\n\n      sendbytes \"char 3 f 8 a\"\n          -> [f, 8, a]\n\n      sendstream \"mix 5 int 4 bool true char f byte 2 double 32.38\"\n          -> [4, true, f, 2, 32.38]\n\n      sendstream \"mix 3 int 1 long 9283928\"\n          -> [1, 9283928, null]\n";
	}

	public void sendTextData(String data) throws JMSException {
		activeMQProducer.sendTextMessage(data);
	}

	public void sendStreamData(String data) throws JMSException {
		String[] array = data.split("\\s+");
		String type = array[0].toLowerCase();

		if (!type.equals("byte") && !type.equals("short") && !type.equals("int") && !type.equals("long")
				&& !type.equals("char") && !type.equals("float") && !type.equals("double") && !type.equals("boolean")
				&& !type.equals("bool") && !type.equals("mixed") && !type.equals("mix")) {
			System.out.println("\"" + type + "\" is not a valid primitive type.\n"
					+ "Please enter the type of the array as either a primitive type: byte, short, int, long, char, float, double, or bool (boolean), or as a mixed type: mix (mixed).\n");
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
	}

	public void sendBytesData(String data) throws JMSException, UnsupportedEncodingException {
		String[] array = data.split("\\s+");
		String type = array[0].toLowerCase();

		if (!type.equals("byte") && !type.equals("short") && !type.equals("int") && !type.equals("long")
				&& !type.equals("char") && !type.equals("float") && !type.equals("double") && !type.equals("boolean")
				&& !type.equals("bool") && !type.equals("mixed") && !type.equals("mix")) {
			System.out.println("\"" + type + "\" is not a valid primitive type.\n"
					+ "Please enter the type of the array as either a primitive type: byte, short, int, long, char, float, double, or bool (boolean), or as a mixed type: mix (mixed).\n");
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
				System.out.println(
						"Mixed messages are not supported for a BytesMessage.\n\nThis would require data to be attached to a BytesMessage to specify what type of data is being sent, which would result in a redundant method that performs identical to using a mixed message with StreamMessage.\n");
			}
		}
	}

	@Override
	public Consumer createConsumer(String address, String clientID, String destinationName) {
		return new ActiveMQConsumer(address, clientID, destinationName);
	}

	@Override
	public Consumer createConsumer(String address, String clientID, String destinationName, String subscriptionName) {
		return new ActiveMQConsumer(address, clientID, destinationName, subscriptionName);
	}

	@Override
	public Producer createProducer(String address, String clientID, String destinationName, boolean useTopics) {
		return new ActiveMQProducer(address, clientID, destinationName, useTopics);
	}

	@Override
	public void connect(String address) throws JMSException {
		// Initialize ConnectionFactory
		connectionFactory = new ActiveMQConnectionFactory(address);

		// Initialize the Connection
		connection = (ActiveMQConnection) connectionFactory.createConnection();
		connection.setClientID("");

		// Initialize Session
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		// Create a Consumer to receive requests from the Advisor
		Destination advisoryDestination = session.createTopic("ActiveMQ.Advisory.Advisor");
		messageConsumer = session.createConsumer(advisoryDestination);
		messageConsumer.setMessageListener(this);

		// Create a Producer for responding to requests on the
		// ActiveMQ.Advisory.Advisor Topic.
		messageProducer = session.createProducer(advisoryDestination);

		// Start the connection
		connection.start();
		time = new TimeKeeper();

		// System.out.println(connection.getConnectionInfo().getConnectionId());
		// System.out.println(connection.getConnectionInfo().getClientId());
		// System.out.println(connection.getConnectionInfo().getClientIp());
		// System.out.println(connection.getStats().getLastSampleTime());
	}

	@Override
	public void disconnectConsumer() throws JMSException {
		if (activeMQConsumer != null && activeMQConsumer.isConnected()) {
			activeMQConsumer.disconnect();
			System.out.println("CONSUMER DISCONNECTED\n");
		} else {
			System.out.println("The Consumer has either already been disconnected or has not been created yet.\n");
		}
	}

	@Override
	public void disconnectProducer() throws JMSException {
		if (activeMQProducer != null && activeMQProducer.isConnected()) {
			activeMQProducer.disconnect();
			System.out.println("PRODUCER DISCONNECTED\n");
		} else {
			System.out.println("The Producer has either already been disconnected or has not been created yet.\n");
		}
	}

	@Override
	public void disconnect() throws JMSException {
		if (activeMQConsumer != null) {
			activeMQConsumer.disconnect();
		}
		if (activeMQProducer != null) {
			activeMQProducer.disconnect();
		}
		if (connection != null) {
			connection.close();
		}
	}

	/**
	 * A method from an implementation of MessageListener to receive and
	 * evaluate various JMS messages. The use of onMessage in the Client is to
	 * receive and send information to an advisor class in order to let the
	 * server know how long each connection has been online.
	 * 
	 * @param message
	 *            - the message that is received from the server.
	 */
	@Override
	public void onMessage(Message message) {
		// Use the Client Consumer for commands to gather information from
		// all of the connections
		if (message instanceof BytesMessage) {
			try {
				BytesMessage bytesMessage = (BytesMessage) message;
				byte command = bytesMessage.readByte();

				// Commands for clients
				if (command == 1) {
					TextMessage textMessage = null;
					String[] stats = getAdvisorStats();

					for (int i = 0; i < stats.length; i++) {
						if (stats[i] != null) {
							textMessage = session.createTextMessage(stats[i]);
							messageProducer.send(textMessage);
						}
					}
				}
			} catch (JMSException err) {
				String error = "Caught receiving advisor message:\n\n" + err + "\n";
				error += err.getStackTrace();
				System.out.println(error);
			}

		}
	}
}