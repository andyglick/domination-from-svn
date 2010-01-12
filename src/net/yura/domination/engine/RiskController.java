// Yura Mamyrin, Group D

package risk.engine;

import java.util.Vector;

/**
 * <p> Risk Controller </p>
 * @author Yura Mamyrin
 */

public class RiskController {

    private Vector obs;

    public RiskController() {
	obs = new Vector();
    }

    /**
     * Adds an observer to the set of observers for this object, provided 
     * that it is not the same as some observer already in the set. 
     * The order in which notifications will be delivered to multiple 
     * observers is not specified. See the class comment.
     *
     * @param   o   an observer to be added.
     * @throws NullPointerException   if the parameter o is null.
     */
    public synchronized void addListener(RiskListener o) {
        if (o == null)
            throw new NullPointerException();
	if (!obs.contains(o)) {
	    obs.addElement(o);
	}
    }

    public int countListeners() {

	return obs.size();

    }

    /**
     * Deletes an observer from the set of observers of this object. 
     * Passing <CODE>null</CODE> to this method will have no effect.
     * @param   o   the observer to be deleted.
     */
    public synchronized void deleteListener(RiskListener o) {
        obs.removeElement(o);
    }


    public void sendMessage(String output, boolean a, boolean b) {

        Object[] arrLocal;

	synchronized (this) {
            arrLocal = obs.toArray();
        }

        for (int i = arrLocal.length-1; i>=0; i--)
            ((RiskListener)arrLocal[i]).sendMessage(output,a,b);
    }

    public void needInput(int s) {

        Object[] arrLocal;

	synchronized (this) {
            arrLocal = obs.toArray();
        }

        for (int i = arrLocal.length-1; i>=0; i--)
            ((RiskListener)arrLocal[i]).needInput(s);

    }

    public void noInput() {

        Object[] arrLocal;

	synchronized (this) {
            arrLocal = obs.toArray();
        }

        for (int i = arrLocal.length-1; i>=0; i--)
            ((RiskListener)arrLocal[i]).noInput();

    }

    public void setGameStatus(String state) {

        Object[] arrLocal;

	synchronized (this) {
            arrLocal = obs.toArray();
        }

        for (int i = arrLocal.length-1; i>=0; i--)
            ((RiskListener)arrLocal[i]).setGameStatus(state);

    }

    public void newGame(boolean t) {

        Object[] arrLocal;

	synchronized (this) {
            arrLocal = obs.toArray();
        }

        for (int i = arrLocal.length-1; i>=0; i--)
            ((RiskListener)arrLocal[i]).newGame(t);

    }

    public void startGame(boolean s) {

        Object[] arrLocal;

	synchronized (this) {
            arrLocal = obs.toArray();
        }

        for (int i = arrLocal.length-1; i>=0; i--)
            ((RiskListener)arrLocal[i]).startGame(s);

    }

    public void closeGame() {

        Object[] arrLocal;

	synchronized (this) {
            arrLocal = obs.toArray();
        }

        for (int i = arrLocal.length-1; i>=0; i--)
            ((RiskListener)arrLocal[i]).closeGame();

    }

    public void setSlider(int min, int c1num, int c2num) {

        Object[] arrLocal;

	synchronized (this) {
            arrLocal = obs.toArray();
        }

        for (int i = arrLocal.length-1; i>=0; i--)
            ((RiskListener)arrLocal[i]).setSlider(min,c1num,c2num);

    }

    public void armiesLeft(int l, boolean s) {

        Object[] arrLocal;

	synchronized (this) {
            arrLocal = obs.toArray();
        }

        for (int i = arrLocal.length-1; i>=0; i--)
            ((RiskListener)arrLocal[i]).armiesLeft(l,s);

    }

    public void showDice(int n, boolean w) {

        Object[] arrLocal;

	synchronized (this) {
            arrLocal = obs.toArray();
        }

        for (int i = arrLocal.length-1; i>=0; i--)
            ((RiskListener)arrLocal[i]).showDice(n,w);

    }

    public void showMapPic(java.awt.Image p) {

        Object[] arrLocal;

	synchronized (this) {
            arrLocal = obs.toArray();
        }

        for (int i = arrLocal.length-1; i>=0; i--)
            ((RiskListener)arrLocal[i]).showMapPic(p);

    }

    public void showCardsFile(String c, boolean m) {

        Object[] arrLocal;

	synchronized (this) {
            arrLocal = obs.toArray();
        }

        for (int i = arrLocal.length-1; i>=0; i--)
            ((RiskListener)arrLocal[i]).showCardsFile(c, m);

    }

    public void serverState(boolean s) {

        Object[] arrLocal;

	synchronized (this) {
            arrLocal = obs.toArray();
        }

        for (int i = arrLocal.length-1; i>=0; i--)
            ((RiskListener)arrLocal[i]).serverState(s);

    }

    public void openBattle(int c1num, int c2num) {

        Object[] arrLocal;

	synchronized (this) {
            arrLocal = obs.toArray();
        }

        for (int i = arrLocal.length-1; i>=0; i--)
            ((RiskListener)arrLocal[i]).openBattle(c1num,c2num);

    }

    public void closeBattle() {

        Object[] arrLocal;

	synchronized (this) {
            arrLocal = obs.toArray();
        }

        for (int i = arrLocal.length-1; i>=0; i--)
            ((RiskListener)arrLocal[i]).closeBattle();

    }

    public void addPlayer(int type, String name, java.awt.Color color, String ip) {

        Object[] arrLocal;

	synchronized (this) {
            arrLocal = obs.toArray();
        }

        for (int i = arrLocal.length-1; i>=0; i--)
            ((RiskListener)arrLocal[i]).addPlayer(type, name, color, ip);

    }

    public void delPlayer(String name) {

        Object[] arrLocal;

	synchronized (this) {
            arrLocal = obs.toArray();
        }

        for (int i = arrLocal.length-1; i>=0; i--)
            ((RiskListener)arrLocal[i]).delPlayer(name);

    }

    public void showDiceResults(int[] att, int[] def) {

        Object[] arrLocal;

	synchronized (this) {
            arrLocal = obs.toArray();
        }

        for (int i = arrLocal.length-1; i>=0; i--)
            ((RiskListener)arrLocal[i]).showDiceResults(att,def);

    }

    public void setNODAttacker(int n) {

        Object[] arrLocal;

	synchronized (this) {
            arrLocal = obs.toArray();
        }

        for (int i = arrLocal.length-1; i>=0; i--)
            ((RiskListener)arrLocal[i]).setNODAttacker(n);

    }

    public void setNODDefender(int n) {

        Object[] arrLocal;

	synchronized (this) {
            arrLocal = obs.toArray();
        }

        for (int i = arrLocal.length-1; i>=0; i--)
            ((RiskListener)arrLocal[i]).setNODDefender(n);

    }

    public void sendDebug(String a) {

        Object[] arrLocal;

	synchronized (this) {
            arrLocal = obs.toArray();
        }

        for (int i = arrLocal.length-1; i>=0; i--)
            ((RiskListener)arrLocal[i]).sendDebug(a);

    }

    public void showMessageDialog(String a) {

        Object[] arrLocal;

	synchronized (this) {
            arrLocal = obs.toArray();
        }

        for (int i = arrLocal.length-1; i>=0; i--)
            ((RiskListener)arrLocal[i]).showMessageDialog(a);

    }

}
