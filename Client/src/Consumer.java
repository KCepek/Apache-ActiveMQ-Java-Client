import java.util.Arrays;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
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
		Object object;
		try {
			if (message instanceof ObjectMessage) {
				ObjectMessage objectMessage = (ObjectMessage) message;
				object = objectMessage.getObject();
				if (object instanceof byte[]) {
					byte[] array = (byte[]) object;
					Object[] data = new Object[] { "byte[]", array };
					((DefaultTableModel) client.getTable().getModel()).addRow(data);
				}
				if (object instanceof short[]) {
					short[] array = (short[]) object;
					Object[] data = new Object[] { "short[]", array };
					((DefaultTableModel) client.getTable().getModel()).addRow(data);
				}
				if (object instanceof int[]) {
					int[] array = (int[]) object;
					Object[] data = new Object[] { "int[]", Arrays.toString(array), array };
					((DefaultTableModel) client.getTable().getModel()).addRow(data);
				}
				if (object instanceof long[]) {
					long[] array = (long[]) object;
					Object[] data = new Object[] { "long[]", array };
					((DefaultTableModel) client.getTable().getModel()).addRow(data);
				}
				if (object instanceof char[]) {
					char[] array = (char[]) object;
					Object[] data = new Object[] { "char[]", array };
					((DefaultTableModel) client.getTable().getModel()).addRow(data);
				}
				if (object instanceof float[]) {
					float[] array = (float[]) object;
					Object[] data = new Object[] { "float[]", array };
					((DefaultTableModel) client.getTable().getModel()).addRow(data);
				}
				if (object instanceof double[]) {
					double[] array = (double[]) object;
					Object[] data = new Object[] { "double[]", array };
					((DefaultTableModel) client.getTable().getModel()).addRow(data);
				}
				if (object instanceof boolean[]) {
					boolean[] array = (boolean[]) object;
					Object[] data = new Object[] { "boolean[]", array };
					((DefaultTableModel) client.getTable().getModel()).addRow(data);
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
