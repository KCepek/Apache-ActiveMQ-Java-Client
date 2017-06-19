package activeMQInterface;

import javax.jms.JMSException;

/**
 * The Producer class is an interface class for running (using a thread for) a
 * Producer that can send various messages to a server, including TextMessage,
 * StreamMessage, and ByteMessage message types.
 */
public interface Producer {
	/**
	 * this method runs a Producer that connects to a server and creates a
	 * MessageProducer for writing to a given server.
	 */
	void run();

	/**
	 * This method closes the connection of a Producer.
	 * 
	 * @throws JMSException
	 *             - if the JMS provider fails to close the Producer due to an
	 *             internal error.
	 */
	void disconnect() throws JMSException;

	/**
	 * This method sends a TextMessage to the server via a String object.
	 * 
	 * @param s
	 *            - the String object to send over the server as a TextMessage.
	 * @throws JMSException
	 *             - if the JMS provider fails to send a message due to an
	 *             internal error.
	 */
	void sendTextMessage(String s) throws JMSException;

	/**
	 * This method sends a StreamMessage with various primitive types.
	 * 
	 * @param size
	 *            - an integer showing how many primitives will be sent.
	 * @param args
	 *            - a variable amount of primitive values to send.
	 * @throws JMSException
	 *             - if the JMS provider fails to send a message due to an
	 *             internal error.
	 */
	void sendMixedStreamMessage(int size, Object... args) throws JMSException;

	/**
	 * This method sends a StreamMessage composed of boolean primitives.
	 * 
	 * @param size
	 *            - an integer showing how many booleans will be sent.
	 * @param args
	 *            - a variable amount of boolean to send.
	 * @throws JMSException
	 *             - if the JMS provider fails to send a message due to an
	 *             internal error.
	 */
	void sendBooleanStreamMessage(int size, boolean... args) throws JMSException;

	/**
	 * This method sends a StreamMessage composed of byte primitives.
	 * 
	 * @param size
	 *            - an integer showing how many bytes will be sent.
	 * @param args
	 *            - a variable amount of bytes to send.
	 * @throws JMSException
	 *             - if the JMS provider fails to send a message due to an
	 *             internal error.
	 */
	void sendByteStreamMessage(int size, byte... args) throws JMSException;

	/**
	 * This method sends a StreamMessage composed of char primitives.
	 * 
	 * @param size
	 *            - an integer showing how many chars will be sent.
	 * @param args
	 *            - a variable amount of chars to send.
	 * @throws JMSException
	 *             - if the JMS provider fails to send a message due to an
	 *             internal error.
	 */
	void sendCharStreamMessage(int size, char... args) throws JMSException;

	/**
	 * This method sends a StreamMessage composed of short primitives.
	 * 
	 * @param size
	 *            - an integer showing how many shorts will be sent.
	 * @param args
	 *            - a variable amount of shorts to send.
	 * @throws JMSException
	 *             - if the JMS provider fails to send a message due to an
	 *             internal error.
	 */
	void sendShortStreamMessage(int size, short... args) throws JMSException;

	/**
	 * This method sends a StreamMessage composed of int primitives.
	 * 
	 * @param size
	 *            - an integer showing how many ints will be sent.
	 * @param args
	 *            - a variable amount of ints to send.
	 * @throws JMSException
	 *             - if the JMS provider fails to send a message due to an
	 *             internal error.
	 */
	void sendIntStreamMessage(int size, int... args) throws JMSException;

	/**
	 * This method sends a StreamMessage composed of long primitives.
	 * 
	 * @param size
	 *            - an integer showing how many longs will be sent.
	 * @param args
	 *            - a variable amount of longs to send.
	 * @throws JMSException
	 *             - if the JMS provider fails to send a message due to an
	 *             internal error.
	 */
	void sendLongStreamMessage(int size, long... args) throws JMSException;

	/**
	 * This method sends a StreamMessage composed of double primitives.
	 * 
	 * @param size
	 *            - an integer showing how many doubles will be sent.
	 * @param args
	 *            - a variable amount of doubles to send.
	 * @throws JMSException
	 *             - if the JMS provider fails to send a message due to an
	 *             internal error.
	 */
	void sendDoubleStreamMessage(int size, double... args) throws JMSException;

	/**
	 * This method sends a StreamMessage composed of float primitives.
	 * 
	 * @param size
	 *            - an integer showing how many floats will be sent.
	 * @param args
	 *            - a variable amount of floats to send.
	 * @throws JMSException
	 *             - if the JMS provider fails to send a message due to an
	 *             internal error.
	 */
	void sendFloatStreamMessage(int size, float... args) throws JMSException;
}
