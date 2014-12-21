package net.yura.domination.lobby.server;

import java.util.List;

/**
 * @author Yura Mamyrin
 */
public interface GameSettingsMXBean {

    void setAIWait(int a);
    int getAIWait();

    void saveGame(int id) throws Exception;
    void saveGameLog(int id) throws Exception;
    List<Integer> markFinished() throws Exception;
}
