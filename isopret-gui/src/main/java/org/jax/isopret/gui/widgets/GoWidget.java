package org.jax.isopret.gui.widgets;


import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import org.jax.isopret.gui.service.model.GeneOntologyComparisonMode;
import org.jax.isopret.gui.service.model.GoCompTerm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Superclass for GoDisplayWidget and GoCompwidget
 */
public interface GoWidget {

    default String limitLabelLength(String label) {
        final int MAX_LABEL_LENGTH = 35;
        final String DOTS = " ...";
        final int DOTS_LENGTH = DOTS.length();
        if (label.length() < MAX_LABEL_LENGTH + DOTS_LENGTH) {
            return label;
        } else {
            return label.substring(0,MAX_LABEL_LENGTH) + DOTS;
        }
    }




}
