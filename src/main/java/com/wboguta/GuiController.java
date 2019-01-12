package com.wboguta;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;
import org.mariuszgromada.math.mxparser.Function;
import org.mariuszgromada.math.mxparser.mXparser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;

import static java.lang.System.out;

public class GuiController {

    private static final Logger log = LoggerFactory.getLogger(GuiController.class);

    @FXML private Button buttonRun;
    @FXML private Button buttonStop;
    @FXML private ComboBox comboBoxFunction;
    @FXML private ScatterChart scatterChart;
    @FXML private TextField textFieldPopulationSize;
    @FXML private TextField textFieldGenotypeSize;
    @FXML private TextField textFieldIterations;
    @FXML private TextField textFieldRangeFrom;
    @FXML private TextField textFieldRangeTo;
    @FXML private Slider sliderSpeed;
    @FXML private CheckBox checkBoxMutation;
    @FXML private CheckBox checkBoxCrossingOver;

    private Semaphore semaphore;
    private boolean isStopped;

    @FXML
    public void initialize() {
        comboBoxFunction.getSelectionModel().selectFirst();
        isStopped = true;
        semaphore = new Semaphore(1); // Only one thread can access
    }


    public void plotFunction(Function function, double rangeFrom, double rangeTo) {
        Argument x;
        Expression expression;
        final XYChart.Series<Double, Double> series = new XYChart.Series<Double, Double>();
        double step = (rangeTo-rangeFrom)/1000;
        for (double i = rangeFrom; i <= rangeTo; i = i + step) {
            x = new Argument("x", i);
            expression = new Expression("f(x)", function, x);
            series.getData().add(new XYChart.Data<Double, Double>(i, expression.calculate()));
        }
        scatterChart.getData().add(series);
    }


    @FXML
    void comboBoxItemSelected(ActionEvent event) {
        // Change ranges
        switch (comboBoxFunction.getSelectionModel().getSelectedIndex()) {
            case(0):  textFieldRangeFrom.setText("-1"); textFieldRangeTo.setText("1"); break;
            case(1):  textFieldRangeFrom.setText("0"); textFieldRangeTo.setText("3");break;
        }
    }


    @FXML
    void buttonStopClicked(ActionEvent event) {
        isStopped = true;
    }


    @FXML
    void buttonRunClicked(ActionEvent event) {

        buttonRun.setDisable(true);
        buttonStop.setDisable(false);
        isStopped = false;
        comboBoxFunction.setDisable(true);
        textFieldGenotypeSize.setDisable(true);
        textFieldGenotypeSize.setDisable(true);
        textFieldIterations.setDisable(true);
        textFieldRangeFrom.setDisable(true);
        textFieldRangeTo.setDisable(true);
        textFieldPopulationSize.setDisable(true);

        // parse function
        final Function function = new Function((String)comboBoxFunction.getValue());
        if(!function.checkSyntax()) {
            mXparser.consolePrintln(function.getErrorMessage());

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("Could not parse function");
            alert.setContentText(function.getErrorMessage());
            alert.showAndWait();

            buttonRun.setDisable(false);
            buttonRun.setDisable(false);
            comboBoxFunction.setDisable(false);
            textFieldGenotypeSize.setDisable(false);
            textFieldGenotypeSize.setDisable(false);
            textFieldIterations.setDisable(false);
            textFieldRangeFrom.setDisable(false);
            textFieldRangeTo.setDisable(false);
            textFieldPopulationSize.setDisable(false);
            return;
        }

        // get values from fields
        int populationSize = Integer.parseInt(textFieldPopulationSize.getText());
        int genotypeSize = Integer.parseInt(textFieldGenotypeSize.getText());
        double rangeFrom = Double.parseDouble(textFieldRangeFrom.getText());
        double rangeTo = Double.parseDouble(textFieldRangeTo.getText());
        final int iterations = Integer.parseInt(textFieldIterations.getText());

        // create population
        final Population population = new Population(populationSize, genotypeSize, rangeFrom, rangeTo);

        // clear chart and draw function
        scatterChart.getData().clear();
        plotFunction(function, rangeFrom, rangeTo);

        final XYChart.Series series = new XYChart.Series();

        //start new thread
        new Thread() {

            private void plotIndividuals(final int i) throws InterruptedException {

                Platform.runLater(new Runnable() {
                    public void run() {

                        try {
                            semaphore.acquire(); // Acquire access
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        series.getData().clear();
                        scatterChart.getData().removeAll(series);
                        while (population.isNextIndividualXY()) {
                            double[] xy = population.getNextIndividualXY();
                            series.getData().add(new XYChart.Data(xy[0], xy[1]));
                        }

                        scatterChart.getData().addAll(series);
                        textFieldIterations.setText(String.valueOf(i));
                        semaphore.release(); // Release lock
                    }
                });

                Thread.sleep(((Double) sliderSpeed.getValue()).intValue()/2);
            }

            public void run() {
                try {

                    //calculate probs and adaptation at first
                    population.calculateAdaptation(function);
                    population.calculateProbs();

                    for (int i = 1; i <= iterations; i++) {

                        // put population on a chart
                        plotIndividuals(i);

                        semaphore.acquire(); // Acquire access
                        if (checkBoxMutation.isSelected()) {
                            population.mutation();
                        }

                        if (checkBoxCrossingOver.isSelected()) {
                            population.crossingOver();
                        }

                        population.calculateAdaptation(function);
                        population.calculateProbs();
                        semaphore.release(); // Release lock

                        plotIndividuals(i);

                        semaphore.acquire(); // Acquire access
                        population.pickNewPopulation();
                        semaphore.release(); // Release lock

                        if(isStopped == true) {
                            break;
                        }
                    }

                    Platform.runLater(new Runnable() {
                        public void run() {
                            buttonRun.setDisable(false);
                            buttonStop.setDisable(true);
                            comboBoxFunction.setDisable(false);
                            textFieldGenotypeSize.setDisable(false);
                            textFieldGenotypeSize.setDisable(false);
                            textFieldIterations.setDisable(false);
                            textFieldIterations.setText(String.valueOf(iterations));
                            textFieldRangeFrom.setDisable(false);
                            textFieldRangeTo.setDisable(false);
                            textFieldPopulationSize.setDisable(false);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }.start();


    }

}
