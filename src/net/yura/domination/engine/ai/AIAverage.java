// Yura Mamyrin

package net.yura.domination.engine.ai;

import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;

/**
 * <p> Loader class for an Easy player </p>
 * 
 * @author Yura Mamyrin
 */
public class AIAverage extends AIEasy {
	
	private AIHardMission mission = new AIHardMission();
	private AIHardCapital capital = new AIHardCapital();

	public AIAverage() {
		this.strategy = new AIHardDomination();
	}
    
    public int getType() {
        return Player.PLAYER_AI_AVERAGE;
    }

    public String getCommand() {
        return "average";
    }
    
    @Override
    protected BaseAIStrategy getStrategyDirect(RiskGame game) {
    	if (game.getGameMode() == RiskGame.MODE_CAPITAL) {
    		return capital;
    	} else if (game.getGameMode() == RiskGame.MODE_SECRET_MISSION) {
    		return mission;
    	}
    	return super.getStrategyDirect(game);
    }
    
}
