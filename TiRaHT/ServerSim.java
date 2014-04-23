import java.io.*;

public class ServerSim {

  private static boolean lokiP‰‰ll‰ = false;
  private static boolean tiraP‰‰ll‰ = false;
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


  // tulostetaan p‰‰valikko

  private static void printMenu() {
    System.out.println("\n== V‰limuistin simulointi ==\n");

    System.out.println("1. aloita simulaatio (k‰sinsyˆttˆ)");
    System.out.println("2. aja simulaatio tiedostosta");
    System.out.println("3. asetukset");
    System.out.println("9. lopeta ohjelma");

    System.out.print("\n> ");
  }


  // simulaation ajo k‰sisyˆtˆll‰

  private static void runSimulation() {
    System.out.println("");

    Cache cache = new Cache(cacheSize); // luodaan uusi v‰limuistirakenne
    cache.setLogFile(lokiTiedosto); // asetetaan lokitiedosto
    System.out.println(cache.getCacheInfo());
    if (tiraP‰‰ll‰)
      System.out.println(cache);

    System.out.println("Anna pyydett‰v‰n tiedoston nimi, \\q lopettaa");
    System.out.print("> ");
    String pyydett‰v‰ = Lue.rivi();

    while (!pyydett‰v‰.equals("\\q")) {

      // pyydet‰‰n tiedostoa v‰limuistista
      System.out.println("\n" + cache.request(pyydett‰v‰) + "\n");
      System.out.println(cache.getCacheInfo());
      if (tiraP‰‰ll‰)
        System.out.println("\n" + cache + "\n");

      System.out.println("Anna pyydett‰v‰n tiedoston nimi, \\q lopettaa");
      System.out.print("> ");
      pyydett‰v‰ = Lue.rivi();
    }
  }


  // simulaation ajo tiedostosta

  private static void runSimulationFromFile() {
    System.out.println("");

    Cache cache = new Cache(cacheSize); // luodaan uusi v‰limuistirakenne
    cache.setLogFile(lokiTiedosto); // asetetaan lokitiedosto
    System.out.println(cache.getCacheInfo());
    if (tiraP‰‰ll‰)
      System.out.println(cache);

    String ajoTiedosto;
    boolean ajoTiedostoOlemassa = true;
    String pyydett‰v‰;
    String jatketaanko = "";

    do {
      if (!ajoTiedostoOlemassa) {
        System.out.println("\nAjettavaa tiedostoa ei lˆydy! (virhe 05)\n");
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

      while ( (pyydett‰v‰ = Lue.riviPuskurista(ajoTiedostoPuskuri)) != null &&
              !jatketaanko.equals("\\q") ) {

        // pyydet‰‰n tiedostoa v‰limuistia
        System.out.println("\n" + cache.request(pyydett‰v‰) + "\n");
        System.out.println(cache.getCacheInfo());
        if (tiraP‰‰ll‰)
          System.out.println("\n" + cache + "\n");

        System.out.println("Paina enteri‰ jatkaaksesi, \\q lopettaa");
        System.out.print("> ");
        jatketaanko = Lue.rivi();
      }
    } catch (Exception e) {

    }
  }


  // asetusten asetus

  private static void setSettings() {
    System.out.println("\nAnna v‰limuistin koko tavuissa (t‰ll‰ hetkell‰ " +
                       cacheSize + " tavua)");
    System.out.print("> ");
    cacheSize = Lue.lluku();

    System.out.println("\nHaluatko n‰hd‰ tietorakenteen kuvauksen\n" +
                       "jokaisen tiedoston pyynnˆn j‰lkeen? (k/e)");
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
      tiraP‰‰ll‰ = true;
    else if (vastaus.compareToIgnoreCase("e") == 0)
      tiraP‰‰ll‰ = false;
  }

}
