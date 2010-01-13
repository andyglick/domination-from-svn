// Yura Mamyrin

package net.yura.domination.engine.core;

import java.io.Serializable;

/**
 * <p> Risk Player </p>
 * @author Yura Mamyrin
 */

public class Statistic implements Serializable {

	private static final long serialVersionUID = 1L;

    // RISK II Statistics
    //  0 countries;
    //  1 armies;
    //  2 kills;
    //  3 casualties;
    //  4 reinforcements;
    //  5 continents;
    //  6 conectedEmpire;
    //  7 attacks;

    //  8 retreats;
    //  9 countriesWon;
    // 10 countriesLost;
    // 11 attacked;

    // in the rest of the game they are knows as num + 1

    public int[] statistics;

    public Statistic() {

	statistics = new int[12];

	for (int i = 0; i < statistics.length; i++) {
	    statistics[i]=0;
	}

    }

    // at the end of a persons go this gets called
    public void endGoStatistics(int a, int b, int c, int d) {

	statistics[0] = a;
	statistics[1] = b;
	statistics[5] = c;
	statistics[6] = d;
/*
	System.out.print("\nStatistic for the last go:\n");
	System.out.print("countries "+statistics[0]+"\n");
	System.out.print("armies "+statistics[1]+"\n");
	System.out.print("kills "+statistics[2]+"\n");
	System.out.print("casualties "+statistics[3]+"\n");
	System.out.print("reinforcements "+statistics[4]+"\n");
	System.out.print("continents "+statistics[5]+"\n");
	System.out.print("conectedEmpire "+statistics[6]+"\n");
	System.out.print("attacks "+statistics[7]+"\n");

	System.out.print("retreats "+statistics[8]+"\n");
	System.out.print("countriesWon "+statistics[9]+"\n");
	System.out.print("countriesLost "+statistics[10]+"\n");
	System.out.print("attacked "+statistics[11]+"\n");
*/
    }

    public void addReinforcements(int a) {
	statistics[4] = statistics[4] + a;
    }

    public void addKill() {
	statistics[2]++;
    }

    public void addCasualty() {
	statistics[3]++;
    }

    public void addAttack() {
	statistics[7]++;
    }

    public void addAttacked() {
	statistics[11]++;
    }

    public void addRetreat() {
	statistics[8]++;
    }

    public void addCountriesWon() {
	statistics[9]++;
    }

    public void addCountriesLost() {
	statistics[10]++;
    }

    public int get(int a) {

	return statistics[a-1];

    }

}
