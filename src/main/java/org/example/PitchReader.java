package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class PitchReader {
    InputStream is;
    Map<String, Long> openOrders;
    Map<String, Long> completedOrders;

    public PitchReader(InputStream is) {
        this.is = is;
    }

    public void readPitch() {
        try(InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(streamReader)) {
            String pitch;
            do {
                // read each line of pitch, skip first S
                pitch = reader.readLine();
                if(pitch != null) {
                    if(pitch.charAt(0) == 'S') {
                        pitch = pitch.substring(1);
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






                    read A add order where A is in 8th position
                    check for unused long order add order message type where the character at ind 8 is either A for normal Add Order or d for long add order with 2 more length for symbol and a 4 length participant id

                    read E order executed and X order canceled -

                    map order id to either shares executed or an open add order
                     */




                    System.out.println(pitch);
                }

            } while(pitch != null);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
