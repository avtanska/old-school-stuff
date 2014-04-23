///////////////////////////////////////////////////////////////////////
//                                  Tietorakenteet K2000
//                                  Arto Wikla 5.2.2000
//                                  Muokannut Atte Tanskanen 2004
//                                  
//Luokka public class Jono                     
//
// Jonon toteutus kaksisuuntaisena linkitettyn‰ rengaslistana. 
// Alkiot ovat tyyppi‰ Tiedosto. 
//
// Vain viimeiseen solmuun yll‰pidet‰‰n linkki‰ (per‰).
// Per‰n seuraaja on keula.
//
// Toteutuksessa solmut ovat luokan Solmu ilmentymi‰.
// 
// konstruktori:
//            public Jono() luo tyhj‰n jonon 
//
// aksessorit:
//
//   public Solmu toQueue(Tiedosto alkio)
//                    laittaa "alkio"n jonoo; null ei kelpaa alkioksi;
//                    palauttaa viitteen uuteen alkioon 
//
//   public Solmu fromQueue()
//                    palauttaa arvonaan jonon ensimm‰isen alkion ja
//                    poistaa sen jonosta; jos jono on tyhj‰, 
//                    metodi palauttaa arvon null 
//
//   public boolean isEmpty() true jos jono on tyhj‰ 
//
//   public void makeEmpty() tyhj‰‰ jonon
//
//   public int getN() jonon alkioiden m‰‰r‰
//
//   public String toString()
//                    muodossa: keula: ( 7 5 8 -23 8 ) :per‰
//
////////////////////////////////////////////////////////////////////////


public class Jono {

  private Solmu per‰;   // per‰.linkki on keula!
  private int lkm;

  public Jono() {
    per‰ = null;   // tyhj‰ jono
    lkm = 0;
  }

  public Solmu toQueue(Tiedosto alkio) { 

    if (alkio == null) // null ei kelpaa
      return null;    // selv‰n teki

    if (per‰ == null) {
      per‰ = new Solmu();    // 1. solmu
      per‰.tieto  = alkio;
      per‰.edellinen = per‰;
      per‰.seuraava = per‰;    // linkitet‰‰n itseens‰
      lkm = 1;
      return per‰;           // valmista tuli
    }

    Solmu vanhaPer‰ = per‰;
    per‰ = new Solmu();  
    per‰.tieto  = alkio;
    per‰.seuraava = vanhaPer‰.seuraava; // uusi per‰ osoittamaan keulaan
    per‰.edellinen = vanhaPer‰;
    vanhaPer‰.seuraava.edellinen = per‰;
    vanhaPer‰.seuraava = per‰;  // ja toiseksi viimeinen osoittamaan
    ++lkm;                     // uuteen viimeiseen

    return per‰;
  }

  public Solmu fromQueue() {

    if (per‰ == null) // tyhj‰
      return null;    // valmista tuli 

    Solmu keula = per‰.seuraava;

    if (per‰ == keula)    // 1 alkion jono: viimeinenkin h‰vitet‰‰n
      per‰ = null;
    else {                // pidempi jono
      per‰.seuraava = keula.seuraava;
      keula.seuraava.edellinen = per‰;
    }

    --lkm;
    return keula;
  }


  public Solmu moveToEnd(Solmu siirrett‰v‰) {
    if (siirrett‰v‰ == null)
      return null;

    if (siirrett‰v‰ == per‰)
      return per‰;

    siirrett‰v‰.edellinen.seuraava = siirrett‰v‰.seuraava;
    siirrett‰v‰.seuraava.edellinen = siirrett‰v‰.edellinen;    
    siirrett‰v‰.seuraava = per‰.seuraava;
    siirrett‰v‰.edellinen = per‰;
    per‰.seuraava.edellinen = siirrett‰v‰;    
    per‰.seuraava = siirrett‰v‰;
    per‰ = siirrett‰v‰;
    return per‰;
  }


  public Solmu remove(Solmu poistettava) {
    if (poistettava == null)
      return null;      
  
    if (per‰ == per‰.seuraava)
      per‰ = null;
    else {
      if (poistettava == per‰)
        per‰ = poistettava.edellinen;
      poistettava.seuraava.edellinen = poistettava.edellinen;
      poistettava.edellinen.seuraava = poistettava.seuraava;
    }
    return poistettava;
  }

  public boolean isEmpty() { return per‰ == null; }

  public void makeEmpty() {per‰ = null; }

  public int getN() { return lkm; } 

  public String toString() {

    String mjono=" keula: ( "; 

    if (!isEmpty()) {

      Solmu p = per‰;
      do {
        mjono += p.seuraava.tieto + " ";
        p = p.seuraava;
      } while (p != per‰);
    }
    return mjono+") :per‰";
  }
}
