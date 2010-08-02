package net.yura.domination.mobile.flashgui;

import net.yura.domination.engine.RiskListener;
import net.yura.domination.engine.core.RiskGame;
import net.yura.mobile.gui.components.OptionPane;
import net.yura.mobile.logging.Logger;

public class MiniFlashRiskAdapter implements RiskListener {

    private MiniFlashGUI mainFrame;

    public MiniFlashRiskAdapter(MiniFlashGUI mainFrame) {
        this.mainFrame = mainFrame;
    }

    @Override
    public void newGame(boolean t) {
        mainFrame.openNewGame(t);
    }

    @Override
    public void closeGame() {
        mainFrame.openMainMneu();
    }

    @Override
    public void sendMessage(String output, boolean redrawNeeded, boolean repaintNeeded) {
        Logger.debug("Game: "+output);
        mainFrame.mapRedrawRepaint(redrawNeeded,repaintNeeded);
    }

    // ======================= game setup =============================

    @Override
    public void addPlayer(int type, String name, int color, String ip) {
        mainFrame.updatePlayers();
    }

    @Override
    public void delPlayer(String name) {
        mainFrame.updatePlayers();
    }

    @Override
    public void showMapPic(RiskGame p) {
        // TODO Auto-generated method stub
    }

    @Override
    public void showCardsFile(String c, boolean m) {
        // TODO Auto-generated method stub
    }

    @Override
    public void startGame(boolean s) {
        mainFrame.startGame(s);
    }

    // ========================= in game ==============================

    int nod;
    @Override
    public void needInput(int s) {

        if (s == RiskGame.STATE_ROLLING) {

                battle.needInput(nod, true);

        }
        else if (s == RiskGame.STATE_DEFEND_YOURSELF) {

                battle.needInput(nod, false);

        }
        //else { // this will update the state in the gameframe

                mainFrame.needInput(s);

        //}

    }

    @Override
    public void noInput() {
        // TODO Auto-generated method stub
    }

    @Override
    public void armiesLeft(int l, boolean s) {
        // TODO Auto-generated method stub
    }

    BattleDialog battle;
    @Override
    public void openBattle(int c1num, int c2num) {

        if (battle == null) {
            battle = new BattleDialog(mainFrame);
            battle.setMaximum(true);
        }

        battle.setup(c1num, c2num);

        // TODO: move main map to centre on where battle is happening

        battle.setVisible(true);

    }

    @Override
    public void closeBattle() {
        battle.setVisible(false);
    }

    @Override
    public void sendDebug(String a) {

    }

    @Override
    public void serverState(boolean s) {

    }

    @Override
    public void setGameStatus(String state) {
        mainFrame.setGameStatus(state);
    }

    /**
     * Sets number of attackers
     * @param n number of attackers
     */
    public void setNODAttacker(int n) {
            if (battle.isVisible() ) {
                    battle.setNODAttacker(n);
            }
    }

    /**
     * Sets number of defenders
     * @param n number of defenders
     */
    public void setNODDefender(int n) {
            if (battle.isVisible() ) {
                    battle.setNODDefender(n);
            }
    }

    @Override
    public void setSlider(int min, int c1num, int c2num) {
        mainFrame.setupMove(min, c1num, c2num, false);
    }

    @Override
    public void showDice(int n, boolean w) {
        nod=n;
    }

    @Override
    public void showDiceResults(int[] att, int[] def) {
        if (battle.isVisible() ) {
                battle.showDiceResults(att, def);
        }
    }

    @Override
    public void showMessageDialog(String a) {
        OptionPane.showMessageDialog(null, a, "TODO: Title", OptionPane.INFORMATION_MESSAGE);
    }

}
