package org.jax.isopret.gui.widgets;


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
