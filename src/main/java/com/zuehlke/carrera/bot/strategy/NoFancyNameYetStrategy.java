package com.zuehlke.carrera.bot.strategy;

import java.sql.SQLException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuehlke.carrera.bot.model.DatabaseService;
import com.zuehlke.carrera.bot.model.SensorEvent;
import com.zuehlke.carrera.bot.model.SensorEventType;

public class NoFancyNameYetStrategy implements BotStrategy {

	class Vector {
		float x, y, z;

		public Vector() {
			x = y = z = 0;
		}

		public Vector(float[] vec) {
			x = vec[0];
			y = vec[1];
			z = vec[2];
		}

		public void add(Vector other) {
			x += other.x;
			y += other.y;
			z += other.z;
		}

		public float scalarProduct(Vector other) {
			float scalar = x * other.x + y * other.y + z * other.z;
			return scalar;
		}

		public float twoNorm(Vector other) {
			float scalar = scalarProduct(other);
			return (float) Math.sqrt(scalar);
		}
	}

	class Lap {
		ArrayList<Vector> gyroVectors;
		ArrayList<Vector> accVectors;
		int sampleCount = 0;

		public Lap() {
			gyroVectors = new ArrayList<Vector>();
			accVectors = new ArrayList<Vector>();
		}

		public void addVectors(Vector gyroVector, Vector accVector) {
			gyroVectors.add(gyroVector);
			accVectors.add(accVector);
			sampleCount++;
		}

		public void setSampleCount(int sampleCount) {
			this.sampleCount = sampleCount;
		}
	}

	class EventLap extends Lap {

		public EventLap() {
			// interpreted map
			// map to
		}
	}

	class ForceVectorMap {
		Lap[] laps;
		Lap averageLap;
		EventLap eventLap; // each sample contains gyro acc and turning event.
							// could be used for prediction and decision
		int numberOfLaps = 0;
		int currentPosition = 0;
		int averageSampleCount = 0;

		public ForceVectorMap(int numberOfLaps) {
			this.numberOfLaps = numberOfLaps;
			for (int i = 0; i < numberOfLaps; i++) {
				laps[i] = new Lap();
			}
		}

		private void averageLaps() {
			// maybe invalidate the last vectors because of straight and fix the
			// samplerate to the least of all lap sample rates?? ==> wouldnt
			// affect map that much
			Vector gyro_vec = new Vector();
			Vector acc_vec = new Vector();
			for (Lap lap : laps) {
				averageSampleCount += lap.sampleCount;
			}
			averageSampleCount /= numberOfLaps; // TODO could get some float
												// shit
			averageLap = new Lap();
			averageLap.setSampleCount(averageSampleCount);

			// invalidating vectors to get the same vector count for all laps
			// would make calculating indexes easier
			for (int i = 0; i < averageSampleCount; i++) {
				gyro_vec.x = gyro_vec.y = gyro_vec.z = 0.0f;
				acc_vec.x = acc_vec.y = acc_vec.z = 0.0f;
				for (Lap lap : laps) {
					gyro_vec.add(lap.gyroVectors.get((int) Math
							.floor(lap.sampleCount / averageSampleCount))); // TODO
																			// could
																			// get
																			// some
																			// float
																			// shit
					acc_vec.add(lap.accVectors.get((int) Math
							.floor(lap.sampleCount / averageSampleCount)));
				}
			}

		}
	}

	class DistanceVectorMap {
		Vector[] distanceVectors;
		private final int index;

		public DistanceVectorMap(int vectorCount, int correspondingIndex) {
			distanceVectors = new Vector[vectorCount];
			index = correspondingIndex;
		}

		public int getIndex() {
			return index;
		}
	}

	enum TurningDirection {
		LEFT_TURN, RIGHT_TURN, STRAIGHT;
	}

	enum BotState {
		LEARNING_LAP_120, IMPROVING, SELF_DESTRUCTION;
	}

	private final int maxLearningLaps = 4;

	private static final Logger logger = LoggerFactory
			.getLogger(NoFancyNameYetStrategy.class);
	private DatabaseService maria;
	private int currentPower = 0;
	private int totalLapCount = 0;
	private int learningLapCount = 0;
	private ForceVectorMap vectorMap;
	private BotState currentState = BotState.LEARNING_LAP_120;
	private Lap currentLap = new Lap();

	private TurningDirection lastTurningEvent; // TODO still null;

	public NoFancyNameYetStrategy() {
		vectorMap = new ForceVectorMap(4);
		/*
		 * maria = new DatabaseService("", "", "", ""); try { if
		 * (maria.connectToMariaDB()) { // connection successful, save the data
		 * } } catch (SQLException e) {
		 * logger.info("Database connection failed: " + e.getMessage()); }
		 */
	}

	@Override
	public double processSensorEvent(SensorEvent data) {
		if (data.getType() == SensorEventType.ROUND_PASSED) {
			nextStateLogic();
			totalLapCount++;
			learningLapCount++;
		} else {
			Vector acc_vec = new Vector(data.getAcc());
			Vector gyro_vec = new Vector(data.getGyr());
			if (currentState == BotState.LEARNING_LAP_120) {
				currentLap.addVectors(gyro_vec, acc_vec);
			} else if (currentState == BotState.IMPROVING) {
				findPositionInModel(gyro_vec, acc_vec);
				decideForPowerSetting();
			}
		}
		return currentPower;
	}

	// Let's minimize Euclidean distance to the map vectors to find ourselves in
	// the map
	private void findPositionInModel(Vector current_gyro, Vector current_acc) {
		boolean positionEstimated = false;
		float minEuclideanDistAcc = -99.9f; // Current minimum of euclidean
		float minEuclideanDistGyro = -99.9f; // distances calculated.
		int index = 0;
		int iteration = 0;
		int minIndex = -1; // Index where we should hook into the model.

		for (int i = 0; i < vectorMap.averageSampleCount; i++) {
			// iterate through all vector pairs and find the "nearest" (twoNorm)
		}
		
		while (!positionEstimated && minEuclideanDistAcc >= 0.0f
				&& minEuclideanDistGyro >= 0.0f && minIndex >= 0) {
			// current
			index++;
			index %= vectorMap.averageSampleCount;
			vectorMap.currentPosition = -99;
		}
	}

	private void nextStateLogic() {
		switch (currentState) {
		case LEARNING_LAP_120:
			if (learningLapCount > maxLearningLaps) {
				currentState = BotState.IMPROVING;
			}
			currentLap = new Lap();
			break;
		case IMPROVING:
			// TODO update model
			// TODO keep state

		default:
			currentState = BotState.SELF_DESTRUCTION;
			currentPower = 200;
			break;
		}
	}

	private void decideForPowerSetting() {

	}

	private TurningDirection decypherGyroData(Vector gyro_vec) {

		// TODO
		if (true) {
			return TurningDirection.LEFT_TURN;
		} else {
			return TurningDirection.RIGHT_TURN;
		}
	}

}
