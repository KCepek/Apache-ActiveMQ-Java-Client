import org.apache.activemq.ActiveMQConnectionFactory;

import activeMQInterface.Producer;

import java.io.UnsupportedEncodingException;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

/**
 * ActiveMQProducer implements Runnable to allow process-based threading. This
 * class also implements the Producer interface to create a JMS Producer for
 * Apache ActiveMQ.
 */
public class ActiveMQProducer implements Runnable, Producer {

	private String address = null;
	private String clientID = null;
	private String destinationName = null;

	private boolean useTopics = false;
	private boolean isConnected = false;

	private Connection connection = null;
	private Session session = null;
	private Destination destination = null;
	private MessageProducer messageProducer = null;

	private TimeKeeper time = null;

	public ActiveMQProducer(String address, String clientID, String destinationName, boolean useTopics) {
		this.address = address;
		this.clientID = clientID;
		this.destinationName = destinationName;
		this.useTopics = useTopics;
	}

	/**
	 * This method returns the ClientID of the ActiveMQProducer as a way to
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
	 * This method returns a boolean to tell whether the Producer is connected
	 * to the server or not.
	 * 
	 * @return a boolean value for whether the Producer is connected.
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
			connection = connectionFactory.createConnection();
			connection.setClientID(clientID);
			isConnected = true;

			// Create a Session
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Create the Topic or Queue to send messages to
			if (useTopics) {
				destination = session.createTopic(destinationName);
			} else {
				destination = session.createQueue(destinationName);
			}

			// Create a MessageProducer to send messages to
			messageProducer = session.createProducer(destination);

			// Start the connection
			connection.start();
			time = new TimeKeeper();

		} catch (Exception e) {
			System.out.println("Caught: " + e);
			e.printStackTrace();
		}
	}

	@Override
	public void sendBooleanStreamMessage(int size, boolean... args) throws JMSException {
		StreamMessage message = session.createStreamMessage();
		// message.setStringProperty("Group", "TestGroup2");
		message.writeInt(size);
		for (boolean arg : args) {
			message.writeBoolean(arg);
		}
		messageProducer.send(message);
	}

	@Override
	public void sendByteStreamMessage(int size, byte... args) throws JMSException {
		StreamMessage message = session.createStreamMessage();
		message.writeInt(size);
		for (byte arg : args) {
			message.writeByte(arg);
		}
		messageProducer.send(message);
	}

	@Override
	public void sendCharStreamMessage(int size, char... args) throws JMSException {
		StreamMessage message = session.createStreamMessage();
		message.writeInt(size);
		for (char arg : args) {
			// message.writeChar(arg);
			message.writeString("" + arg);
		}
		messageProducer.send(message);
	}

	@Override
	public void sendShortStreamMessage(int size, short... args) throws JMSException {
		StreamMessage message = session.createStreamMessage();
		message.writeInt(size);
		for (short arg : args) {
			message.writeShort(arg);
		}
		messageProducer.send(message);
	}

	@Override
	public void sendIntStreamMessage(int size, int... args) throws JMSException {
		StreamMessage message = session.createStreamMessage();
		message.writeInt(size);
		for (int arg : args) {
			message.writeInt(arg);
		}
		messageProducer.send(message);
	}

	@Override
	public void sendLongStreamMessage(int size, long... args) throws JMSException {
		StreamMessage message = session.createStreamMessage();
		message.writeInt(size);
		for (long arg : args) {
			message.writeLong(arg);
		}
		messageProducer.send(message);
	}

	@Override
	public void sendDoubleStreamMessage(int size, double... args) throws JMSException {
		StreamMessage message = session.createStreamMessage();
		message.writeInt(size);
		for (double arg : args) {
			message.writeDouble(arg);
		}
		messageProducer.send(message);
	}

	@Override
	public void sendFloatStreamMessage(int size, float... args) throws JMSException {
		StreamMessage message = session.createStreamMessage();
		message.writeInt(size);
		for (float arg : args) {
			message.writeFloat(arg);
		}
		messageProducer.send(message);
	}

	@Override
	public void sendMixedStreamMessage(int size, Object... args) throws JMSException {
		StreamMessage message = session.createStreamMessage();
		message.writeInt(size);
		for (Object arg : args) {

			if (arg instanceof Boolean) {
				message.writeBoolean((boolean) arg);
			} else if (arg instanceof Byte) {
				message.writeByte((byte) arg);
			} else if (arg instanceof String) {
				message.writeString((String) arg);
			} else if (arg instanceof Short) {
				message.writeShort((short) arg);
			} else if (arg instanceof Integer) {
				message.writeInt((int) arg);
			} else if (arg instanceof Long) {
				message.writeLong((long) arg);
			} else if (arg instanceof Double) {
				message.writeDouble((double) arg);
			} else if (arg instanceof Float) {
				message.writeFloat((float) arg);
			}
		}
		messageProducer.send(message);
	}

	public void sendBooleanBytesMessage(int size, boolean... args) throws JMSException {
		BytesMessage message = session.createBytesMessage();
		message.writeByte((byte) 1);
		message.writeInt(size);

		for (boolean arg : args) {
			message.writeBoolean(arg);
		}
		messageProducer.send(message);
	}

	public void sendByteBytesMessage(int size, byte... args) throws JMSException {
		BytesMessage message = session.createBytesMessage();
		message.writeByte((byte) 2);
		message.writeInt(size);

		for (byte arg : args) {
			message.writeByte(arg);
		}
		messageProducer.send(message);
	}

	public void sendCharBytesMessage(int size, char... args) throws JMSException, UnsupportedEncodingException {
		BytesMessage message = session.createBytesMessage();
		message.writeByte((byte) 3);
		message.writeInt(size);

		String s = "";
		for (char arg : args) {
			s += arg;
		}
		byte[] byteArray = s.getBytes("UTF-8");

		for (byte b : byteArray) {
			message.writeByte(b);
		}
		messageProducer.send(message);
	}

	public void sendShortBytesMessage(int size, short... args) throws JMSException {
		BytesMessage message = session.createBytesMessage();
		message.writeByte((byte) 4);
		message.writeInt(size);

		for (short arg : args) {
			message.writeShort(arg);
		}
		messageProducer.send(message);
	}

	public void sendIntBytesMessage(int size, int... args) throws JMSException {
		BytesMessage message = session.createBytesMessage();
		message.writeByte((byte) 5);
		message.writeInt(size);

		for (int arg : args) {
			message.writeInt(arg);
		}
		messageProducer.send(message);
	}

	public void sendLongBytesMessage(int size, long... args) throws JMSException {
		BytesMessage message = session.createBytesMessage();
		message.writeByte((byte) 6);
		message.writeInt(size);

		for (long arg : args) {
			message.writeLong(arg);
		}
		messageProducer.send(message);
	}

	public void sendDoubleBytesMessage(int size, double... args) throws JMSException {
		BytesMessage message = session.createBytesMessage();
		message.writeByte((byte) 7);
		message.writeInt(size);

		for (double arg : args) {
			message.writeDouble(arg);
		}
		messageProducer.send(message);
	}

	public void sendFloatBytesMessage(int size, float... args) throws JMSException {
		BytesMessage message = session.createBytesMessage();
		message.writeByte((byte) 8);
		message.writeInt(size);

		for (float arg : args) {
			message.writeFloat(arg);
		}
		messageProducer.send(message);
	}

	public void sendTextMessage(String s) throws JMSException {
		TextMessage message = session.createTextMessage(s);
		// textMessage.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
		messageProducer.send(message);
	}

	@Override
	public void disconnect() throws JMSException {
		isConnected = false;
		connection.close();
	}
}