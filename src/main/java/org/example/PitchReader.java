package org.example;

import org.example.model.Order;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class PitchReader {
    InputStream is;
    private final Map<String, Order> openOrders;
    private final Map<String, Long> completedOrders;

    public PitchReader(InputStream is) {
        this.is = is;
        openOrders = new HashMap<>();
        completedOrders = new HashMap<>();
    }

    public void readPitch() {
        try(InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(streamReader)) {
            String pitch;
            do {
                // read each line of pitch
                pitch = reader.readLine();
                if(pitch != null) {
                    // ignore leading S character
                    if(pitch.charAt(0) == 'S') {
                        pitch = pitch.substring(1);
                    }

                    // Identify type of message
                    char pitchType = pitch.charAt(8);

                    // operate Add, Execute, Cancel, or Trade depending on pitch type

                    switch(pitchType) {
                        // Add Order - add Order to openOrders
                        case 'A':
                            this.addOrder(pitch);
                            break;
                        // Execute Order - removes shares indicated from openOrder Order and also updates completedOrders
                        case 'E':
                            this.executeOrder(pitch);
                            break;
                        // Cancel Order - remove shares indicated from openOrder Order
                        case 'X':
                            this.cancelOrder(pitch);
                            break;
                        // Trade Message - updates completedOrders with share amount
                        case 'P':
                            this.tradeMessage(pitch);
                            break;
                        // Add Order - long format
                        case 'd':
                            this.addLongOrder(pitch);
                            break;
                        case 'r':
                            // trade message long
                            break;
                    }

                    /*TODO
                    Example 4 part sequence:
                    Add Order
                    28803219A4K27GA00003GS000200DIA   0001298500Y

                    Execute Order
                    28803224E4K27GA00003G00007700004AQ00001
                    28803224E4K27GA00003G00007600004AQ00002

                    Cancel Order
                    28803238X4K27GA00003G000047




                    Example Trade:
                    28803240P4K27GA00003PB000100DXD   0000499600000N4AQ00003




                    TODO: check for A, E, X, P, d, and r
                    If it's an A, add to open Orders with symbol and share amount
                    If it's E, remove the shares amount from open order, add that to executed order or - CHECK FOR MAX, remove open order if none remaining
                    If it's X, remove shares amount from open order, remove open order if none remaining
                    If it's P, add that amount to executed amount
                    If it's d or r, same as A and P but parsing differently




                    read A add order where A is in 8th position
                    check for unused long order add order message type where the character at ind 8 is either A for normal Add Order or d for long add order with 2 more length for symbol and a 4 length participant id

                    read E order executed and X order canceled -

                    map order id to either shares executed or an open add order
                     */




                }

            } while(pitch != null);




            System.out.println();
            // Testing only
            System.out.println(completedOrders);










        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // parses Add Order pitch message into its components and adds symbol and shares to openOrders map
    private void addOrder(String pitch) {
        // unused fields parsed for scalability
        int timestamp = Integer.parseInt(pitch.substring(0, 8));
        String orderId = pitch.substring(9, 21);
        char side = pitch.charAt(21);
        int shares = Integer.parseInt(pitch.substring(22, 28));
        String symbol = pitch.substring(28, 34);
        int price = Integer.parseInt(pitch.substring(34, 44));
        char Display = pitch.charAt(44);

        System.out.println("Add Order");
        System.out.println("Order ID: " + orderId);
        System.out.println("Shares: " + shares);
        System.out.println("Symbol: " + symbol);
        System.out.println();

        openOrders.put(orderId, new Order(symbol, shares));
    }

    // parses long version of Add Order pitch message into its components and adds symbol and shares to openOrders map
    private void addLongOrder(String pitch) {
        // unused fields parsed for scalability
        int timestamp = Integer.parseInt(pitch.substring(0, 8));
        String orderId = pitch.substring(9, 21);
        char side = pitch.charAt(21);
        int shares = Integer.parseInt(pitch.substring(22, 28));
        String symbol = pitch.substring(28, 36);
        int price = Integer.parseInt(pitch.substring(36, 46));
        char Display = pitch.charAt(46);
        String participantId = pitch.substring(47);

        System.out.println("Add Order");
        System.out.println("Order ID: " + orderId);
        System.out.println("Shares: " + shares);
        System.out.println("Symbol: " + symbol);
        System.out.println();

        openOrders.put(orderId, new Order(symbol, shares));
    }

    // parses Order Cancel pitch message into its components and updates openOrders with new shares amount or removal
    private void cancelOrder(String pitch) {
        // unused fields parsed for scalability
        int timestamp = Integer.parseInt(pitch.substring(0, 8));
        String orderId = pitch.substring(9, 21);
        int shares = Integer.parseInt(pitch.substring(21, 27));

        System.out.println("Order Cancel");
        System.out.println("Order ID: " + orderId);
        System.out.println("Shares: " + shares);
        System.out.println();

        Order order = openOrders.get(orderId);
        if(order != null) {
            // remove cancelled shares from open order
            // if there are no more open shares left, remove this order id from the openOrders map
            if(order.cancel(shares)) {
                openOrders.remove(orderId);
            }
        }
    }

    // parses Order Cancel pitch message into its components and updates openOrders with new shares amount or removal and updates completedOrders with share amount
    private void executeOrder(String pitch) {
        // unused fields parsed for scalability
        int timestamp = Integer.parseInt(pitch.substring(0, 8));
        String orderId = pitch.substring(9, 21);
        int shares = Integer.parseInt(pitch.substring(21, 27));
        String executionId = pitch.substring(27);

        System.out.println("Order Execute");
        System.out.println("Order ID: " + orderId);
        System.out.println("Shares: " + shares);
        System.out.println();

        Order order = openOrders.get(orderId);
        if(order != null) {
            // adds executed shares to completedOrders count
            completedOrders.put(order.getSymbol(), completedOrders.getOrDefault(order.getSymbol(), 0L) + shares);
            // remove cancelled shares from open order
            // if there are no more open shares left, remove this order id from the openOrders map
            if(order.cancel(shares)) {
                openOrders.remove(orderId);
            }
        }
    }

    // parses Trade pitch message into its components and updates completedOrders with share amount
    private void tradeMessage(String pitch) {
        // unused fields parsed for scalability
        int timestamp = Integer.parseInt(pitch.substring(0, 8));
        String orderId = pitch.substring(9, 21);
        char side = pitch.charAt(21);
        int shares = Integer.parseInt(pitch.substring(22, 28));
        String symbol = pitch.substring(28, 34);
        int price = Integer.parseInt(pitch.substring(34, 44));
        String executionId = pitch.substring(44);

        System.out.println("Trade Message");
        System.out.println("Order ID: " + orderId);
        System.out.println("Shares: " + shares);
        System.out.println("Symbol: " + symbol);
        System.out.println();


        // adds executed shares to completedOrders count
        completedOrders.put(symbol, completedOrders.getOrDefault(symbol, 0L) + shares);
    }

    // TODO: Prints the top 10 traded symbols by executed shares
    void printTopTen() {
    }
}
