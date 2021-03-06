package rabbitescape.engine.behaviours.climbing;

import rabbitescape.engine.BehaviourTools;
import rabbitescape.engine.Block;
import rabbitescape.engine.ChangeDescription.State;
import rabbitescape.engine.World;
import rabbitescape.engine.behaviours.Climbing;
import rabbitescape.engine.Rabbit;

import static rabbitescape.engine.ChangeDescription.State.RABBIT_CLIMBING_RIGHT_END;
import static rabbitescape.engine.Direction.RIGHT;

public class ClimbingRightEnd implements ClimbingInterFace {

    @Override
    public State getState() {
        return RABBIT_CLIMBING_RIGHT_END;
    }

    @Override
    public ClimbingInterFace newState(BehaviourTools t, Climbing climbing) {
        int nextX = t.nextX();
        int nextY = t.nextY();
        Block nextBlock = t.world.getBlockAt(nextX, nextY);
        Block aboveBlock = t.world.getBlockAt(t.rabbit.x, t.rabbit.y - 1);

        if (!t.isRoof(aboveBlock) && t.isWall(nextBlock)) {
            if (t.rabbit.dir == RIGHT) {
                return new ClimbingRightStart();
            } else {
                return new ClimbingLeftStart();
            }
        }

        return new NotClimbing();
    }

    @Override
    public boolean behave(World world, Rabbit rabbit, Climbing climbing) {
        BehaviourTools t = new BehaviourTools(rabbit, world);

        rabbit.x = t.nextX();
        --rabbit.y;
        if (t.hereIsUpSlope()) {
            rabbit.onSlope = true;
        }
        climbing.abilityActive = false;
        return true;
    }
}