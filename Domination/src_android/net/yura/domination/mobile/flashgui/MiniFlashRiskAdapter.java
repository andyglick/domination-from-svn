package net.yura.domination.mobile.flashgui;

import javax.microedition.lcdui.Image;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskListener;
import net.yura.domination.engine.core.RiskGame;
import net.yura.mobile.gui.components.OptionPane;
import net.yura.mobile.logging.Logger;

public class MiniFlashRiskAdapter implements RiskListener {

    private Risk myRisk;
    private MiniFlashGUI mainFrame;
    private GameActivity gameFrame;

    public MiniFlashRiskAdapter(Risk risk) {
        myRisk = risk;
        risk.addRiskListener( this );
    }

    
    public void openMainMenu() {
        if (mainFrame==null) {
            mainFrame = new MiniFlashGUI(myRisk);
        }
        mainFrame.openMainMenu();
    }
    
    @Override
    public void newGame(boolean t) {
        mainFrame.openNewGame(t);
    }

    @Override
    public void closeGame() {
        if (gameFrame!=null) {
            gameFrame.setVisible(false);
            gameFrame = null;
        }
        openMainMenu();
    }

    @Override
    public void sendMessage(String output, boolean redrawNeeded, boolean repaintNeeded) {
        Logger.debug("Game: "+output);
        if (gameFrame!=null) {
            gameFrame.mapRedrawRepaint(redrawNeeded,repaintNeeded);
        }
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
        mainFrame.showMapPic(p);
    }

    @Override
    public void showCardsFile(String c, boolean m) {
        mainFrame.showCardsFile(c,m);
    }
    
    @Override
    public void startGame(boolean s) {
        if (mainFrame!=null) {
            mainFrame.setVisible(false);
            mainFrame = null;
        }
        gameFrame = new GameActivity(myRisk);
        gameFrame.startGame(s);
    }

    // ========================= in game ==============================

    int nod;
    @Override
    public void needInput(int s) {

        switch(s) {
            case RiskGame.STATE_ROLLING:
                battle.needInput(nod, true);
                break;
            case RiskGame.STATE_DEFEND_YOURSELF:
                battle.needInput(nod, false);
                break;
            case RiskGame.STATE_BATTLE_WON:
                move.setVisible(true);
                break;
        }
        //else { // this will update the state in the gameframe

            if (gameFrame!=null) {
                    gameFrame.needInput(s);
            }
        //}

    }

    @Override
    public void noInput() {
        if (gameFrame!=null) {
            gameFrame.noInput();
        }
    }

    @Override
    public void armiesLeft(int extraArmies, boolean noEmptyCountries) {
        // not used, we use getters now to get this info
    }

    BattleDialog battle;
    @Override
    public void openBattle(int c1num, int c2num) {

        if (battle == null) {
            battle = new BattleDialog(myRisk);
        }

        Image c1img = gameFrame.pp.getCountryImage(c1num);
        Image c2img = gameFrame.pp.getCountryImage(c2num);
        
        battle.setup(c1num, c2num,c1img,c2img);

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
        if (gameFrame!=null) {
            gameFrame.setGameStatus(state);
        }
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

    MoveDialog move;
    @Override
    public void setSlider(int min, int c1num, int c2num) {
        if (move==null) {
            move = new MoveDialog(myRisk) {
                @Override
                public void setVisible(boolean b) {
                    super.setVisible(b);
                    if (!b) {
                        move=null;
                    }
                }
            };
        }
        
        Image c1img = gameFrame.pp.getCountryImage(c1num);
        Image c2img = gameFrame.pp.getCountryImage(c2num);
        
        move.setupMove(min, c1num, c2num, c1img, c2img, false);
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
        OptionPane.showMessageDialog(null, a, null, OptionPane.INFORMATION_MESSAGE);
    }

}
