/*
 *  Luokkassa m‰‰ritelty kaikki alareunan napit.
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class PainikeRivi extends JPanel implements ActionListener {

  private JButton k‰ynnist‰Pys‰yt‰Nappi;
  private JButton tyhjenn‰Nappi;
  private JButton arvoNappi;
  private JButton seuraavaNappi;
  private JLabel sukupolviKentt‰;
  private JLabel solujaKentt‰;
  private Font nappienKirjasin;
  private Insets nappienMarginaali;
  private RuudukkoAlusta ruudukkoAlusta;
  private boolean onkoK‰ynniss‰;


  /*
   *  Luokan konstruktori, jossa luodaan nappien ja
   *  tekstikenttien sis‰ltˆ ja ulkon‰kˆ.
   */

  public PainikeRivi(RuudukkoAlusta ruudukkoAlusta) {

    // Alustetaan luokan muuttujat, mm. luodaan
    // napit ja tekstikent‰t.

    this.ruudukkoAlusta = ruudukkoAlusta;
    onkoK‰ynniss‰ = false;
    nappienKirjasin = new Font("SansSerif", Font.BOLD, 12);
    nappienMarginaali = new Insets(0, 3, 0, 3);

    k‰ynnist‰Pys‰yt‰Nappi = new JButton("K‰ynnist‰");
    tyhjenn‰Nappi = new JButton("Tyhjenn‰");
    arvoNappi = new JButton("Arvo");
    seuraavaNappi = new JButton("Seuraava");

    sukupolviKentt‰ = new JLabel("Sukupolvi: 0");
    solujaKentt‰ = new JLabel("Soluja: 0");

    // M‰‰ritell‰‰n nappien fontit ja nappien
    // tekstien marginaalit.

    k‰ynnist‰Pys‰yt‰Nappi.setFont(nappienKirjasin);
    tyhjenn‰Nappi.setFont(nappienKirjasin);
    arvoNappi.setFont(nappienKirjasin);
    seuraavaNappi.setFont(nappienKirjasin);

    k‰ynnist‰Pys‰yt‰Nappi.setMargin(nappienMarginaali);
    tyhjenn‰Nappi.setMargin(nappienMarginaali);
    arvoNappi.setMargin(nappienMarginaali);
    seuraavaNappi.setMargin(nappienMarginaali);

    // Lis‰t‰‰n komponentit oman luokan s‰iliˆˆn

    this.add(k‰ynnist‰Pys‰yt‰Nappi);
    this.add(tyhjenn‰Nappi);
    this.add(arvoNappi);
    this.add(seuraavaNappi);
    this.add(Box.createGlue());
    this.add(sukupolviKentt‰);
    this.add(solujaKentt‰);

    // M‰‰ritell‰‰n tapahtumakuuntelijat napeille

    k‰ynnist‰Pys‰yt‰Nappi.addActionListener(this);
    tyhjenn‰Nappi.addActionListener(this);
    arvoNappi.addActionListener(this);
    seuraavaNappi.addActionListener(this);
    
    this.setBorder(BorderFactory.createEtchedBorder());

  }


  /*
   *  Hoidellaan K‰ynnist‰/Pys‰yt‰-napin tapahtumat.
   */

  private void k‰ynnist‰Pys‰yt‰() {
    if (onkoK‰ynniss‰) {
      ruudukkoAlusta.pys‰yt‰Ajastin();
      k‰ynnist‰Pys‰yt‰Nappi.setText("K‰ynnist‰");
      onkoK‰ynniss‰ = false;
    }
    else {
      ruudukkoAlusta.k‰ynnist‰Ajastin();
      k‰ynnist‰Pys‰yt‰Nappi.setText("Pys‰yt‰");
      onkoK‰ynniss‰ = true;
    }
  }

  
  /* 
   *  Pys‰ytet‰‰n ajastin ja asetetaan 
   *  K‰ynnist‰/Pys‰yt‰-napin teksti.  
   */

  private void pys‰yt‰() {
    ruudukkoAlusta.pys‰yt‰Ajastin();
    k‰ynnist‰Pys‰yt‰Nappi.setText("K‰ynnist‰");
    onkoK‰ynniss‰ = false;
  }


  /*
   *  Asetetaan Sukupolvi-tekstikent‰n teksti.
   */

  public void asetaSukupolvi(int sukupolviNro) {
    sukupolviKentt‰.setText("Sukupolvi: " + sukupolviNro);
  }


  /*
   *  Asetetaan Soluja-tekstikent‰n teksti.
   */

  public void asetaSoluja(int solujaNro) {
    solujaKentt‰.setText("Soluja: " + solujaNro);
  }


  /* 
   *  Hoidellaan nappien aiheuttamat tapahtumat.
   */

  public void actionPerformed(ActionEvent tapahtuma) {
    Object aiheuttaja = tapahtuma.getSource();
    
    // K‰ynnist‰/Pys‰yt‰-nappi
    
    if (aiheuttaja == k‰ynnist‰Pys‰yt‰Nappi) {
      k‰ynnist‰Pys‰yt‰();
    }

    // Tyhjenn‰-nappi

    if (aiheuttaja == tyhjenn‰Nappi) {
      pys‰yt‰();
      ruudukkoAlusta.tyhjenn‰();
    }

    // Arvo-nappi

    if (aiheuttaja == arvoNappi) {
      pys‰yt‰();
      ruudukkoAlusta.arvo();
    }

    // Seuraava-nappi

    if (aiheuttaja == seuraavaNappi) {
      pys‰yt‰();
      ruudukkoAlusta.seuraavaSukupolvi();
    }
  }

}
