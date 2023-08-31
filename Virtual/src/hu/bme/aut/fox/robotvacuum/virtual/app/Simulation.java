package hu.bme.aut.fox.robotvacuum.virtual.app;

import hu.bme.aut.fox.robotvacuum.RobotVacuum;
import hu.bme.aut.fox.robotvacuum.interpretation.SimpleInterpreter;
import hu.bme.aut.fox.robotvacuum.movement.SimpleMovementController;
import hu.bme.aut.fox.robotvacuum.navigation.smart.SmartNavigator;
import hu.bme.aut.fox.robotvacuum.virtual.components.*;

import java.io.FileNotFoundException;

public class Simulation {

	private VirtualWorld world;
	private VirtualRadar radar;
	private VirtualMotor motor;
	private RobotVacuum vacuum;

	public Simulation() {
		try {
			world = new VirtualWorldLoader("world2").load();
			vacuum = new RobotVacuum(
				1.0,
				radar = new VirtualRadar(world),
				motor = new VirtualMotor(world),
				new SimpleInterpreter(),
				new SmartNavigator(),
				new SimpleMovementController()
			);
			motor.setSize(1.0);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	public void start() {
		vacuum.start();
	}

	public RobotVacuum getRobotVacuum() { return vacuum; }

	public VirtualWorld getWorld() {
		return world;
	}

	public VirtualRadar getRadar() {
		return radar;
	}

	public VirtualMotor getMotor() {
		return motor;
	}
}
