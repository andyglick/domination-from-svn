package net.yura.domination.mobile.flashgui;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;
import net.yura.domination.engine.RiskListener;
import net.yura.domination.engine.core.RiskGame;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.gui.components.ProgressBar;
import net.yura.mobile.gui.components.Window;
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

    @Override
    public void needInput(int s) {
        mainFrame.needInput(s);
    }

    @Override
    public void noInput() {
        // TODO Auto-generated method stub
    }

    @Override
    public void armiesLeft(int l, boolean s) {
        // TODO Auto-generated method stub
    }

    Window battle;
    @Override
    public void openBattle(int c1num, int c2num) {

        if (battle == null) {
            battle = new Frame( mainFrame.resBundle.getProperty("battle.title") );
        }
        try {
            Image red_img = Image.createImage("/red_dice.png");
            Sprite red_dice = new Sprite(red_img, red_img.getWidth()/3, red_img.getHeight()/3 ); // 29x29

            Image blue_img = Image.createImage("/blue_dice.png");
            Sprite blue_dice = new Sprite(blue_img, blue_img.getWidth()/3, blue_img.getHeight()/3 ); // 29x29

            ProgressBar redbar = new ProgressBar(red_dice);
            ProgressBar bluebar = new ProgressBar(blue_dice);


        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        

    }

    @Override
    public void closeBattle() {
        // TODO Auto-generated method stub
    }

    @Override
    public void sendDebug(String a) {
        // TODO Auto-generated method stub
    }

    @Override
    public void serverState(boolean s) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setGameStatus(String state) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setNODAttacker(int n) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setNODDefender(int n) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setSlider(int min, int c1num, int c2num) {
        // TODO Auto-generated method stub
    }

    @Override
    public void showDice(int n, boolean w) {
        // TODO Auto-generated method stub
    }

    @Override
    public void showDiceResults(int[] att, int[] def) {
        // TODO Auto-generated method stub
    }

    @Override
    public void showMessageDialog(String a) {
        // TODO Auto-generated method stub
    }

}
