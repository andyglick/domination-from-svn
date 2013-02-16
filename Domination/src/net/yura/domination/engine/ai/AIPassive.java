package net.yura.domination.engine.ai;

import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;

public class AIPassive implements AI {
	
	protected BaseAIStrategy strategy = new BaseAIStrategy();

    @Override
    public int getType() {
    	return Player.PLAYER_AI_CRAP;
    }
    
    @Override
    public String getCommand() {
    	return "crap";
    }
    
    @Override
    public AIStrategy getStrategy(RiskGame game) {
    	BaseAIStrategy strat = getStrategyDirect(game);
    	strat.game = game;
    	strat.player = game.getCurrentPlayer();
    	return strat;
    }

	protected BaseAIStrategy getStrategyDirect(RiskGame game) {
		return strategy;
	}
	
}
