package com.wboguta;

import javafx.application.Platform;
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

import static java.lang.System.out;

public class GuiController
{
    private static final Logger log = LoggerFactory.getLogger(GuiController.class);

    @FXML private Button buttonRun;
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

    @FXML
    public void initialize() {
        comboBoxFunction.getSelectionModel().selectFirst();
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
    void buttonRunClicked(ActionEvent event) {

        buttonRun.setDisable(true);

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
                        series.getData().clear();
                        scatterChart.getData().removeAll(series);
                        while (population.isNextIndividualXY()) {
                            double[] xy = population.getNextIndividualXY();
                            series.getData().add(new XYChart.Data(xy[0], xy[1]));
                        }
                        scatterChart.getData().addAll(series);
                        textFieldIterations.setText(String.valueOf(i));
                    }
                });
                Thread.sleep(((Double) sliderSpeed.getValue()).intValue()/2);
            }

            public void run() {
                try {

                    //calculate probs and adaptation at first
                    population.calculateAdaptation(function);
                    population.calculateProbs();

                    for (int i = 0; i < iterations; i++) {

                        // put population on a chart
                        plotIndividuals(i);

                        if (checkBoxMutation.isSelected()) {
                            population.mutation();
                        }

                        if (checkBoxCrossingOver.isSelected()) {
                            population.crossingOver();
                        }

                        population.calculateAdaptation(function);
                        population.calculateProbs();

                        plotIndividuals(i);

                        population.pickNewPopulation();
                    }

                    buttonRun.setDisable(false);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }.start();


    }

}
