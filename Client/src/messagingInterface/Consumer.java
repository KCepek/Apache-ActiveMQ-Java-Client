package messagingInterface;

import javax.jms.JMSException;
import javax.jms.Message;

public interface Consumer {
	void run();

	void onMessage(Message message);

	void unsubscribe() throws JMSException;

	void disconnect() throws JMSException;
}
