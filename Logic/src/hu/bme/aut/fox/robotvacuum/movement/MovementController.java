package hu.bme.aut.fox.robotvacuum.movement;

import hu.bme.aut.fox.robotvacuum.RobotVacuum;

public interface MovementController {

	Movement getNextMovement(RobotVacuum.State state, double targetX, double targetY);

	final class Movement {

		private final double distance;
		private final double angle;

		public Movement(double distance, double angle) {
			this.distance = distance;
			this.angle = angle;
		}

		public double getDistance() {
			return distance;
		}

		public double getAngle() {
			return angle;
		}
	}
}
