import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
 
public class Producer implements Runnable {

	private String address;
	private String clientID;
	private String topicName;

	private Connection connection;
	private Session session;
	private MessageProducer messageProducer;

	public Producer(String address, String clientID, String topicName) {
		this.address = address;
		this.clientID = clientID;
		this.topicName = topicName;
	}

	@Override
	public void run() {
		try {
			// Create a ConnectionFactory
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(address);

			// Create a Connection
			connection = connectionFactory.createConnection();
			connection.setClientID(clientID);

			// Create a Session
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Create the Topic to send messages to
			Topic topic = session.createTopic(topicName);

			// Create a MessageProducer to send messages to
			messageProducer = session.createProducer(topic);

		} catch (Exception e) {
			System.out.println("Caught: " + e);
			e.printStackTrace();
		}
	}

	public String getTopicName() {
		return topicName;
	}

	public void closeConnection() throws JMSException {
		connection.close();
	}

	public void sendByteArray(byte[] array) throws JMSException {
		ObjectMessage objectMessage = session.createObjectMessage(array);
		objectMessage.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
		messageProducer.send(objectMessage);
	}

	public void sendShortArray(short[] array) throws JMSException {
		ObjectMessage objectMessage = session.createObjectMessage(array);
		objectMessage.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
		messageProducer.send(objectMessage);
	}

	public void sendIntArray(int[] array) throws JMSException {
		ObjectMessage objectMessage = session.createObjectMessage(array);
		objectMessage.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
		messageProducer.send(objectMessage);
	}

	public void sendLongArray(long[] array) throws JMSException {
		ObjectMessage objectMessage = session.createObjectMessage(array);
		objectMessage.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
		messageProducer.send(objectMessage);
	}

	public void sendCharArray(char[] array) throws JMSException {
		ObjectMessage objectMessage = session.createObjectMessage(array);
		objectMessage.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
		messageProducer.send(objectMessage);
	}

	public void sendFloatArray(float[] array) throws JMSException {
		ObjectMessage objectMessage = session.createObjectMessage(array);
		objectMessage.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
		messageProducer.send(objectMessage);
	}

	public void sendDoubleArray(double[] array) throws JMSException {
		ObjectMessage objectMessage = session.createObjectMessage(array);
		objectMessage.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
		messageProducer.send(objectMessage);
	}

	public void sendBooleanArray(boolean[] array) throws JMSException {
		ObjectMessage objectMessage = session.createObjectMessage(array);
		objectMessage.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
		messageProducer.send(objectMessage);
	}

	public void sendTextMessage(String s) throws JMSException {
		TextMessage textMessage = session.createTextMessage(s);
		// textMessage.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
		messageProducer.send(textMessage);
	}

	public static void main(String[] args) throws Exception {
		// Thread t = new Thread(new Event(info[i]));
		// t.setDaemon(false);
		// t.start();
	}
}