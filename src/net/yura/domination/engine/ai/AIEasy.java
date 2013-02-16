// Yura Mamyrin

package net.yura.domination.engine.ai;

import net.yura.domination.engine.core.Player;

/**
 * <p> Loader class for an Easy player </p>
 * 
 * @author Yura Mamyrin
 */
public class AIEasy extends AIPassive {

	public AIEasy() {
		this.strategy = new AIHardDomination();
	}
    
    public int getType() {
        return Player.PLAYER_AI_EASY;
    }

    public String getCommand() {
        return "easy";
    }
    
}
