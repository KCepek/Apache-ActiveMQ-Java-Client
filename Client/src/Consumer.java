import java.util.Arrays;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageEOFException;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.swing.table.DefaultTableModel;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public class Consumer implements Runnable, MessageListener {
	private String address;
	private String clientID;
	private String topicName;
	private String subscriptionName;

	private Connection connection;
	private Session session;
	private MessageConsumer messageConsumer;

	private Client client;

	public Consumer(String address, String clientID, String topicName, String subscriptionName, Client client) {
		this.address = address;
		this.clientID = clientID;
		this.topicName = topicName;
		this.subscriptionName = subscriptionName;
		this.client = client;
	}

	@Override
	public void run() {
		try {
			// Create a ConnectionFactory
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(address);

			// Create a Connection
			connection = (ActiveMQConnection) connectionFactory.createConnection();
			connection.setClientID(clientID);

			// Create a Session
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Register to be notified of all messages on the topic
			Topic topic = session.createTopic(topicName);

			// Create a MessageConsumer to consume messages
			messageConsumer = session.createDurableSubscriber(topic, subscriptionName);

			// Start the connection
			connection.start();

			// MessageListener processes received messages
			messageConsumer.setMessageListener(this);

		} catch (Exception e) {
			System.out.println("Caught: " + e);
			e.printStackTrace();
		}
	}

	@Override
	public void onMessage(Message message) {
		try {
			if (message instanceof StreamMessage) {
				StreamMessage src = (StreamMessage) message;
				Object lastRead = null;
				Object type = null;
				int count = -1;
				int size = 0;

				byte[] arrayByte = null;
				short[] arrayShort = null;
				int[] arrayInt = null;
				long[] arrayLong = null;
				char[] arrayChar = null;
				float[] arrayFloat = null;
				double[] arrayDouble = null;
				boolean[] arrayBoolean = null;

				try {
					// Get the size and type (first variable) of array to store
					do {
						lastRead = src.readObject();
						if (lastRead != null) {
							if (count == -1) {
								size = (int) lastRead;
							} else if (count == 0) {
								type = lastRead;
							}
							count++;
						}
					} while (lastRead != null && count < 1);

					// Get the array data for the given type
					if (type instanceof Byte) {
						arrayByte = new byte[size];
						arrayByte[0] = (byte) type;
						do {
							lastRead = src.readObject();
							if (lastRead != null) {
								arrayByte[count] = (byte) lastRead;
								count++;
							}
						} while (lastRead != null);

					} else if (type instanceof Short) {
						arrayShort = new short[size];
						arrayShort[0] = (short) type;
						do {
							lastRead = src.readObject();
							if (lastRead != null) {
								arrayShort[count] = (short) lastRead;
								count++;
							}
						} while (lastRead != null);

					} else if (type instanceof Integer) {
						arrayInt = new int[size];
						arrayInt[0] = (int) type;
						do {
							lastRead = src.readObject();
							if (lastRead != null) {
								arrayInt[count] = (int) lastRead;
								count++;
							}
						} while (lastRead != null);

					} else if (type instanceof Long) {
						arrayLong = new long[size];
						arrayLong[0] = (long) type;
						do {
							lastRead = src.readObject();
							if (lastRead != null) {
								arrayLong[count] = (int) lastRead;
								count++;
							}
						} while (lastRead != null);

					} else if (type instanceof Character) {
						arrayChar = new char[size];
						arrayChar[0] = (char) type;
						do {
							lastRead = src.readObject();
							if (lastRead != null) {
								arrayChar[count] = (char) lastRead;
								count++;
							}
						} while (lastRead != null);

					} else if (type instanceof Float) {
						arrayFloat = new float[size];
						arrayFloat[0] = (float) type;
						do {
							lastRead = src.readObject();
							if (lastRead != null) {
								arrayFloat[count] = (float) lastRead;
								count++;
							}
						} while (lastRead != null);

					} else if (type instanceof Double) {
						arrayDouble = new double[size];
						arrayDouble[0] = (double) type;
						do {
							lastRead = src.readObject();
							if (lastRead != null) {
								arrayDouble[count] = (double) lastRead;
								count++;
							}
						} while (lastRead != null);

					} else if (type instanceof Boolean) {
						arrayBoolean = new boolean[size];
						arrayBoolean[0] = (boolean) type;
						do {
							lastRead = src.readObject();
							if (lastRead != null) {
								arrayBoolean[count] = (boolean) lastRead;
								count++;
							}
						} while (lastRead != null);

					}
				} catch (java.lang.ArrayIndexOutOfBoundsException err) {
					client.displayMessageDialog("The array size was set too small.", "Error");
				} catch (MessageEOFException noMoreData) {
					// Unfortunately, there is no method to peek at the next bit
					// of stream data for a StreamMessage in Java, so the
					// exception will have to be used

					// Add the stream that isn't null to the table as an array
					if (arrayByte != null) {
						Object[] data = new Object[] { "byte[]", Arrays.toString(arrayByte), arrayByte };
						((DefaultTableModel) client.getTable().getModel()).addRow(data);
					} else if (arrayShort != null) {
						Object[] data = new Object[] { "short[]", Arrays.toString(arrayShort), arrayShort };
						((DefaultTableModel) client.getTable().getModel()).addRow(data);
					} else if (arrayInt != null) {
						Object[] data = new Object[] { "int[]", Arrays.toString(arrayInt), arrayInt };
						((DefaultTableModel) client.getTable().getModel()).addRow(data);
					} else if (arrayLong != null) {
						Object[] data = new Object[] { "long[]", Arrays.toString(arrayLong), arrayLong };
						((DefaultTableModel) client.getTable().getModel()).addRow(data);
					} else if (arrayChar != null) {
						Object[] data = new Object[] { "char[]", Arrays.toString(arrayChar), arrayChar };
						((DefaultTableModel) client.getTable().getModel()).addRow(data);
					} else if (arrayFloat != null) {
						Object[] data = new Object[] { "float[]", Arrays.toString(arrayFloat), arrayFloat };
						((DefaultTableModel) client.getTable().getModel()).addRow(data);
					} else if (arrayDouble != null) {
						Object[] data = new Object[] { "double[]", Arrays.toString(arrayDouble), arrayDouble };
						((DefaultTableModel) client.getTable().getModel()).addRow(data);
					} else if (arrayBoolean != null) {
						Object[] data = new Object[] { "boolean[]", Arrays.toString(arrayBoolean), arrayBoolean };
						((DefaultTableModel) client.getTable().getModel()).addRow(data);
					}
				}

			} else if (message instanceof TextMessage) {
				TextMessage textMessage = (TextMessage) message;
				String text = textMessage.getText();
				Object[] data = new Object[] { "String", text };
				((DefaultTableModel) client.getTable().getModel()).addRow(data);
			}
		} catch (JMSException e) {
			System.out.println(e.getStackTrace());
		}
	}

	public void removeDurableSubscriber() throws JMSException {
		messageConsumer.close();
		session.unsubscribe(subscriptionName);
	}

	public void closeConnection() throws JMSException {
		connection.close();
	}
}
