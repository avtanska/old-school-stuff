/*
 *  Luokassa m‰‰ritell‰‰n ohjelman ikkuna ja 
 *  k‰yttˆliittym‰n elementit.
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class OhjelmaIkkuna extends JFrame {

  private ValikkoPalkki valikkoPalkki;
  private PainikeRivi painikeRivi;
  private RuudukkoAlusta ruudukkoAlusta;


  /*
   *  Luokan konstruktori, alustetaan tarvittavat muuttujat
   *  ja m‰‰ritell‰‰n ikkunan asettelu.
   */

  public OhjelmaIkkuna() {
    setTitle("Game of Life");
    setSize(607, 480);
    setResizable(false);

    ruudukkoAlusta = new RuudukkoAlusta();
    painikeRivi = new PainikeRivi(ruudukkoAlusta);
    valikkoPalkki = new ValikkoPalkki(this, ruudukkoAlusta);   
   
    ruudukkoAlusta.asetaPainikeRivi(painikeRivi);

    Container contentPane = getContentPane();
    contentPane.add(ruudukkoAlusta, "Center");
    contentPane.add(painikeRivi, "South");

    // Hoidellaan ikkunan sulkeminen sis‰luokalla,
    // niin ei tarvitse korvata turhia metodeita.

    addWindowListener(
      new WindowAdapter() {
        public void windowClosing(WindowEvent tapahtuma) {
          System.exit(0);
        }
      }
    );    

  }


}