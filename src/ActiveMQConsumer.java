
import java.util.Arrays;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageEOFException;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import activeMQInterface.Consumer;

/**
 * ActiveMQConsumer implements Runnable to allow process-based threading and
 * MessageListener to listen to messages from a server. This class also
 * implements the Consumer interface to create a JMS Consumer for Apache
 * ActiveMQ.
 */
public class ActiveMQConsumer implements Runnable, MessageListener, Consumer {
	private String address = null;
	private String clientID = null;
	private String destinationName = null;
	private String subscriptionName = null;

	private boolean isConnected = false;

	private Connection connection = null;
	private Session session = null;
	private Topic topic = null;
	private Queue queue = null;
	private MessageConsumer messageConsumer = null;

	private TimeKeeper time = null;

	public ActiveMQConsumer(String address, String clientID, String destinationName, String subscriptionName) {
		this.address = address;
		this.clientID = clientID;
		this.destinationName = destinationName;
		this.subscriptionName = subscriptionName;
	}

	public ActiveMQConsumer(String address, String clientID, String destinationName) {
		this.address = address;
		this.clientID = clientID;
		this.destinationName = destinationName;
	}

	/**
	 * This method returns the address of the server the activeMQConsumer is
	 * connected to.
	 * 
	 * @return a String representation of the address.
	 * @throws JMSException
	 */
	public String getAddress() throws JMSException {
		return address;
	}

	/**
	 * This method returns the ClientID of the ActiveMQConsumer as a way to
	 * identify the connection.
	 * 
	 * @return a String representation of the ClientID. If one is not provided,
	 *         ActiveMQ will randomly generate one with information from the
	 *         computer it is ran on.
	 * @throws JMSException
	 */
	public String getID() throws JMSException {
		return connection.getClientID();
	}

	/**
	 * This method returns the Destination the activeMQConsumer is listening to.
	 * 
	 * @return a String representation of the Destination.
	 * @throws JMSException
	 */
	public String getDestination() throws JMSException {
		return destinationName;
	}

	/**
	 * This method returns the subscription name of the activeMQConsumer if it's
	 * a durable subscriber, else, returning null.
	 * 
	 * @return a String representation of the Destination.
	 * @throws JMSException
	 */
	public String getSubscription() throws JMSException {
		return subscriptionName;
	}

	/**
	 * This method returns a boolean to tell whether the Consumer is connected
	 * to the server or not.
	 * 
	 * @return
	 */
	public boolean isConnected() {
		return isConnected;
	}

	/**
	 * This method returns a time in the format of X-XX:XX:XX.XXXX
	 * (days-hours:minutes:seconds.miliseconds).
	 * 
	 * @return a String value of the formatted time passed since run() was
	 *         called.
	 */
	public String getBaseTime() {
		return time.getBaseTime();
	}

	@Override
	public void run() {
		try {
			// Create a ConnectionFactory
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(address);

			// Create a Connection
			connection = (ActiveMQConnection) connectionFactory.createConnection();
			connection.setClientID(clientID);
			isConnected = true;

			// Create a Session
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Create a Topic or Queue to listen to messages with a
			// MessageConsumer
			if (subscriptionName != null) {
				// A durable subscriber is created to read Topic messages when
				// the activeMQClient is offline
				topic = session.createTopic(destinationName);
				messageConsumer = session.createDurableSubscriber(topic,
						subscriptionName/*
										 * , "Group = '" + clientID + "'", false
										 */);
			} else {
				queue = session.createQueue(destinationName);
				messageConsumer = session.createConsumer(queue);
			}

			// MessageListener processes received messages
			messageConsumer.setMessageListener(this);

			// Start the connection
			connection.start();
			time = new TimeKeeper();

		} catch (Exception e) {
			System.out.println("Caught: " + e);
			e.printStackTrace();
			time = null;
		}
	}

	@Override
	public void onMessage(Message message) {
		try {
			// if (message.propertyExists("Group") &&
			// message.getStringProperty("Group").equals("Test Group 2")) {
			// System.out.println("Yes");
			// String[] testArray = new String[]{"Hey, we can send specific",
			// "messages via selectors for PTP."};
			// activeMQClient.insertData(new Object[] { "byte[]",
			// Arrays.toString(testArray), testArray });
			// }

			if (message instanceof StreamMessage) {
				StreamMessage streamMessage = (StreamMessage) message;
				Object value = null;
				Object[] values = null;
				boolean oneType = true;
				boolean hasType = false;
				Object type = null;
				int check = 0;
				int size = 0;

				try {
					// Get the size of the StreamMessage data
					size = (int) streamMessage.readObject();
					values = new Object[size];

					// Get StreamMessage data
					for (int i = 0; i < size; i++) {
						value = streamMessage.readObject();
						if (value instanceof String) {
							values[i] = ((String) value).charAt(0);
						} else {
							values[i] = value;
						}

						if (values[i] == null && !hasType) {
							check++;
						}
						if (i == check) {
							type = values[0];
							hasType = true;
						} else {
							if (values[i] != null && !type.getClass().equals(values[i].getClass())) {
								oneType = false;
							}
						}
					}

					printConsistency(values, type, oneType);
				} catch (MessageEOFException noMoreData) {
					printConsistency(values, type, oneType);
				}
			} else if (message instanceof BytesMessage) {
				BytesMessage bytesMessage = (BytesMessage) message;
				int size = 0;
				byte type = 0;

				try {
					// Get the size and type of BytesMessage
					type = bytesMessage.readByte();
					size = (int) bytesMessage.readInt();

					if (type == 1) {
						boolean[] values = new boolean[size];
						for (int i = 0; i < size; i++) {
							values[i] = bytesMessage.readBoolean();
						}
						System.out.println("boolean[] type: " + Arrays.toString(values));
					} else if (type == 2) {
						byte[] values = new byte[size];
						for (int i = 0; i < size; i++) {
							values[i] = bytesMessage.readByte();
						}
						System.out.println("byte[] type: " + Arrays.toString(values));
					} else if (type == 3) {
						char[] values = new char[size];
						for (int i = 0; i < size; i++) {
							values[i] = (char) bytesMessage.readByte();
						}
						System.out.println("char[] type: " + Arrays.toString(values));
					} else if (type == 4) {
						short[] values = new short[size];
						for (int i = 0; i < size; i++) {
							values[i] = bytesMessage.readShort();
						}
						System.out.println("short[] type: " + Arrays.toString(values));
					} else if (type == 5) {
						int[] values = new int[size];
						for (int i = 0; i < size; i++) {
							values[i] = bytesMessage.readInt();
						}
						System.out.println("int[] type: " + Arrays.toString(values));
					} else if (type == 6) {
						long[] values = new long[size];
						for (int i = 0; i < size; i++) {
							values[i] = bytesMessage.readLong();
						}
						System.out.println("long[] type: " + Arrays.toString(values));
					} else if (type == 7) {
						double[] values = new double[size];
						for (int i = 0; i < size; i++) {
							values[i] = bytesMessage.readDouble();
						}
						System.out.println("double[] type: " + Arrays.toString(values));
					} else if (type == 8) {
						float[] values = new float[size];
						for (int i = 0; i < size; i++) {
							values[i] = bytesMessage.readFloat();
						}
						System.out.println("float[] type: " + Arrays.toString(values));
					}
				} catch (MessageEOFException noMoreData) {
					String error = "Caught while receiving data from the ActiveMQConsumer:\n\n" + noMoreData + "\n";
					error += noMoreData.getStackTrace();
					System.out.println(error);
				}
				// BytesMessage bytesMessage = (BytesMessage) message;
				// byte[] values = null;
				// long length = 0;
				//
				// try {
				// length = bytesMessage.getBodyLength();
				// values = new byte[(int) length];
				//
				// bytesMessage.readBytes(values);
				//
				// activeMQClient.insertData(new Object[] { "byte[]",
				// Arrays.toString(values), values });
				// } catch (MessageEOFException noMoreData) {
				// String error = "Caught while receiving data from the
				// ActiveMQConsumer:\n\n" + noMoreData + "\n";
				// error += noMoreData.getStackTrace();
				// activeMQClient.displayMessageDialog(error, "Error");
				// }

			} else if (message instanceof TextMessage) {
				TextMessage textMessage = (TextMessage) message;
				String text = textMessage.getText();
				System.out.println(text);
			}
		} catch (JMSException err) {
			String error = "Caught while receiving data from the ActiveMQConsumer:\n\n" + err + "\n";
			error += err.getStackTrace();
			System.out.println(error);
		}
	}

	/**
	 * This method takes information to insert into a table in the
	 * ActiveMQClient GUI.
	 * 
	 * @param values
	 *            - the values to insert into a table.
	 * @param type
	 *            - the type of the values to be inserted into the table given
	 *            that oneType is true.
	 * @param oneType
	 *            - a boolean indicating whether all of the values in the Object
	 *            array are of the same type. If they are, then the data
	 *            inserted into the table will indicate that they are all of the
	 *            same type.
	 */
	public void printConsistency(Object[] values, Object type, boolean oneType) {
		if (oneType) {
			int sizeN = values.length;
			if (type instanceof Boolean) {
				boolean[] valuesN = new boolean[sizeN];
				for (int i = 0; i < sizeN; i++) {
					if (values[i] != null) {
						valuesN[i] = (boolean) values[i];
					}
				}
				System.out.println("boolean[] type: " + Arrays.toString(valuesN));
			} else if (type instanceof Byte) {
				byte[] valuesN = new byte[sizeN];
				for (int i = 0; i < sizeN; i++) {
					if (values[i] != null) {
						valuesN[i] = (byte) values[i];
					}
				}
				System.out.println("byte[] type: " + Arrays.toString(valuesN));
			} else if (type instanceof Character) {
				// char[] valuesN = ((String) values[0]).toCharArray();
				char[] valuesN = new char[sizeN];
				for (int i = 0; i < sizeN; i++) {
					if (values[i] != null) {
						valuesN[i] = (char) values[i];
					}
				}
				System.out.println("char[] type: " + Arrays.toString(valuesN));
			} else if (type instanceof Short) {
				short[] valuesN = new short[sizeN];
				for (int i = 0; i < sizeN; i++) {
					if (values[i] != null) {
						valuesN[i] = (short) values[i];
					}
				}
				System.out.println("short[] type: " + Arrays.toString(valuesN));
			} else if (type instanceof Integer) {
				int[] valuesN = new int[sizeN];
				for (int i = 0; i < sizeN; i++) {
					if (values[i] != null) {
						valuesN[i] = (int) values[i];
					}
				}
				System.out.println("int[] type: " + Arrays.toString(valuesN));
			} else if (type instanceof Long) {
				long[] valuesN = new long[sizeN];
				for (int i = 0; i < sizeN; i++) {
					if (values[i] != null) {
						valuesN[i] = (long) values[i];
					}
				}
				System.out.println("long[] type: " + Arrays.toString(valuesN));
			} else if (type instanceof Double) {
				double[] valuesN = new double[sizeN];
				for (int i = 0; i < sizeN; i++) {
					if (values[i] != null) {
						valuesN[i] = (double) values[i];
					}
				}
				System.out.println("double[] type: " + Arrays.toString(valuesN));
			} else if (type instanceof Float) {
				float[] valuesN = new float[sizeN];
				for (int i = 0; i < sizeN; i++) {
					if (values[i] != null) {
						valuesN[i] = (float) values[i];
					}
				}
				System.out.println("float[] type: " + Arrays.toString(valuesN));
			}
		} else {
			System.out.println("Object[] type: " + Arrays.toString(values));
		}
	}

	@Override
	public void unsubscribe() throws JMSException {
		if (subscriptionName != null) {
			messageConsumer.close();
			session.unsubscribe(subscriptionName);
		}
	}

	@Override
	public void disconnect() throws JMSException {
		isConnected = false;
		connection.close();
	}
}