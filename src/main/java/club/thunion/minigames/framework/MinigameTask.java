package club.thunion.minigames.framework;

import java.util.Properties;

public abstract class MinigameTask<L extends MinigameLogic> {
    private final int priority;
    private boolean active = true;
    private boolean removed = false;
    private final Class<L> compatibleLogicClass;

    public void tryExecute(MinigameLogic logic, int tickCounter) {
        if (compatibleLogicClass.isInstance(logic)) {
            this.execute(compatibleLogicClass.cast(logic), tickCounter);
        }
    }

    public abstract void execute(L logic, int tickCounter);

    protected MinigameTask(int priority, Class<L> compatibleLogicClass) {
        this.priority = priority;
        this.compatibleLogicClass = compatibleLogicClass;
    }

    public int getPriority() {
        return priority;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public boolean isRemoved() {
        return removed;
    }

    public Class<L> getCompatibleLogicClass() {
        return compatibleLogicClass;
    }
}
