package com.wind.toastlib;

/**
     * Interface that defines the behavior of the main content of a transient  layout.
     */
    public interface ContentViewCallback {
        /**
         * Animates the content of the transient layout in.
         *
         * @param delay Animation delay.
         * @param duration Animation duration.
         */
         void animateContentIn(int delay, int duration);

        /**
         * Animates the content of the transientlayout out.
         *
         * @param delay Animation delay.
         * @param duration Animation duration.
         */
        void animateContentOut(int delay, int duration);
    }