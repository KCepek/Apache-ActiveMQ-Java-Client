import java.util.ArrayList;

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
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ConnectionInfo;
import org.apache.activemq.command.RemoveInfo;

/**
 * ActiveMQAdvisor implements receives information from advisory Topics to
 * gather connection data from the broker.
 */
@SuppressWarnings("serial")
public class ActiveMQAdvisor extends JFrame implements MessageListener {

	// Connection Parameters
	private ActiveMQConnectionFactory connectionFactory = null;
	private ActiveMQConnection connection = null;
	private Session session = null;
	private Destination advisoryDestination = null;
	private MessageConsumer consumerConnection = null;
	private MessageConsumer consumerAdvisor = null;
	private MessageProducer producerAdvisor = null;

	private ArrayList<Object[]> clients = new ArrayList<Object[]>();
	private TimeKeeper time = null;

	public static void main(String[] args) throws JMSException {
	}

	/**
	 * This method creates a connection to the server for monitoring purposes.
	 */
	public void connect() {
		try {
			// Initialize ConnectionFactory
			connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

			// Initialize Connection
			connection = (ActiveMQConnection) connectionFactory.createConnection();
			connection.setClientID("");

			// Initialize Session
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			advisoryDestination = session.createTopic("ActiveMQ.Advisory.Connection");
			consumerConnection = session.createConsumer(advisoryDestination);
			consumerConnection.setMessageListener(this);

			advisoryDestination = session.createTopic("ActiveMQ.Advisory.Advisor");
			consumerAdvisor = session.createConsumer(advisoryDestination);
			consumerAdvisor.setMessageListener(this);

			producerAdvisor = session.createProducer(advisoryDestination);

			// Start Connection
			connection.start();
			time = new TimeKeeper();

		} catch (Exception err) {
			String error = "Caught while connecting:\n\n" + err + "\n";
			error += err.getStackTrace();
			System.out.println(error);
		}
	}

	/**
	 * This method closes all connections to a server for a given client.
	 */
	public void disconnect() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (Exception err) {
			String error = "Caught while closing:\n\n" + err + "\n";
			error += err.getStackTrace();
			System.out.println(error);
		}
	}

	/**
	 * This listener sends a request to an advisory topic to get the time data
	 * from a client.
	 */
	public void requeset() {
		try {
			checkInsertTime(connection.getClientID(), time.getBaseTime());
			BytesMessage message = session.createBytesMessage();
			message.writeByte((byte) 1);
			producerAdvisor.send(message);
		} catch (JMSException err) {
			String error = "Caught requesting time:\n\n" + err + "\n";
			error += err.getStackTrace();
			System.out.println(error);
		}
	}

	/**
	 * A method from an implementation of MessageListener to receive and
	 * evaluate various JMS messages.
	 * 
	 * @param message
	 *            - the message that is received from the server.
	 */
	@Override
	public void onMessage(Message message) {
		if (message instanceof ActiveMQMessage) {

			ActiveMQMessage activeMQMessage = (ActiveMQMessage) message;
			if (activeMQMessage.getDataStructure() instanceof ConnectionInfo) {
				// Inserts the data into the table from the connection message
				// advisory topic
				ConnectionInfo infoConnect = (ConnectionInfo) activeMQMessage.getDataStructure();
				Object[] client = { infoConnect.getClientId(), infoConnect.getClientIp(), "", activeMQMessage };
				clients.add(client);
			} else if (activeMQMessage.getDataStructure() instanceof RemoveInfo) {
				// Remove the data from the table from the connection removed
				// message from the advisory topic
				RemoveInfo infoRemove = (RemoveInfo) activeMQMessage.getDataStructure();
				for (int i = 0; i < clients.size(); i++) {
					ActiveMQMessage infoClient = (ActiveMQMessage) clients.get(i)[3];
					ConnectionInfo infoConnect = (ConnectionInfo) infoClient.getDataStructure();

					// The connectionId of the connect message from the advisory
					// topic is the same as the objectId from the remove message
					// from the advisory topic.

					// System.out.println(infoRemove.getObjectId());
					// System.out.println(infoConnect.getConnectionId());

					if (infoRemove.getObjectId() == infoConnect.getConnectionId()) {
						clients.remove(i);
					}
				}
			} else if (activeMQMessage instanceof TextMessage) {
				try {
					TextMessage textMessage = (TextMessage) message;
					String text = textMessage.getText();

					int space = text.indexOf(' ');
					String time = text.substring(0, space);
					String clientID = text.substring(space + 1);

					// System.out.println(time);
					// System.out.println(clientID);

					checkInsertTime(clientID, time);
				} catch (JMSException err) {
					String error = "Caught while closing:\n\n" + err + "\n";
					error += err.getStackTrace();
					System.out.println(error);
				}

			}
		}
	}

	/**
	 * Inserts a time value for a given clientID if it is connected to the
	 * server (in the table).
	 * 
	 * @param clientID
	 *            - the String representation of a clientID that could be on the
	 *            server.
	 * @param time
	 *            - a String representation of the time.
	 */
	public void checkInsertTime(String clientID, String time) {
		for (int i = 0; i < clients.size(); i++) {
			String storedID = (String) clients.get(i)[0];

			if (storedID.equals(clientID)) {
				Object[] client = clients.get(i);
				client[2] = time;
				clients.set(i, client);
			}
		}
	}
}