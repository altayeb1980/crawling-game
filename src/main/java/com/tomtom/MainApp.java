package com.tomtom;

import java.util.Scanner;

import com.tomtom.exception.FormatMapException;

public class MainApp {
	public static void main(String[] args) throws FormatMapException {
		Scanner sc = new Scanner(System.in);
		try {
			System.out.println("Please select how to play the game ");
			System.out.println("1 Minimum ");
			System.out.println("2 Normal ");
			System.out.println("3 Extended ");
			System.out.println("your choice for the game: ");
			
			int gameToPlay = sc.nextInt();
			play(gameToPlay);
		} finally {
			sc.close();
		}
	}

	private static void play(int gameToPlay) throws FormatMapException {
		DungeonMap dungeonMap = new DungeonMap("data.txt");
		switch (gameToPlay) {
		case 1:
			dungeonMap.playMinimum();
			break;
		case 2:
			dungeonMap.buildGraph();
			dungeonMap.buildCoordinates();
			dungeonMap.playNormal();
			break;

		case 3:
			dungeonMap.buildGraph();
			dungeonMap.buildCoordinates();
			dungeonMap.playExtended();
			break;

		default:
			System.out.println("Sorry not supprted type, only 1,2,3 supported");
			break;
		}
	}
}
