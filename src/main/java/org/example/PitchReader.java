package org.example;

import org.example.model.Order;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

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
                        // Trade Message - long format
                        case 'r':
                            this.tradeLongMessage(pitch);
                            break;
                    }
                }
            } while(pitch != null);

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

        openOrders.put(orderId, new Order(symbol, shares));
    }

    // parses Order Cancel pitch message into its components and updates openOrders with new shares amount or removal
    private void cancelOrder(String pitch) {
        // unused fields parsed for scalability
        int timestamp = Integer.parseInt(pitch.substring(0, 8));
        String orderId = pitch.substring(9, 21);
        int shares = Integer.parseInt(pitch.substring(21, 27));

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

        // adds executed shares to completedOrders count
        completedOrders.put(symbol, completedOrders.getOrDefault(symbol, 0L) + shares);
    }

    // parses long version of Trade pitch message into its components and updates completedOrders with share amount
    private void tradeLongMessage(String pitch) {
        // unused fields parsed for scalability
        int timestamp = Integer.parseInt(pitch.substring(0, 8));
        String orderId = pitch.substring(9, 21);
        char side = pitch.charAt(21);
        int shares = Integer.parseInt(pitch.substring(22, 28));
        String symbol = pitch.substring(28, 36);
        int price = Integer.parseInt(pitch.substring(36, 46));
        String executionId = pitch.substring(46);

        // adds executed shares to completedOrders count
        completedOrders.put(symbol, completedOrders.getOrDefault(symbol, 0L) + shares);
    }


    // Prints the top 10 traded symbols by executed shares using TreeMap
    void printTopTen() {
        // use comparator and new treemap to get top 10 elements
        SharesComparator sharesComparator = new SharesComparator(completedOrders);
        TreeMap<String, Long> sortedCompletedOrders = new TreeMap<>(sharesComparator);
        sortedCompletedOrders.putAll(completedOrders);

        int limit = 10;
        for(var entry : sortedCompletedOrders.entrySet()) {
            if(limit > 0) {
                System.out.println(entry.getKey() + "  " + entry.getValue());
                sortedCompletedOrders.remove(entry.getKey());
                limit--;
            } else {
                break;
            }
        }
    }

    // Use custom comparator, sorts based on value in completedOrders map for a given symbol
    private static class SharesComparator implements Comparator<String> {
        Map<String, Long> base;
        public SharesComparator(Map<String, Long> base) {
            this.base = base;
        }
        @Override
        public int compare(String a, String b) {
            if(base.get(a) >= base.get(b)) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}
