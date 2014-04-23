public class Solmu implements Comparable {
    public Tiedosto tieto;
    public Solmu edellinen;
    public Solmu seuraava;

    public Solmu() {
      tieto = null;
      edellinen = null;
      seuraava = null;
    }

    public Solmu(Tiedosto alkio) {
      tieto = alkio;
      edellinen = null;
      seuraava = null;
    }

    public int compareTo(Object toinen) {
      return tieto.compareTo(toinen);
    }

    // palautetaan linkki solmuun tallennettuun Tiedostoon
    public Tiedosto getObject() {
      return tieto;
    }

    public String toString() {
      return tieto.toString();
    }
}

