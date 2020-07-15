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
  }
  void run(){
    //fetch data - our char is 16bit, we adding two 8bits into one.
    char opcode = (char)((memory[pc] << 8) | memory[pc+1]);

    //decode opcode

    //excute opcode
  }

  public byte[] getDisplay(){
    return display;
  }
}