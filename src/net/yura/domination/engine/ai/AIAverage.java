// Yura Mamyrin

package net.yura.domination.engine.ai;

import net.yura.domination.engine.core.Player;

/**
 * @author Steven Hawkins
 */
public class AIAverage extends AbstractAI {

    public int getType() {
        return Player.PLAYER_AI_AVERAGE;
    }

    public String getCommand() {
        return "average";
    }

}
