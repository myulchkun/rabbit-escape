package rabbitescape.engine.behaviours;

import static rabbitescape.engine.Block.Type.*;
import static rabbitescape.engine.ChangeDescription.State.*;
import static rabbitescape.engine.Direction.*;
import static rabbitescape.engine.Token.Type.*;

import java.util.Map;

import rabbitescape.engine.*;
import rabbitescape.engine.ChangeDescription.State;

public class Climbing extends Behaviour
{
    boolean hasAbility = false;
    public boolean abilityActive = false;

    @Override
    public void cancel()
    {
        abilityActive = false;
    }

    @Override
    public boolean checkTriggered( Rabbit rabbit, World world )
    {
        Token token = world.getTokenAt( rabbit.x, rabbit.y );
        if ( !hasAbility && token != null && token.type == climb )
        {
            world.changes.removeToken( token );
            return true;
        }
        return false;
    }

    @Override
    public State newState( Rabbit rabbit, World world, boolean triggered )
    {
        if ( triggered )
        {
            hasAbility = true;
        }

        if ( !hasAbility )
        {
            return null;
        }

        BehaviourTools t = new BehaviourTools( rabbit );

        switch ( rabbit.state )
        {
            case RABBIT_CLIMBING_RIGHT_START:
            case RABBIT_CLIMBING_LEFT_START:
                return newStateStart( t, rabbit, world );
            case RABBIT_CLIMBING_RIGHT_CONTINUE_1:
            case RABBIT_CLIMBING_LEFT_CONTINUE_1:
                return newStateCont1( t );
            case RABBIT_CLIMBING_RIGHT_CONTINUE_2:
            case RABBIT_CLIMBING_LEFT_CONTINUE_2:
                return newStateCont2( t, rabbit, world );
            default:
                return newStateNotClimbing( t, rabbit, world );
        }
    }

    private State newStateStart( BehaviourTools t, Rabbit rabbit, World world )
    {
        Block endBlock = world.getBlockAt( destX( rabbit ), rabbit.y - 1 );

        if ( isWall( rabbit, endBlock ) )
        {
            return t.rl(
                RABBIT_CLIMBING_RIGHT_CONTINUE_2,
                RABBIT_CLIMBING_LEFT_CONTINUE_2
            );
        }
        else
        {
            return t.rl(
                RABBIT_CLIMBING_RIGHT_END,
                RABBIT_CLIMBING_LEFT_END
            );
        }
    }

    private State newStateCont1( BehaviourTools t )
    {
        return t.rl(
            RABBIT_CLIMBING_RIGHT_CONTINUE_2,
            RABBIT_CLIMBING_LEFT_CONTINUE_2
        );
    }

    private State newStateCont2( BehaviourTools t, Rabbit rabbit, World world )
    {
        Block aboveBlock = world.getBlockAt( rabbit.x, rabbit.y - 1 );

        if ( isRoof( aboveBlock ) )
        {
            abilityActive = false;
            return t.rl(
                RABBIT_CLIMBING_RIGHT_BANG_HEAD,
                RABBIT_CLIMBING_LEFT_BANG_HEAD
            );
        }

        Block endBlock = world.getBlockAt( destX( rabbit ), rabbit.y - 1 );

        if ( isWall( rabbit, endBlock ) )
        {
            return t.rl(
                RABBIT_CLIMBING_RIGHT_CONTINUE_1,
                RABBIT_CLIMBING_LEFT_CONTINUE_1
            );
        }
        else
        {
            return t.rl(
                RABBIT_CLIMBING_RIGHT_END,
                RABBIT_CLIMBING_LEFT_END
            );
        }
    }

    private State newStateNotClimbing(
        BehaviourTools t, Rabbit rabbit, World world )
    {
        int nextX = destX( rabbit );
        int nextY = destY( rabbit, world );
        Block nextBlock = world.getBlockAt( nextX, nextY );
        Block aboveBlock = world.getBlockAt( rabbit.x, rabbit.y - 1 );

        if ( !isRoof( aboveBlock ) && isWall( rabbit, nextBlock ) )
        {
            return t.rl(
                RABBIT_CLIMBING_RIGHT_START,
                RABBIT_CLIMBING_LEFT_START
            );
        }

        return null;
    }

    private boolean isRoof( Block block )
    {
        return
        (
               block != null
            && (
                   block.type == solid_flat
                || block.type == solid_up_left
                || block.type == solid_up_right
            )
        );
    }

    private boolean isWall( Rabbit rabbit, Block block )
    {
        return
        (
               block != null
            && (
                block.type == solid_flat
                || (
                       block.riseDir() == opposite( rabbit.dir )
                    && isSolid( block )
                )
            )
        );
    }

    private boolean isSolid( Block block )
    {
        return (
               block.type == solid_flat
            || block.type == solid_up_left
            || block.type == solid_up_right
        );
    }

    private int destX( Rabbit rabbit )
    {
        return ( rabbit.dir == RIGHT ) ? rabbit.x + 1 : rabbit.x - 1;
    }

    private int destY( Rabbit rabbit, World world )
    {
        if ( goingUpSlope( rabbit, world ) )
        {
            return rabbit.y - 1;
        }
        else
        {
            return rabbit.y;
        }
    }

    private boolean goingUpSlope( Rabbit rabbit, World world )
    {
        if ( rabbit.onSlope )
        {
            if( isUpSlope( rabbit, world ) )
            {
                return true;
            }
        }
        return false;
    }

    private boolean isUpSlope( Rabbit rabbit, World world )
    {
        Block thisBlock = world.getBlockAt( rabbit.x, rabbit.y );

        return (
            thisBlock != null && thisBlock.riseDir() == rabbit.dir
        );
    }

    @Override
    public boolean behave( World world, Rabbit rabbit, State state )
    {
        switch ( state )
        {
            case RABBIT_CLIMBING_RIGHT_START:
            case RABBIT_CLIMBING_LEFT_START:
            {
                abilityActive = true;
                return true;
            }
            case RABBIT_CLIMBING_RIGHT_END:
            case RABBIT_CLIMBING_LEFT_END:
            {
                rabbit.x = destX( rabbit );
                --rabbit.y;
                if ( isUpSlope( rabbit, world ) )
                {
                    rabbit.onSlope = true;
                }
                abilityActive = false;
                return true;
            }
            case RABBIT_CLIMBING_RIGHT_CONTINUE_1:
            case RABBIT_CLIMBING_LEFT_CONTINUE_1:
            {
                return true;
            }
            case RABBIT_CLIMBING_RIGHT_CONTINUE_2:
            case RABBIT_CLIMBING_LEFT_CONTINUE_2:
            {
                --rabbit.y;
                return true;
            }
            case RABBIT_CLIMBING_RIGHT_BANG_HEAD:
            case RABBIT_CLIMBING_LEFT_BANG_HEAD:
            {
                rabbit.dir = opposite( rabbit.dir );
                return true;
            }
            default:
            {
                return false;
            }
        }
    }

    @Override
    public void saveState( Map<String, String> saveState )
    {
        BehaviourState.addToStateIfTrue(
            saveState, "Climbing.hasAbility", hasAbility
        );

        BehaviourState.addToStateIfTrue(
            saveState, "Climbing.abilityActive", abilityActive
        );
    }

    @Override
    public void restoreFromState( Map<String, String> saveState )
    {
        hasAbility = BehaviourState.restoreFromState(
            saveState, "Climbing.hasAbility", hasAbility
        );

        abilityActive = BehaviourState.restoreFromState(
            saveState, "Climbing.abilityActive", abilityActive
        );
    }
}