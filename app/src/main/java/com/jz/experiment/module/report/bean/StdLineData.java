package com.jz.experiment.module.report.bean;

import com.jz.experiment.module.data.bean.SampleRow;

import java.io.Serializable;
import java.util.List;

public class StdLineData implements Serializable {

    private double[] fitedXX;
    private double[] fitedYY;
    private double[] stdXX;
    private double[] stdYY;
    private double[] unknowXX;
    private double[] unknowYY;

    private String equation;
    private String R2;
    private List<SampleRow> stdRows;
    private List<SampleRow> unknownRows;
    public StdLineData(double[] fitxx, double[] fityy,double[] stdxx,
                       double[] stdyy,double[] unknownXX,double[] unknownYY,
                       String equation,String R2){

        this.fitedXX=fitxx;
        this.fitedYY=fityy;
        this.stdXX=stdxx;
        this.stdYY=stdyy;
        this.unknowXX=unknownXX;
        this.unknowYY=unknownYY;
        this.equation=equation;
        this.R2= R2;
    }
    public double[] getFitedXX() {
        return fitedXX;
    }

    public void setFitedXX(double[] fitedXX) {
        this.fitedXX = fitedXX;
    }

    public double[] getFitedYY() {
        return fitedYY;
    }

    public void setFitedYY(double[] fitedYY) {
        this.fitedYY = fitedYY;
    }

    public double[] getStdXX() {
        return stdXX;
    }

    public void setStdXX(double[] stdXX) {
        this.stdXX = stdXX;
    }

    public double[] getStdYY() {
        return stdYY;
    }

    public void setStdYY(double[] stdYY) {
        this.stdYY = stdYY;
    }

    public double[] getUnknowXX() {
        return unknowXX;
    }

    public void setUnknowXX(double[] unknowXX) {
        this.unknowXX = unknowXX;
    }

    public double[] getUnknowYY() {
        return unknowYY;
    }

    public void setUnknowYY(double[] unknowYY) {
        this.unknowYY = unknowYY;
    }

    public String getEquation() {
        return equation;
    }

    public void setEquation(String equation) {
        this.equation = equation;
    }

    public String getR2() {
        return R2;
    }

    public void setR2(String r2) {
        R2 = r2;
    }

    public List<SampleRow> getStdRows() {
        return stdRows;
    }

    public void setStdRows(List<SampleRow> stdRows) {
        this.stdRows = stdRows;
    }

    public List<SampleRow> getUnknownRows() {
        return unknownRows;
    }

    public void setUnknownRows(List<SampleRow> unknownRows) {
        this.unknownRows = unknownRows;
    }
}
