public class Course {
    private final int id;
    private final String name;
    private final int timeId;
    private final int roomId;

    public Course(int id, String name, int timeId, int roomId) {
        this.id = id;
        this.name = name;
        this.timeId = timeId;
        this.roomId = roomId;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getTimeId() { return timeId; }
    public int getRoomId() { return roomId; }

    @Override
    public String toString() {
        return id + ". " + name + " (time_id=" + timeId + ", room_id=" + roomId + ")";
    }
}
