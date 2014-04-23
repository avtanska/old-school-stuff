import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.Calendar;
import java.text.DateFormat;
import java.util.Date;


public class Kalenteri extends HttpServlet {

  final String dbDriver="org.postgresql.Driver";
  final String dbServer ="jdbc:postgresql://localhost:10388/tsoha";
  final String dbUser= "avtanska";        // replace with your db user account
  final String dbPassword ="postgres"; // replace with your password


  public void service(HttpServletRequest req, HttpServletResponse res)
         throws ServletException, IOException {
          
    ServletOutputStream out;
    res.setContentType("text/html");
    out= res.getOutputStream();
    
    Calendar kalenteri = Calendar.getInstance();
    
    HttpSession session = req.getSession(false);
    String sessionTunnus = (String)session.getValue("sessionTunnus");

    
    /*
     * Tämä muuttuja määrittelee kuinka monta pikseliä yhden 
     * tunnin korkeus kalenterissa on
     */ 

    final int TUNNIN_KORKEUS = 26;

    
    String[] valitutTaulu = req.getParameterValues("valitut");  
    String poista = req.getParameter("poista");
    String henkilo = req.getParameter("henkilo");
    String ryhma = req.getParameter("ryhma");
    String alkupvm = req.getParameter("alkupvm");
    String viikkoAlusta = req.getParameter("viikkoAlusta");
    String valitut = "";
    String henkilöTaiRyhmä = "";
       
    
    if (henkilo != null && henkilo.equals("tyhjä")) {
      henkilo = null;
    }
    if (ryhma != null && ryhma.equals("tyhjä")) {
      ryhma = null;
    }
    if (viikkoAlusta == null) {
      viikkoAlusta = "";
    }    

    out.println("<html><head><title>Database query from DB (tsoha)</title>" +
    "<link rel='stylesheet' style='text/css' " +
    "href='http://db.cs.helsinki.fi/u/avtanska/tsoha/perus.css'></head>" +
    "<body>");
  
  

    /*
     * Tarkistetaan onko valittu ryhmää tai henkilöä.
     */  
  
    if (ryhma == null && henkilo == null) {
      out.println("<h3>Valitse ryhmä tai henkilö.</h3></body></html>");
    }

 
    /*
     * Jos valinnat kunnossa aloitetaan kalenterin generointi
     */

    else {
    
      Connection con=null;
      con= createDbConnection(dbDriver,dbServer,dbUser,dbPassword,out);
      if (con==null) {
         out.println("</body></html>");
         return;
       }

      Statement stmt = null;
      ResultSet rs = null;


      /*
       * Tulostetaan sivun otsikko ja henkilön kalenterin tapauksessa 
       * henkilön tiedot ja ryhmän tapauksessa ryhmän jäsenet
       */ 

      if (henkilo != null) {
        henkilöTaiRyhmä = "henkilo=" + henkilo;
        out.println("<h3>Varaukset henkilölle '" + henkilo + "'</h3>");

        try {
          stmt = con.createStatement(); 
          rs = stmt.executeQuery("SELECT * FROM henkilö WHERE tunnus='" +
                                 henkilo + "'");
          
          while (rs.next()) {
            out.println("<p><b>Nimi:</b> " + rs.getString("etunimi") +
                        " " + rs.getString("sukunimi") + "<br />");
            out.println("<b>Puhelin:</b> " + rs.getString("puh") +
                        "<br />");
            out.println("<b>Email:</b> " + rs.getString("sposti") +
                        "</p>");
          }
 
        } catch (Exception e) {
           out.println("Tietokantavirhe: " + e);
        }          

        
      }
      else if (ryhma != null) {
        henkilöTaiRyhmä = "ryhma=" + ryhma;
        out.println("<h3>Varaukset ryhmälle '" + ryhma + "'</h3>");

        try {
          stmt = con.createStatement(); 
          rs = stmt.executeQuery("SELECT tunnus FROM jäsenyys WHERE ryhmä='" +
                                 ryhma + "'");
          
          out.println("<b>Jäsenet:</b> ");

          while (rs.next()) {
            if (!rs.isLast()) {
              out.println(rs.getString("tunnus") + ", ");
            }
            else {
              out.println(rs.getString("tunnus"));
            }

          }
 
        } catch (Exception e) {
           out.println("Tietokantavirhe: " + e);
        }
      }
  

      out.println("<form method='post' " + 
         "action='http://db.cs.helsinki.fi/s/avtanska/Kalenteri'>");
  
  
      /* 
       * Jos painettu 'Poista valitut'-nappia, muunnetaan valinnat
       * SQL-lauseeseen sopivaan muotoon   
       */   

      if (poista != null && valitutTaulu != null) {
        for (int i = 0; i < valitutTaulu.length; i++) {
          valitut += valitutTaulu[i] + ",";
        }
        valitut = valitut.substring(0, valitut.length()-1);
      }
  
  
      /*
       * Alustetaan kalenteri osoittamaan käynnissä olevan
       * viikon ensimmäiseen päivään (Euroopassa maanantai)
       */
  
      DateFormat df = DateFormat.getDateInstance();
      Date tempPvm = null;
      int alkuun = 0;

      if (alkupvm == null) {
        alkuun = 
           kalenteri.getFirstDayOfWeek() - kalenteri.get(Calendar.DAY_OF_WEEK);
        if (kalenteri.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
          alkuun = -6;
        }
        kalenteri.add(Calendar.DATE, alkuun);
        alkupvm = "" + df.format(kalenteri.getTime());
      } 

 
      /*
       * Jos kalenteriin tullaan koko vuoden kalenterin kautta,
       * asetetaan kalenteri osoittamaan sieltä valitun päivän
       * sisältämän viikon alkuun.
       */
     
      if (viikkoAlusta.equals("true")) {
        try {
          tempPvm = df.parse(alkupvm);
        } catch (Exception e) {
          out.println("Virhe: " + e);
        }

        kalenteri.setTime(tempPvm);
        
        alkuun = 
           kalenteri.getFirstDayOfWeek() - kalenteri.get(Calendar.DAY_OF_WEEK);
        if (kalenteri.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
          alkuun = -6;
        }
        kalenteri.add(Calendar.DATE, alkuun);
        alkupvm = "" + df.format(kalenteri.getTime());
      }

      try {
        tempPvm = df.parse(alkupvm);
      } catch (Exception e) {
        out.println("Virhe: " + e);
      }

      kalenteri.setTime(tempPvm);
  
  
      /*
       * Tässä pyöritellään kalenteria edestakaisin, jotta saadaan
       * oikeat päivämäärät sen tulostusta ohjaaville nuolille, sekä
       * päivämäärät tulostettavan aikavälin päätepisteille
       */
  
      kalenteri.add(Calendar.DATE, -1);
      String taakse = df.format(kalenteri.getTime());
  
      kalenteri.add(Calendar.DATE, 2);
      String eteen = df.format(kalenteri.getTime());
  
      kalenteri.add(Calendar.DATE, -8);
      String viikkoTaakse = df.format(kalenteri.getTime());
  
      kalenteri.add(Calendar.DATE, 14);
      String viikkoEteen = df.format(kalenteri.getTime());
  
      kalenteri.add(Calendar.DATE, -7);
      String viikonAlku = CheckDate.c(kalenteri.get(Calendar.DATE)) +
                          "." + CheckDate.c(kalenteri.get(Calendar.MONTH)+1) +
                          "." + kalenteri.get(Calendar.YEAR);
  
      out.println("<table width='800' class='blank'><tr><td>" +
                  "Varaukset ajalle: <b>" + viikonAlku + " - ");
  
      kalenteri.add(Calendar.DATE, 6);
      String viikonLoppu = CheckDate.c(kalenteri.get(Calendar.DATE)) +
                           "." + CheckDate.c(kalenteri.get(Calendar.MONTH)+1) +
                           "." + kalenteri.get(Calendar.YEAR);
  
      out.println(viikonLoppu + "</b></td>");
  
      kalenteri.add(Calendar.DATE, -6);
  
 
      String vuosiNyt = "" + kalenteri.get(Calendar.YEAR);
  
      out.println("<td><a href='http://db.cs.helsinki.fi/" +
                  "s/avtanska/VuosiKalenteri?henkilo=" + henkilo  + 
                  "&ryhma=" + ryhma + "&vuosi=" + vuosiNyt + "'>" +
                  "Koko vuoden kalenteri</a></td>");
                  
       
      /*
       * Varausten poistoon tarkoitetut napit ja muutama
       * piilokenttä, jotta tarvittavat tiedot pysyvät
       * mukana matkassa
       */             
                  
      out.println("<td><input type='submit' name='poista' " +
                  "value='Poista valitut'><input type='reset' " +
                  "name='reset' value='Tyhjennä valinnat'>");
    
      if (henkilo != null) {
        out.println("<input type='hidden' name='henkilo' value='" + 
                    henkilo + "'>");
      }
      else if (ryhma != null) {
        out.println("<input type='hidden' name='ryhma' value='" + 
                    ryhma + "'>");
      }
    
      out.println("<input type='hidden' name='alkupvm' value='" + 
                  viikonAlku + "'></td></tr></table>");
                  
                  
                  
      /* 
       * Kalenterin tulostus alkaa
       */             
  
      out.println("<table cellpadding='0' cellspacing='1' " + 
                  "style='background: #000000'>");
  
  
      out.println("<tr><td class='kalenteri' style='padding: 4 0 4 0' " + 
                  "colspan='8' align='center'>");
  
      /*
       * Ohjausnuolet
       */

      out.println("<a href='?alkupvm=" + viikkoTaakse + "&" + henkilöTaiRyhmä +
                  "'>&lt;&lt;</a>&nbsp;");
      out.println("&nbsp;&nbsp;<a href='?alkupvm=" + taakse + "&" + 
                  henkilöTaiRyhmä + "'>&lt;</a>&nbsp;");
      out.println("&nbsp;<a href='?alkupvm=" + eteen + "&" + henkilöTaiRyhmä +
                  "'>&gt;</a>&nbsp;");
      out.println("&nbsp;&nbsp;<a href='?alkupvm=" + viikkoEteen + "&" + 
                  henkilöTaiRyhmä + "'>&gt;&gt;</a></td></tr><tr>");
    
    
      /*
       * Päivien otsikot kalenteriin
       */
    
      out.println("<th>klo</th>");
  
      for (int i = 0; i < 7; i++) {  
        int viikonPäivä = kalenteri.get(Calendar.DAY_OF_WEEK);
  
        switch (viikonPäivä) {
          case Calendar.MONDAY: 
             out.println("<th width='100'>Maanantai</th>"); break;
          case Calendar.TUESDAY: 
             out.println("<th width='100'>Tiistai</th>"); break;
          case Calendar.WEDNESDAY: 
             out.println("<th width='100'>Keskiviikko</th>"); break;
          case Calendar.THURSDAY: 
             out.println("<th width='100'>Torstai</th>"); break;
          case Calendar.FRIDAY: 
             out.println("<th width='100'>Perjantai</th>"); break;
          case Calendar.SATURDAY: 
             out.println("<th width='100'>Lauantai</th>"); break;
          case Calendar.SUNDAY: 
             out.println("<th width='100'>Sunnuntai</th>"); break;
        }
  
        kalenteri.add(Calendar.DATE, 1);  
      }
  
      kalenteri.add(Calendar.DATE, -7);
  
      out.println("</tr>");
  
  
     
      /*
       * Varauksien käsittely alkaa
       */  
  
      con=null;
      con= createDbConnection(dbDriver,dbServer,dbUser,dbPassword,out);
      if (con==null) {
         out.println("</body></html>");
         return;
      }
  
      int varaustenMäärä = 0;
      int aikaisinVaraus = 0;
  
  
      stmt = null;
      rs = null;
  
      try {
        stmt = con.createStatement();
  
        
        /* 
         *  Haetaan viikon aikaisimman varauksen alkuaika, jotta voidaan
         *  halutessa trimmata turhat tyhjät pois aamuista kalenterissa
         */
  
        if (henkilo != null) {
          rs = stmt.executeQuery(
              "select min(alkuaika) as aikaisin from varaus where tunnus='" +
              henkilo + "' and pvm between to_date('" + viikonAlku +
              "', 'DD.MM.YYYY') and to_date('" + viikonLoppu +
              "', 'DD.MM.YYYY')");
        }
        else if (ryhma != null) {
          rs = stmt.executeQuery(
              "select min(alkuaika) as aikaisin from varaus where ryhmä='" +
              ryhma + "' and pvm between to_date('" + viikonAlku +
              "', 'DD.MM.YYYY') and to_date('" + viikonLoppu +
              "', 'DD.MM.YYYY')");
        }
   
  
        if (rs.next()) {  
          if (rs.getString("aikaisin") != null) {
            aikaisinVaraus =  
               Integer.parseInt((rs.getString("aikaisin")).substring(0,2));
          }
        }
  
  
        /*
         *  Kommentoi 'aikaisinVaraus = 0' rivi pois, jos
         *  haluat, että tila ennen viikon aikaisinta
         *  varausta trimmataan pois. Nyt ominaisuus on 
         *  käytössä vain admin-käyttäjällä testitarkoituksessa.
         */
 
         if (!sessionTunnus.equals("admin")) {
           aikaisinVaraus = 0;
         }
  
  
  
        /*
         *  Varausten poisto, ryhmän varauksen poistaminen poistaa
         *  varauksen myös kaikilta ryhmän jäseniltä
         */
  
        if (valitutTaulu != null && poista != null) {

          if (ryhma != null) {            
 

            /*
             * Otetaan talteen poistettavien ryhmävarausten tiedot
             */
          
            rs = stmt.executeQuery("SELECT * FROM varaus WHERE vid IN " +
                "(" + valitut + ")");     
  
           
            /*
             * Poistetaan ryhmän jäseniltä varaukset, joiden tiedot
             * täsmäävät ylläsaatujen kanssa
             */ 

            while (rs.next()) {
               String deleteJäseneltä =
                 "DELETE FROM varaus WHERE tunnus IN " +
                 "(SELECT tunnus FROM jäsenyys WHERE ryhmä='" + 
                 ryhma + "') AND pvm='" + rs.getString("pvm") + "' AND " +
                 "alkuaika='" + rs.getString("alkuaika") + "' AND " + 
                 "kesto='" + rs.getString("kesto") + "' AND " +
                  "aihe='" + rs.getString("aihe") + "' AND " +
                 "näkyvyys='" + rs.getString("näkyvyys") + "' AND " +
                 "varaaja='" + rs.getString("varaaja") + "'";

               int count = stmt.executeUpdate(deleteJäseneltä);
            }
          }

        
          /*
           * Yleinen poisto, eli poistaa vain ja ainoastaan varaukset
           * jotka käyttäjä on ruksannut
           */

          String queryDelete = "DELETE FROM varaus WHERE vid IN " +
                               "(" + valitut + ")";
          
          int count = stmt.executeUpdate(queryDelete);
    
          
        } // loppu if poistetaan varauksia
  

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
  
      out.println("<tr><td class='kalenteri' valign='top'>" +
                  "<table class='ajat' cellpadding='0' cellspacing='0'>");

      for (int i = (0 + aikaisinVaraus); i < 24; i++) {
        if (i == 23) {
          out.println("<tr><td class='kalenteri' valign='top' height='" + 
                      ((TUNNIN_KORKEUS)-1) + "'>" + CheckDate.c(i) +  
                      ":00</td></tr>");
        }
        else {
          out.println("<tr><td class='kalenteri' valign='top' height='" + 
                      TUNNIN_KORKEUS + "'>" + CheckDate.c(i) + ":00</td></tr>");
        }
      }

      out.println("</table></td>");
  
  
  
      /*
       * Käydään seitsemän päivää kalenterin tämänhetkisestä päivämäärästä
       * lähtien läpi 
       */  
  
      for (int i = 0; i < 7; i++) {  // forin alku
  
        out.println("<td class='kalenteri' style='background-image: " + 
            "url(http://db.cs.helsinki.fi/u/avtanska/tsoha/img/kale_bg.gif)'" +
            " valign='top'>");
   
        String käsiteltäväPäivä = 
            "" + CheckDate.c(kalenteri.get(Calendar.DATE)) + "." + 
            CheckDate.c((kalenteri.get(Calendar.MONTH)+1)) + "." + 
            kalenteri.get(Calendar.YEAR);
   
   
        try {
          stmt = con.createStatement();
          double edellisenLoppu = 0;
          boolean päivänEnsimmäinen = true;
 
  
          /*
           *  Haetaan joko henkilön tai ryhmän varaukset
           */
 
          if (henkilo != null) {
            rs = stmt.executeQuery(
                "SELECT * FROM varaus WHERE tunnus='" + henkilo +
                "' AND pvm=to_date('" + käsiteltäväPäivä +
                "', 'DD.MM.YYYY') ORDER BY alkuaika");
          }
          else if (ryhma != null) {
            rs = stmt.executeQuery(
                "SELECT * FROM varaus WHERE ryhmä='" + ryhma +
                "' and pvm=to_date('" + käsiteltäväPäivä +
                "', 'DD.MM.YYYY') order by alkuaika");
          }
 
    
 
          /*
           *  Käydään yhden päivän kaikki varaukset läpi yksi kerrallaan
           */
 
          boolean onkoVarauksia = false;

          while(rs.next()) {
 
            onkoVarauksia = true;

            Calendar varausKesto = Calendar.getInstance();
            String poistoNappi = "";
            String varausPvm = rs.getString("pvm");
            String alkuAikaString = rs.getString("alkuaika");
            String varausAlkaa = "";
            String varausLoppuu = "";
            double kesto = 0;
            double tyhjaTila = 0;
            double alkuAika = 0;

            alkuAika = Integer.parseInt(alkuAikaString.substring(0,2));
            kesto = (new Double(rs.getString("kesto"))).doubleValue();

            if (((rs.getString("alkuaika")).substring(3,5)).equals("30")) {
              alkuAika = alkuAika + .5;
            }
            
            


            /*
             *  Lasketaan ja tulostellaan tyhjä tila ennen
             *  varausta
             */

            if (päivänEnsimmäinen) {
              tyhjaTila = alkuAika - aikaisinVaraus;
            } 
            else {
              tyhjaTila = alkuAika - edellisenLoppu;
            }

            edellisenLoppu = alkuAika + kesto;
 

            out.println("<table cellspacing='0' cellpadding='0' " +
                        "width='100' class='varaus'>");

            
            if (päivänEnsimmäinen && tyhjaTila != 0) {
              out.println("<tr><td width='100' class='kalenteri' " +
                 "style='background: url(http://db.cs.helsinki.fi/u/avtanska" +
                 "/tsoha/img/transparent.gif)' height='" + 
                 ((tyhjaTila*TUNNIN_KORKEUS)-1) + "'></td></tr>");
            } else if (tyhjaTila != 0) {
              out.println("<tr><td width='100' class='kalenteri'  " +
                 "style='background: url(http://db.cs.helsinki.fi/u/avtanska" +
                 "/tsoha/img/transparent.gif)' height='" + 
                 ((tyhjaTila*TUNNIN_KORKEUS)-1) + "'></td></tr>");
            }

 
            /*
             * Jos varaus ei ala kello 00:00, tulostetaan varauksen
             * yläreunaan musta viiva
             */

            if ((päivänEnsimmäinen && tyhjaTila != 0) || tyhjaTila != 0) {
              out.println("<tr><td height='1' style='background: #000000'>" +
                          "</td></tr>");
            }

            
            /*
             * Sitten itse varauksen tulostus
             */

            out.println("<tr><td valign='top' class='varaus'" +            
                        " height='" + ((kesto*TUNNIN_KORKEUS)-1) + "'>");
                 

            if (sessionTunnus.equals(rs.getString("varaaja")) || 
                sessionTunnus.equals("admin")) {
              poistoNappi = "<input type='checkbox' name='valitut' " +
                            "value='" + rs.getString("vid") + "'>";
            }


            /*
             *  Valmistellaan varauksen alku-, ja loppuaika 
             *  tulostuskuntoon
             */

            varausAlkaa = (rs.getString("alkuaika")).substring(0,5);
            varausLoppuu = "";
            
            varausKesto.clear();
            varausKesto.set(Calendar.HOUR, 
                            Integer.parseInt(alkuAikaString.substring(0,2)));
            varausKesto.set(Calendar.MINUTE, 
                            Integer.parseInt(alkuAikaString.substring(3,5)));
            varausKesto.add(Calendar.MINUTE, (int)(kesto * 60));

            varausLoppuu += 
               "" + CheckDate.c(varausKesto.get(Calendar.HOUR_OF_DAY)) + ":";

            varausLoppuu += 
              "" + CheckDate.c(varausKesto.get(Calendar.MINUTE));

            
            /*
             *  Tulostetaan varauksen tietoja
             */            

            if (rs.getString("näkyvyys").equals("2") &&
                !sessionTunnus.equals(rs.getString("tunnus")) &&
                !sessionTunnus.equals(rs.getString("varaaja")) &&
                !sessionTunnus.equals("admin")) {
              out.println("<p class='varaus'><b>Varattu</b><br />" +
                varausAlkaa + "-" + varausLoppuu + "</p>");
            }
            else {
              if (kesto < 2) {
                out.println("<p class='varaus'><b>" + rs.getString("aihe") +
                            "</b>&nbsp;&nbsp;" + poistoNappi + "</p>");
              }
              else {
                out.println("<p class='varaus'><b>" +  rs.getString("aihe") +
                            "</b><br />" + varausAlkaa + "-" + varausLoppuu +
                            "<br />Varaaja: " + rs.getString("varaaja") +
                            poistoNappi + "</p>");
              }
            }

            out.println("</td></tr>");

            /* 
             * Tulostetaan varauksen alareunaan musta viiva, jos varaus
             * ei lopu kello 24:00
             */
 
            if (alkuAika + kesto < 24) {
              out.println("<tr><td height='1' style='background: #000000'>" +
                          "</td></tr>");
            }

            out.println("</table>");
 
            päivänEnsimmäinen = false;
            varaustenMäärä++;
            
          } // loppu while

          if (!onkoVarauksia) {
            out.println("&nbsp;");
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
   
        kalenteri.add(Calendar.DATE, 1);
        out.println("</td>");
  
      } // loppu for
  
  
      /*
       *  Suljetaan yhteys
       */
  
      try {
        if (rs!=null) rs.close();
        if (stmt!=null) stmt.close();
        con.close();
      } catch(SQLException e) {
          out.println("An SQL Exception was thrown.");
      }
        
      out.println("</tr><tr><th colspan='8'>Varausten lukumäärä: " +
                  varaustenMäärä + "</th></tr></table></form></body></html>");
    
      
    
     
     
    } // loppu else ryhmä ja henkilö ei null
  
  }
    
  
  
  private Connection createDbConnection(
      String dbDriver, String dbServer, String dbUser, String dbPassword,
      ServletOutputStream out) throws IOException {

      try{
          Class.forName(dbDriver); 
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