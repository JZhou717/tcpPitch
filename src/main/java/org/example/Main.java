package org.example;

import java.io.InputStream;

public class Main {
    public static void main(String[] args) {
        InputStream is = Main.class.getClassLoader().getResourceAsStream("pitch_example_data");
        PitchReader pitchReader = new PitchReader(is);
        pitchReader.readPitch();
        pitchReader.printTopTen();
    }
}