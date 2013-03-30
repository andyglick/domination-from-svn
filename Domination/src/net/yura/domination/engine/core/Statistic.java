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
    public static final int COUNTRIES = 1,
                            ARMIES = 2,
                            KILLS = 3,
                            CASUALTIES = 4,
                            REINFORCEMENTS = 5,
                            CONTINENTS = 6,
                            CONECTED_EMPIRE = 7,
                            ATTACKS = 8,

                            RETREATS = 9,
                            COUNTRIES_WON = 10,
                            COUNTRIES_LOST = 11,
                            ATTACKED = 12,

                            CARDS = 13;

    // in the rest of the game they are knows as num + 1
    public int[] statistics;

    public Statistic() {
	statistics = new int[13];
	for (int i = 0; i < statistics.length; i++) {
	    statistics[i]=0;
	}
    }

    // at the end of a persons go this gets called
    public void endGoStatistics(int countries, int armies, int continents, int conectedEmpire, int cards) {

	statistics[COUNTRIES-1] = countries;
	statistics[ARMIES-1] = armies;
	statistics[CONTINENTS-1] = continents;
	statistics[CONECTED_EMPIRE-1] = conectedEmpire;
	statistics[CARDS-1] = cards;
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
	statistics[REINFORCEMENTS-1] = statistics[REINFORCEMENTS-1] + a;
    }

    public void addKill() {
	statistics[KILLS-1]++;
    }

    public void addCasualty() {
	statistics[CASUALTIES-1]++;
    }

    public void addAttack() {
	statistics[ATTACKS-1]++;
    }

    public void addAttacked() {
	statistics[ATTACKED-1]++;
    }

    public void addRetreat() {
	statistics[RETREATS-1]++;
    }

    public void addCountriesWon() {
	statistics[COUNTRIES_WON-1]++;
    }

    public void addCountriesLost() {
	statistics[COUNTRIES_LOST-1]++;
    }

    public int get(int a) {
	return statistics[a-1];
    }

}
