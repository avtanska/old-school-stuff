import java.io.*;
import java.text.*;
import java.util.*;

public class Log {

  private String logFile;
  PrintWriter out;


  // luodaan PrintWriter-olio, jossa automaattinen
  // puskurin tyhjennys käytössä

  public Log(String logFile) throws FileNotFoundException {
    out = new PrintWriter( new FileOutputStream(logFile,true), true );
  }


  // kirjoitetaan yksi rivi lokitiedostoon

  public void write(String entry) {
    SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
    entry = format.format(new Date()) + " " + entry;
    out.println(entry);
  }

}


