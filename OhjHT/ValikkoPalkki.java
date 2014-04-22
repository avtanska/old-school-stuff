/*
 *  Luokkassa m‰‰ritelty ohjelman valikoiden ulkoasu
 *  ja toiminta.
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ValikkoPalkki extends JMenuBar implements ActionListener {
    
  private JMenuBar valikkoPalkki;
  private JMenu tiedostoValikko; 
  private JMenu nopeusValikko;
  private JMenuItem tietojaKohta;
  private JMenuItem lopetaKohta;
  private JMenuItem nopeusYksi;
  private JMenuItem nopeusKaksi;
  private JMenuItem nopeusKolme;
  private JMenuItem nopeusNelj‰;
  private JMenuItem nopeusViisi;
  private TietojaDialogi dialogi;
  private JFrame ylempiIkkuna;
  private RuudukkoAlusta ruudukkoAlusta;
    
  
  /*
   * Alustetaan valikkopalkki
   */
  
  public ValikkoPalkki(JFrame ylempiIkkuna, RuudukkoAlusta ruudukkoAlusta) {
    
    this.ylempiIkkuna = ylempiIkkuna;    
    this.ruudukkoAlusta = ruudukkoAlusta;     
    
    valikkoPalkki = new JMenuBar();
    
    // Luodaan valikot
    
    tiedostoValikko = new JMenu("Tiedosto");    
    tietojaKohta = new JMenuItem("Tietoja");
    lopetaKohta = new JMenuItem("Lopeta");    
    
    nopeusValikko = new JMenu("Nopeus");
    nopeusYksi = new JMenuItem("1 - Nopein");
    nopeusKaksi = new JMenuItem("2 - Nopeampi");
    nopeusKolme = new JMenuItem("3 - Normaali");
    nopeusNelj‰ = new JMenuItem("4 - Hitaampi");
    nopeusViisi = new JMenuItem("5 - Hitain");
    
    ylempiIkkuna.setJMenuBar(valikkoPalkki);
    
    valikkoPalkki.add(tiedostoValikko);
    tiedostoValikko.add(tietojaKohta);   
    tiedostoValikko.add(lopetaKohta);
    
    valikkoPalkki.add(nopeusValikko);
    nopeusValikko.add(nopeusYksi);
    nopeusValikko.add(nopeusKaksi);
    nopeusValikko.add(nopeusKolme);
    nopeusValikko.add(nopeusNelj‰);
    nopeusValikko.add(nopeusViisi);
    
    // Lis‰t‰‰n tapahtumakuuntelijat valikoihin
    
    tietojaKohta.addActionListener(this);
    lopetaKohta.addActionListener(this);
    nopeusYksi.addActionListener(this);
    nopeusKaksi.addActionListener(this);
    nopeusKolme.addActionListener(this);
    nopeusNelj‰.addActionListener(this);
    nopeusViisi.addActionListener(this);
    
  }
    
  
  /*
   * K‰sitell‰‰n valikon aiheuttamat tapahtumat
   */    
  
  public void actionPerformed(ActionEvent tapahtuma) {
    Object aiheuttaja = tapahtuma.getSource();
    
    if (aiheuttaja == nopeusYksi)
      ruudukkoAlusta.asetaViive(1);
    if (aiheuttaja == nopeusKaksi)
      ruudukkoAlusta.asetaViive(100);
    if (aiheuttaja == nopeusKolme)
      ruudukkoAlusta.asetaViive(300);  
    if (aiheuttaja == nopeusNelj‰)
      ruudukkoAlusta.asetaViive(700);
    if (aiheuttaja == nopeusViisi)
      ruudukkoAlusta.asetaViive(1500);
    
    if (aiheuttaja == tietojaKohta) {
      int x = (int)ylempiIkkuna.getLocation().getX(); // ylemm‰n ikkunan 
      int y = (int)ylempiIkkuna.getLocation().getY(); // sijainti ruudulla

      if (dialogi == null) { // ensimm‰inen kerta
        dialogi = new TietojaDialogi(ylempiIkkuna);
        dialogi.setLocation(x+175, y+125); // asetetaan dialogi keskelle ikkunaa
        dialogi.show();
      }
      else {
        dialogi.setLocation(x+175, y+125);
        dialogi.show();
      }
    }
    else if (aiheuttaja == lopetaKohta) {
      System.exit(0);
    }
    
    
    
    
  }
  
}
