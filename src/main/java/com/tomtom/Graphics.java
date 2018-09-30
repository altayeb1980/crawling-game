package com.tomtom;

public class Graphics {

	private char[][] g;
	private int width;
	private int heigth;

	public Graphics(int width, int heigth) {
		this.width = width;
		this.heigth = heigth;
		g = new char[heigth][width];
		init(width, heigth);
	}

	private void init(int width, int heigth) {
		for (int row = 0; row < heigth; row++) {
			for (int col = 0; col < width; col++) {
				g[row][col] = ' ';
			}
		}
	}

	public void draw(int x, int y, char c) {
		if (x >= 0 && x < heigth && y >= 0 && y < width) {
			g[x][y] = c;
		}
	}

	public void display() {
		System.out.println();
		for (int row = 0; row < heigth; row++) {
			for (int col = 0; col < width; col++) {
				System.out.print(g[row][col]);
			}
			System.out.println();
		}
	}
}
