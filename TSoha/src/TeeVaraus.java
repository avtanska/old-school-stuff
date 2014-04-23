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
     * Asetetaan tyhj�t arvot
     */

    if (submit != null) {
      if (henkilo.equals("tyhj�")) {
        henkilo = null;
      }
      if (ryhma.equals("tyhj�")) {
        ryhma = null;
      }
      if (nakyvyys.equals("tyhj�")) {
        nakyvyys = null;
      }
      if (paiva.equals("tyhj�") || kuukausi.equals("tyhj�")
          || vuosi.equals("tyhj�")) {
        paiva = null;
      }
      if (alkuaika.equals("tyhj�") || kesto.equals("tyhj�")) {
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
      virhe += "<span class='virhe'>Valitse henkil� tai ryhm�</span><br />";
    if (aihe == null)
      virhe += "<span class='virhe'>Varauksen aihe puuttuu</span><br />";
    if (nakyvyys == null)
      virhe += "<span class='virhe'>Valitse n�kyvyys</span><br />";
    if (paiva == null)
      virhe += "<span class='virhe'>Valitse p�iv�m��r�</span><br />";
    if (alkuaika == null)
      virhe += "<span class='virhe'>Valitse alkuaika ja kesto</span><br />";


    /*
     * Jos tullaan sivulle 'Tee varaus'-linkin kautta, tai l�hetet��n
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
       * Haetaan ryhm�t tietokannasta
       */

      out.println("<td>Ryhm�:<br /><select name='ryhma'>" +
                  "<option value='tyhj�'>-- valitse --");

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
          rs = stmt.executeQuery("SELECT nimi FROM ryhm� ORDER BY nimi");
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
       * Varauksen p�iv�m��r�
       */

      out.println("<tr><td>Varauksen p�iv�m��r�:<br />" +
                  "<select name='paiva'><option value='tyhj�'>DD");
      for (int i = 0; i < 31; i++) {
        out.println("<option>" + CheckDate.c(i+1));
      }


      out.println("</select>" +
                  "<select name='kuukausi'><option value='tyhj�'>MM");

      for (int i = 0; i < 12; i++) {
        out.println("<option>" + CheckDate.c(i+1));
      }

      out.println("</select>" +
                  "<select name='vuosi'><option value='tyhj�'>YYYY" +
                  "<option>2002<option>2003<option>2004</select>" +
                  "</td>");


      /*
       * Haetaan henkil�t tietokannasta, admin-k�ytt�j�� ei listaan
       * tulosteta.
       */

      out.println("<td>Henkil�:<br /><select name='henkilo'>" +
                  "<option value='tyhj�'>-- valitse --");


      if (con==null) {
         out.println("</body></html>");
         return;
      }

      stmt = null;
      rs = null;
      try {
          stmt = con.createStatement();
          rs = stmt.executeQuery("SELECT tunnus FROM henkil� ORDER BY tunnus");
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
                  "<select name='alkuaika'><option value='tyhj�'>HH:MM");

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
       * Haetaan n�kyvyydet tietokannasta
       */

      out.println("<td>N�kyvyys:<br /><select name='nakyvyys'>" +
                  "<option value='tyhj�'>-- valitse --");

      stmt = null;
      rs = null;
      try {
          stmt = con.createStatement();
          rs = stmt.executeQuery("SELECT * FROM n�kyvyys");
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
                  "<select name='kesto'><option value='tyhj�'>--");

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
                  "<input type='reset' name='reset' value='Tyhjenn�'></td>" +
                  "</table></form></body></html>");



    } // if submit == null



    /*
     * Kun lomake l�hetetty oikein t�ytetyill� kentill�
     */

    else {


      out.println("" +
        "<html><head><title>l</title><link rel='stylesheet' type='text/css'" +
        "href='http://db.cs.helsinki.fi/u/avtanska/tsoha/perus.css' />" +
        "</head><body>");

      String pvm = paiva + "." + kuukausi + "." + vuosi;

      /*
       * Huolehditaan, ett� ei tehd� varausta esim. 30.2.2002
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
       * Tarkistetaan, mahtuuko yritetty varaus henkil�n tai kaikkien
       * ryhm�n henkil�iden kalentereihin
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
       * puoleny�n yli menev� osuus pois ja kerrotaan k�ytt�j�lle 
       * asiasta.
       */

      if (alkuaikaYritys + kestoYritys > 24) {        
        kestoYritys = kestoYritys - ((alkuaikaYritys + kestoYritys) - 24);
        kesto = "" + kestoYritys;
        varausKatkaistu = 
          "<span class='virhe'>Varauksesi jatkuu seuraavan" +
          " p�iv�n puolelle ja tietokantaan<br /> on tallennettu " +
          " vain varausp�iv�n puolelle mahtuva osa. <br />Jos haluat jatkaa" +
          " varausta seuraavaksi p�iv�ksi, ole hyv�<br /> ja tee uusi" +
          " varaus aamuksi.</span>";
      }

 
      /*
       * SQL-lause varauksen tallettamiseen
       */

      String insertVaraus = "";

      if (henkilo != null) {
        insertVaraus =
           "INSERT INTO varaus " +
              "(tunnus, pvm, alkuaika, kesto, aihe, n�kyvyys, varaaja) " +
              "VALUES ('" + henkilo + "', to_date('" + pvm +
              "', 'DD.MM.YYYY'), '" + alkuaika + "'," + kesto + ",'" + aihe +
              "'," + nakyvyys + ",'" + sessionTunnus + "')";
      }
      else if (ryhma != null) {
        insertVaraus =
           "INSERT INTO varaus " +
              "(pvm, alkuaika, kesto, aihe, n�kyvyys, varaaja, ryhm�) " +
            "VALUES (to_date('" + pvm + "', 'DD.MM.YYYY'), '" + alkuaika +
            "'," + kesto + ",'" + aihe + "'," + nakyvyys +
            ",'" + sessionTunnus + "', '" + ryhma  + "')";
      }



      if (henkilo != null) {
  
        /*
         * Haetaan henkil�n kaikki varaukset sille p�iv�lle, jolle
         * uutta varausta yritet��n tehd�.
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
             * Muutetaan k�sitelt�v�n varauksen ajat laskettavaan muotoon
             */
  
            alkuaikaSaatu =
            (new Double((rs.getString("alkuaika")).substring(0,2))).doubleValue();
  
            if((new Double("0." +
               (rs.getString("alkuaika")).substring(3,5))).doubleValue() == 0.3) {
              alkuaikaSaatu = alkuaikaSaatu + 0.5;
            }
  
            kestoSaatu = new Double(rs.getString("kesto")).doubleValue();
  
  
            /*
             * Tarkistetaan, menev�tk� yritetty ja kalenterissa jo oleva varaus
             * p��llekk�in. P��llekk�isyyksi� on kolmea lajia. Kaikille on
             * omat virheilmoituksensa.
             */
  
            if (alkuaikaYritys == alkuaikaSaatu) {
              mahtuuKalenteriin = false;
              virheIlmoitus = "Yritt�m�si varaus alkaa samanaikaisesti " +
                              "vanhan varauksen kanssa.";
              break;
            }
            else if (alkuaikaYritys < alkuaikaSaatu &&
                     (alkuaikaYritys+kestoYritys) > alkuaikaSaatu) {
              mahtuuKalenteriin = false;
              double erotus = (alkuaikaYritys+kestoYritys)-alkuaikaSaatu;
              virheIlmoitus = "Yritt�m�si varaus on " + erotus +
                              " tuntia liian pitk�.<br />" +
                              "Se menee " +
                              "seuraavan varauksen p��lle.";
              break;
            }
            else if (alkuaikaYritys > alkuaikaSaatu &&
                     (alkuaikaSaatu+kestoSaatu) > alkuaikaYritys) {
              mahtuuKalenteriin = false;
  
              double erotus = (alkuaikaSaatu+kestoSaatu)-alkuaikaYritys;
              virheIlmoitus = "Yritt�m�si varaus alkaa " + erotus +
                              " tuntia liian aikaisin.<br /> " +
                              "Edellinen varaus ei ole viel� ehtinyt loppua.";
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
         * Jos varausta tehd��n ryhm�lle, haetaan tietokannasta 
         * ryhm�n kaikkien j�senten varaukset sille p�iv�lle,
         * jolle varausta yritet��n tehd�
         */
  
        try {
          stmt = con.createStatement();
          selectVaraukset =
             "SELECT * FROM varaus WHERE tunnus IN " +
             "(SELECT tunnus FROM j�senyys WHERE ryhm�='" + ryhma + 
             "') AND pvm=to_date('" + pvm +
             "', 'DD.MM.YYYY') ORDER BY alkuaika";
  
          rs = stmt.executeQuery(selectVaraukset);
  
          while(rs.next()) {
  
            /*
             * Muutetaan k�sitelt�v�n varauksen ajat laskettavaan muotoon
             */
  
            alkuaikaSaatu =
            (new Double((rs.getString("alkuaika")).substring(0,2))).doubleValue();
  
            if((new Double("0." +
               (rs.getString("alkuaika")).substring(3,5))).doubleValue() == 0.3) {
              alkuaikaSaatu = alkuaikaSaatu + 0.5;
            }
  
            kestoSaatu = new Double(rs.getString("kesto")).doubleValue();
  
  
            /*
             * Tarkistetaan, menev�tk� yritetty ja kalenterissa jo oleva varaus
             * p��llekk�in. P��llekk�isyyksi� on kolmea lajia. Kaikille on
             * omat virheilmoituksensa.
             */
  
            if (alkuaikaYritys == alkuaikaSaatu) {
              mahtuuKalenteriin = false;
              virheIlmoitus = "Yritt�m�si varaus alkaa samanaikaisesti " +
                              "mm. henkil�n '" + rs.getString("tunnus") + 
                              "' vanhan varauksen kanssa.";
              break;
            }
            else if (alkuaikaYritys < alkuaikaSaatu &&
                     (alkuaikaYritys+kestoYritys) > alkuaikaSaatu) {
              mahtuuKalenteriin = false;
              double erotus = (alkuaikaYritys+kestoYritys)-alkuaikaSaatu;
              virheIlmoitus = "Yritt�m�si varaus on " + erotus +
                              " tuntia liian pitk�.<br />" +
                              "Se menee mm. henkil�n '" + 
                              rs.getString("tunnus") +
                              "' seuraavan varauksen p��lle.";
              break;
            }
            else if (alkuaikaYritys > alkuaikaSaatu &&
                     (alkuaikaSaatu+kestoSaatu) > alkuaikaYritys) {
              mahtuuKalenteriin = false;
  
              double erotus = (alkuaikaSaatu+kestoSaatu)-alkuaikaYritys;
              virheIlmoitus = "Yritt�m�si varaus alkaa " + erotus +
                              " tuntia liian aikaisin.<br /> " +
                              "Mm. henkil�n '" + rs.getString("tunnus") +
                              "' edellinen varaus ei ole viel� ehtinyt loppua.";
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
       * Jos varaus mahtuu kalenteriin, vied��n sen tiedot tietokantaan.
       * Jos varaus kuuluu ryhm�lle, tehd��n varaus my�s kaikkien ryhm�n
       * j�senten kalentereihin.
       */

      if (mahtuuKalenteriin) {

        stmt = null;
        rs = null;
        try {
            stmt = con.createStatement();
            int count = stmt.executeUpdate(insertVaraus);

            if (ryhma != null) {
              rs = stmt.executeQuery(
                 "SELECT tunnus FROM j�senyys WHERE ryhm�='" + ryhma + "'");
              while (rs.next()) {
                count = stmt.executeUpdate(
                   "INSERT INTO varaus " +
                   "(tunnus, pvm, alkuaika, kesto, aihe, n�kyvyys, varaaja) " +
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


         String henkil�TaiRyhm� = "";

         if (henkilo != null) {
           henkil�TaiRyhm� = henkilo;
         }
         else if (ryhma != null) {
           henkil�TaiRyhm� = ryhma;
         }

         out.println("<h3>Kiitos varauksesta.</h3>");

 
         /*
          * Huomautetaan k�ytt�j��, jos varaus on jouduttu katkaisemaan
          */

         if (!varausKatkaistu.equals("")) {
           out.println("<span class='virhe'><h3>HUOM!</h3></span>" +
                       "<p>" + varausKatkaistu + "</p>");
         }

         out.println("<p>Kenelle: <b>" + henkil�TaiRyhm� + "</b></p>" +
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
          out.println("<p>Tarkista viel� kyseisen henkil�n kalenterista " +
                      "vapaat ajat.</p>");
        }
        else if (ryhma != null) {
          out.println("<p>Tarkista viel� kyseisen ryhm�n kaikille " +
                      "j�senille yhteiset vapaat ajat<br />ryhm�n" +
                      "hallintasivulla olevan 'Ryhm�n ajat'-napin kautta.</p>");
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
         * Tulostetaan varauksen tekij�lle n�kym�, miten yritetty varaus
         * menee kalenterissa jo olevan varauksen p��lle.
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
