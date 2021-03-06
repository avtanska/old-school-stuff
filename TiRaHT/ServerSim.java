import java.io.*;

public class ServerSim {

  private static boolean lokiPäällä = false;
  private static boolean tiraPäällä = false;
  private static long cacheSize = 100000; // KB, 100000 vakioasetus
  private static String lokiTiedosto = "log.txt"; // vakioasetus log.txt

  public static void main(String args[]) {

    int valinta;

    printMenu();

    while ( (valinta = Lue.kluku()) != 9 ) {

      switch (valinta) {

        case 1:
          runSimulation();
          break;

        case 2:
          runSimulationFromFile();
          break;

        case 3:
          setSettings();
          break;

        case 9:
          break;

        default:
          System.out.println("\nValitse haluamasi toiminnon numero!" +
                             " (virhe 01)");
          break;

      }

      printMenu();

    }

  }


  // tulostetaan päävalikko

  private static void printMenu() {
    System.out.println("\n== Välimuistin simulointi ==\n");

    System.out.println("1. aloita simulaatio (käsinsyöttö)");
    System.out.println("2. aja simulaatio tiedostosta");
    System.out.println("3. asetukset");
    System.out.println("9. lopeta ohjelma");

    System.out.print("\n> ");
  }


  // simulaation ajo käsisyötöllä

  private static void runSimulation() {
    System.out.println("");

    Cache cache = new Cache(cacheSize); // luodaan uusi välimuistirakenne
    cache.setLogFile(lokiTiedosto); // asetetaan lokitiedosto
    System.out.println(cache.getCacheInfo());
    if (tiraPäällä)
      System.out.println(cache);

    System.out.println("Anna pyydettävän tiedoston nimi, \\q lopettaa");
    System.out.print("> ");
    String pyydettävä = Lue.rivi();

    while (!pyydettävä.equals("\\q")) {

      // pyydetään tiedostoa välimuistista
      System.out.println("\n" + cache.request(pyydettävä) + "\n");
      System.out.println(cache.getCacheInfo());
      if (tiraPäällä)
        System.out.println("\n" + cache + "\n");

      System.out.println("Anna pyydettävän tiedoston nimi, \\q lopettaa");
      System.out.print("> ");
      pyydettävä = Lue.rivi();
    }
  }


  // simulaation ajo tiedostosta

  private static void runSimulationFromFile() {
    System.out.println("");

    Cache cache = new Cache(cacheSize); // luodaan uusi välimuistirakenne
    cache.setLogFile(lokiTiedosto); // asetetaan lokitiedosto
    System.out.println(cache.getCacheInfo());
    if (tiraPäällä)
      System.out.println(cache);

    String ajoTiedosto;
    boolean ajoTiedostoOlemassa = true;
    String pyydettävä;
    String jatketaanko = "";

    do {
      if (!ajoTiedostoOlemassa) {
        System.out.println("\nAjettavaa tiedostoa ei löydy! (virhe 05)\n");
      }
      System.out.println("Anna ajettavan tiedoston nimi. \\q lopettaa");
      System.out.print("> ");
      ajoTiedosto = Lue.rivi();
      ajoTiedostoOlemassa = (new File(ajoTiedosto)).exists();
    } while ( !ajoTiedostoOlemassa && !ajoTiedosto.equals("\\q") );

    try {

      // luodaan puskuroitu lukija tiedoston lukua varten
      // Lue.riviPuskurista() lukee puskurista rivin kerrallaan
      BufferedReader ajoTiedostoPuskuri =
          new BufferedReader( new FileReader( ajoTiedosto ) );

      while ( (pyydettävä = Lue.riviPuskurista(ajoTiedostoPuskuri)) != null &&
              !jatketaanko.equals("\\q") ) {

        // pyydetään tiedostoa välimuistia
        System.out.println("\n" + cache.request(pyydettävä) + "\n");
        System.out.println(cache.getCacheInfo());
        if (tiraPäällä)
          System.out.println("\n" + cache + "\n");

        System.out.println("Paina enteriä jatkaaksesi, \\q lopettaa");
        System.out.print("> ");
        jatketaanko = Lue.rivi();
      }
    } catch (Exception e) {

    }
  }


  // asetusten asetus

  private static void setSettings() {
    System.out.println("\nAnna välimuistin koko tavuissa (tällä hetkellä " +
                       cacheSize + " tavua)");
    System.out.print("> ");
    cacheSize = Lue.lluku();

    System.out.println("\nHaluatko nähdä tietorakenteen kuvauksen\n" +
                       "jokaisen tiedoston pyynnön jälkeen? (k/e)");
    System.out.print("> ");
    String vastaus = Lue.rivi();
    while ( vastaus.compareToIgnoreCase("k") != 0 &&
            vastaus.compareToIgnoreCase("e") != 0 ) {
      System.out.println("Anna joko kirjain " +
                         " k tai e. (virhe 04)");
      System.out.print("> ");
      vastaus = Lue.rivi();
    }

    if (vastaus.compareToIgnoreCase("k") == 0)
      tiraPäällä = true;
    else if (vastaus.compareToIgnoreCase("e") == 0)
      tiraPäällä = false;
  }

}
