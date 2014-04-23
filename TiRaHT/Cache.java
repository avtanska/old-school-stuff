import java.io.*;

public class Cache {

  private AVLPuu puu;
  private Jono jono;
  private Log log;

  private long CACHE_SIZE; // max v‰limuistin koko tavuissa
  private long cacheFreeSpace;
  private int lkm = 0; // tiedostojen lukum‰‰r‰ v‰limuistissa

  public Cache(long cacheSize) {
    puu = new AVLPuu();
    jono = new Jono();
    CACHE_SIZE = cacheSize;
    cacheFreeSpace = cacheSize;
  }

  // pyydet‰‰n tiedostoa v‰limuistista
  // logiikka kuvattu LIITE 1:ss‰

  public String request(String tiedostonNimi) {

    // tarkastetaan lˆytyykˆ v‰limuistista
    Solmu pyydettyTiedosto = find(tiedostonNimi);

    if (pyydettyTiedosto != null) { // tiedosto v‰limuistissa
      File tiedostoLevylla = new File(tiedostonNimi);

      if (tiedostoLevylla.lastModified() >
          pyydettyTiedosto.getObject().lastModified()) {

        // levytiedosto uudempi kuin v‰limuistissa oleva
        // p‰ivitet‰‰n v‰limuistissa olevan tiedoston tiedot
        String palauta = update(pyydettyTiedosto, tiedostoLevylla);
        if (log != null)
          log.write(palauta);
        return palauta;
      }
      else { // tiedostoa ei ole muutettu
        String palauta = update(pyydettyTiedosto); // siirret‰‰n jonon per‰‰n
        if (log != null)
          log.write(palauta);
        return palauta;
      }
    }
    else { // tiedosto ei v‰limuistissa
      String palauta = insert(tiedostonNimi); // lis‰t‰‰n v‰limuistiin
      if (log != null)
        log.write(palauta);
      return palauta;
    }
  }


  // lis‰t‰‰n tiedosto rakenteeseen, jos onnistuu

  private String insert(String tiedostonNimi) {
    File tiedosto = new File(tiedostonNimi);

    if (tiedosto.exists()) { // tiedosto on olemassa
      Tiedosto cacheTiedosto =
          new Tiedosto(tiedosto.getPath(), tiedosto.length(),
                       Lue.tavutTiedostosta(tiedosto),
                       tiedosto.lastModified());

      if (tiedosto.length() > CACHE_SIZE) // tiedosto liian suuri cacheen
        return "HTTP 1.0/200 OK (too big for cache) " + cacheTiedosto;

      // jos v‰limuistissa ei tarpeeksi vapaata tilaa
      // poistetaan vanhoja tiedostoja
      if (cacheFreeSpace < tiedosto.length()) {
        while (cacheFreeSpace < tiedosto.length()) {
          // lis‰t‰‰n vapaata tilaa poistetun koolla
          cacheFreeSpace += removeLRU();
        }
      }

      // nyt kaikki ehdot t‰yttyv‰t
      // lis‰t‰‰n tiedosto v‰limuistirakenteeseen
      puu.insert( jono.toQueue(cacheTiedosto) );
      cacheFreeSpace -= tiedosto.length();
      lkm++;
      return "HTTP 1.0/200 OK (fetched to cache) " + cacheTiedosto;
    }
    else { // tiedostoa ei lˆytynyt levylt‰k‰‰n
      return "HTTP 1.0/404 Not Found '" + tiedostonNimi + "'";
    }
  }


  // p‰ivitet‰‰n tietorakenne onnistuneen haun yhteydess‰
  // (siirret‰‰n tiedosto jonon per‰‰n)

  private String update(Solmu cacheSolmu) {
    Tiedosto cacheTiedosto = cacheSolmu.getObject();
    jono.moveToEnd(cacheSolmu);
    return "HTTP 1.0/200 OK (found in cache) " + cacheTiedosto;
  }


  // p‰ivitet‰‰n tietorakenne, jos levyll‰ uudempi versio
  // (muutetaan tiedot ja siirret‰‰n jonon per‰‰n)

  private String update(Solmu cacheSolmu, File levyTiedosto) {
    Tiedosto cacheTiedosto = cacheSolmu.getObject();

    // p‰ivitetty tiedosto levyll‰ suurempi kuin v‰limuistin koko
    if (levyTiedosto.length() > CACHE_SIZE) {
      // poistetaan vanha tiedosto rakenteesta
      // palautetaan levylt‰ muutettu tiedosto
      remove(cacheTiedosto);
      Tiedosto palautaTiedosto =
          new Tiedosto(levyTiedosto.getPath(), levyTiedosto.length(),
                       Lue.tavutTiedostosta(levyTiedosto),
                       levyTiedosto.lastModified());

      return "HTTP 1.0/200 OK (modified file too big for cache) " +
             palautaTiedosto;
    }

    // tiedosto jonon per‰‰n, jotta se ei joudu poistouhan alle
    jono.moveToEnd(cacheSolmu);

    // p‰ivitell‰‰n tietoja
    cacheFreeSpace += cacheTiedosto.getLength();
    cacheTiedosto.setLastModified(levyTiedosto.lastModified());
    cacheTiedosto.setLength(levyTiedosto.length());

    // p‰ivitetty tiedosto suurempi kuin v‰limuistin vapaa tila
    // poistetaan riitt‰v‰ m‰‰r‰ vanhoja tiedostoja
    if (cacheFreeSpace < cacheTiedosto.getLength()) {
      while (cacheFreeSpace < cacheTiedosto.getLength()) {
        // lis‰t‰‰n vapaata tilaa poistetun koolla
        cacheFreeSpace += removeLRU();
      }
    }

    // p‰ivitet‰‰n datasis‰ltˆ vasta kun v‰limuistissa riitt‰v‰sti tilaa
    cacheTiedosto.setBytes(Lue.tavutTiedostosta(levyTiedosto));

    cacheFreeSpace -= cacheTiedosto.getLength();
    return "HTTP 1.0/200 OK (updated in cache) " + cacheTiedosto;
  }


  // poistetaan tiedosto v‰limuistista
  // tarvitaan, jos p‰ivitetty tiedosto liian suuri v‰limuistiin

  private void remove(Tiedosto cacheTiedosto) {
    jono.remove( puu.remove(new Solmu(cacheTiedosto)) );
    cacheFreeSpace += cacheTiedosto.getLength();
    --lkm;
  }


  // poistaa 'Least Recently Used' -tiedoston v‰limuistista
  // poistaa jonon keulilta, palauttaa poistetun tiedoston koon

  private long removeLRU() {
    Solmu poistettava = puu.remove( jono.fromQueue() );
    lkm--;
    return poistettava.getObject().getLength();
  }


  // etsii tiedostoa v‰limuistista

  private Solmu find(String tiedostonNimi) {
    return puu.find(new Solmu(new Tiedosto(tiedostonNimi)));
  }


  // asettaa v‰limuistin tapahtumien lokitiedoston

  public boolean setLogFile(String lokiTiedosto) {
    try {
      log = new Log(lokiTiedosto);
    } catch (FileNotFoundException e) {
      return false;
    }
    return true;
  }

  // n‰ytt‰‰ v‰limuistin tietoja

  public String getCacheInfo() {
    return "V‰limuistin koko: " + CACHE_SIZE + " tavua\n" +
           "Vapaana: " + cacheFreeSpace + " tavua\n" +
           "Tiedostoja v‰limuistissa: " + lkm + "\n";
  }

  // tulostaa tietorakenteen

  public String toString() {
    return "" + jono + "\n" + puu;
  }

}
