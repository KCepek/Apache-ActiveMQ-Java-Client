package activeMQInterface;

/**
 * The Client class is an interface for general monitoring of a server and
 * creating Producers and Consumers, which send and receive various messages.
 */
public interface Client {
	/**
	 * This method creates a connection to the server for monitoring purposes.
	 */
	void connect();

	/**
	 * This method closes all connections to a server for a given client.
	 */
	void disconnect();

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
	 * @return a Producer object that can be ran via the provided data
	 *         in order to write messages to a server.
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
	 * @return a Consumer object that can be ran via the provided data
	 *         in order to connect and listen to messages on a server.
	 */
	Consumer createConsumer(String address, String clientID, String destinationName, String subscriptionName);

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
	 * @return a Consumer object that can be ran via the provided data
	 *         in order to connect and listen to messages on a server.
	 */
	Consumer createConsumer(String address, String clientID, String destinationName);
}
