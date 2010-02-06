// Yura Mamyrin

package net.yura.domination.engine.ai;

import java.util.Random;
import java.util.Vector;
import net.yura.domination.engine.core.Card;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;

/**
 * <p> Class for AIEasyPlayer </p>
 * @author Yura Mamyrin
 */

public class AICrap {

    protected Random r = new Random(); // this was always static

    protected RiskGame game;
    protected Player player;


    public String getBattleWon() {

	return "move all";

    }

    public String getTacMove() {

	return "nomove";

    }

    public String getTrade() {

	  Vector cards = player.getCards();

	  if (cards.size() >= 3 && !( game.getTradeCap()==true && cards.size() < 5 ) ) {

	    Card card1=null, card2=null, card3=null;

	    for (int a=0; a< cards.size() ; a++) {
		if (card1 != null && card2 != null && card3 != null) { break; }
		card1 = (Card)cards.elementAt(a);

		for (int b=(a+1); b< cards.size() ; b++) {
		    if (card1 != null && card2 != null && card3 != null) { break; }
		    card2 = (Card)cards.elementAt(b);

		    for (int c=(b+1); c< cards.size() ; c++) {
			if (card1 != null && card2 != null && card3 != null) { break; }
			card3 = (Card)cards.elementAt(c);

			if ( game.checkTrade(card1, card2, card3) ) { break; }
			else { card3=null; }

		    }
		}
	    }


	    if (card3 != null) {

		String output = "trade ";

		  if (card1.getName().equals("wildcard")) { output = output + card1.getName(); }
		  else { output = output + ((Country)card1.getCountry()).getColor(); }

		output = output+" ";

		  if (card2.getName().equals("wildcard")) { output = output + card2.getName(); }
		  else { output = output + ((Country)card2.getCountry()).getColor(); }

		output = output+" ";

		  if (card3.getName().equals("wildcard")) { output = output + card3.getName(); }
		  else { output = output + ((Country)card3.getCountry()).getColor(); }

		return output;

	    }

	    return "endtrade";
	  }

	  return "endtrade";

    }

    public String getPlaceArmies() {

		if ( game.NoEmptyCountries()==false ) {
		    return "autoplace";
		}
		else {
		    Vector t = player.getTerritoriesOwned();
		    Vector n;
		    String name=null;
		    Random rand = new Random();
//			if (game.getSetup())
//				return "placearmies " + ((Country)t.elementAt(rand.nextInt(t.size()))).getColor() +" "+player.getExtraArmies();
//			else 
				return "placearmies " + ((Country)t.elementAt(rand.nextInt(t.size()))).getColor() +" 1";
		}

    }

    public String getAttack() {

	return "endattack";

    }

    public String getRoll() {

	return "retreat";

    }

    public String getCapital() {

	    Vector t = player.getTerritoriesOwned();
	    return "capital " + ((Country)t.elementAt( r.nextInt(t.size()) )).getColor();

    }


	public String getAutoDefendString() {

	    int n=((Country)game.getDefender()).getArmies();

            if (n > game.getMaxDefendDice()) {
                return "roll "+game.getMaxDefendDice();
            }

	    return "roll "+n;
	}

    /**
     * Attempts to find the first territory that can be used to attack from
     * @param p player object
     * @return Sring name is a move to attack from any space they can (that has less than 500 armies)
     * else returns null
     */
    public String findAttackableTerritory(Player p) {
    	Vector countries = p.getTerritoriesOwned();
    	
    	for (int i=0; i<countries.size(); i++) {
    		Vector neighbors = ((Country)countries.elementAt(i)).getNeighbours();
    		for (int j=0; j<neighbors.size(); j++) {
    			if (((Country)neighbors.elementAt(j)).getOwner() != p) {
    				if ((p.getCapital() != null && ((Country)countries.elementAt(i)).getColor() != p.getCapital().getColor()) || p.getCapital() == null)
    					return ((Country)countries.elementAt(i)).getColor()+"";
    			}
    		}
    	}
    	
    	return null;
    }

}
