package com.tomtom;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

import com.tomtom.exception.FormatMapException;
import com.tomtom.model.Room;

public class DungeonMap {

	private List<Room> rooms = new ArrayList<>();
	private static final int DIRECTIONS_COUNT = 4;// n,e,s,w
	private static final String DIRECTIONS = "nesw";

	private int[][] indexMap;
	private int[][] graph;

	private List<String[]> namesMap = new ArrayList<>();

	private int currentRoomIndex = 0;// starting room

	private Graphics graphics;

	public DungeonMap(String fileName) throws FormatMapException {

		try (BufferedReader br = Files.newBufferedReader(Paths.get(fileName))) {

			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.length() == 0)
					continue;

				String[] fields = line.split("\\s+");

				if (fields.length < 2) {
					throw new FormatMapException("Invalid Data map: " + line);
				}

				String roomName = fields[0];

				if (roomName.length() > 2) {
					throw new FormatMapException("invalid room name: " + roomName);
				}

				Room room = findRoomByName(roomName);
				if (room != null) {
					throw new FormatMapException("redundant room name : " + roomName);
				}

				room = new Room(roomName);
				rooms.add(room);

				String[] doors = new String[4];

				for (int i = 1; i < fields.length; i++) {
					String nextRoomSplitter = fields[i];
					int colIndex = nextRoomSplitter.indexOf(":");
					if (colIndex == -1) {
						throw new FormatMapException(
								"next room format error, format should be direction:roomname " + nextRoomSplitter);
					}
					String doorDirection = nextRoomSplitter.substring(0, colIndex).trim();
					if (doorDirection.length() != 1) {
						throw new FormatMapException("door direction format error, should be nesw " + doorDirection);
					}
					int dirIndex = DIRECTIONS.indexOf(doorDirection);
					if (dirIndex == -1) {
						throw new FormatMapException("door direction value error, should be nesw " + doorDirection);
					}
					if (colIndex + 1 >= nextRoomSplitter.length()) {
						throw new FormatMapException("invalid format : no next room ");
					}
					String nextRoomName = nextRoomSplitter.substring(colIndex + 1);
					doors[dirIndex] = nextRoomName;
				}
				namesMap.add(doors);
			}

		} catch (IOException exp) {
			throw new FormatMapException(" Invalid Map Source  : ", exp);
		}

		buildIndexMap();
		//buildGraph();
		//buildCoordinates();
	}

	public void buildCoordinates() {
		Stack<Room> stack = new Stack<>();
		Room r = rooms.get(0);
		int minX = 0;
		int maxX = 0;
		int minY = 0;
		int maxY = 0;

		r.setVisited(true);
		r.setX(0);
		r.setY(0);
		stack.push(r);

		while (!stack.isEmpty()) {
			Room room = stack.peek();
			int roomIndex = rooms.indexOf(room);
			int nextRoomIndex = getNotVisitedNighbir(roomIndex);
			if (nextRoomIndex == -1) {
				stack.pop();
			} else {
				Room nextRoom = rooms.get(nextRoomIndex);
				nextRoom.setVisited(true);
				nextRoom.setCoordinates(room, graph[roomIndex][nextRoomIndex]);
				stack.push(nextRoom);
			}
		}

		minX = maxX = rooms.get(0).getX();
		minY = maxY = rooms.get(0).getY();

		for (int j = 1; j < rooms.size(); j++) {
			Room room = rooms.get(j);
			minX = Math.min(minX, room.getX());
			maxX = Math.max(maxX, room.getX());
			minY = Math.min(minY, room.getY());
			maxY = Math.max(maxY, room.getY());
		}

		int heigth = maxX - minX + 5;
		int width = maxY - minY + 5;
		graphics = new Graphics(width, heigth);

		for (int roomIndex = 0; roomIndex < rooms.size(); roomIndex++) {
			Room room = rooms.get(roomIndex);
			room.setVisited(false);
			room.setX(room.getX()+Math.abs(minX));
			room.setY(room.getY() - minY);
			room.draw(graphics, indexMap, roomIndex);
		}

	}

	private int getNotVisitedNighbir(int roomIndex) {
		for (int i = 0; i < graph[roomIndex].length; i++) {
			int nextRoomIndex = graph[roomIndex][i];
			if (nextRoomIndex != -1 && !rooms.get(i).isVisited()) {
				return i;
			}
		}
		return -1;
	}

	private Room findRoomByName(String name) {
		for (int i = 0; i < rooms.size(); i++) {
			if (rooms.get(i).hasName(name)) {
				return rooms.get(i);
			}
		}
		return null;
	}

	private int findRoomIndexByName(String name) {
		for (int i = 0; i < rooms.size(); i++) {
			if (rooms.get(i).hasName(name)) {
				return i;
			}
		}
		return -1;
	}

	public void buildGraph() {
		int numberOfRooms = rooms.size();
		graph = new int[numberOfRooms][numberOfRooms];
		for (int i = 0; i < numberOfRooms; i++) {
			for (int j = 0; j < numberOfRooms; j++) {
				graph[i][j] = getDirIndex(i, j);
			}
		}
	}

	// Path from roomIndex the next Room Index
	private int getDirIndex(int roomIndex, int nextRoomIndex) {
		for (int dirIndex = 0; dirIndex < DIRECTIONS_COUNT; dirIndex++) {
			if (indexMap[roomIndex][dirIndex] == nextRoomIndex) {
				return dirIndex;
			}
		}
		return -1;
	}

	// roomIndex, dirIndex, nextRoomIndex
	private void buildIndexMap() throws FormatMapException {
		indexMap = new int[rooms.size()][DIRECTIONS_COUNT];
		for (int roomIndex = 0; roomIndex < namesMap.size(); roomIndex++) {
			String[] nextRoomNames = namesMap.get(roomIndex);

			for (int dirIndex = 0; dirIndex < nextRoomNames.length; dirIndex++) {// j = 0 (n, j=1 = e, j=2, s, j=3=w)
				String nextRoomName = nextRoomNames[dirIndex];

				if (nextRoomName == null) {
					indexMap[roomIndex][dirIndex] = -1;// no path from roomnames[i] into DIRECTIONS[j]
				} else {
					// Room nextRoom = findRoomByName(nextRoomName);
					int nextRoomIndex = findRoomIndexByName(nextRoomName);
					if (nextRoomIndex == -1) {
						throw new FormatMapException("no data entry for nextRoomName:" + nextRoomName);
					}
					indexMap[roomIndex][dirIndex] = findRoomIndexByName(nextRoomName);// rooms.indexOf(nextRoom);//
																						// index direction from room i
																						// (taking
					// direction j to
					// reach next room index)
				}

			}

		}
	}

	public void playMinimum() {
		Scanner sc = new Scanner(System.in);
		try {
			for (;;) {
				System.out.println("you are in room :" + rooms.get(currentRoomIndex));
				StringBuilder possibleBuilder = new StringBuilder();
				for (int dirIndex = 0; dirIndex < indexMap[currentRoomIndex].length; dirIndex++) {
					int nextRoomIndex = indexMap[currentRoomIndex][dirIndex];
					if (nextRoomIndex != -1) {
						possibleBuilder.append(DIRECTIONS.charAt(dirIndex));
					}
				}
				System.out.println("possible moves :" + possibleBuilder);
				System.out.println("your choice :");

				String yourChoice = sc.nextLine().trim();
				if (yourChoice.length() == 0) {
					System.out.println("GoodBye!");
					break;
				}
				int dirIndex = DIRECTIONS.indexOf(yourChoice);

				// if length> 1 no need to continue
				if (yourChoice.length() != 1) {
					System.out.println("Bad Entry, your choice must be senw ");
					continue;
				}

				// if direct is wriong , noo need to continue
				if (dirIndex == -1) {
					System.out.println("Bad Entry, your choice must be senw ");
					continue;
				}
				int nextRoomIndex = indexMap[currentRoomIndex][dirIndex];
				if (nextRoomIndex == -1) {
					System.out.println("Sorry noway for this direction");
				} else {
					currentRoomIndex = nextRoomIndex;
				}
			}
		} finally {
			sc.close();
		}

	}

	public void playExtended() {
		// dijkstra(graph, 0);
		int nextRoomIndex = currentRoomIndex;
		Scanner sc = new Scanner(System.in);
		try {
			for (;;) {
				Room currentRoom = rooms.get(currentRoomIndex);
				currentRoom.enter();
				currentRoom.draw(graphics, indexMap, currentRoomIndex);
				graphics.display();

				System.out.println("you are in room :" + rooms.get(currentRoomIndex));
				StringBuilder possibleBuilder = new StringBuilder();
				for (int dirIndex = 0; dirIndex < indexMap[currentRoomIndex].length; dirIndex++) {
					int dirRoomIndex = indexMap[currentRoomIndex][dirIndex];
					if (dirRoomIndex != -1) {
						possibleBuilder.append(DIRECTIONS.charAt(dirIndex));
					}
				}
				System.out.println("possible moves :" + possibleBuilder);
				System.out.println("your choice :");

				String yourChoice = sc.nextLine().trim();
				if (yourChoice.length() == 0) {
					System.out.println("GoodBye!");
					break;
				}

				String[] splitChoice = yourChoice.split(" ");
				if (splitChoice.length > 2) {
					System.out.println("Bad Entry, your choice must be senw ");
					continue;
				}

				if (splitChoice.length == 2) {
					if (!splitChoice[0].equals("find")) {
						System.out.println("Bad Entry, your choice must be find ");
						continue;
					}

					String destRoomName = splitChoice[1];
					int destRoomIndex = findRoomIndexByName(destRoomName);
					if (destRoomIndex == currentRoomIndex) {
						System.out.println("your are already in the same place");
						continue;
					}
					if (destRoomIndex == -1) {
						System.out.println("Not Available Destination ");
						continue;
					}

					List<List<String>> pathes = dijkstra(graph, currentRoomIndex);
					List<String> list = pathes.get(destRoomIndex);

					if (list.isEmpty()) {
						System.out.println("sorry no way to move  ");
						continue;
					}
					StringBuilder yourPathBuilder = new StringBuilder();
					for (String str : list) {
						yourPathBuilder.append(str);
					}
					System.out.println("finding path from " + rooms.get(currentRoomIndex).getRoomName() + " to "
							+ rooms.get(destRoomIndex).getRoomName());
					System.out.println("the path is " + yourPathBuilder);
					continue;

				} else {
					yourChoice = splitChoice[0];
				}

				nextRoomIndex = currentRoomIndex;
				for (int i = 0; i < yourChoice.length(); i++) {
					char dirChar = yourChoice.charAt(i);
					int dirIndex = DIRECTIONS.indexOf(dirChar);

					if (dirIndex == -1) {
						System.out.println("Bad Entry, your choice must be senw ");
						break;
					}

					if (indexMap[nextRoomIndex][dirIndex] == -1) {
						System.out.println("Sorry noway for this direction");
						break;
					} else {
						nextRoomIndex = indexMap[nextRoomIndex][dirIndex];
					}
				}
				if (nextRoomIndex != currentRoomIndex) {
					currentRoom.exit();
					currentRoom.draw(graphics, indexMap, currentRoomIndex);
					currentRoomIndex = nextRoomIndex;
				}

			}
		} finally {
			sc.close();
		}

	}

	public void playNormal() {
		Scanner sc = new Scanner(System.in);
		int nextRoomIndex = currentRoomIndex;
		try {
			for (;;) {
				Room currentRoom = rooms.get(currentRoomIndex);
				currentRoom.enter();
				currentRoom.draw(graphics, indexMap, currentRoomIndex);
				graphics.display();

				System.out.println("you are in room :" + rooms.get(currentRoomIndex));
				StringBuilder possibleBuilder = new StringBuilder();
				for (int dirIndex = 0; dirIndex < indexMap[currentRoomIndex].length; dirIndex++) {
					int dirRoomIndex = indexMap[currentRoomIndex][dirIndex];
					if (dirRoomIndex != -1) {
						possibleBuilder.append(DIRECTIONS.charAt(dirIndex));
					}
				}
				System.out.println("possible moves :" + possibleBuilder);
				System.out.println("your choice :");

				String yourChoice = sc.nextLine().trim();
				if (yourChoice.length() == 0) {
					System.out.println("GoodBye!");
					break;
				}

				nextRoomIndex = currentRoomIndex;
				for (int i = 0; i < yourChoice.length(); i++) {
					char dirChar = yourChoice.charAt(i);
					int dirIndex = DIRECTIONS.indexOf(dirChar);

					if (dirIndex == -1) {
						System.out.println("Bad Entry, your choice must be senw ");
						break;
					}

					if (indexMap[nextRoomIndex][dirIndex] == -1) {
						System.out.println("Sorry noway for this direction");
						break;
					} else {
						nextRoomIndex = indexMap[nextRoomIndex][dirIndex];
					}
				}
				if (nextRoomIndex != currentRoomIndex) {
					currentRoom.exit();
					currentRoom.draw(graphics, indexMap, currentRoomIndex);
					currentRoomIndex = nextRoomIndex;
				}
			}
		} finally {
			sc.close();
		}

	}

	public void display() {

	}

//	private void printGraph() {
//		for (int i = 0; i < graph.length; i++) {
//			System.out.print(i + ": ");
//			for (int j = 0; j < graph[i].length; j++) {
//				int dirIndex = graph[i][j];
//				System.out.print(dirIndex == -1 ? -1 : DIRECTIONS.charAt(dirIndex) + " ");
//			}
//			System.out.println();
//		}
//	}
//
//	private void printIndexMap() {
//		for (int i = 0; i < indexMap.length; i++) {
//			System.out.print(i + " " + rooms.get(i).getRoomName() + " ");
//			for (int j = 0; j < indexMap[i].length; j++) {
//				int nextRoomIndex = indexMap[i][j];
//				if (nextRoomIndex == -1) {
//					System.out.print(" -1 ");
//				} else {
//					System.out.print(DIRECTIONS.charAt(j) + " : " + rooms.get(nextRoomIndex).getRoomName());
//				}
//			}
//			System.out.println();
//		}
//	}

	private int minDistance(int dist[], Boolean visistedSet[]) {
		// Initialize min value
		int min = Integer.MAX_VALUE, min_index = -1;

		for (int v = 0; v < rooms.size(); v++)
			if (visistedSet[v] == false && dist[v] <= min) {
				min = dist[v];
				min_index = v;
			}

		return min_index;
	}

	private List<List<String>> dijkstra(int graph[][], int startingRoomIndex) {
		int numberOfRooms = rooms.size();
		int dist[] = new int[numberOfRooms]; // The output array. dist[i] will hold
		List<List<String>> pathes = new ArrayList<>();
		Boolean visitedRoom[] = new Boolean[numberOfRooms];
		for (int i = 0; i < numberOfRooms; i++) {
			dist[i] = Integer.MAX_VALUE;
			visitedRoom[i] = false;
			pathes.add(new ArrayList<>());
		}
		dist[startingRoomIndex] = 0;
		for (int count = 0; count < numberOfRooms - 1; count++) {
			int u = minDistance(dist, visitedRoom);
			visitedRoom[u] = true;
			for (int v = 0; v < numberOfRooms; v++) {
				if (!visitedRoom[v] && graph[u][v] != -1 && dist[u] != Integer.MAX_VALUE && dist[u] + 1 < dist[v]) {
					dist[v] = dist[u] + 1;
					int dirIndex = graph[u][v];
					//System.out.println(pathes.get(v) + ":" + DIRECTIONS.charAt(dirIndex));
					List<String> uList = pathes.get(u);
					List<String> vList = pathes.get(v);
					vList.addAll(uList);
					vList.add(DIRECTIONS.charAt(dirIndex) + "");
				}
			}
		}
		return pathes;
	}
}
