// Yura Mamyrin

package net.yura.domination.engine.ai;

import net.yura.domination.engine.core.Player;

/**
 * @author Steven Hawkins
 */
public class AIHard extends AbstractAI {

    public int getType() {
        return Player.PLAYER_AI_HARD;
    }

    public String getCommand() {
        return "hard";
    }

}
