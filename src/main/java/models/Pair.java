package models;

public class Pair {
    private final String id;
    private final long count;

    public Pair(String showId, long count) {
        this.id = showId;
        this.count = count;
    }

    public String getId() {
        return id;
    }

    public long getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "ShowIdWithDownloads{" +
                "showId='" + id + '\'' +
                ", count=" + count +
                '}';
    }
}
