import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JTextField;
import java.beans.*; //property change stuff
import java.awt.*;
import java.awt.event.*;

/**
 * JDialogCustom extends JDialog and implements ActionListener and
 * PropertyChangeListener to create a custom JDialog that has a JTextField and
 * customizable buttons.
 */
@SuppressWarnings("serial")
public class JDialogCustom extends JDialog implements ActionListener, PropertyChangeListener {

	private String typedText = null;
	private JTextField textField;

	private JOptionPane optionPane;

	private String button1 = "Enter";
	private String button2 = "Cancel";

	public String getValidatedText() {
		return typedText;
	}

	/**
	 * Creates a JDialog with a JTextField option for inputting text.
	 * 
	 * @param frame
	 *            - the frame to add the custom JDialog to.
	 */
	public JDialogCustom(Frame frame) {
		super(frame, true);

		setTitle("Create Topic");

		textField = new JTextField(20);

		// Create an array of the text and components to be displayed.
		String message = "Please enter a new Topic name.";
		Object[] array = { message, textField };

		// Create an array specifying the number of dialog buttons
		// and their text.
		Object[] options = { button1, button2 };

		// Create the JOptionPane.
		optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_OPTION, null, options,
				options[0]);

		// Make this dialog display it.
		setContentPane(optionPane);

		// Handle window closing correctly.
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				/*
				 * Instead of directly closing the window, we're going to change
				 * the JOptionPane's value property.
				 */
				optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
			}
		});

		// Ensure the text field always gets the first focus.
		addComponentListener(new ComponentAdapter() {
			public void componentShown(ComponentEvent ce) {
				textField.requestFocusInWindow();
			}
		});

		// Register an event handler that puts the text into the option pane.
		textField.addActionListener(this);

		// Register an event handler that reacts to option pane state changes.
		optionPane.addPropertyChangeListener(this);
	}

	/**
	 * This method handles ActionEvents from the JTextField.
	 */
	public void actionPerformed(ActionEvent e) {
		optionPane.setValue(button1);
	}

	/**
	 * This method reacts to state changes. Provided that the text in the
	 * JTextField is not blank, it will be returned. If the text in the
	 * JTextField is blank, a window will pop up asking the user to try again.
	 */
	public void propertyChange(PropertyChangeEvent e) {
		String prop = e.getPropertyName();

		if (isVisible() && (e.getSource() == optionPane)
				&& (JOptionPane.VALUE_PROPERTY.equals(prop) || JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
			Object value = optionPane.getValue();

			if (value == JOptionPane.UNINITIALIZED_VALUE) {
				// ignore reset
				return;
			}

			// Reset the JOptionPane's value.
			// If you don't do this, then if the user
			// presses the same button next time, no
			// property change event will be fired.
			optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

			if (button1.equals(value)) {
				typedText = textField.getText();
				if (!typedText.isEmpty()) {
					// we're done; clear and dismiss the dialog
					clearAndHide();
				} else {
					// text was invalid
					JOptionPane.showMessageDialog(JDialogCustom.this, "The Destination should not be blank.",
							"Try Again", JOptionPane.ERROR_MESSAGE);
					typedText = null;
					textField.requestFocusInWindow();
				}
			} else { // user closed dialog or clicked cancel
				typedText = null;
				clearAndHide();
			}
		}
	}

	/**
	 * This method clears the text in the JTextField and hides it.
	 */
	public void clearAndHide() {
		textField.setText(null);
		setVisible(false);
	}
}