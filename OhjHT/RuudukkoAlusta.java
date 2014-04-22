/*
 *  Luokassa toteutetaan kaikki simulaation liittyv‰
 *  piirt‰minen ja hiiren tapahtumak‰sittely.
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class RuudukkoAlusta extends JPanel implements MouseMotionListener, 
                                                      MouseListener {


  private final int SOLUNKOKO = 4;
  private boolean vasenNappi;
  private SoluRuudukko soluRuudukko;
  private boolean[][] ruudukko;
  private int viive;
  private int solujaApu;
  private int sukupolviApu;
  private Timer ajastin;
  private PainikeRivi painikeRivi;

  
  /*
   *  Luokan konstruktori.
   */

  public RuudukkoAlusta() {
    
    viive = 300;
    solujaApu = 0;
    sukupolviApu = 0;
    
    // Alustetaan uusi soluruudukko
    
    soluRuudukko = new SoluRuudukko(SOLUNKOKO);
    soluRuudukko.tyhjenn‰();
    ruudukko = soluRuudukko.annaRuudukko();

    addMouseListener(this);
    addMouseMotionListener(this);

    // Luodaan Timer-luokasta ilmentym‰ hoitamaan sukupolven
    // vaihtuminen aikav‰lein. T‰ss‰ k‰ytetty sis‰luokaa
    // ActionListener-rajapinnan toteuttamiseen, jotta
    // ajastimeen tarvittavat toiminnot olisivat kaikki samassa
    // paikassa.

    ajastin = new Timer(viive,
      new ActionListener() {
        public void actionPerformed(ActionEvent tapahtuma) {
	        soluRuudukko.generoiSeuraavaSukupolvi();
	        ruudukko = soluRuudukko.annaRuudukko();
	        repaint();
        }
      }
    );

  }

  
  /*
   *  Piirret‰‰n el‰v‰t solut ruudulle.
   */

  private void piirr‰El‰v‰tSolut(Graphics piirtopinta, boolean[][] ruudukko) {
    Color solunV‰ri = new Color(136, 0, 0);
    piirtopinta.setColor(solunV‰ri);

    for (int y = 0; y < soluRuudukko.annaKorkeus(); y++) {
      for (int x = 0; x < soluRuudukko.annaLeveys(); x++) {
        if (ruudukko[y][x]) {
          int drawFromX = x * SOLUNKOKO;
          int drawFromY = y * SOLUNKOKO;
          piirtopinta.fillRect(drawFromX, drawFromY,
                     SOLUNKOKO, SOLUNKOKO);
        }
      }
    }

  }


  /*
   *  P‰ivitet‰‰n jokaisella piirtokerralla Sukupolvi-, ja
   *  Soluja-tekstikenttien arvot.
   */

  private void p‰ivit‰SukupolviJaSolujaKent‰t() {
    painikeRivi.asetaSukupolvi(soluRuudukko.annaSukupolvi());
    painikeRivi.asetaSoluja(soluRuudukko.annaSolujenM‰‰r‰());
  }


  /*
   *  Katsotaan onko simulaatio vakiintunut toistuviin kuvioihin.
   *  Tarkistetaan solujen lukum‰‰r‰ alussa, ja siit‰ l‰htien sadan
   *  sukupolven v‰lein. Jos per‰kk‰isiss‰ sadalla jaollisissa
   *  sukupolvissa on sama m‰‰r‰ soluja, pys‰ytet‰‰n simulaatio.
   *  Muuten otetaan luvut talteen ja tarkistetaan taas sadan
   *  sukupolven p‰‰st‰.
   */

  private void testaaVakiintuminen() {

    // Solujen m‰‰r‰ alussa.

    if (soluRuudukko.annaSukupolvi() == 0) {
      sukupolviApu = soluRuudukko.annaSukupolvi(); // tietenkin nolla
      solujaApu = soluRuudukko.annaSolujenM‰‰r‰();
    }

    // Tarkistetaan solujen m‰‰r‰ sadan sukupolven kuluttua.
    // Jos lukujen erotus on nolla, pys‰ytet‰‰n simulaatio.
    // Jos erotus ei ole nolla, otetaan sukupolven numero ja
    // solujen m‰‰r‰ talteen ja jatketaan simulaatiota.

    if (soluRuudukko.annaSukupolvi() == sukupolviApu+100) {
      if (solujaApu - soluRuudukko.annaSolujenM‰‰r‰() == 0) {
        ajastin.stop();
      }
      else {
        solujaApu = soluRuudukko.annaSolujenM‰‰r‰();
        sukupolviApu = soluRuudukko.annaSukupolvi();
      }
    }
  }


  /*
   *  Piirret‰‰n ruudukon viivat.
   */

  private void piirr‰RuudukonViivat(Graphics piirtopinta) {
    for (int i = 0; i <= 600; i += SOLUNKOKO) {
      Color viivojenV‰ri = new Color(190, 190, 190);
      piirtopinta.setColor(viivojenV‰ri);
      if (i > 400) { // piirret‰‰n juuri ikkunan kokoinen viivaruudukko
        piirtopinta.drawLine(i, 0, i, 400); // pystyviivat
      }
      else {
        piirtopinta.drawLine(0, i, 600, i); // vaakaviivat
        piirtopinta.drawLine(i, 0, i, 400); // pystyviivat
      }
    }
  }

  
  /*
   *  K‰ynnistet‰‰n ajastin.
   */

  public void k‰ynnist‰Ajastin() {
    ajastin.start();
  }


  /*
   *  Pys‰ytet‰‰n ajastin.
   */

  public void pys‰yt‰Ajastin() {
    ajastin.stop();
  }


  /*
   *  Tyhjennet‰‰n ruudukko.
   */

  public void tyhjenn‰() {
    soluRuudukko.tyhjenn‰();
    repaint();
  }


  /*
   *  Arvotaan uusi tilanne ruudukkoon.
   */

  public void arvo() {
    soluRuudukko.arvoRuudukko();
    repaint();
  }


  /*
   *  Edett‰‰n seuraavaan sukupolveen.
   */

  public void seuraavaSukupolvi() {
    soluRuudukko.generoiSeuraavaSukupolvi();
    ruudukko = soluRuudukko.annaRuudukko();
    repaint();
  }


  /*
   *  Asetetaan ohjelman k‰ytt‰m‰ PainikeRivi-luokan
   *  ilmentym‰, jotta voidaan p‰ivitt‰‰ Soluja ja
   *  Sukupolvi-kent‰t painikeriviss‰.
   */

  public void asetaPainikeRivi(PainikeRivi painikeRivi) {
    this.painikeRivi = painikeRivi;
  }


  /*
   *  Asetetaan ajastimen viive millisekunneissa.
   */
  
  public void asetaViive(int viive) {
    ajastin.setDelay(viive);
    repaint();
  }
  

  /*
   *  Piirt‰misrutiinien m‰‰rittely
   */

  public void paintComponent(Graphics piirtopinta) {    
    super.paintComponent(piirtopinta);
    Color taustaV‰ri = new Color(180, 180, 180);
    setBackground(taustaV‰ri);

    piirr‰El‰v‰tSolut(piirtopinta, ruudukko);

    p‰ivit‰SukupolviJaSolujaKent‰t();

    testaaVakiintuminen();

    piirr‰RuudukonViivat(piirtopinta);    
  }


  /*
   *  MouseMotionListener ja MouseListener-rajapintaluokkien
   *  vaatimien metodien toteutus.
   */

  public void mousePressed(MouseEvent tapahtuma) {
    int x = tapahtuma.getX();
    int y = tapahtuma.getY();

    vasenNappi = SwingUtilities.isLeftMouseButton(tapahtuma);
    soluRuudukko.asetaSolunTila(x, y, SOLUNKOKO, vasenNappi);
    repaint();
  }


  public void mouseClicked(MouseEvent tapahtuma) {
    int x = tapahtuma.getX();
    int y = tapahtuma.getY();
    soluRuudukko.asetaSolunTila(x, y, SOLUNKOKO, vasenNappi);
    repaint();
  }


  public void mouseDragged(MouseEvent tapahtuma) {
    int x = tapahtuma.getX();
    int y = tapahtuma.getY();
    soluRuudukko.asetaSolunTila(x, y, SOLUNKOKO, vasenNappi);
    repaint();
  }


  /* 
   *  Tyhj‰t metodit, jotka on pakko korvata.   
   */

  public void mouseMoved(MouseEvent tapahtuma) {}

  public void mouseReleased(MouseEvent tapahtuma) {}

  public void mouseEntered(MouseEvent tapahtuma) {}

  public void mouseExited(MouseEvent tapahtuma) {}

}