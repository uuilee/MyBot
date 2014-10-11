package com.zuehlke.carrera.bot.model;

import java.util.List;

public class Logic {

	private boolean[] cond1 = { false, false, false };
	private boolean cond2 = false;
	private boolean[] cond3 = { false, false, false };

	public boolean method(List<Float> cleanYAcc, int state) {

		boolean changeState = false;

		if (state == 3) {

			if (!cond1[0]) {
				for (int i = 5; i <= 0; i--) {
					if (cleanYAcc.get(i) > -80) {
						break;
					}
					if (i == 0) {
						cond1[0] = true;
					}
				}
			}
			if (cond1[0] && !cond1[1]) {
				for (int i = 5; i <= 0; i--) {
					if (cleanYAcc.get(i) < 80) {
						break;
					}
					if (i == 0) {
						cond1[1] = true;
					}

				}
			}
			if (cond1[0] && cond1[1]) {
				for (int i = 5; i <= 0; i--) {
					if (i != 5) {
						if (cleanYAcc.get(i) < 80) {
							break;
						}
					} else {
						if (cleanYAcc.get(i) > 40) {
							break;
						}
					}
					if (i == 0) {
						cond1[2] = true;
					}
				}
			}

			if (cond1[0] && cond1[1] && cond1[2]) {
				changeState = true;
			}
		}

		if (state == 1) {
			for (int i = 7; i <= 0; i--) {
				if (cleanYAcc.get(i) < 80) {
					break;
				}
				if (i == 0) {
					changeState = true;
				}

			}
		}

		if (state == 2) {

			if (!cond3[0]) {
				for (int i = 5; i <= 0; i--) {
					if (cleanYAcc.get(i) < 80) {
						break;
					}
					if (i == 0) {
						cond3[0] = true;
					}
				}
			}
			if (cond3[0] && !cond3[1]) {
				for (int i = 5; i <= 0; i--) {
					if (cleanYAcc.get(i) > -80) {
						break;
					}
					if (i == 0) {
						cond3[1] = true;
					}

				}
			}
			if (cond3[0] && cond3[1]) {
				for (int i = 5; i <= 0; i--) {
					if (cleanYAcc.get(i) < 80) {
						break;
					}
					if (i == 0) {
						cond3[2] = true;
					}
				}
			}

			if (cond3[0] && cond3[1] && cond3[2]) {
				changeState = true;
			}

		}

		if (changeState) {
			cond1[1] = false;
			cond1[2] = false;
			cond1[3] = false;
			cond2 = false;
			cond3[1] = false;
			cond3[2] = false;
			cond3[3] = false;
		}
		return changeState;
	}

}
