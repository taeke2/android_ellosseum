package com.gbsoft.ellosseum;

public class MyBounceInterpolator implements android.view.animation.Interpolator{
    private double mAmplitude = 1;
    private double mFrequency = 10;

    MyBounceInterpolator(double amplitude, double frequency) {
        mAmplitude = amplitude;
        mFrequency = frequency;
    }

    @Override
    public float getInterpolation(float input) {
        return (float) (-1 * Math.pow(Math.E, -input/ mAmplitude) *
                Math.cos(mFrequency * input) + 1);
    }
}
