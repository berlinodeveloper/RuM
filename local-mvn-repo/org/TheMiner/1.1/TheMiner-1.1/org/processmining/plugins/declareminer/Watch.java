package org.processmining.plugins.declareminer;

import java.util.Date;

public class Watch {
	private long time_ = 0;
	private boolean inPause_ = false;

	// Start the watch
	public synchronized void start() {
		if (inPause_) {
			time_ = new Date().getTime() - time_;
			inPause_ = false;
		} else {
			time_ = new Date().getTime();
		}
	}

	// Pause the time measuring.
	public synchronized void pause() {
		time_ = new Date().getTime() - time_;
		inPause_ = true;
	}

	// Number of miliseconds since the watch was started, excluding pauses.
	public synchronized long msecs() {
		return (inPause_ ? time_ : (new Date().getTime() - time_));
	}

	// Number of seconds since the watch was started, excluding pauses.
	public long secs() {
		return (msecs() / 1000L);
	}

	// Number of minutes since the watch was started, excluding pauses.
	public long mins() {
		return (secs() / 60L);
	}

	// Number of hours since the watch was started, excluding pauses.
	public long hours() {
		return (mins() / 60L);
	}
}
