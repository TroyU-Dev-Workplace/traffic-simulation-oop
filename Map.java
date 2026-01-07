package com.traffic.sim.simulation.map;

/**
 * Represents the simulation grid.
 * TileType values:
 * - 0 = ROAD (đường xe chạy)
 * - 1 = BLOCKED (vùng cấm - tòa nhà, cây cối)
 * - 2 = SIDEWALK (vỉa hè - người đi bộ có thể đi)
 * - 3 = CROSSWALK (vạch kẻ đường - người đi bộ qua đường)
 * - 4 = STOP_LINE (vạch dừng đèn đỏ)
 * - 5 = LANE_DIVIDER (vạch chia làn)
 */
public class Map {
    public static final int ROAD = 0;
    public static final int BLOCKED = 1;
    public static final int SIDEWALK = 2;
    public static final int CROSSWALK = 3;
    public static final int STOP_LINE = 4;
    public static final int LANE_DIVIDER = 5;

    private int[][] grid;
    private int width; // số cột (x) = 50
    private int height; // số hàng (y) = 36

    public Map(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new int[height][width]; // grid[y][x]
        initializeDefaultMap();
    }

    private void initializeDefaultMap() {
        // Bước 1: Đặt tất cả là ROAD trước
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = ROAD;
            }
        }

        // Bước 2: Đặt 4 góc là BLOCKED (tòa nhà/vùng cấm)
        // Góc trên bên trái: x=0-16, y=0-9
        fillArea(0, 0, 16, 9, BLOCKED);
        // Góc trên bên phải: x=33-49, y=0-9
        fillArea(33, 0, 49, 9, BLOCKED);
        // Góc dưới bên trái: x=0-16, y=26-35
        fillArea(0, 26, 16, 35, BLOCKED);
        // Góc dưới bên phải: x=33-49, y=26-35
        fillArea(33, 26, 49, 35, BLOCKED);

        // Bước 3: Đặt vỉa hè (SIDEWALK) - viền trong của các góc
        // Góc trên bên trái
        fillArea(15, 0, 16, 9, SIDEWALK); // cạnh phải
        fillArea(0, 8, 14, 9, SIDEWALK); // cạnh dưới

        // Góc trên bên phải
        fillArea(33, 0, 34, 9, SIDEWALK); // cạnh trái
        fillArea(35, 8, 49, 9, SIDEWALK); // cạnh dưới

        // Góc dưới bên trái
        fillArea(0, 26, 14, 27, SIDEWALK); // cạnh trên
        fillArea(15, 26, 16, 35, SIDEWALK); // cạnh phải

        // Góc dưới bên phải
        fillArea(35, 26, 49, 27, SIDEWALK); // cạnh trên
        fillArea(33, 26, 34, 35, SIDEWALK); // cạnh trái

        // Bước 4: Vạch kẻ đường cho người đi bộ (CROSSWALK)
        // Vạch phía trên (ngang): x=17-32, y=7-8
        fillArea(17, 7, 32, 8, CROSSWALK);
        // Vạch phía dưới (ngang): x=17-32, y=27-28
        fillArea(17, 27, 32, 28, CROSSWALK);
        // Vạch phía trái (dọc): x=14-15, y=10-25
        fillArea(14, 10, 15, 25, CROSSWALK);
        // Vạch phía phải (dọc): x=34-35, y=10-25
        fillArea(34, 10, 35, 25, CROSSWALK);

        // Bước 5: Vạch chia làn đường (LANE_DIVIDER)
        // Phía trên: x=25, y=0-5 (nhưng y=0-5 đã là blocked, nên y=0-6 trong phần
        // đường)
        fillArea(24, 0, 25, 6, LANE_DIVIDER);
        // Phía dưới: x=24, y=30-35
        fillArea(24, 29, 25, 35, LANE_DIVIDER);
        // Phía trái: x=0-12, y=17
        fillArea(0, 17, 13, 18, LANE_DIVIDER);
        // Phía phải: x=37-49, y=18
        fillArea(36, 17, 49, 18, LANE_DIVIDER);

        // Bước 6: Vạch dừng đèn đỏ (STOP_LINE)
        // Phía trên: x=17-25, y=5
        fillArea(17, 5, 25, 6, STOP_LINE);
        // Phía dưới: x=24-32, y=30
        fillArea(24, 29, 32, 30, STOP_LINE);
        // Phía trái: x=12, y=17-25
        fillArea(12, 17, 13, 25, STOP_LINE);
        // Phía phải: x=37, y=10-18
        fillArea(36, 10, 37, 18, STOP_LINE);
    }

    private void fillArea(int x1, int y1, int x2, int y2, int tileType) {
        for (int y = y1; y <= y2 && y < height; y++) {
            for (int x = x1; x <= x2 && x < width; x++) {
                if (x >= 0 && y >= 0) {
                    grid[y][x] = tileType;
                }
            }
        }
    }

    public int getTileType(int x, int y) {
        if (isValidCoordinate(x, y)) {
            return grid[y][x];
        }
        return BLOCKED;
    }

    public boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public boolean isRoad(int x, int y) {
        int type = getTileType(x, y);
        return type == ROAD || type == LANE_DIVIDER || type == STOP_LINE;
    }

    public boolean isSidewalk(int x, int y) {
        return getTileType(x, y) == SIDEWALK;
    }

    public boolean isCrosswalk(int x, int y) {
        return getTileType(x, y) == CROSSWALK;
    }

    public boolean isWalkable(int x, int y) {
        int type = getTileType(x, y);
        return type == SIDEWALK || type == CROSSWALK;
    }

    public boolean isDrivable(int x, int y) {
        int type = getTileType(x, y);
        return type == ROAD || type == CROSSWALK || type == LANE_DIVIDER || type == STOP_LINE;
    }

    public boolean isBlocked(int x, int y) {
        return getTileType(x, y) == BLOCKED;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[][] getGrid() {
        return grid;
    }
}
