import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TeeVaraus extends HttpServlet {

  final String dbDriver="org.postgresql.Driver";
  final String dbServer ="jdbc:postgresql://localhost:10388/tsoha";
  final String dbUser= "avtanska";        // replace with your db user account
  final String dbPassword ="postgres"; // replace with your password


  public void service(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
    ServletOutputStream out;
    res.setContentType("text/html");
    out= res.getOutputStream();

    String submit = req.getParameter("submit");
    HttpSession session = req.getSession(false);
    String sessionTunnus = (String)session.getValue("sessionTunnus");

    String paiva = req.getParameter("paiva");
    String kuukausi = req.getParameter("kuukausi");
    String vuosi = req.getParameter("vuosi");
    String aihe = req.getParameter("aihe");
    String henkilo = req.getParameter("henkilo");
    String ryhma = req.getParameter("ryhma");
    String alkuaika = req.getParameter("alkuaika");
    String kesto = req.getParameter("kesto");
    String nakyvyys = req.getParameter("nakyvyys");
    String virhe = "";

    /*
     * Asetetaan tyhjät arvot
     */

    if (submit != null) {
      if (henkilo.equals("tyhjä")) {
        henkilo = null;
      }
      if (ryhma.equals("tyhjä")) {
        ryhma = null;
      }
      if (nakyvyys.equals("tyhjä")) {
        nakyvyys = null;
      }
      if (paiva.equals("tyhjä") || kuukausi.equals("tyhjä")
          || vuosi.equals("tyhjä")) {
        paiva = null;
      }
      if (alkuaika.equals("tyhjä") || kesto.equals("tyhjä")) {
        alkuaika = null;
      }
      if (aihe.length() == 0) {
        aihe = null;
      }

    }


    /*
     * Luodaan virheilmoitukset puuttuville kentille
     */

    if (henkilo == null && ryhma == null)
      virhe += "<span class='virhe'>Valitse henkilö tai ryhmä</span><br />";
    if (aihe == null)
      virhe += "<span class='virhe'>Varauksen aihe puuttuu</span><br />";
    if (nakyvyys == null)
      virhe += "<span class='virhe'>Valitse näkyvyys</span><br />";
    if (paiva == null)
      virhe += "<span class='virhe'>Valitse päivämäärä</span><br />";
    if (alkuaika == null)
      virhe += "<span class='virhe'>Valitse alkuaika ja kesto</span><br />";


    /*
     * Jos tullaan sivulle 'Tee varaus'-linkin kautta, tai lähetetään
     * lomake puuttuvilla tiedoilla.
     */

    if (submit == null || (submit != null && virhe.length() > 0)) {

      out.println("<html><head><title>Tee varaus</title>" +
                  "<script type='text/javascript' src='" +
                  "http://db.cs.helsinki.fi/u/avtanska/tsoha/tarkista.js'>" +
                  "</script>" +
                  "<link rel='stylesheet' style='text/css' " +
                  "href='http://db.cs.helsinki.fi/u/avtanska/tsoha/" +
                  "perus.css'></head>" +
                  "<body bgcolor=white>");

      /*
       * Tulostetaan virheilmoitus tarvittaessa
       */

      if (submit != null && virhe.length() > 0) {
        out.println("<p>" + virhe + "</p>");
      }

      out.println("<h3>Tee varaus, " + sessionTunnus + "</h3>");


      /*
       * Tulostetaan lomake varauksen tekoa varten
       */

      out.println("<form method='post' name='varaus' " +
                  "onSubmit='return tarkista()' " +
                  "action='http://db.cs.helsinki.fi/s/avtanska/TeeVaraus'>" +
                  "<table cellpadding='10'><tr><td>" +
                  "Varauksen aihe:<br /><input name='aihe' type='text'></td>");


      /*
       * Haetaan ryhmät tietokannasta
       */

      out.println("<td>Ryhmä:<br /><select name='ryhma'>" +
                  "<option value='tyhjä'>-- valitse --");

      Connection con=null;
      con= createDbConnection(dbDriver,dbServer,dbUser,dbPassword,out);
      if (con==null) {
         out.println("</body></html>");
         return;
      }

      Statement stmt = null;
      ResultSet rs = null;
      try {
          stmt = con.createStatement();
          rs = stmt.executeQuery("SELECT nimi FROM ryhmä ORDER BY nimi");
          while(rs.next()) {
               out.println("<option>" + rs.getString("nimi"));
          }

      } catch (SQLException ee) {
            out.println("Tietokantavirhe "+ee.getMessage());
      } finally {
            try {
               if (rs!=null) rs.close();
               if (stmt!=null) stmt.close();

            } catch(SQLException e) {
               out.println("An SQL Exception was thrown.");
            }
      }

      out.println("</select>&nbsp;&nbsp;tai</td></tr>");


      /*
       * Varauksen päivämäärä
       */

      out.println("<tr><td>Varauksen päivämäärä:<br />" +
                  "<select name='paiva'><option value='tyhjä'>DD");
      for (int i = 0; i < 31; i++) {
        out.println("<option>" + CheckDate.c(i+1));
      }


      out.println("</select>" +
                  "<select name='kuukausi'><option value='tyhjä'>MM");

      for (int i = 0; i < 12; i++) {
        out.println("<option>" + CheckDate.c(i+1));
      }

      out.println("</select>" +
                  "<select name='vuosi'><option value='tyhjä'>YYYY" +
                  "<option>2002<option>2003<option>2004</select>" +
                  "</td>");


      /*
       * Haetaan henkilöt tietokannasta, admin-käyttäjää ei listaan
       * tulosteta.
       */

      out.println("<td>Henkilö:<br /><select name='henkilo'>" +
                  "<option value='tyhjä'>-- valitse --");


      if (con==null) {
         out.println("</body></html>");
         return;
      }

      stmt = null;
      rs = null;
      try {
          stmt = con.createStatement();
          rs = stmt.executeQuery("SELECT tunnus FROM henkilö ORDER BY tunnus");
          while(rs.next()) {
            if (!(rs.getString("tunnus")).equals("admin")) {
               out.println("<option>" + rs.getString("tunnus"));
            }
          }

      } catch (SQLException ee) {
            out.println("Tietokantavirhe "+ee.getMessage());
      } finally {
            try {
               if (rs!=null) rs.close();
               if (stmt!=null) stmt.close();

            } catch(SQLException e) {
               out.println("An SQL Exception was thrown.");
            }
      }

      out.println("</select></td>");


      /*
       * Varauksen alkuaika
       */

      out.println("</tr><tr><td>Varauksen alkuaika:<br />" +
                  "<select name='alkuaika'><option value='tyhjä'>HH:MM");

      for (int i = 0; i < 24; i++) {
        if (i < 10) {
          out.println("<option>0" + i + ":00");
          out.println("<option>0" + i + ":30");
        }
        else {
          out.println("<option>" + i + ":00");
          out.println("<option>" + i + ":30");
        }
      }

      out.println("</select></td>");


      /*
       * Haetaan näkyvyydet tietokannasta
       */

      out.println("<td>Näkyvyys:<br /><select name='nakyvyys'>" +
                  "<option value='tyhjä'>-- valitse --");

      stmt = null;
      rs = null;
      try {
          stmt = con.createStatement();
          rs = stmt.executeQuery("SELECT * FROM näkyvyys");
          while(rs.next()) {
               out.println("<option value='" + rs.getString("nid") +
                           "'>" + rs.getString("kuvaus"));
          }
      } catch (SQLException ee) {
            out.println("Tietokantavirhe "+ee.getMessage());
      } finally {
            try {
               if (rs!=null) rs.close();
               if (stmt!=null) stmt.close();
               con.close();
            } catch(SQLException e) {
               out.println("An SQL Exception was thrown.");
            }
      }

      out.println("</select></td></tr>");


      out.println("<tr><td colspan='2'>Varauksen kesto:<br/>" +
                  "<select name='kesto'><option value='tyhjä'>--");

      for (int i = 0; i < 24; i++) {
        if (i == 0) {
          out.println("<option>0.5");
        }
        else {
          out.println("<option>" + i + ".0");
          out.println("<option>" + i + ".5");
        }
      }
      out.println("<option>24.0");

      out.println("</select>&nbsp;tuntia</td></tr>");

      out.println("<td colspan='2' align='left'>" +
                  "<input name='submit' type='submit' value='Varaa'>" +
                  "<input type='reset' name='reset' value='Tyhjennä'></td>" +
                  "</table></form></body></html>");



    } // if submit == null



    /*
     * Kun lomake lähetetty oikein täytetyillä kentillä
     */

    else {


      out.println("" +
        "<html><head><title>l</title><link rel='stylesheet' type='text/css'" +
        "href='http://db.cs.helsinki.fi/u/avtanska/tsoha/perus.css' />" +
        "</head><body>");

      String pvm = paiva + "." + kuukausi + "." + vuosi;

      /*
       * Huolehditaan, että ei tehdä varausta esim. 30.2.2002
       */

      pvm = CheckDate.checkDate(pvm);


      


      Connection con = null;
      con= createDbConnection(dbDriver,dbServer,dbUser,dbPassword,out);

      if (con==null) {
         out.println("</body></html>");
         return;
      }

      Statement stmt = null;
      ResultSet rs = null;


      /*
       * Tarkistetaan, mahtuuko yritetty varaus henkilön tai kaikkien
       * ryhmän henkilöiden kalentereihin
       */

      boolean mahtuuKalenteriin = true;
      String virheIlmoitus = "";
      String varausKatkaistu = "";
      String selectVaraukset = "";
      double alkuaikaSaatu = 0;
      double kestoSaatu = 0;
   

      /*
       * Muutetaan yritetyn varauksen ajat laskettavaan muotoon
       */

      double alkuaikaYritys =
          (new Double(alkuaika.substring(0,2))).doubleValue();

      if ((new Double("0." + alkuaika.substring(3,5))).doubleValue() == 0.3) {
        alkuaikaYritys = alkuaikaYritys + 0.5;
      }

      double kestoYritys = new Double(kesto).doubleValue();

      double uusiKesto = 0;


      /* 
       * Jos yritetty varaus menee vuorokausirajan yli, katkaistaan
       * puolenyön yli menevä osuus pois ja kerrotaan käyttäjälle 
       * asiasta.
       */

      if (alkuaikaYritys + kestoYritys > 24) {        
        kestoYritys = kestoYritys - ((alkuaikaYritys + kestoYritys) - 24);
        kesto = "" + kestoYritys;
        varausKatkaistu = 
          "<span class='virhe'>Varauksesi jatkuu seuraavan" +
          " päivän puolelle ja tietokantaan<br /> on tallennettu " +
          " vain varauspäivän puolelle mahtuva osa. <br />Jos haluat jatkaa" +
          " varausta seuraavaksi päiväksi, ole hyvä<br /> ja tee uusi" +
          " varaus aamuksi.</span>";
      }

 
      /*
       * SQL-lause varauksen tallettamiseen
       */

      String insertVaraus = "";

      if (henkilo != null) {
        insertVaraus =
           "INSERT INTO varaus " +
              "(tunnus, pvm, alkuaika, kesto, aihe, näkyvyys, varaaja) " +
              "VALUES ('" + henkilo + "', to_date('" + pvm +
              "', 'DD.MM.YYYY'), '" + alkuaika + "'," + kesto + ",'" + aihe +
              "'," + nakyvyys + ",'" + sessionTunnus + "')";
      }
      else if (ryhma != null) {
        insertVaraus =
           "INSERT INTO varaus " +
              "(pvm, alkuaika, kesto, aihe, näkyvyys, varaaja, ryhmä) " +
            "VALUES (to_date('" + pvm + "', 'DD.MM.YYYY'), '" + alkuaika +
            "'," + kesto + ",'" + aihe + "'," + nakyvyys +
            ",'" + sessionTunnus + "', '" + ryhma  + "')";
      }



      if (henkilo != null) {
  
        /*
         * Haetaan henkilön kaikki varaukset sille päivälle, jolle
         * uutta varausta yritetään tehdä.
         */
  
        try {
          stmt = con.createStatement();
          selectVaraukset =
             "SELECT * FROM varaus WHERE tunnus='" +
             henkilo + "' AND pvm=to_date('" + pvm +
             "', 'DD.MM.YYYY') ORDER BY alkuaika";
  
          rs = stmt.executeQuery(selectVaraukset);
  
          while(rs.next()) {
  
            /*
             * Muutetaan käsiteltävän varauksen ajat laskettavaan muotoon
             */
  
            alkuaikaSaatu =
            (new Double((rs.getString("alkuaika")).substring(0,2))).doubleValue();
  
            if((new Double("0." +
               (rs.getString("alkuaika")).substring(3,5))).doubleValue() == 0.3) {
              alkuaikaSaatu = alkuaikaSaatu + 0.5;
            }
  
            kestoSaatu = new Double(rs.getString("kesto")).doubleValue();
  
  
            /*
             * Tarkistetaan, menevätkö yritetty ja kalenterissa jo oleva varaus
             * päällekkäin. Päällekkäisyyksiä on kolmea lajia. Kaikille on
             * omat virheilmoituksensa.
             */
  
            if (alkuaikaYritys == alkuaikaSaatu) {
              mahtuuKalenteriin = false;
              virheIlmoitus = "Yrittämäsi varaus alkaa samanaikaisesti " +
                              "vanhan varauksen kanssa.";
              break;
            }
            else if (alkuaikaYritys < alkuaikaSaatu &&
                     (alkuaikaYritys+kestoYritys) > alkuaikaSaatu) {
              mahtuuKalenteriin = false;
              double erotus = (alkuaikaYritys+kestoYritys)-alkuaikaSaatu;
              virheIlmoitus = "Yrittämäsi varaus on " + erotus +
                              " tuntia liian pitkä.<br />" +
                              "Se menee " +
                              "seuraavan varauksen päälle.";
              break;
            }
            else if (alkuaikaYritys > alkuaikaSaatu &&
                     (alkuaikaSaatu+kestoSaatu) > alkuaikaYritys) {
              mahtuuKalenteriin = false;
  
              double erotus = (alkuaikaSaatu+kestoSaatu)-alkuaikaYritys;
              virheIlmoitus = "Yrittämäsi varaus alkaa " + erotus +
                              " tuntia liian aikaisin.<br /> " +
                              "Edellinen varaus ei ole vielä ehtinyt loppua.";
              break;
            }
            else {
              mahtuuKalenteriin = true;
            }
  
  
          }
  
        } catch (SQLException ee) {
              out.println("Tietokantavirhe "+ee.getMessage());
        } finally {
              try {
                 if (rs!=null) rs.close();
                 if (stmt!=null) stmt.close();
  
              } catch(SQLException e) {
                 out.println("An SQL Exception was thrown.");
              }
        }

      } // loppu if henkilon varaus
          
      else if (ryhma != null) {
        
        /*
         * Jos varausta tehdään ryhmälle, haetaan tietokannasta 
         * ryhmän kaikkien jäsenten varaukset sille päivälle,
         * jolle varausta yritetään tehdä
         */
  
        try {
          stmt = con.createStatement();
          selectVaraukset =
             "SELECT * FROM varaus WHERE tunnus IN " +
             "(SELECT tunnus FROM jäsenyys WHERE ryhmä='" + ryhma + 
             "') AND pvm=to_date('" + pvm +
             "', 'DD.MM.YYYY') ORDER BY alkuaika";
  
          rs = stmt.executeQuery(selectVaraukset);
  
          while(rs.next()) {
  
            /*
             * Muutetaan käsiteltävän varauksen ajat laskettavaan muotoon
             */
  
            alkuaikaSaatu =
            (new Double((rs.getString("alkuaika")).substring(0,2))).doubleValue();
  
            if((new Double("0." +
               (rs.getString("alkuaika")).substring(3,5))).doubleValue() == 0.3) {
              alkuaikaSaatu = alkuaikaSaatu + 0.5;
            }
  
            kestoSaatu = new Double(rs.getString("kesto")).doubleValue();
  
  
            /*
             * Tarkistetaan, menevätkö yritetty ja kalenterissa jo oleva varaus
             * päällekkäin. Päällekkäisyyksiä on kolmea lajia. Kaikille on
             * omat virheilmoituksensa.
             */
  
            if (alkuaikaYritys == alkuaikaSaatu) {
              mahtuuKalenteriin = false;
              virheIlmoitus = "Yrittämäsi varaus alkaa samanaikaisesti " +
                              "mm. henkilön '" + rs.getString("tunnus") + 
                              "' vanhan varauksen kanssa.";
              break;
            }
            else if (alkuaikaYritys < alkuaikaSaatu &&
                     (alkuaikaYritys+kestoYritys) > alkuaikaSaatu) {
              mahtuuKalenteriin = false;
              double erotus = (alkuaikaYritys+kestoYritys)-alkuaikaSaatu;
              virheIlmoitus = "Yrittämäsi varaus on " + erotus +
                              " tuntia liian pitkä.<br />" +
                              "Se menee mm. henkilön '" + 
                              rs.getString("tunnus") +
                              "' seuraavan varauksen päälle.";
              break;
            }
            else if (alkuaikaYritys > alkuaikaSaatu &&
                     (alkuaikaSaatu+kestoSaatu) > alkuaikaYritys) {
              mahtuuKalenteriin = false;
  
              double erotus = (alkuaikaSaatu+kestoSaatu)-alkuaikaYritys;
              virheIlmoitus = "Yrittämäsi varaus alkaa " + erotus +
                              " tuntia liian aikaisin.<br /> " +
                              "Mm. henkilön '" + rs.getString("tunnus") +
                              "' edellinen varaus ei ole vielä ehtinyt loppua.";
              break;
            }
            else {
              mahtuuKalenteriin = true;
            }
  
  
          }
  
        } catch (SQLException ee) {
              out.println("Tietokantavirhe "+ee.getMessage());
        } finally {
              try {
                 if (rs!=null) rs.close();
                 if (stmt!=null) stmt.close();
  
              } catch(SQLException e) {
                 out.println("An SQL Exception was thrown.");
              }
        }
       
      
      
      }     



      /*
       * Jos varaus mahtuu kalenteriin, viedään sen tiedot tietokantaan.
       * Jos varaus kuuluu ryhmälle, tehdään varaus myös kaikkien ryhmän
       * jäsenten kalentereihin.
       */

      if (mahtuuKalenteriin) {

        stmt = null;
        rs = null;
        try {
            stmt = con.createStatement();
            int count = stmt.executeUpdate(insertVaraus);

            if (ryhma != null) {
              rs = stmt.executeQuery(
                 "SELECT tunnus FROM jäsenyys WHERE ryhmä='" + ryhma + "'");
              while (rs.next()) {
                count = stmt.executeUpdate(
                   "INSERT INTO varaus " +
                   "(tunnus, pvm, alkuaika, kesto, aihe, näkyvyys, varaaja) " +
                   "VALUES ('" + rs.getString("tunnus") + "', to_date('" +
                   pvm + "', 'DD.MM.YYYY'), '" + alkuaika + "'," + kesto +
                   ",'" + aihe + "', " + nakyvyys + ",'" + sessionTunnus +
                   "')");
              }
            }

        } catch (SQLException ee) {
              out.println("Tietokantavirhe "+ee.getMessage());
        } finally {
              try {
                 if (rs!=null) rs.close();
                 if (stmt!=null) stmt.close();
                 con.close();
              } catch(SQLException e) {
                 out.println("An SQL Exception was thrown.");
              }
        }


         String henkilöTaiRyhmä = "";

         if (henkilo != null) {
           henkilöTaiRyhmä = henkilo;
         }
         else if (ryhma != null) {
           henkilöTaiRyhmä = ryhma;
         }

         out.println("<h3>Kiitos varauksesta.</h3>");

 
         /*
          * Huomautetaan käyttäjää, jos varaus on jouduttu katkaisemaan
          */

         if (!varausKatkaistu.equals("")) {
           out.println("<span class='virhe'><h3>HUOM!</h3></span>" +
                       "<p>" + varausKatkaistu + "</p>");
         }

         out.println("<p>Kenelle: <b>" + henkilöTaiRyhmä + "</b></p>" +
           "<p>Aihe: <b>" + aihe + "</b></p><p>Pvm: <b>" + pvm +
           "</b></p><p>Alkuaika: <b>" + alkuaika + "</b></p>" +
           "<p>Kesto: <b>" + kesto + " tuntia</b></p>");

      } // loppu if mahtuuKalenteriin


      /*
       * Jos varaus ei mahdu kalenteriin
       */
      
      else {
        out.println("<h3>Varaus ei mahdu kalenteriin.</h3>" +
                    "<p class='virhe'>" + virheIlmoitus + "</p>");

        if (henkilo != null ) {
          out.println("<p>Tarkista vielä kyseisen henkilön kalenterista " +
                      "vapaat ajat.</p>");
        }
        else if (ryhma != null) {
          out.println("<p>Tarkista vielä kyseisen ryhmän kaikille " +
                      "jäsenille yhteiset vapaat ajat<br />ryhmän" +
                      "hallintasivulla olevan 'Ryhmän ajat'-napin kautta.</p>");
        }

        double loppuKorkeus =
          (alkuaikaYritys+kestoYritys < alkuaikaSaatu+kestoSaatu
           ? alkuaikaSaatu+kestoSaatu : alkuaikaYritys+kestoYritys);


        /*
         * Korjataan liian suuri loppuKorkeus
         */

        if (loppuKorkeus >= 19) {
          loppuKorkeus = 19;
        }


        /*
         * Tulostetaan varauksen tekijälle näkymä, miten yritetty varaus
         * menee kalenterissa jo olevan varauksen päälle.
         */

        out.println("<table border='2' height='" + (loppuKorkeus*20+100) +
                    "' class='kalenteri'><tr>");

        out.println("<td class='kalenteri' valign='top'>" +
                    "<table border='0' class='ajat' cellpadding='0' " +
                    " cellspacing='0'>");

        for (int i = 0; i < (int)loppuKorkeus+5; i++) {
          out.println("<tr><td class='kalenteri' valign='top' " +
                      "height='20' style='font-size: 8pt'>"
                      + CheckDate.c(i) + ":00</td></tr>");
        }

        out.println("</table></td>");

        out.println("<td class='kalenteri' " +
                    "valign='top'><table class='kalenteri'><tr><td height='" +
                    alkuaikaSaatu*20 + "' class='kalenteri' width='100'>" +
                    "&nbsp;</td></tr><tr>" +
                    "<td class='varaus' valign='top' height='" +
                    kestoSaatu*20 + "'><b>Vanha varaus</b></td></tr>" +
                    "</table></td><td class='kalenteri' valign='top'>" +
                    "<table class='kalenteri'><tr>" +
                    "<td class='kalenteri' height='" + alkuaikaYritys*20 +
                    "' width='100'>&nbsp;</td></tr><tr><td class='yritys' " +
                    " valign='top' height='" +
                    kestoYritys*20 + "'><b>Yritetty varaus</b></td>" +
                    "</tr></table></td></tr></table>");
      }

      out.println("</body></html>");

    }

  }


  private Connection createDbConnection(
    String dbDriver, String dbServer, String dbUser, String dbPassword,
    ServletOutputStream out) throws IOException {

    // establish a database connection
    try{
        Class.forName(dbDriver);               // load driver
    } catch (ClassNotFoundException e) {
          out.println("Couldn't find driver "+dbDriver);
          return null;
    }
    Connection con=null;
    try {
       con = DriverManager.getConnection(dbServer,dbUser,dbPassword);
    } catch (SQLException se) {
          out.println("Couldn\'t get connection to "+dbServer+
                      " for "+ dbUser+"<br>");
          out.println(se.getMessage());
    }
    return con;
  }


}
