package com.tomtom.dungeon;

import java.util.List;
import java.util.Map;

import com.tomtom.model.Room;

public interface DungeonData {
	Map<Room, List<Room>> parseDataDungeon();
}
