package net.yura.domination.engine.ai;

public interface AIStrategy {

    String getBattleWon();
    String getTacMove();
    String getTrade();
    String getPlaceArmies();
    String getAttack();
    String getRoll();
    String getCapital();
    String getAutoDefendString();
	
}
