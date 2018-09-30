package com.tomtom.model;

import com.tomtom.Graphics;

public class Room {

	private String roomName;
	private int x;
	private int y;
	private boolean visited;
	private static final int HGAP = 3;// room name 2 + | == 3
	private static final int VGAP = 2;// room name 2 + | == 3
	
	private boolean active;

	public Room(String roomName) {
		this.roomName = roomName;
	}

	// a0 n:a3
	public Room(String roomName, Room neighboor, int directionIndex) {
		this.roomName = roomName;
		setCoordinates(neighboor, directionIndex);
	}

	public Room(String roomName, int x, int y) {
		this.roomName = roomName;
		this.x = x;
		this.y = y;
	}

	public void setCoordinates(Room neighboor, int directionIndex) {
		switch (directionIndex) {
		// n
		case 0:
			x = neighboor.x - VGAP;
			y = neighboor.y;
			break;
		// e
		case 1:
			x = neighboor.x;
			y = neighboor.y + HGAP;

			break;
		// s
		case 2:
			x = neighboor.x + VGAP;
			y = neighboor.y;

			break;
		// w
		case 3:
			x = neighboor.x;
			y = neighboor.y - HGAP;
			break;
		}
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void shift(int dx, int dy) {
		x = x + dx;
		y = y + dy;

	}

	public boolean hasName(String name) {
		return roomName.equals(name);
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	public void draw(Graphics g, int[][] indexMap, int roomIndex) {
		if (active) {
			g.draw(x, y, '*');
			g.draw(x, y + 1, '*');
		} else {
			g.draw(x, y, roomName.charAt(0));
			g.draw(x, y + 1, roomName.charAt(1));
		}

		for (int dirIndex = 0; dirIndex < indexMap[roomIndex].length; dirIndex++) {
			if (indexMap[roomIndex][dirIndex] != -1) {
				switch (dirIndex) {
				// n
				case 0:
					g.draw(x - 1, y, '|');
					break;
				// e
				case 1:
					g.draw(x, y + 2, '-');
					break;
				// s
				case 2:
					g.draw(x + 1, y, '|');
					break;
				// w
				case 3:
					g.draw(x, y - 1, '-');
					break;
				}

			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((roomName == null) ? 0 : roomName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Room other = (Room) obj;
		if (roomName == null) {
			if (other.roomName != null)
				return false;
		} else if (!roomName.equals(other.roomName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return roomName;
	}

	public void enter() {
		active = true;
	}

	public void exit() {
		active = false;
	}

}
