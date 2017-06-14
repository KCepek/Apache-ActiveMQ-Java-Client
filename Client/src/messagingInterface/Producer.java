package messagingInterface;

import javax.jms.JMSException;

public interface Producer {
	void run();

	void disconnect() throws JMSException;

	void sendTextMessage(String s) throws JMSException;

	void sendMixedStreamMessage(int size, Object... args) throws JMSException;

	void sendBooleanStreamMessage(int size, boolean... args) throws JMSException;

	void sendByteStreamMessage(int size, byte... args) throws JMSException;

	void sendCharStreamMessage(int size, char... args) throws JMSException;

	void sendShortStreamMessage(int size, short... args) throws JMSException;

	void sendIntStreamMessage(int size, int... args) throws JMSException;

	void sendLongStreamMessage(int size, long... args) throws JMSException;

	void sendDoubleStreamMessage(int size, double... args) throws JMSException;

	void sendFloatStreamMessage(int size, float... args) throws JMSException;
}
