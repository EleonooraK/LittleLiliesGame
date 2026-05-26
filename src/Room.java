import java.util.Random;

public class Room {
    private String name;
    private String[] text;
    private String color;

    private int x;
    private int y;

    private Room north, south, east, west;

    public Room(String name, String[] text, String color, int x, int y) {
        this.name = name;
        this.text = text;
        this.color = color;
        this.x = x;
        this.y = y;
    }

    public void setNorth(Room r) { this.north = r; }
    public void setSouth(Room r) { this.south = r; }
    public void setEast(Room r)  { this.east = r; }
    public void setWest(Room r)  { this.west = r; }

    public Room getExit(String direction) {
        if (direction.equals("north")) return north;
        if (direction.equals("south")) return south;
        if (direction.equals("east"))  return east;
        if (direction.equals("west"))  return west;
        return null;
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getColorTheme() {
        return color;
    }

    public String getRandomText(Random random) {
        if (text == null || text.length == 0) return "";
        int i = random.nextInt(text.length);
        return text[i];
    }
}
