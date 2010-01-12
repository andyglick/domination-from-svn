// Yura Mamyrin

package risk.engine.ai;

import risk.engine.*;
import risk.engine.core.*;

import java.util.*;

/**
 * <p> Class for AIEasyPlayer </p>
 * @author Yura Mamyrin
 */

public class AIEasy extends AICrap {

    protected class Attack {
	Country source;
	Country destination;

	public Attack(Country s, Country d){
	    source=s;
	    destination=d;
	}
	public String toString(){
	    if (source == null || destination == null) { return ""; }
	    return "attack " + source.getColor() + " " + destination.getColor();
	}

    }

    public String getPlaceArmies() {

		if ( game.NoEmptyCountries()==false ) {
		    return "autoplace";
		}
		else {
		    Vector t = player.getTerritoriesOwned();
		    Vector n;
		    String name=null;
			name = findAttackableTerritory(player);
			if ( name == null ) {
			return "placearmies " + ((Country)t.elementAt(0)).getColor() +" "+player.getExtraArmies()  ;
		    }

		    if (game.getSetup() ) {
			return "placearmies " + name +" "+player.getExtraArmies() ;
		    }

		    return "placearmies " + name +" 1";

		}

    }

    public String getAttack() {
	//Vector t = player.getTerritoriesOwned();
	Vector outputs = new Vector();
	Attack move;

	/*  // Extract method: findAttackableNeighbors() 
	Vector n;
	for (int a=0; a< t.size() ; a++) {
	    if ( ((Country)t.elementAt(a)).getArmies() > 1 ) {
		n = ((Country)t.elementAt(a)).getNeighbours();
		for (int b=0; b< n.size() ; b++) {
		    if ( ((Country)n.elementAt(b)).getOwner() != player ) {
			outputs.add( "attack " + ((Country)t.elementAt(a)).getColor() + " " + ((Country)n.elementAt(b)).getColor() );
		    }
		}
	    }
	}  */
	outputs = findAttackableNeighbors(player.getTerritoriesOwned(),0);
	if (outputs.size() > 0) {
		move = (Attack) outputs.elementAt( (int)Math.round(Math.random() * (outputs.size()-1) ) );
		//System.out.println(player.getName() + ": "+ move.toString());    //TESTING
		return move.toString();
		//return (String)outputs.elementAt( (int)Math.round(Math.random() * (outputs.size()-1) ) );
	}
	return "endattack";
    }



    public String getRoll() {
	    int n=((Country)game.getAttacker()).getArmies() - 1;
	    if (n > 3) {
		    return "roll "+3;
	    }
	    return "roll "+n;
    }


    /******************
     * Helper Methods *
     ******************/

    /************
     * @name findAttackableNeighbors
     * @param t Vector of teritories
     * @param ratio - threshold of attack to defence armies to filter out
     * @return a Vector of possible attacks for a given list of territories
     * 	where the ratio of source/target armies is above ratio
     **************/
    public Vector findAttackableNeighbors(Vector t, double ratio){
	Vector output = new Vector();
	Vector n=new Vector();
    	Country source,target;
	if (ratio<0) { ratio = 0;}
	for (int a=0; a< t.size() ; a++) {
	    source=(Country)t.elementAt(a);
	    if ( source.getOwner() == player && source.getArmies() > 1 ) {
		n = source.getNeighbours();
		for (int b=0; b< n.size() ; b++) {
		    target=(Country)n.elementAt(b);
		    if ( target.getOwner() != player && 
			( (double)(source.getArmies()/target.getArmies()) > ratio) 
		      	) {     // simplify logic
			//output.add( "attack " + source.getColor() + " " + target.getColor() );
			output.add(new Attack(source,target));
		    }
		}
	    }
	}
	return output;
    }

    /************
     * @name findAttackableNeighbors
     * @param t Vector of teritories
     * @param ratio - threshold of attack to defence armies to filter out
     * @return a Vector of possible attacks for a given list of territories
     * 	where the ratio of source/target armies is above ratio
     **************/
    public Vector getPossibleAttacks(Vector t){
	Vector output = new Vector();
	Vector n=new Vector();
    	Country source,target;
	for (int a=0; a< t.size() ; a++) {
	    source=(Country)t.elementAt(a);
	    if ( source.getOwner() == player && source.getArmies() > 1 ) {
		n = source.getNeighbours();
		for (int b=0; b< n.size() ; b++) {
		    target=(Country)n.elementAt(b);
		    if ( target.getOwner() != player ) {     // simplify logic
			//output.add( "attack " + source.getColor() + " " + target.getColor() );
			output.add(new Attack(source,target));
		    }
		}
	    }
	}
	return output;
    }

    /*******************
     * @name filterAttacks
     * @param options - Vector of Attacks
     * @param advantage - how much of an absolute advantage to have
     * @return Vector of attacks with specified advantage
     *******************/

    public Vector filterAttacks(Vector options, int advantage){
	Attack temp = null;
	Vector moves = new Vector();
	for(int j=0; j<options.size(); j++){
		temp=(Attack)options.get(j);
		if ( ( ((Country)temp.source).getArmies() - ((Country)temp.destination).getArmies()) > advantage) {
			moves.add(temp);
		}
	}
	return moves;
    }

}
