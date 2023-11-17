package club.thunion.minigames.sg.tasks;

import club.thunion.minigames.framework.MinigameTask;
import club.thunion.minigames.framework.item.CustomItemBehavior;
import club.thunion.minigames.framework.item.CustomItemRegistry;
import club.thunion.minigames.sg.SurvivalGameLogic;
import club.thunion.minigames.sg.item.*;

public class RegisterCustomItemsTask extends MinigameTask<SurvivalGameLogic> {
    private final int deregisterTick;

    public RegisterCustomItemsTask(int deregisterTick) {
        super(Integer.MIN_VALUE + 1, SurvivalGameLogic.class);
        this.deregisterTick = deregisterTick;
    }

    private void register(CustomItemBehavior... behaviors) {
        for (CustomItemBehavior behavior: behaviors) {
            CustomItemRegistry.registerCustomItem(this, behavior);
        }
    }

    @Override
    public void execute(SurvivalGameLogic logic, int tickCounter) {
        if (tickCounter == 0) {
            register(new DeathScythe(), new Fireball(), new IceBomb(), new SignalScreener(), new ThunderTrident(), new WitherBomb());
        }
        if (tickCounter == deregisterTick) {
            CustomItemRegistry.removeCustomItemsInGroup(this);
            this.setRemoved(true);
        }
    }
}
