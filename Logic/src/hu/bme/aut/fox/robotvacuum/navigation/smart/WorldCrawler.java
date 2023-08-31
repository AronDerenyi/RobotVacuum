package hu.bme.aut.fox.robotvacuum.navigation.smart;

import hu.bme.aut.fox.robotvacuum.world.Field;
import hu.bme.aut.fox.robotvacuum.world.World;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

class WorldCrawler {

	private final World world;
	private final Field entry;

	WorldCrawler(World world, Field entry) {
		this.world = world;
		this.entry = entry;
	}

	Node find(boolean ignoreUnreachable, Function<Node, Boolean> evaluator) {
		List<Field> scanned = new LinkedList<>();
		List<Node> edge = new LinkedList<>();

		scanned.add(entry);
		edge.add(new Node(null, entry));

		while (edge.size() > 0) {
			Node node = edge.remove(0);
			if (evaluator.apply(node)) return node;

			Field field = node.field;
			Field[] neighbors = {
					world.getGridField(field.getX() + 1, field.getY()),
					world.getGridField(field.getX() - 1, field.getY()),
					world.getGridField(field.getX(), field.getY() + 1),
					world.getGridField(field.getX(), field.getY() - 1)
			};

			for (Field neighbor : neighbors) {
				if (neighbor != null) {
					if (ignoreUnreachable ? neighbor.isReachable() : !neighbor.isObstacle()) {
						if (!scanned.contains(neighbor)) {
							scanned.add(neighbor);
							edge.add(new Node(node, neighbor));
						}
					}
				}
			}
		}

		return null;
	}

	static class Node {

		final Node parent;
		final int depth;
		final Field field;

		private Node(Node parent, Field field) {
			this.parent = parent;
			this.depth = parent == null ? 0 : parent.depth + 1;
			this.field = field;
		}
	}
}
