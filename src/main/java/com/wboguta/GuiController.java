package com.wboguta;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
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
    @FXML private ComboBox comboBoxFunctions;
    @FXML private ScatterChart scatterChart;
    @FXML private TextField textFieldPopulationSize;
    @FXML private TextField textFieldGenotypeSize;
    @FXML private TextField textFieldIterations;


    @FXML
    void buttonRunClicked(ActionEvent event) throws CloneNotSupportedException {

        if(comboBoxFunctions.getValue() == null) return;

        Function function = new Function((String)comboBoxFunctions.getValue());
        if(!function.checkSyntax()) {
            mXparser.consolePrintln(function.getErrorMessage());
            return;
        }

        Population population = new Population(Integer.parseInt(textFieldPopulationSize.getText()), Integer.parseInt(textFieldGenotypeSize.getText()));
        System.out.print(population);


        XYChart.Series series1 = new XYChart.Series();



       // for(int i=0; i<5; i++) {
            population.crossingOver();
            //System.out.print(population);

            population.mutation();
            //System.out.print(population);

            population.selection(function);
            //System.out.print(population);
        //}

        series1.setName("Option 1");
        while(population.isNextIndividualXY()) {
            double[] xy = population.getNextIndividualXY();
            series1.getData().add(new XYChart.Data(xy[0], xy[1]));
        }
        scatterChart.getData().addAll(series1);

        System.out.print(population);


    }

}
