package club.thunion.minigames.util;

import java.util.LinkedList;

public class Milestone {

    private final LinkedList<KeyPoint> keyPoints = new LinkedList<>();

    private int progress = 0;

    public Milestone() {

    }

    public Milestone addKeyPoint(int timing, Runnable operation) {
        KeyPoint kp = new KeyPoint(timing, operation);
        if (keyPoints.isEmpty()) {
            keyPoints.add(kp);
        } else {
            int index = 0;
            for (var k : keyPoints) {
                if (k.timing > timing) {
                    keyPoints.add(index, kp);
                    return this;
                }
                index++;
            }
            keyPoints.add(index, kp);
        }
        return this;
    }

    public void update(int newProgress) {
        if (newProgress > progress) {
            progress = newProgress;
            updateKeyPoints();
        }
    }

    private void updateKeyPoints() {
        KeyPoint kp;
        for (var it = keyPoints.iterator(); it.hasNext();) {
            kp = it.next();
            if (progress >= kp.timing) {
                kp.operation.run();
                it.remove();
            } else {
                break;
            }
        }
    }

    public record KeyPoint(int timing, Runnable operation) { }
}
