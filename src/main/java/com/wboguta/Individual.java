package com.wboguta;

import java.util.BitSet;
import java.util.Random;

public class Individual implements Cloneable{

    private BitSet genotype;
    private int genotypeSize;
    private int adaptation;
    private double probability;


    public Individual(int genotypeSize) {
        this.genotypeSize = genotypeSize;
        genotype = new BitSet(genotypeSize);

        for (int i = 0; i < genotypeSize; i++) {
            genotype.set(i, new Random().nextBoolean());
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException{

        Individual individual = null;
        individual = (Individual) super.clone();
        individual.genotype = (BitSet) this.genotype.clone();

        return individual;
    }


    public int decodeGenotype() {
        int result = 0;
        for (int i = 0; i < genotypeSize; i++) {
            if (genotype.get(i)) {
                result |= (1 << i);
            }
        }
        result &= Integer.MAX_VALUE;
        return result;
    }

    public void flipBit(int index) {
        genotype.flip(index);
    }

    public void setBit(int index, boolean value) {
        genotype.set(index, value);
    }

    public boolean getBit(int index) {
        return genotype.get(index);
    }

    public void setAdaptation(int adaptation) {
        this.adaptation = adaptation;
    }

    public int getAdaptation() {
        return adaptation;
    }

    @Override
    public String toString() {

        String str = "";

        for (int i = genotypeSize-1; i >= 0; i--) {
            str += genotype.get(i) ? 1 : 0;
        }

        str += " genotypeSize=" + genotypeSize;
        str += " adaptation=" + getAdaptation();
        str += " probability=" + probability;

        return str;
    }


    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }
}
