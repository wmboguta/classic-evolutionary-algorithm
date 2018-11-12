package com.wboguta;

import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;
import org.mariuszgromada.math.mxparser.Function;
import org.mariuszgromada.math.mxparser.mXparser;

import java.util.ArrayList;
import java.util.Random;

public class Population {

    private ArrayList<Individual> container;
    private ArrayList<Individual> prevContainer;
    private int populationSize;
    private int genotypeSize;
    static int iterator;

    public Population(int populationSize, int genotypeSize) {
        this.populationSize = populationSize;
        this.genotypeSize = genotypeSize;
        container = new ArrayList<Individual>();
        prevContainer = new ArrayList<Individual>();
        for(int i=0; i<populationSize; i++) {
            container.add(new Individual(genotypeSize));
        }
        iterator = 0;
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

    public void crossingOver() throws CloneNotSupportedException {

        //pick two random individuals
        Individual randomIndividual1 = container.get(getRandomNumberInRange(0, populationSize-1));
        Individual randomIndividual2 = container.get(getRandomNumberInRange(0, populationSize-1));

//        System.out.println(randomIndividual1);
//        System.out.println(randomIndividual2);

        if(randomIndividual2.decodeGenotype() == randomIndividual1.decodeGenotype()) {
            System.out.println("Nothing to cross over, genotypes are the same");
        }

        //decide if individuals will cross over
        if(!new Random().nextBoolean()) return;

        //pick random crossingOverPoint
        int crossingOverPoint = getRandomNumberInRange(1, genotypeSize-2);

//        System.out.println(crossingOverPoint);

        //create copies of individuals
        Individual tempRandomIndividual1;
        Individual tempRandomIndividual2;
        tempRandomIndividual1 = (Individual) randomIndividual1.clone();
        tempRandomIndividual2 = (Individual) randomIndividual2.clone();

        //change bits between individuals starting from picked point
        for(int i = crossingOverPoint; i >= 0; i--) {
            randomIndividual1.setBit(i , tempRandomIndividual2.getBit(i));
            randomIndividual2.setBit(i , tempRandomIndividual1.getBit(i));
        }

//        System.out.println(randomIndividual1);
//        System.out.println(randomIndividual2);
    }


    public void selection(Function function) {

        // calculate adaptation
        for (int i = 0; i < populationSize; i++) {
            Individual individual  = container.get(i);
            Argument x = new Argument("x", individual.decodeGenotype());
            Expression expression = new Expression("f(x)",function, x);
            individual.setAdaptation((int) expression.calculate());
            //mXparser.consolePrintln("Res: " + expression.getExpressionString() + " = " + individual.getAdaptation());
        }

        //calculate probs
        int adaptationSum = 0;

        for (int i = 0; i < populationSize; i++) {
            adaptationSum += container.get(i).getAdaptation();
        }

        ArrayList<Integer> array = new ArrayList<Integer>();
        ArrayList<Double> probs = new ArrayList<Double>();

        for (int i = 0; i < populationSize; i++) {
            container.get(i).setProbability((double)container.get(i).getAdaptation()/adaptationSum);
            array.add(i);
            probs.add(container.get(i).getProbability());
        }

        //System.out.print(this);

        //pick new population
        RandomItem rnd = new RandomItem(array, probs);
        prevContainer.clear();
        prevContainer = (ArrayList<Individual>) container.clone();
        for (int i = 0; i < populationSize; i++) {
            container.set(i, prevContainer.get(rnd.random()));
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
        xy[0] = container.get(iterator).decodeGenotype();
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
