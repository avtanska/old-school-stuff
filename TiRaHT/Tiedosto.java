import java.text.*;
import java.util.*;

public class Tiedosto implements Comparable {
    private String name;
    private long length;
    private byte[] bytes;
    private long lastModified;

    public Tiedosto(String name) {
      this.name = name;
    }

    public Tiedosto(String name, long length, byte[] bytes, long lastModified) {
      this.name = name;
      this.length = length;
      this.bytes = bytes;
      this.lastModified = lastModified;
    }


    public int compareTo(Object toinen) {
      return this.name.compareTo( ((Tiedosto)toinen).name );
    }


    public long getLength() {
      return length;
    }

    public long lastModified() {
      return lastModified;
    }

    public void setLength(long length) {
      this.length = length;
    }

    public void setBytes(byte[] bytes) {
      this.bytes = bytes;
    }

    public void setLastModified(long lastModified) {
      this.lastModified = lastModified;
    }

    // muutetaan lastModified luettavaan muotoon

    private String lastModifiedString() {
      SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
      return format.format(new Date(lastModified));
    }

    // tiedoston merkkiesitys

    public String toString() {
      return "[ " + name + " | " + length + " bytes | " +
             lastModifiedString() + " ]";
    }

}
