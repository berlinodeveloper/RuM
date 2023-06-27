package org.processmining.plugins.declareminer.util;

public class Approssimator {

	public static double approssimate(double d, int p) {
		return Math.rint(d * Math.pow(10, p)) / Math.pow(10, p);
	}

}
