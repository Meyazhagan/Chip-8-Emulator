package Emulator;

import chip8.Chip;

public class Main {
  public static void main(String[] args) {
    Chip c = new Chip();
    c.init();
    ChipFrame frame = new ChipFrame(c);
  }
}