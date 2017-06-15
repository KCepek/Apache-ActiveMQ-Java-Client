
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

import messagingInterface.Consumer;

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

	private Client client;

	public ActiveMQConsumer(String address, String clientID, String destinationName, String subscriptionName,
			Client client) {
		this.address = address;
		this.clientID = clientID;
		this.destinationName = destinationName;
		this.subscriptionName = subscriptionName;
		this.client = client;
	}

	public ActiveMQConsumer(String address, String clientID, String destinationName, Client client) {
		this.address = address;
		this.clientID = clientID;
		this.destinationName = destinationName;
		this.client = client;
	}

	public boolean isConnected() {
		return isConnected;
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
				// the client is offline
				topic = session.createTopic(destinationName);
				messageConsumer = session.createDurableSubscriber(topic, subscriptionName);
			} else {
				queue = session.createQueue(destinationName);
				messageConsumer = session.createConsumer(queue);
			}

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
			client.setExtent(client.getVerticalScrollBarExtent());
			client.setValue(client.getVerticalScrollBarValue());
			client.setMaximum(client.getVerticalScrollBarMaximum());

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

					fillClientTable(values, type, oneType);
				} catch (MessageEOFException noMoreData) {
					fillClientTable(values, type, oneType);
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
						client.insertData(new Object[] { "boolean[]", Arrays.toString(values), values });
					} else if (type == 2) {
						byte[] values = new byte[size];
						for (int i = 0; i < size; i++) {
							values[i] = bytesMessage.readByte();
						}
						client.insertData(new Object[] { "byte[]", Arrays.toString(values), values });
					} else if (type == 3) {
						char[] values = new char[size];
						for (int i = 0; i < size; i++) {
							values[i] = (char) bytesMessage.readByte();
						}
						client.insertData(new Object[] { "char[]", Arrays.toString(values), values });
					} else if (type == 4) {
						short[] values = new short[size];
						for (int i = 0; i < size; i++) {
							values[i] = bytesMessage.readShort();
						}
						client.insertData(new Object[] { "short[]", Arrays.toString(values), values });
					} else if (type == 5) {
						int[] values = new int[size];
						for (int i = 0; i < size; i++) {
							values[i] = bytesMessage.readInt();
						}
						client.insertData(new Object[] { "int[]", Arrays.toString(values), values });
					} else if (type == 6) {
						long[] values = new long[size];
						for (int i = 0; i < size; i++) {
							values[i] = bytesMessage.readLong();
						}
						client.insertData(new Object[] { "long[]", Arrays.toString(values), values });
					} else if (type == 7) {
						double[] values = new double[size];
						for (int i = 0; i < size; i++) {
							values[i] = bytesMessage.readDouble();
						}
						client.insertData(new Object[] { "double[]", Arrays.toString(values), values });
					} else if (type == 8) {
						float[] values = new float[size];
						for (int i = 0; i < size; i++) {
							values[i] = bytesMessage.readFloat();
						}
						client.insertData(new Object[] { "float[]", Arrays.toString(values), values });
					}
				} catch (MessageEOFException noMoreData) {
					String error = "Caught while receiving data from the ActiveMQConsumer:\n\n" + noMoreData + "\n";
					error += noMoreData.getStackTrace();
					client.displayMessageDialog(error, "Error");
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
				// client.insertData(new Object[] { "byte[]",
				// Arrays.toString(values), values });
				// } catch (MessageEOFException noMoreData) {
				// String error = "Caught while receiving data from the
				// ActiveMQConsumer:\n\n" + noMoreData + "\n";
				// error += noMoreData.getStackTrace();
				// client.displayMessageDialog(error, "Error");
				// }

			} else if (message instanceof TextMessage) {
				TextMessage textMessage = (TextMessage) message;
				String text = textMessage.getText();
				Object[] data = new Object[] { "String", text };
				client.insertData(data);
			}
		} catch (JMSException err) {
			String error = "Caught while receiving data from the ActiveMQConsumer:\n\n" + err + "\n";
			error += err.getStackTrace();
			client.displayMessageDialog(error, "Error");
		}
	}

	public void fillClientTable(Object[] values, Object type, boolean oneType) {
		if (oneType) {
			int sizeN = values.length;
			if (type instanceof Boolean) {
				boolean[] valuesN = new boolean[sizeN];
				for (int i = 0; i < sizeN; i++) {
					if (values[i] != null) {
						valuesN[i] = (boolean) values[i];
					}
				}
				client.insertData(new Object[] { "boolean[]", Arrays.toString(valuesN), valuesN });
			} else if (type instanceof Byte) {
				byte[] valuesN = new byte[sizeN];
				for (int i = 0; i < sizeN; i++) {
					if (values[i] != null) {
						valuesN[i] = (byte) values[i];
					}
				}
				client.insertData(new Object[] { "byte[]", Arrays.toString(valuesN), valuesN });
			} else if (type instanceof Character) {
				// char[] valuesN = ((String) values[0]).toCharArray();
				char[] valuesN = new char[sizeN];
				for (int i = 0; i < sizeN; i++) {
					if (values[i] != null) {
						valuesN[i] = (char) values[i];
					}
				}
				client.insertData(new Object[] { "char[]", Arrays.toString(valuesN), valuesN });
			} else if (type instanceof Short) {
				short[] valuesN = new short[sizeN];
				for (int i = 0; i < sizeN; i++) {
					if (values[i] != null) {
						valuesN[i] = (short) values[i];
					}
				}
				client.insertData(new Object[] { "short[]", Arrays.toString(valuesN), valuesN });
			} else if (type instanceof Integer) {
				int[] valuesN = new int[sizeN];
				for (int i = 0; i < sizeN; i++) {
					if (values[i] != null) {
						valuesN[i] = (int) values[i];
					}
				}
				client.insertData(new Object[] { "int[]", Arrays.toString(valuesN), valuesN });
			} else if (type instanceof Long) {
				long[] valuesN = new long[sizeN];
				for (int i = 0; i < sizeN; i++) {
					if (values[i] != null) {
						valuesN[i] = (long) values[i];
					}
				}
				client.insertData(new Object[] { "long[]", Arrays.toString(valuesN), valuesN });
			} else if (type instanceof Double) {
				double[] valuesN = new double[sizeN];
				for (int i = 0; i < sizeN; i++) {
					if (values[i] != null) {
						valuesN[i] = (double) values[i];
					}
				}
				client.insertData(new Object[] { "double[]", Arrays.toString(valuesN), valuesN });
			} else if (type instanceof Float) {
				float[] valuesN = new float[sizeN];
				for (int i = 0; i < sizeN; i++) {
					if (values[i] != null) {
						valuesN[i] = (float) values[i];
					}
				}
				client.insertData(new Object[] { "float[]", Arrays.toString(valuesN), valuesN });
			}
		} else {
			client.insertData(new Object[] { "Object[]", Arrays.toString(values), values });
		}
	}

	@Override
	public void unsubscribe() throws JMSException {
		messageConsumer.close();
		session.unsubscribe(subscriptionName);
	}

	@Override
	public void disconnect() throws JMSException {
		isConnected = false;
		connection.close();
	}
}