package net.yura.domination.engine.ai;

import net.yura.domination.engine.core.RiskGame;

/**
 * @author Yura Mamyrin
 */
public interface AI {

    int getType();
    String getCommand();
    
    AIStrategy getStrategy(RiskGame game);
}
