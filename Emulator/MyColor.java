package Emulator;

import java.awt.*;

public class MyColor extends Color{

    public MyColor(int rgb) {
        super(rgb);
    }

    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "MyColor";
    }
    
}