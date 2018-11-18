package com.wboguta;

import jdk.nashorn.internal.runtime.arrays.ArrayLikeIterator;

import java.util.ArrayList;
import java.util.Random;

public class RandomItem {

    private ArrayList<Integer> numbers;
    private ArrayList<Double> probs;
    private Random rand;

    public RandomItem(ArrayList<Integer> numbers, ArrayList<Double> probs){
        this.numbers = numbers;
        this.probs = probs;
        rand = new Random();
    }

    public RandomItem(){
        rand = new Random();
        numbers = new ArrayList<Integer>();
        probs = new ArrayList<Double>();
    }

    public void clearLists() {
        numbers.clear();
        probs.clear();
    }

    public void addToLists(int number, double probability) {
        numbers.add(number);
        probs.add(probability);
    }

    public int random(){
        double p = rand.nextDouble();
        double sum = 0.0;
        int i = 0;
        while( sum < p && i < probs.size() ){
            sum += probs.get(i);
            i++;
        }
        return numbers.get(i-1);
    }
}
