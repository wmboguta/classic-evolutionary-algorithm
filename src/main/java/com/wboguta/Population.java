package com.wboguta;

import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;
import org.mariuszgromada.math.mxparser.Function;
import java.util.ArrayList;
import java.util.Random;

public class Population {


    private ArrayList<Individual> container;
    private ArrayList<Individual> prevContainer;
    private int populationSize;
    private int genotypeSize;
    static int iterator;
    private double adaptationSum;
    private RandomItem randomItem;
    private double rangeFrom;
    private double rangeTo;


    public Population(int populationSize, int genotypeSize, double rangeFrom, double rangeTo) {
        this.populationSize = populationSize;
        this.genotypeSize = genotypeSize;
        this.rangeFrom = rangeFrom;
        this.rangeTo = rangeTo;
        container = new ArrayList<Individual>();
        prevContainer = new ArrayList<Individual>();
        for(int i=0; i<populationSize; i++) {
            container.add(new Individual(genotypeSize));
        }
        iterator = 0;
        randomItem = new RandomItem();
    }

    private static int getRandomNumberInRange(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public void mutation() {
        //pick random individual
        Individual randomIndividual = container.get(getRandomNumberInRange(0, populationSize-1));

        //pick random genotype bit and flip it's value
        randomIndividual.flipBit(getRandomNumberInRange(0, genotypeSize-1));
    }

    public void crossingOver() {
        //pick two random individuals
        Individual randomIndividual1 = container.get(getRandomNumberInRange(0, populationSize-1));
        Individual randomIndividual2 = container.get(getRandomNumberInRange(0, populationSize-1));

        if(randomIndividual2.decodeGenotypeInt() == randomIndividual1.decodeGenotypeInt()) {
            //System.out.println("Nothing to cross over, genotypes are the same");
            return;
        }

        //decide if individuals will cross over
        if(!new Random().nextBoolean()) return;

        //pick random crossingOverPoint
        int crossingOverPoint = getRandomNumberInRange(1, genotypeSize - 2);

        //create copies of individuals
        Individual tempRandomIndividual1;
        Individual tempRandomIndividual2;
        try {
            tempRandomIndividual1 = (Individual) randomIndividual1.clone();
            tempRandomIndividual2 = (Individual) randomIndividual2.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return;
        }

        //change bits between individuals starting from picked point
        for (int i = crossingOverPoint; i >= 0; i--) {
            randomIndividual1.setBit(i, tempRandomIndividual2.getBit(i));
            randomIndividual2.setBit(i, tempRandomIndividual1.getBit(i));
        }
    }


    public void calculateAdaptation(Function function) {
        for (int i = 0; i < populationSize; i++) {
            Argument x = new Argument("x", container.get(i).decodeGenotypeDouble(rangeFrom, rangeTo));
            Expression expression = new Expression("f(x)",function, x);
            container.get(i).setAdaptation(expression.calculate());
            //mXparser.consolePrintln("Res: " + expression.getExpressionString() + " = " + individual.getAdaptation());
        }
    }

    public void calculateProbs() {
        adaptationSum = 0.0;
        for (int i = 0; i < populationSize; i++) {
            adaptationSum += container.get(i).getAdaptation();
        }
        for (int i = 0; i < populationSize; i++) {
            container.get(i).setProbability((double)container.get(i).getAdaptation()/adaptationSum);
        }
    }

    public void pickNewPopulation() throws CloneNotSupportedException {
        randomItem.clearLists();
        for (int i = 0; i < populationSize; i++) {
            randomItem.addToLists(i, container.get(i).getProbability());
        }
        prevContainer.clear();
        prevContainer = (ArrayList<Individual>) container.clone();
        container.clear();

        for (int i = 0; i < populationSize; i++) {
            container.add((Individual) prevContainer.get(randomItem.random()).clone());
        }
    }

    public boolean isNextIndividualXY() {
        if(iterator >= populationSize) {
            iterator = 0;
            return false;
        }
        return true;
    }

    public double[] getNextIndividualXY() {
        double[] xy = {0, 0};
        xy[0] = container.get(iterator).decodeGenotypeDouble(rangeFrom, rangeTo);
        xy[1] = container.get(iterator).getAdaptation();
        iterator++;
        return xy;
    }

    @Override
    public String toString() {
        String str = "";
        str += "populationSize=" + populationSize + "\n";
        for (int i = 0; i < populationSize; i++) {
            str += container.get(i).toString() + "\n";
        }
        return str;
    }


}
