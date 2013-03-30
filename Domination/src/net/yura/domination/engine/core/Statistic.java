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
    private int[] statistics;

    public Statistic() {
	statistics = new int[13];
	for (int i = 0; i < statistics.length; i++) {
	    statistics[i]=0;
	}
    }

    // at the end of a persons go this gets called
    public void endGoStatistics(int countries, int armies, int continents, int conectedEmpire, int cards) {

	statistics[getIndexFromStatistic(COUNTRIES)] = countries;
	statistics[getIndexFromStatistic(ARMIES)] = armies;
	statistics[getIndexFromStatistic(CONTINENTS)] = continents;
	statistics[getIndexFromStatistic(CONECTED_EMPIRE)] = conectedEmpire;
	statistics[getIndexFromStatistic(CARDS)] = cards;
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

    public void addReinforcements(final int a) {
	statistics[getIndexFromStatistic(REINFORCEMENTS)] += a;
    }

    public void addKill() {
	statistics[getIndexFromStatistic(KILLS)]++;
    }

    public void addCasualty() {
	statistics[getIndexFromStatistic(CASUALTIES)]++;
    }

    public void addAttack() {
	statistics[getIndexFromStatistic(ATTACKS)]++;
    }

    public void addAttacked() {
	statistics[getIndexFromStatistic(ATTACKED)]++;
    }

    public void addRetreat() {
	statistics[getIndexFromStatistic(RETREATS)]++;
    }

    public void addCountriesWon() {
	statistics[getIndexFromStatistic(COUNTRIES_WON)]++;
    }

    public void addCountriesLost() {
	statistics[getIndexFromStatistic(COUNTRIES_LOST)]++;
    }

    public int get(int statistic) {
	return statistics[ getIndexFromStatistic(statistic) ];
    }
    
    private static int getIndexFromStatistic(int statistic) {
        return statistic-1;
    }

}
