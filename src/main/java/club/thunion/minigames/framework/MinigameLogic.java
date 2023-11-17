package club.thunion.minigames.framework;

import java.util.*;

public class MinigameLogic {
    private int tickCounter = 0;
    private final TreeSet<MinigameTask<?>> taskSet = new TreeSet<>(Comparator.comparingInt(MinigameTask::getPriority));
    private final Map<String, MinigameTask<?>> taskRegistry = new HashMap<>();

    public MinigameLogic(Properties properties) {
    }

    public MinigameTask<?> getTask(String name) {
        return taskRegistry.get(name);
    }

    public boolean registerTask(String name, MinigameTask<?> task) {
        if (!taskRegistry.containsKey(name)) {
            taskRegistry.put(name, task);
            taskSet.add(task);
            return true;
        }
        return false;
    }

    public boolean markTaskForRemoval(String name) {
        MinigameTask<?> task = taskRegistry.get(name);
        if (task == null) return false;
        task.setRemoved(true);
        return true;
    }

    public boolean markTaskForRemoval(MinigameTask<?> task) {
        if (taskSet.contains(task)) {
            task.setRemoved(true);
            return true;
        }
        return false;
    }

    public TreeSet<MinigameTask<?>> getTaskSet() {
        return taskSet;
    }

    public void tick() {
        for (MinigameTask<?> task: taskSet) {
            if (task.isRemoved()) continue;
            task.tryExecute(this, tickCounter);
        }
        MinigameTask<?> task;
        for (Iterator<MinigameTask<?>> it = taskRegistry.values().iterator(); it.hasNext();) {
            task = it.next();
            if (task.isRemoved()) {
                it.remove();
                taskSet.remove(task);
            } else if (task.isActive()) {
                task.tryExecute(this, tickCounter);
            }
        }
        tickCounter ++;
    }
}
