package messagingInterface;

import javax.jms.Connection;

public interface ServerMessaging {
	Connection connect(String address, String clientID);

	void disconnect();

	Producer createProducer(String address, String clientID, boolean useTopics);

	Consumer createConsumer(String address, String clientID, boolean useTopics, String destinationName, String subscriptionName);
}
