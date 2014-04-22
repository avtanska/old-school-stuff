/*
 *  Luokassa luodaan ja n�ytet��n ohjelman ikkuna.
 */

import javax.swing.*;

public class GameOfLife {
    
  /*
   *  Luokan main-metodi, joka k�ynnist�� ohjelman.
   */
  
  public static void main(String[] args) {
    JFrame ohjelmaIkkuna = new OhjelmaIkkuna();
    ohjelmaIkkuna.show();

    // Asetetaan ikkunan tyyliksi k�ytt�j�rjestelm�st�
    // riippumaton ulkoasu.

    String laf = UIManager.getCrossPlatformLookAndFeelClassName();

    try {
      UIManager.setLookAndFeel(laf);
      SwingUtilities.updateComponentTreeUI(ohjelmaIkkuna);
    }
    catch(Exception e) { System.out.println("Exception: " + e); }    
  }
  
}