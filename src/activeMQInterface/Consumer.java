package activeMQInterface;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * The Consumer class is an interface class for running (using a thread for) a
 * Consumer that can receive various messages from a server, including
 * TextMessage, StreamMessage, and ByteMessage message types.
 */
public interface Consumer extends MessageListener {
	/**
	 * This method runs a Consumer that connects to a server and creates a
	 * MessageConsumer for listening to messages on a given server.
	 */
	void run();

	/**
	 * A method extended from MessageListener to allow a client to receive
	 * messages and process them via this method.
	 * 
	 * @param message
	 *            - the message received from a Producer via the server.
	 */
	@Override
	void onMessage(Message message);

	/**
	 * This method unsubscribes a given Consumer from a Topic if Topics are
	 * being used.
	 * 
	 * @throws JMSException
	 *             - if the JMS provider fails to unsubscribe a Consumer due to
	 *             an internal error.
	 */
	void unsubscribe() throws JMSException;

	/**
	 * This method closes the connection of a Consumer.
	 * 
	 * @throws JMSException
	 *             - if the JMS provider fails to close the Consumer due to an
	 *             internal error.
	 */
	void disconnect() throws JMSException;
}
