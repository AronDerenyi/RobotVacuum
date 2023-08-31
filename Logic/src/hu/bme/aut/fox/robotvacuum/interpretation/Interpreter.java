package hu.bme.aut.fox.robotvacuum.interpretation;

import hu.bme.aut.fox.robotvacuum.RobotVacuum;
import hu.bme.aut.fox.robotvacuum.hardware.Radar;
import hu.bme.aut.fox.robotvacuum.world.World;

public interface Interpreter {

	Interpretation interpretRadar(
			World currentWorld,
			RobotVacuum.State currentState,
			Radar.RadarData[] radarData
	);

	Interpretation interpretMovement(
			World currentWorld,
			RobotVacuum.State currentState,
			double distance
	);

	Interpretation interpretRotation(
			World currentWorld,
			RobotVacuum.State currentState,
			double angle
	);

	final class Interpretation {

		private final World world;
		private final RobotVacuum.State state;

		public Interpretation(World world, RobotVacuum.State state) {
			this.world = world;
			this.state = state;
		}

		public World getWorld() {
			return world;
		}

		public RobotVacuum.State getState() {
			return state;
		}
	}
}
