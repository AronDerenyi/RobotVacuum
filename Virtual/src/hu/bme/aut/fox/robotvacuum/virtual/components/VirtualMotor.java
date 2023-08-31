package hu.bme.aut.fox.robotvacuum.virtual.components;

import hu.bme.aut.fox.robotvacuum.hardware.Motor;

public class VirtualMotor implements Motor {
	private static final double speed = 20;
	private static final double rotationSpeed = 20;
	private double radius;
	private boolean isRunning = false;
	VirtualWorld world;

	public VirtualMotor(VirtualWorld world) {
		this.world = world;
	}

	@Override
	public double move(double distance) {
		final Position prevPosition = world.getRobotVacuum();
		final double time = distance / speed;
		sleep(time);
		double realDistance = getMaxMove(prevPosition, distance);
//		realDistance = distance;
		final double dx = Math.cos(prevPosition.direction) * realDistance;
		final double dy = Math.sin(prevPosition.direction) * realDistance;
		world.setRobotVacuum(new Position(prevPosition.x + dx, prevPosition.y + dy, prevPosition.direction));
		return realDistance;
	}

	@Override
	public double rotate(double angle) {
		final Position prevPosition = world.getRobotVacuum();
		final double time = Math.abs(angle / rotationSpeed);
		sleep(time);
		world.setRobotVacuum(new Position(prevPosition.x, prevPosition.y, prevPosition.direction + angle));
		return angle;
	}

	private static void sleep(double d) {
		try {
			Thread.sleep((long)(d * 1000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private Double getMaxMove(Position position, double distance) {
		for (VirtualWorld.WorldObject object : world.getObjects()) {
			Double dist = VirtualRadar.getObjectsDistance(object, position, position.direction);
			if (dist != null && dist + radius < distance)
				return dist;
		}
		return distance;
	}


	@Override
	public void start() {
		isRunning = true;
	}

	@Override
	public void stop() {
		isRunning = false;
	}

	public void setSize(double size) {
		this.radius = size / 2;
	}
}
