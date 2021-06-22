// IAutoInterface.aidl
package com.example.myapplicationw03;

// Declare any non-default types here with import statements

interface IAutoInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

    int getSomething();
    boolean getExeg();
    boolean getSendable();
    void setSendable();

    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
}