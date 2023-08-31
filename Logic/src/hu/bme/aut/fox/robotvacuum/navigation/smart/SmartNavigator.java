package hu.bme.aut.fox.robotvacuum.navigation.smart;

import hu.bme.aut.fox.robotvacuum.RobotVacuum;
import hu.bme.aut.fox.robotvacuum.navigation.Navigator;
import hu.bme.aut.fox.robotvacuum.world.Field;
import hu.bme.aut.fox.robotvacuum.world.World;

import java.util.LinkedList;
import java.util.List;

public class SmartNavigator implements Navigator {

	@Override
	public Target[] getTargetPath(World world, RobotVacuum.State state) {
		Field onField = world.getField(state.getPositionX(), state.getPositionY());
		if (onField == null) return null;

		WorldCrawler crawler = new WorldCrawler(world, onField);
		WorldCrawler.Node targetNode = crawler.find(true, node -> !node.field.isCleaned());

		if (targetNode == null) return null;
		if (!targetNode.field.isReachable()) return null;

		int minX = targetNode.field.getX();
		int minY = targetNode.field.getY();
		int maxX = targetNode.field.getX();
		int maxY = targetNode.field.getY();

		WorldCrawler.Node node = targetNode.parent;
		while (node != null) {
			if (node.field.getX() < minX) minX = node.field.getX();
			if (node.field.getY() < minY) minY = node.field.getY();
			if (node.field.getX() > maxX) maxX = node.field.getX();
			if (node.field.getY() > maxY) maxY = node.field.getY();
			node = node.parent;
		}

		SubWorld subWorld = new SubWorld(world, minX, minY, maxX - minX + 1, maxY - minY + 1);
		SubWorld.Node pathEnd = subWorld.getShortestPath(onField, targetNode.field);

		List<Target> path = new LinkedList<>();
		SubWorld.Node pathNode = pathEnd;
		while (pathNode != null) {
			path.add(0, new Target(
					world.toWorldCoordinate(pathNode.field.getX()) + world.getGridScale() / 2,
					world.toWorldCoordinate(pathNode.field.getY()) + world.getGridScale() / 2
			));
			pathNode = pathNode.parent;
		}

		if (path.size() == 0) {
			while (targetNode != null) {
				path.add(0, new Target(
						world.toWorldCoordinate(targetNode.field.getX()) + world.getGridScale() / 2,
						world.toWorldCoordinate(targetNode.field.getY()) + world.getGridScale() / 2
				));
				targetNode = targetNode.parent;
			}
		}

		return path.toArray(new Target[0]);
	}
}
