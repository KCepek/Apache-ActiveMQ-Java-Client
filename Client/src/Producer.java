import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.StreamMessage;
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
	
	public void sendByteStream(int size, byte... args) throws JMSException {
		StreamMessage message = session.createStreamMessage();
		message.writeInt(size);
		for(byte arg : args) {
			message.writeByte(arg);
		}
		messageProducer.send(message);
	}
	
	public void sendShortStream(int size, short... args) throws JMSException {
		StreamMessage message = session.createStreamMessage();
		message.writeInt(size);
		for(short arg : args) {
			message.writeShort(arg);
		}
		messageProducer.send(message);
	}
	
	public void sendIntStream(int size, int... args) throws JMSException {
		StreamMessage message = session.createStreamMessage();
		message.writeInt(size);
		for(int arg : args) {
			message.writeInt(arg);
		}
		messageProducer.send(message);
	}
	
	public void sendLongStream(int size, long... args) throws JMSException {
		StreamMessage message = session.createStreamMessage();
		message.writeInt(size);
		for(long arg : args) {
			message.writeLong(arg);
		}
		messageProducer.send(message);
	}
	
	public void sendCharStream(int size, char... args) throws JMSException {
		StreamMessage message = session.createStreamMessage();
		message.writeInt(size);
		for(char arg : args) {
			message.writeChar(arg);
		}
		messageProducer.send(message);
	}
	
	public void sendFloatStream(int size, float... args) throws JMSException {
		StreamMessage message = session.createStreamMessage();
		message.writeInt(size);
		for(float arg : args) {
			message.writeFloat(arg);
		}
		messageProducer.send(message);
	}
	
	public void sendDoubleStream(int size, double... args) throws JMSException {
		StreamMessage message = session.createStreamMessage();
		message.writeInt(size);
		for(double arg : args) {
			message.writeDouble(arg);
		}
		messageProducer.send(message);
	}
	
	public void sendBooleanStream(int size, boolean... args) throws JMSException {
		StreamMessage message = session.createStreamMessage();
		message.writeInt(size);
		for(boolean arg : args) {
			message.writeBoolean(arg);
		}
		messageProducer.send(message);
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