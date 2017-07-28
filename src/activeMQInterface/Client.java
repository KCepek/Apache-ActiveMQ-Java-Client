package activeMQInterface;

import java.util.Set;

import javax.jms.JMSException;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;

/**
 * The Client class is an interface for general monitoring of a server and
 * creating Producers and Consumers, which send and receive various messages.
 */
public interface Client {
	/**
	 * This method creates a connection to the server for monitoring purposes.
	 * 
	 * @param address
	 *            - the address to connect to.
	 */
	void connect(String address) throws JMSException;

	/**
	 * This method closes all connections to a server for a given client.
	 * 
	 * @throws JMSException
	 *             - if there is an error closing connections, i.e. they have
	 *             already been closed.
	 */
	void disconnect() throws JMSException;

	/**
	 * This method creates a Producer to write messages to a server for
	 * Consumers to listen to.
	 * 
	 * @param address
	 *            - the address of the server to write messages to.
	 * @param clientID
	 *            - the name of the client to identify a given Producer.
	 * @param destinationName
	 *            - the name of the topic or queue to write messages to.
	 * @param useTopics
	 *            - true if the user wants to use a Topic, false if the user
	 *            wants to use a Queue.
	 * @return a Producer object that can be ran via the provided data in order
	 *         to write messages to a server.
	 */
	Producer createProducer(String address, String clientID, String destinationName, boolean useTopics);

	/**
	 * This method creates a Consumer to listen to messages from producers from
	 * permanent (subscription-based) topics.
	 * 
	 * @param address
	 *            - the address of the server to listen on.
	 * @param clientID
	 *            - the name of the client to identify a given Consumer.
	 * @param destinationName
	 *            - the name of the topic to be used.
	 * @param subscriptionName
	 *            - the name of the client's subscription name for a given
	 *            topic.
	 * @return a Consumer object that can be ran via the provided data in order
	 *         to connect and listen to messages on a server.
	 */
	Consumer createConsumer(String address, String clientID, String destinationName, String subscriptionName);

	/**
	 * This method will disconnect the Consumer if it has been created.
	 * 
	 * @throws JMSException
	 *             - if there is an error closing the Consumer, i.e., it has
	 *             already been closed.
	 */
	public void disconnectConsumer() throws JMSException;

	/**
	 * This method creates a Consumer to listen to messages from producers from
	 * queues.
	 * 
	 * @param address
	 *            - the address of the server to listen on.
	 * @param clientID
	 *            - the name of the client to identify a given Consumer.
	 * @param destinationName
	 *            - the name of the client's subscription name for a given
	 *            queue.
	 * @return a Consumer object that can be ran via the provided data in order
	 *         to connect and listen to messages on a server.
	 */
	Consumer createConsumer(String address, String clientID, String destinationName);

	/**
	 * This method will disconnect the Consumer if it has been created.
	 * 
	 * @throws JMSException
	 *             - if there is an error closing the Producer, i.e., it has
	 *             already been closed.
	 */
	public void disconnectProducer() throws JMSException;
	
	/**
	 * Provides an array of Strings which holds all of the names of queues on the server the Client is connected to.
	 * @return
	 * @throws JMSException
	 */
	String[] listQueues() throws JMSException;

	/**
	 * Provides an array of Strings which holds all of the names of topics on the server the Client is connected to.
	 * @return
	 * @throws JMSException
	 */
	String[] listTopics() throws JMSException;

	/**
	 * This method provides a String value for all of the Command Prompt
	 * commands available and how to use them.
	 * 
	 * @return a String representation of the available commands via the Command
	 *         Prompt.
	 */
	public String getCommandInfo();
}
