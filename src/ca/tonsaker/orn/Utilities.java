package ca.tonsaker.orn;

import java.awt.Color;

public final class Utilities {

    public static Color randomColorWord(String str){
        int i = Math.abs(str.hashCode()) % 8;
        switch (i){
            case 0: return Color.GREEN;
            case 1: return Color.CYAN;
            case 2: return Color.PINK;
            case 3: return Color.MAGENTA;
            case 4: return Color.ORANGE;
            case 5: return Color.RED;
            case 6: return Color.YELLOW;
            case 7: return Color.WHITE;
        }
        return Color.BLACK;
    }

}
