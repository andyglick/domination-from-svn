// Yura Mamyrin

package net.yura.domination.engine.ai;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Player;

/**
 * <p> Class for AIEasyPlayer </p>
 * 
 * Uses a simplistic attack/reenforcement approach -
 *   look across all territories (in random order) and try to place/attack the most enemy positions as possible.
 * 
 * This AI does not use any strategic logic.
 * 
 * @author Yura Mamyrin
 */
public class AIEasy extends AICrap {

    
    /**
     * Finds all countries that can be attacked from.
     * @param p player object
     * @param attack true if this is durning attack, which requires the territority to have 2 or more armies
     * @return a Vector of countries, never null
     */
    public List findAttackableTerritories(Player p, boolean attack) {
    	List countries = p.getTerritoriesOwned();
    	List result = new ArrayList();
    	for (int i=0; i<countries.size(); i++) {
    		Country country = (Country)countries.get(i);
    		if ((!attack || country.getArmies() > 1) && !ownsNeighbours(p, country)) {
				result.add(country);
    		}
    	}
    	return result;
    }
    
    
    
    public String getPlaceArmies() {
		if ( game.NoEmptyCountries()==false ) {
		    return "autoplace";
		}
	    List t = player.getTerritoriesOwned();
	    List n = findAttackableTerritories(player, false);
	    List copy = new ArrayList(n);
	    Country c = null;
	    if (n.isEmpty() || t.size() == 1) {
	    	c = (Country)t.get(0);
		    return getPlaceCommand(c, player.getExtraArmies());
	    }
	    if (n.size() == 1) {
	    	c = (Country)n.get(0);
	    	return getPlaceCommand(c, player.getExtraArmies());
	    }
	    HashSet toTake = new HashSet();
	    Country fallback = null;
	    int additional = 1;
		while (!n.isEmpty()) {
			c = (Country)n.remove( r.nextInt(n.size()) );
			List cn = c.getNeighbours();
			for (int i = 0; i < cn.size(); i++) {
				Country other = (Country)cn.get(i);
				if (other.getOwner() == player || toTake.contains(other)) {
					continue;
				}
				int diff = c.getArmies() - 2 - (3*other.getArmies()/2 + other.getArmies()%2);
				if (diff >= 0) {
					toTake.add(other);
					continue;
				}
				if (-diff <= player.getExtraArmies()) {
					return getPlaceCommand(c, -diff);
				}
				if (fallback == null) {
					fallback = c;
					additional = Math.max(1, -diff);
				}
			}
		}
		if (fallback == null) {
			fallback = randomCountry(copy);
		}
		return getPlaceCommand(fallback, additional);
    }

	public String getAttack() {
		List v = findAttackableTerritories(player, true);
		
		while (!v.isEmpty()) {
			Country c = (Country)v.remove( r.nextInt(v.size()) );
			List n = c.getNeighbours();
			for (int i = 0; i < n.size(); i++) {
				Country other = (Country)n.get(i);
				if (other.getOwner() != player && c.getArmies() - 1 > other.getArmies()) {
					return "attack " + c.getColor() + " " + other.getColor();
				}
			}
		}
		
		return "endattack";
	}

	/**
	 * Rolls the max as long as the attack is favorable (attack is greater than defense), but will keep rolling if we're breaking a continent or eliminating.
	 */
    public String getRoll() {
	    int n=game.getAttacker().getArmies() - 1;
	    if (n < 3 && n <= game.getDefender().getArmies() && game.getDefender().getContinent().getOwner() == null && game.getDefender().getOwner().getTerritoriesOwned().size() != 1) {
	    	return "retreat";
	    }
	    return "roll " + Math.min(3, n);
    }
    
    @Override
    public String getBattleWon() {
    	if (player.getCapital() == game.getAttacker()) {
    		return "move " + game.getMustMove(); 
    	}
    	return super.getBattleWon();
    }

    public int tradeCombinationsToScan() {
    	return 10000;
    }
    
	protected String getPlaceCommand(Country country, int armies) {
		return "placearmies " + country.getColor() + " " + (!game.getSetup()?1:Math.max(1, Math.min(player.getExtraArmies(), armies)));
	}

}
