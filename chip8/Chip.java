package chip8;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Chip{
  private char[] memory; //memory 4k
  private char[] V; //register
  private char I; //address pointer
  private char pc;

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
  }
  public void run(){
    //fetch data - our char is 16bit, we adding two 8bits into one.
    char opcode = (char)((memory[pc] << 8) | memory[pc+1]);
    System.out.println(Integer.toHexString(opcode) + " : ");

    //decode opcode

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
        try{
          input.close();
        }
        catch(IOException e){}
      }
    }
  }

  public boolean needRedraw(){
    return needRedraw;
  }

  public void removeDrawFlag(){
    needRedraw = false;
  }
}