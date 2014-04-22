/*
 *  Luokassa m‰‰ritell‰‰n Tietoja-dialogi 
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class TietojaDialogi extends JDialog implements ActionListener {
      
  /*
   *  Luodaan dialogi.
   */
  
  public TietojaDialogi(JFrame vanhempi) {
    
    super(vanhempi, "Tietoja Game of Life:sta", true);    
    
    JPanel p = new JPanel();    
    JLabel l = new JLabel("Game of Life");    
    p.add(l, "Center");            
    
    JPanel p2 = new JPanel();    
    JLabel l2 = new JLabel("Atte Tanskanen 2002");
    p2.add(l2);        
    
    JPanel p3 = new JPanel();
    JButton ok = new JButton("Ok");
    p3.add(ok);
    
    Container contentPane = getContentPane();
    contentPane.add(p, "North");
    contentPane.add(p2, "Center");
    contentPane.add(p3, "South"); 
    
    ok.addActionListener(this);
         
    setSize(230, 150);
  }
  
  
  /*
   *  K‰sitell‰‰n dialogin aiheuttamat tapahtumat.
   */
  
  public void actionPerformed(ActionEvent evt) {
    setVisible(false);
  }
  
}  