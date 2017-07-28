/**
 * TimeKeeper is a stopwatch-like class that keeps track of the time passed when
 * a TimeKeeper object is instantiated. It uses System.nanoTime(), which
 * provides the amount of time passed in nanoseconds represented by a long type.
 * Since a long can hold 2^63 - 1 different values, that means the timer will be
 * limited by this range. Therefore, the timer will only be accurate up to 292
 * years.
 */
public class TimeKeeper {
	private long startTime = 0;

	/**
	 * This constructor instantiates a TimeKeeper object with the current
	 * system's nanosecond time.
	 */
	public TimeKeeper() {
		startTime = System.nanoTime();
	}

	/**
	 * This method returns the amount of nanoseconds elapsed from when the
	 * object was first instantiated.
	 * 
	 * @return a long value representing the amount of elapsed nanoseconds.
	 */
	public long getNanoTime() {
		long endTime = System.nanoTime();
		return endTime - startTime;
	}

	/**
	 * This method returns a time in the format of X-XX:XX:XX.XXXX
	 * (days-hours:minutes:seconds.miliseconds).
	 * 
	 * @return a String value of the formatted time passed since the object was
	 *         first instantiated.
	 */
	public String getBaseTime() {
		long endTime = System.nanoTime();

		long days = (endTime - startTime) / 86400000000000l;
		endTime -= days * 86400000000000l;
		long hours = (endTime - startTime) / 3600000000000l;
		endTime -= hours * 3600000000000l;
		long minutes = (endTime - startTime) / 60000000000l;
		endTime -= minutes * 60000000000l;
		long baseSeconds = (endTime - startTime) / 1000000000;
		long miliSeconds = (endTime - startTime) % 1000000000;

		return days + "-" + String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":"
				+ String.format("%02d", baseSeconds) + "." + String.format("%4.4s", "" + miliSeconds);
	}
}
