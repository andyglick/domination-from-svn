package net.yura.domination.lobby.server;

import net.yura.domination.engine.ai.AIManager;

/**
 * @author Yura Mamyrin
 */
public class GameSettings implements GameSettingsMXBean {

    public void setAIWait(int a) {
        AIManager.setWait(a);
    }

    public int getAIWait() {
        return AIManager.getWait();
    }
    
}
