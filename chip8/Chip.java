package chip8;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Chip{
  private char[] memory; //memory 4k
  private char[] V; //register
  private char I; //address pointer
  private char pc; //program counter

  private char[] stack;
  private int stackPointer;

  private int display_timer;
  private int sound_timer;

  private byte[] keys;
  private byte[] display;

  private boolean needRedraw;

  public void init(){
    memory = new char[4096];
    V = new char[16];
    I = 0x0;
    pc = 0x200;

    stack = new char[16];
    stackPointer = 0;

    display_timer = 0;
    sound_timer = 0;

    keys = new byte[16];
    display = new byte[64* 32];

    needRedraw = false;
    loadFont();
  }
  public void run(){
    //fetch data - our char is 16bit, we adding two 8bits into one.
    char opcode = (char)((memory[pc] << 8) | memory[pc+1]);
    System.out.print(Integer.toHexString(opcode) + " : ");
    //decode opcode
    switch(opcode & 0xf000){
      case 0x0000 : break;
      case 0x1000 : break;
      case 0x2000 :{//calls subroutine at 0NNN
                    stack[stackPointer++] = pc;
                    pc = (char)(opcode & 0x0fff); 
                    break;}
      // case 0x3000 : break;
      // case 0x4000 : break;
      // case 0x5000 : break;
      case 0x6000 : {//Sets VX to NN.
                    int x = (opcode & 0x0f00) >> 8;
                    V[x] = (char)(opcode & 0x00ff);
                    pc += 2;
                    break;}
      case 0x7000 : {//add NN to x
                  int x = (opcode & 0x0f00) >> 8;
                  char nn = (char)(opcode & 0x00ff);
                  V[x] = (char)((V[x] + nn) & 0xff);
                  pc += 2;
                  break;}
      // case 0x8000 : break;
      // case 0x9000 : break;
      case 0xA000 : //Sets I to the address NNN.
                    I = (char)(opcode & 0x0fff);
                    pc += 2;
                    break;
      // case 0xB000 : break;
      // case 0xC000 : break;
      case 0xD000 : {//Display
                    int x = V[(opcode & 0x0f00) >> 8];
                    int y = V[(opcode & 0x00f0) >> 4];
                    int heigth = opcode & 0x000f;
                    V[0xf] = 0;

                    for(int _y =0 ; _y < heigth; _y++){
                      int line = memory[I + _y];
                      for(int _x =0; _x < 8; _x++){
                        int pixel = line & (0x80 >> _x);
                        if(pixel != 1){
                          int totalX = x + _x;
                          int totalY = y + _y;
                          int index = totalY * 64 + totalX;
                          if(display[index] ==1) V[0xf] =1;

                          display[index] ^= 1;
                        }
                      }
                    }
                    pc += 2;
                    needRedraw = true;
                    break;}
      // case 0xE000 : break;
      // case 0xF000 : break;


      default:System.err.print("Undefinded function! ");
              System.exit(0);
              break;
    }

    //excute opcode
  }

  public byte[] getDisplay(){
    return display;
  }

  public void loadProgram(String file){
    DataInputStream input = null;
    try{
    input = new DataInputStream(new FileInputStream(new File(file)));
    int offset = 0;
    while(input.available() > 0){
      memory[0x200 + offset] = (char)(input.readByte() & 0xff);
      offset++;
    }

    }
    catch(IOException e){
      e.printStackTrace();
      System.exit(0);
    }
    finally{
      if(input != null){ 
        try{ input.close(); } catch(IOException e){}
      }
    }
  }

  public boolean needRedraw(){
    return needRedraw;
  }

  public void removeDrawFlag(){
    needRedraw = false;
  }
  public void loadFont(){
    int offset = 0;
    while(offset < ChipData.fontset.length){
      memory[0x50 + offset] = (char)(ChipData.fontset[offset] & 0xff);
      offset++;
    }
  }
}