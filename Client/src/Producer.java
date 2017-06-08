import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
 
public class Producer implements Runnable {

	private String address = null;
	private String clientID = null;
	private String destinationName = null;
	
	private boolean useTopics = true;;

	private Connection connection = null;
	private Session session = null;
	private Destination destination = null;
	private MessageProducer messageProducer = null;

	public Producer(String address, String clientID, String destinationName) {
		this.address = address;
		this.clientID = clientID;
		
		if (destinationName.charAt(0) == 'Q') {
			useTopics = false;
		}
		this.destinationName = destinationName.substring(1);
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

			// Create the Queue or Topics to send messages to
			if (useTopics) {
				destination = session.createTopic(destinationName);
			} else {
				destination = session.createQueue(destinationName);
			}

			// Create a MessageProducer to send messages to
			messageProducer = session.createProducer(destination);

		} catch (Exception e) {
			System.out.println("Caught: " + e);
			e.printStackTrace();
		}
	}

	public String getDestinationName() {
		return destinationName;
	}

	public void closeConnection() throws JMSException {
		connection.close();
	}
	
	public void sendMixedStreamMessage(int size, Object... args) throws JMSException {
		StreamMessage message = session.createStreamMessage();
		message.writeInt(size);
		for (Object arg : args) {
			if (arg instanceof Byte) {
				message.writeByte((byte) arg);
			} else if (arg instanceof Short) {
				message.writeShort((short) arg);
			} else if (arg instanceof Integer) {
				message.writeInt((int) arg);
			} else if (arg instanceof Long) {
				message.writeLong((long) arg);
			} else if (arg instanceof String) {
				message.writeString((String) arg);
			} else if (arg instanceof Float) {
				message.writeFloat((float) arg);
			} else if (arg instanceof Double) {
				message.writeDouble((double) arg);
			} else if (arg instanceof Boolean) {
				message.writeBoolean((boolean) arg);
			}
		}
		messageProducer.send(message);
	}
	
	public void sendByteStreamMessage(int size, byte... args) throws JMSException {
		StreamMessage message = session.createStreamMessage();
		message.writeInt(size);
		for(byte arg : args) {
			message.writeByte(arg);
		}
		messageProducer.send(message);
	}
	
	public void sendShortStreamMessage(int size, short... args) throws JMSException {
		StreamMessage message = session.createStreamMessage();
		message.writeInt(size);
		for(short arg : args) {
			message.writeShort(arg);
		}
		messageProducer.send(message);
	}
	
	public void sendIntStreamMessage(int size, int... args) throws JMSException {
		StreamMessage message = session.createStreamMessage();
		message.writeInt(size);
		for(int arg : args) {
			message.writeInt(arg);
		}
		messageProducer.send(message);
	}
	
	public void sendLongStreamMessage(int size, long... args) throws JMSException {
		StreamMessage message = session.createStreamMessage();
		message.writeInt(size);
		for(long arg : args) {
			message.writeLong(arg);
		}
		messageProducer.send(message);
	}
	
	public void sendCharStreamMessage(int size, char... args) throws JMSException {
		StreamMessage message = session.createStreamMessage();
		message.writeInt(size);
		for(char arg : args) {
			//message.writeChar(arg);
			message.writeString("" + arg);
		}
		messageProducer.send(message);
	}
	
	public void sendFloatStreamMessage(int size, float... args) throws JMSException {
		StreamMessage message = session.createStreamMessage();
		message.writeInt(size);
		for(float arg : args) {
			message.writeFloat(arg);
		}
		messageProducer.send(message);
	}
	
	public void sendDoubleStreamMessage(int size, double... args) throws JMSException {
		StreamMessage message = session.createStreamMessage();
		message.writeInt(size);
		for(double arg : args) {
			message.writeDouble(arg);
		}
		messageProducer.send(message);
	}
	
	public void sendBooleanStreamMessage(int size, boolean... args) throws JMSException {
		StreamMessage message = session.createStreamMessage();
		message.writeInt(size);
		for(boolean arg : args) {
			message.writeBoolean(arg);
		}
		messageProducer.send(message);
	}
	
	public void sendBytesMessage(int size, Object... args) throws JMSException {
		BytesMessage message = session.createBytesMessage();
		message.writeInt(size);
		for (Object arg : args) {
			if (arg instanceof Byte) {
				message.writeByte((byte) arg);
			} else if (arg instanceof Short) {
				message.writeShort((short) arg);
			} else if (arg instanceof Integer) {
				message.writeInt((int) arg);
			} else if (arg instanceof Long) {
				message.writeLong((long) arg);
			} else if (arg instanceof Character) {
				message.writeChar((char) arg);
			} else if (arg instanceof Float) {
				message.writeFloat((float) arg);
			} else if (arg instanceof Double) {
				message.writeDouble((double) arg);
			} else if (arg instanceof Boolean) {
				message.writeBoolean((boolean) arg);
			}
		}
		messageProducer.send(message);
	}
	
	public void sendBytesMessage(int size, byte... args) throws JMSException {
		BytesMessage message = session.createBytesMessage();
		message.writeInt(size);
		for(byte arg : args) {
			message.writeByte(arg);
		}
		messageProducer.send(message);
	}
	
	public void sendBytesMessage(int size, short... args) throws JMSException {
		BytesMessage message = session.createBytesMessage();
		message.writeInt(size);
		for(short arg : args) {
			message.writeShort(arg);
		}
		messageProducer.send(message);
	}
	
	public void sendBytesMessage(int size, int... args) throws JMSException {
		BytesMessage message = session.createBytesMessage();
		message.writeInt(size);
		for(int arg : args) {
			message.writeInt(arg);
		}
		messageProducer.send(message);
	}
	
	public void sendBytesMessage(int size, long... args) throws JMSException {
		BytesMessage message = session.createBytesMessage();
		message.writeInt(size);
		for(long arg : args) {
			message.writeLong(arg);
		}
		messageProducer.send(message);
	}
	
	public void sendBytesMessage(int size, char... args) throws JMSException {
		BytesMessage message = session.createBytesMessage();
		message.writeInt(size);
		for(char arg : args) {
			message.writeChar(arg);
		}
		messageProducer.send(message);
	}
	
	public void sendBytesMessage(int size, float... args) throws JMSException {
		BytesMessage message = session.createBytesMessage();
		message.writeInt(size);
		for(float arg : args) {
			message.writeFloat(arg);
		}
		messageProducer.send(message);
	}
	
	public void sendBytesMessage(int size, double... args) throws JMSException {
		BytesMessage message = session.createBytesMessage();
		message.writeInt(size);
		for(double arg : args) {
			message.writeDouble(arg);
		}
		messageProducer.send(message);
	}
	
	public void sendBytesMessage(int size, boolean... args) throws JMSException {
		BytesMessage message = session.createBytesMessage();
		message.writeInt(size);
		for(boolean arg : args) {
			message.writeBoolean(arg);
		}
		messageProducer.send(message);
	}

	public void sendTextMessage(String s) throws JMSException {
		TextMessage message = session.createTextMessage(s);
		// textMessage.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
		messageProducer.send(message);
	}
}