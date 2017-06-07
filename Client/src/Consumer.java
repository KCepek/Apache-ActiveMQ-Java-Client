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
				StreamMessage streamMessage = (StreamMessage) message;
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
					
					for (int i = 0; i < size; i++) {
						values[i] = streamMessage.readObject();
						if (values[i] == null && !hasType) {
							check++;
						}
						if (i == check) {
							type = values[0];
							hasType = true;
						} else {
							if ( values[i] != null && !type.getClass().equals(values[i].getClass()) ) {
								oneType = false;
							}
						}
					}
					
					fillClientTable(values, type, oneType);
				} catch (MessageEOFException noMoreData) {
					fillClientTable(values, type, oneType);
				}
			} else if (message instanceof TextMessage) {
				TextMessage textMessage = (TextMessage) message;
				String text = textMessage.getText();
				Object[] data = new Object[] { "String", text };
				client.insertData(data);
			}
		} catch (JMSException err) {
			String error = "Caught while receiving data from the Consumer:\n\n" + err + "\n";
			error += err.getStackTrace();
			client.displayMessageDialog(error, "Error");
		}
	}

	public void removeDurableSubscriber() throws JMSException {
		messageConsumer.close();
		session.unsubscribe(subscriptionName);
	}

	public void closeConnection() throws JMSException {
		connection.close();
	}
	
	public void fillClientTable(Object[] values, Object type, boolean oneType) {
		if (oneType) {
			int sizeN = values.length;
			if (type instanceof Byte) {
				byte[] valuesN = new byte[sizeN];
				for (int i = 0; i < sizeN; i++) {
					if (values[i] != null) {
						valuesN[i] = (byte) values[i];
					}
				}
				client.insertData(new Object[] { "byte[]", Arrays.toString(valuesN), valuesN });
			} else if (type instanceof Short) {
				short[] valuesN = new short[values.length];
				for (int i = 0; i < sizeN; i++) {
					if (values[i] != null) {
						valuesN[i] = (short) values[i];
					}
				}
				client.insertData(new Object[] { "short[]", Arrays.toString(valuesN), valuesN });
			} else if (type instanceof Integer) {
				int[] valuesN = new int[values.length];
				for (int i = 0; i < sizeN; i++) {
					if (values[i] != null) {
						valuesN[i] = (int) values[i];
					}
				}
				client.insertData(new Object[] { "int[]", Arrays.toString(valuesN), valuesN });
			} else if (type instanceof Long) {
				long[] valuesN = new long[values.length];
				for (int i = 0; i < sizeN; i++) {
					if (values[i] != null) {
						valuesN[i] = (long) values[i];
					}
				}
				client.insertData(new Object[] { "long[]", Arrays.toString(valuesN), valuesN });
			} else if (type instanceof Character) {
				char[] valuesN = new char[values.length];
				for (int i = 0; i < sizeN; i++) {
					if (values[i] != null) {
						valuesN[i] = (char) values[i];
					}
				}
				client.insertData(new Object[] { "char[]", Arrays.toString(valuesN), valuesN });
			} else if (type instanceof Float) {
				float[] valuesN = new float[values.length];
				for (int i = 0; i < sizeN; i++) {
					if (values[i] != null) {
						valuesN[i] = (float) values[i];
					}
				}
				client.insertData(new Object[] { "float[]", Arrays.toString(valuesN), valuesN });
			} else if (type instanceof Double) {
				double[] valuesN = new double[values.length];
				for (int i = 0; i < sizeN; i++) {
					if (values[i] != null) {
						valuesN[i] = (double) values[i];
					}
				}
				client.insertData(new Object[] { "double[]", Arrays.toString(valuesN), valuesN });
			} else if (type instanceof Boolean) {
				boolean[] valuesN = new boolean[values.length];
				for (int i = 0; i < sizeN; i++) {
					if (values[i] != null) {
						valuesN[i] = (boolean) values[i];
					}
				}
				client.insertData(new Object[] { "boolean[]", Arrays.toString(valuesN), valuesN });
			}
		} else {
			client.insertData(new Object[] { "Object[]", Arrays.toString(values), values });
		}
	}
}
