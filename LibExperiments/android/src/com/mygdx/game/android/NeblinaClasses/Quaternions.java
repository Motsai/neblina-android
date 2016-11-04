package com.mygdx.game.android.NeblinaClasses;


/**
 * Created by scott on 2016-06-20.
 */
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

@DynamoDBTable(tableName = "Quaternions")

public class Quaternions extends Object {
    private String timestamp;
    private double q1;
    private double q2;
    private double q3;
    private double q4;


    //Default Constructor
    public Quaternions (){}

    //Complete Constructor
    public Quaternions(String timestamp, double q1, double q2, double q3, double q4){
        this.timestamp = timestamp;
        this.q1 = q1;
        this.q2 = q2;
        this.q3 = q3;
        this.q4 = q4;
    }

    //HASH KEY
    @DynamoDBHashKey(attributeName = "timestamp")
    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    //ATTRIBUTES
    @DynamoDBAttribute(attributeName = "q1")
    public double getQ1() {return q1;}
    public void setQ1(double q1) {
        this.q1 = q1;
    }

    @DynamoDBAttribute(attributeName = "q2")
    public double getQ2() {
        return q2;
    }
    public void setQ2(double q2) {
        this.q2 = q2;
    }

    @DynamoDBAttribute(attributeName = "q3")
    public double getQ3() {
        return q3;
    }
    public void setQ3(double q3) {
        this.q3 = q3;
    }

    @DynamoDBAttribute(attributeName = "q4")
    public double getQ4() {
        return q4;
    }
    public void setQ4(double q4) {
        this.q4 = q4;
    }

}
