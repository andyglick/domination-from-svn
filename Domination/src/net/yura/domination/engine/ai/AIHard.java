package net.yura.domination.engine.ai;

import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;

/**
 * @author Yura Mamyrin
 */
public class AIHard implements AI {

    public int getType() {
        return Player.PLAYER_AI_HARD;
    }

    public String getCommand() {
        return "hard";
    }

    public void setGame(RiskGame game) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getBattleWon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getTacMove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getTrade() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getPlaceArmies() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getAttack() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getRoll() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getCapital() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getAutoDefendString() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
