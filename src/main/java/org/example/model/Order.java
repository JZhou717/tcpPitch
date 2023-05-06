package org.example.model;

public class Order {
    private String symbol;
    private int shares;

    public Order(String symbol, int shares) {
        this.symbol = symbol;
        this.shares = shares;
    }

    public int getShares() {
        return shares;
    }

    public String getSymbol() {
        return symbol;
    }

    // removes the cancelled shares from this open order, returns true if no shares left
    public boolean cancel(int cancelShares) {
        int newShares = this.shares - cancelShares;
        if(newShares <= 0) {
            return true;
        }
        this.shares = newShares;
        return false;
    }
}
