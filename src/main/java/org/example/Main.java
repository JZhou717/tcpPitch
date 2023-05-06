package org.example;

import java.io.InputStream;

public class Main {
    public static void main(String[] args) {
        /**
         * TODO:
         * read messages as file, read line by line
         *
         * use map to link symbol to execution volume by shares
         *
         * two types to focus on: Orders and Trade Messages
         * store Add Order to map, wait for order cancel or order executed to count
         * read trade messages for volume of hidden order
         *
         * ignore Trade Break, Symbol Clear, Auction, Status, Retail, and long messages (‘B’, ‘s’, ‘r’, ‘d’, ‘H’, ‘I’, ‘J’, ‘R’)
         *
         * Iterate over map to get the top 10 values - maybe use heap for top 10 once we have map
         *
         */

        InputStream is = Main.class.getClassLoader().getResourceAsStream("trunc_pitch_example_data");
        PitchReader pitchReader = new PitchReader(is);
        pitchReader.readPitch();
        pitchReader.printTopTen();
    }
}