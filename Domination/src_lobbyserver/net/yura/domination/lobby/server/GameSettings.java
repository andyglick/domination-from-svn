package net.yura.domination.lobby.server;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.ai.AIManager;
import net.yura.domination.engine.core.RiskGame;

/**
 * @author Yura Mamyrin
 */
public class GameSettings implements GameSettingsMXBean {

    @Override
    public void setAIWait(int a) {
        AIManager.setWait(a);
    }

    @Override
    public int getAIWait() {
        return AIManager.getWait();
    }

    ServerRisk getServerGame(int id) {
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
        for (Thread thread: threadArray) {
            if (thread instanceof ServerRisk) {
                ServerRisk risk = (ServerRisk) thread;
                if (risk.sgr.getId() == id) {
                    return risk;
                }
            }
        }
        throw new IllegalArgumentException("game "+id+" not found");
    }

    @Override
    public void saveGame(int id) throws Exception {
        ServerRisk risk = getServerGame(id);
        File file = new File("game"+id+".save");
        FileOutputStream fout = new FileOutputStream(file);
        risk.getGame().saveGame(fout);
        fout.close();
    }

    @Override
    public void saveGameLog(int id) throws Exception {
        ServerRisk risk = getServerGame(id);
        File file = new File("game"+id+".log");
        RiskUtil.saveGameLog(file, risk.getGame());
    }

    @Override
    public List<Integer> markFinished() throws Exception {

        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
        List<Integer> games = new ArrayList();
        for (Thread thread: threadArray) {
            if (thread instanceof ServerRisk) {
                ServerRisk risk =(ServerRisk)thread;

                if (!risk.sgr.isFinished() && risk.getGame().getState() == RiskGame.STATE_GAME_OVER) {
                    risk.sgr.gameFinished("ERROR");
                    games.add(risk.sgr.getId());
                }
            }
        }
        return games;
    }
}
