package hu.bme.aut.fox.robotvacuum.navigation.smart;

import hu.bme.aut.fox.robotvacuum.world.Field;
import hu.bme.aut.fox.robotvacuum.world.World;

import java.util.*;

class SubWorld {

	private final World world;
	private final int startX;
	private final int startY;
	private final int width;
	private final int height;

	private final List<Field> corners;
	private final List<Wall> walls;

	SubWorld(World world, int startX, int startY, int width, int height) {
		this.world = world;
		this.startX = startX;
		this.startY = startY;
		this.width = width;
		this.height = height;

		corners = getCorners();
		walls = getWalls();
	}

	Node getShortestPath(Field from, Field to) {
		Set<Node> nodes = new HashSet<>();

		nodes.add(new Node(from, null, 0));
		nodes.add(new Node(to, null, Double.POSITIVE_INFINITY));
		for (Field corner : corners) {
			nodes.add(new Node(corner, null, Double.POSITIVE_INFINITY));
		}

		while (nodes.size() > 0) {
			Node node = Collections.min(nodes, Comparator.comparingDouble(it -> it.distance));
			nodes.remove(node);

			if (node.distance == Double.POSITIVE_INFINITY) {
				return null;
			}

			if (node.field == to) {
				return node;
			}

			for (Node next : nodes) {
				if (!isPathBlocked(node.field, next.field)) {
					double distanceX = next.field.getX() - node.field.getX();
					double distanceY = next.field.getY() - node.field.getY();
					double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
					double sumDistance = node.distance + distance;
					if (sumDistance < next.distance) {
						next.parent = node;
						next.distance = sumDistance;
					}
				}
			}
		}

		return null;
	}

	private boolean isBlocking(Field field) {
		return field == null || !field.isReachable();
	}

	private List<Field> getCorners() {
		List<Field> corners = new LinkedList<>();

		for (int y = startY; y < startY + height - 1; y++) {
			for (int x = startX; x < startX + width - 1; x++) {
				Field field00 = world.getGridField(x, y);
				Field field01 = world.getGridField(x + 1, y);
				Field field10 = world.getGridField(x, y + 1);
				Field field11 = world.getGridField(x + 1, y + 1);

				int blocking = 0;
				if (isBlocking(field00)) blocking++;
				if (isBlocking(field01)) blocking++;
				if (isBlocking(field10)) blocking++;
				if (isBlocking(field11)) blocking++;

				if (blocking == 1) {
					if (isBlocking(field00)) corners.add(field11);
					if (isBlocking(field01)) corners.add(field10);
					if (isBlocking(field10)) corners.add(field01);
					if (isBlocking(field11)) corners.add(field00);
				}
			}
		}

		return corners;
	}

	private List<Wall> getWalls() {
		List<Wall> walls = new LinkedList<>();

		for (int y = startY; y < startY + height - 1; y++) {
			int start = startX;

			for (int x = startX; x <= startX + width; x++) {
				boolean bottomBlocking = isBlocking(world.getGridField(x, y));
				boolean topBlocking = isBlocking(world.getGridField(x, y + 1));
				if (x == startX + width || bottomBlocking == topBlocking) {
					if (start != x) walls.add(new Wall(true, start, y + 1, x - start));
					start = x + 1;
				}
			}
		}

		for (int x = startX; x < startX + width - 1; x++) {
			int start = startY;

			for (int y = startY; y <= startY + height; y++) {
				boolean leftBlocking = isBlocking(world.getGridField(x, y));
				boolean rightBlocking = isBlocking(world.getGridField(x + 1, y));
				if (y == startY + height || leftBlocking == rightBlocking) {
					if (start != y) walls.add(new Wall(false, x + 1, start, y - start));
					start = y + 1;
				}
			}
		}

		return walls;
	}

	private boolean isPathBlocked(Field from, Field to) {
		for (Wall wall : walls) {
			if (wall.intersects(
					from.getX() + 0.5, from.getY() + 0.5,
					to.getX() + 0.5, to.getY() + 0.5
			)) return true;
		}
		return false;
	}

	private static class Wall {

		final boolean horizontal;
		final int x;
		final int y;
		final int length;

		private Wall(boolean horizontal, int x, int y, int length) {
			this.horizontal = horizontal;
			this.x = x;
			this.y = y;
			this.length = length;
		}

		private boolean intersects(double startX, double startY, double endX, double endY) {
			if (horizontal) {
				if (startY == endY) return false;
				if (startY < y && endY < y || startY > y && endY > y) return false;
				double slope = (endX - startX) / (endY - startY);
				double intersectionX = slope * (y - startY) + startX;
				return intersectionX >= x && intersectionX <= x + length;
			} else {
				if (startX == endX) return false;
				if (startX < x && endX < x || startX > x && endX > x) return false;
				double slope = (endY - startY) / (endX - startX);
				double intersectionY = slope * (x - startX) + startY;
				return intersectionY >= y && intersectionY <= y + length;
			}
		}
	}

	static class Node {

		final Field field;

		Node parent;
		double distance;

		private Node(Field field, Node parent, double distance) {
			this.field = field;
			this.parent = parent;
			this.distance = distance;
		}
	}
}
