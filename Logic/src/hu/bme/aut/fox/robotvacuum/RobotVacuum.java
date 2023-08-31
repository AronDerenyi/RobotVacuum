package hu.bme.aut.fox.robotvacuum;

import hu.bme.aut.fox.robotvacuum.hardware.Motor;
import hu.bme.aut.fox.robotvacuum.hardware.Radar;
import hu.bme.aut.fox.robotvacuum.interpretation.Interpreter;
import hu.bme.aut.fox.robotvacuum.movement.MovementController;
import hu.bme.aut.fox.robotvacuum.navigation.Navigator;
import hu.bme.aut.fox.robotvacuum.world.World;

import java.util.*;

public class RobotVacuum {

	private final Radar radar;
	private final Motor motor;

	private final Interpreter interpreter;
	private final Navigator navigator;
	private final MovementController movementController;

	private boolean running = false;
	private Thread thread = null;

	private State state;
	private World world;

	private Queue<Navigator.Target> targets = new LinkedList<>();

	public RobotVacuum(
			double size,
			Radar radar, Motor motor,
			Interpreter interpreter, Navigator navigator, MovementController movementController
	) {
		this.radar = radar;
		this.motor = motor;

		this.interpreter = interpreter;
		this.navigator = navigator;
		this.movementController = movementController;

		state = new State(size, 0, 0, 0);
		world = new World(0.2);
	}

	public State getState() {
		return state;
	}

	public World getWorld() {
		return world;
	}

	public List<Navigator.Target> getTargets() {
		return new ArrayList<>(this.targets);
	}

	public void start() {
		if (running) throw new IllegalStateException("The vacuum has already been started");
		running = true;

		radar.start();
		motor.start();

		thread = new Thread(() -> {
			while (running) loop();
		});
		thread.start();
	}

	public void stop() {
		if (!running) throw new IllegalStateException("The vacuum hasn't been started");
		running = false;

		try {
			thread.join();
		} catch (InterruptedException ignored) {

		}

		radar.stop();
		motor.stop();
	}

	private void loop() {
		Radar.RadarData[] radarData = radar.getRadarData();
		Interpreter.Interpretation interpretation = interpreter.interpretRadar(world, state, radarData);
		world = interpretation.getWorld();
		state = interpretation.getState();

		MovementController.Movement movement = getMovement();

		double rotation = motor.rotate(movement.getAngle());
		interpretation = interpreter.interpretRotation(world, state, rotation);
		world = interpretation.getWorld();
		state = interpretation.getState();

		double distance = motor.move(movement.getDistance());
		interpretation = interpreter.interpretMovement(world, state, distance);
		world = interpretation.getWorld();
		state = interpretation.getState();
	}

	private MovementController.Movement getMovement() {
		MovementController.Movement movement;

		do {
			Navigator.Target target = getTarget();

			if (target == null) {
				movement = new MovementController.Movement(0, Math.PI * 0.1);
			} else {
				movement = movementController.getNextMovement(
						state,
						target.getX(),
						target.getY()
				);
				if (movement == null) nextTarget();
			}
		} while (movement == null);

		return movement;
	}

	private Navigator.Target getTarget() {
		if (targets.size() == 0) {
			Navigator.Target[] targets = navigator.getTargetPath(world, state);
			if (targets != null) Collections.addAll(this.targets, targets);
		}

		return targets.peek();
	}

	private void nextTarget() {
		targets.remove();
	}

	public static class State {

		private final double size;
		private final double positionX;
		private final double positionY;
		private final double direction;

		public State() {
			this(0, 0, 0, 0);
		}

		public State(double size, double positionX, double positionY, double direction) {
			this.size = size;
			this.positionX = positionX;
			this.positionY = positionY;
			this.direction = direction;
		}

		public double getSize() {
			return size;
		}

		public double getPositionX() {
			return positionX;
		}

		public double getPositionY() {
			return positionY;
		}

		public double getDirection() {
			return direction;
		}
	}
}
