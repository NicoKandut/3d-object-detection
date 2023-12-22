package nicok.thesis.performance;

import java.util.HashMap;

public class Timer {
    private final HashMap<String, Long> marks = new HashMap<>();

    public void addMark(String name) {
        marks.put(name, System.nanoTime());
    }

    public long getNanosBetween(String from, String to) {
        return marks.get(to) - marks.get(from);
    }

    public double getMillisBetween(String from, String to) {
        return ((double)(marks.get(to) - marks.get(from))/ 1000000);
    }
}
