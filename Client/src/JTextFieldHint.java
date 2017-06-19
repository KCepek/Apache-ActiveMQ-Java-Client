import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

/**
 * JTextFieldHint extends JTextField and implements FocusListener in order to
 * display grey default text that is not intractable and not modifiable when
 * clicking in a JTextField.
 */
@SuppressWarnings("serial")
public class JTextFieldHint extends JTextField implements FocusListener {

	private final String hint;
	private boolean showingHint;

	/**
	 * This is constructor for a JTextFieldHint.
	 * 
	 * @param hint
	 *            - the text to show as grey text.
	 * @param size
	 *            - the amount of columns, which determines the width.
	 */
	public JTextFieldHint(final String hint, int size) {
		super(hint, size);
		super.setForeground(Color.GRAY);
		this.hint = hint;
		this.showingHint = true;
		super.addFocusListener(this);
	}

	/**
	 * When a JTextFieldHint object gains focused (i.e., is clicked on), hide
	 * the text by changing it to an empty String.
	 */
	@Override
	public void focusGained(FocusEvent e) {
		if (this.getText().isEmpty()) {
			super.setText("");
			super.setForeground(Color.BLACK);
			showingHint = false;
		}
	}

	/**
	 * When a JTextFieldHint object loses focus (i.e., something else is clicked
	 * on), show the default text.
	 */
	@Override
	public void focusLost(FocusEvent e) {
		if (this.getText().isEmpty()) {
			super.setText(hint);
			super.setForeground(Color.GRAY);
			showingHint = true;
		}
	}

	/**
	 * This method gets the text from a JTextFieldHint. If the default text is
	 * still in the box, provide a blank String, else, provide the entered
	 * String.
	 */
	@Override
	public String getText() {
		return showingHint ? "" : super.getText();
	}
}
