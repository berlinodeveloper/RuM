package org.processmining.plugins.dataaware;

/*
 * Calcola il fattoriale di un numero
 */

class Factorial {
	static int fatt(int x) {
		int i;
		int f = 1;

		for (i = 1; i <= x; i = i + 1) {
			f = f * i;
		}

		return f;
	}
}
