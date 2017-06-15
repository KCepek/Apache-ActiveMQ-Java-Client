package messagingInterface;

public interface ServerMessaging {
	void connect();

	void disconnect();

	Producer createProducer(String address, String clientID, String destinationName, boolean useTopics);

	Consumer createConsumer(String address, String clientID, String destinationName, String subscriptionName);
	
	Consumer createConsumer(String address, String clientID, String destinationName);
}
