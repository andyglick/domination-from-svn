package net.yura.domination.engine;

/**
 * @author Yura Mamyrin
 */
public interface OnlineRisk {

    public void sendUserCommand(String mtemp);
    public void sendGameCommand(String mtemp);

    public void closeGame();

    public boolean isThisMe(String name);

}
