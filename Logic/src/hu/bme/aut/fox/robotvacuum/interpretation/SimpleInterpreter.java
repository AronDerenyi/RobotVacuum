package hu.bme.aut.fox.robotvacuum.interpretation;

import hu.bme.aut.fox.robotvacuum.RobotVacuum;
import hu.bme.aut.fox.robotvacuum.hardware.Radar;
import hu.bme.aut.fox.robotvacuum.world.Field;
import hu.bme.aut.fox.robotvacuum.world.World;

import java.util.LinkedList;
import java.util.List;

public class SimpleInterpreter implements Interpreter {

	private static final double RADIUS_ERROR = 0.25;
	private static final double RAY_ERROR = 0.02;
	private static final double DOUBLE_ERROR = 0.01;

	@Override
	public Interpretation interpretRadar(
			World currentWorld,
			RobotVacuum.State currentState,
			Radar.RadarData[] radarData
	) {
		List<Field> emptyFields = new LinkedList<>();
		List<Field> unreachableFields = new LinkedList<>();
		List<Field> obstacleFields = new LinkedList<>();

		double gridScale = currentWorld.getGridScale();

		double posX = currentState.getPositionX();
		double posY = currentState.getPositionY();

		double radius = currentState.getSize() / 2 + RADIUS_ERROR * gridScale;
		double radiusSquared = radius * radius;

		for (Radar.RadarData data : radarData) {
			double dirX = Math.cos(data.getDirection());
			double dirY = Math.sin(data.getDirection());
			double dist = data.getDistance() + RAY_ERROR * gridScale;

			double stepSize = gridScale * 0.1;
			for (double progress = 0; progress < dist; progress += stepSize) {
				int gridX = currentWorld.toGridCoordinate(posX + dirX * progress);
				int gridY = currentWorld.toGridCoordinate(posY + dirY * progress);
				if (currentWorld.getGridField(gridX, gridY) == null) {
					emptyFields.add(new Field(gridX, gridY, false, true, false));
				}
			}

			if (data.isHit()) {
				double hitX = posX + dirX * dist;
				double hitY = posY + dirY * dist;

				double hitCenterX = (Math.floor(hitX / gridScale) + 0.5) * gridScale;
				double hitCenterY = (Math.floor(hitY / gridScale) + 0.5) * gridScale;

				double overEstimatedRadius = Math.ceil(radius / gridScale) * gridScale;
				double approxMinX = hitCenterX - overEstimatedRadius;
				double approxMinY = hitCenterY - overEstimatedRadius;
				double approxMaxX = hitCenterX + overEstimatedRadius;
				double approxMaxY = hitCenterY + overEstimatedRadius;

				double doubleEqualError = DOUBLE_ERROR * gridScale;
				for (double y = approxMinY; y <= approxMaxY + doubleEqualError; y += gridScale) {
					for (double x = approxMinX; x <= approxMaxX + doubleEqualError; x += gridScale) {
						double distanceX = x - hitCenterX;
						double distanceY = y - hitCenterY;

						if (distanceX * distanceX + distanceY * distanceY < radiusSquared) {
							int gridX = currentWorld.toGridCoordinate(x);
							int gridY = currentWorld.toGridCoordinate(y);
							Field field = currentWorld.getGridField(gridX, gridY);

							if (field == null || !field.isObstacle()) {
								unreachableFields.add(new Field(
										gridX, gridY,
										false, false,
										field != null && field.isCleaned()
								));
							}
						}
					}
				}

				int hitGridX = currentWorld.toGridCoordinate(hitX);
				int hitGridY = currentWorld.toGridCoordinate(hitY);
				if (currentWorld.getGridField(hitGridX, hitGridY) != null) {
					obstacleFields.add(new Field(hitGridX, hitGridY, true, false, false));
				}
			}
		}

		List<Field> newFields = new LinkedList<>();
		newFields.addAll(emptyFields);
		newFields.addAll(unreachableFields);
		newFields.addAll(obstacleFields);

		return new Interpretation(
				new World(currentWorld, newFields.toArray(new Field[0])),
				currentState
		);
	}

	@Override
	public Interpretation interpretMovement(
			World currentWorld,
			RobotVacuum.State currentState,
			double distance
	) {
		RobotVacuum.State state = new RobotVacuum.State(
				currentState.getSize(),
				currentState.getPositionX() + Math.cos(currentState.getDirection()) * distance,
				currentState.getPositionY() + Math.sin(currentState.getDirection()) * distance,
				currentState.getDirection()
		);

		List<Field> cleanedFields = new LinkedList<>();

		double gridScale = currentWorld.getGridScale();

		double posX = currentState.getPositionX();
		double posY = currentState.getPositionY();

		double radius = currentState.getSize() / 2;
		double radiusSquared = radius * radius;

		double overEstimatedRadius = Math.ceil(radius / gridScale) * gridScale;
		double approxMinX = posX - overEstimatedRadius;
		double approxMinY = posY - overEstimatedRadius;
		double approxMaxX = posX + overEstimatedRadius;
		double approxMaxY = posY + overEstimatedRadius;

		double doubleEqualError = DOUBLE_ERROR * gridScale;
		for (double y = approxMinY; y <= approxMaxY + doubleEqualError; y += gridScale) {
			for (double x = approxMinX; x <= approxMaxX + doubleEqualError; x += gridScale) {
				double distanceX = x - posX;
				double distanceY = y - posY;

				if (distanceX * distanceX + distanceY * distanceY < radiusSquared) {
					int gridX = currentWorld.toGridCoordinate(x);
					int gridY = currentWorld.toGridCoordinate(y);
					Field field = currentWorld.getGridField(gridX, gridY);

					cleanedFields.add(new Field(
							gridX, gridY,
							field != null && field.isObstacle(),
							field == null || field.isReachable(),
							true
					));
				}
			}
		}

		World world = new World(currentWorld, cleanedFields.toArray(new Field[0]));
		return new Interpretation(world, state);
	}

	@Override
	public Interpretation interpretRotation(
			World currentWorld,
			RobotVacuum.State currentState,
			double angle
	) {
		RobotVacuum.State state = new RobotVacuum.State(
				currentState.getSize(),
				currentState.getPositionX(),
				currentState.getPositionY(),
				currentState.getDirection() + angle
		);

		return new Interpretation(currentWorld, state);
	}
}
