import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.Calendar;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class RyhmanAjat extends HttpServlet { 

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
    String omatryhmat = req.getParameter("omatryhmat");


    out.println("<html><head><title>Database query from DB (tsoha)</title>" +
                "<link rel='stylesheet' style='text/css' " +
                "href='http://db.cs.helsinki.fi/u/avtanska/tsoha/perus.css'>" +
                "</head><body bgcolor=white>");
  
    /*
     * Tarkistetaan onko valittu ryhmää
     */
     
    if (omatryhmat.equals("tyhjä")) {
      out.println("<h3>Valitse ryhmäsi.</h3>");
    }

    /*
     * Jos valinnat kunnossa aloitetaan vapaiden aikojen laskeminen
     */
 
    else {
   
      out.println("<form method='post' action='http://db.cs.helsinki.fi/" +
                  "s/avtanska/Kalenteri'>");
                  
      String alkupvm = req.getParameter("alkupvm");


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

      out.println("<h3>Ryhmän '" + omatryhmat + "' kaikkien henkilöiden " +
                  "varaukset koottuna yhteen:</h3>");

             
      /*
       * Solujen värit selityksineen
       */

      out.println("<p><table><tr><td style='background: #000000' " +
                  "width='100' height='13'>&nbsp;</td>" +
                  "<td style='background: #ffffff' width='30' " +
                  "align='center'>=</td>" + 
                  "<td style='background: #ffffff'>varattu</td>" +
                  "</tr></table></p>");

      out.println("<p><table><tr><td width='100' height='15'>" +
                  "<table cellpadding='0' cellspacing='1' bgcolor='#000000'>" +
                  "<tr><td width='100' height='15' style='background: " +
                  "#eeeeee'>&nbsp;</td></tr></table></td>" +
                  "<td style='background: #ffffff' width='30' "+
                  "align='center'>=</td>" + 
                  "<td style='background: #ffffff'>vapaa kaikilla</td>" +
                  "</tr></table></p>");


      out.println("<p>Varaukset ajalle: <b>" + viikonAlku + " - ");

      kalenteri.add(Calendar.DATE, 6);
      String viikonLoppu = CheckDate.c(kalenteri.get(Calendar.DATE)) +
                           "." + CheckDate.c(kalenteri.get(Calendar.MONTH)+1) +
                           "." + kalenteri.get(Calendar.YEAR);

      out.println(viikonLoppu + "</b></p>");

      kalenteri.add(Calendar.DATE, -6);


      Connection con=null;
      con= createDbConnection(dbDriver,dbServer,dbUser,dbPassword,out);
      if (con==null) {
         out.println("</body></html>");
         return;
      }
         
     
      /*
       * Luodaan 48 x 7 taulukko, johon kootaan kaikkien ryhmän
       * jäsenten viikon varaukset. 48 siksi, että puoli tuntia
       * on pienin kalenterin tuntema aikayksikkö 24*2 = 48
       * 
       * Luodaan myös väliaikainen klooni oikeasta kalenterista,
       * jonka avulla käydään viikon päivät läpi.
       */

      boolean[][] ryhmänVaraukset = new boolean[48][7];
      Calendar tempKalenteri = (Calendar)kalenteri.clone();
      
      
      /*
       * Alustetaan taulukko. Jos taulukossa arvo true, niin
       * jollakin ryhmän henkilöllä on varaus kyseisessä kohdassa
       */
      
      for (int i = 0; i < ryhmänVaraukset.length; i++) {
        for (int j = 0; j < ryhmänVaraukset[i].length; j++) {
          ryhmänVaraukset[i][j] = false;
        }
      }

      Statement stmt1 = null;
      Statement stmt2 = null;
      ResultSet rs1 = null; 
      ResultSet rs2 = null; 


      try {
        stmt1 = con.createStatement();
        stmt2 = con.createStatement();

 
        /*
         * Haetaan ryhmän jäsenet tietokannasta
         */

        String selectJäsenet =
           "SELECT tunnus FROM jäsenyys WHERE ryhmä='" + omatryhmat + "'";

        rs1 = stmt1.executeQuery(selectJäsenet);

     
        /* 
         * Käydään jokainen ryhmän jäsen läpi
         */

        while (rs1.next()) {

          /*
           * Käydään viikko läpi
           */

          for (int i = 0; i < 7; i++) {
 
            String käsiteltäväPäivä = 
               "" + CheckDate.c(tempKalenteri.get(Calendar.DATE)) + 
               "." + CheckDate.c(tempKalenteri.get(Calendar.MONTH)+1) + 
               "." + tempKalenteri.get(Calendar.YEAR);      
 
  
            /*
             * Haetaan käsittelyssä olevan henkilön varaukset 
             * käsiteltävälle päivälle.
             */            
     
            String selectVaraukset =
               "SELECT * FROM varaus WHERE tunnus ='" + 
               rs1.getString("tunnus") + "' AND pvm=to_date('" + 
               käsiteltäväPäivä + "', 'DD.MM.YYYY') " +
               "ORDER BY alkuaika"; 
 
            rs2 = stmt2.executeQuery(selectVaraukset);               
   
   
            /*
             * Käydään yhden päivän varaukset läpi, ja vaihdetaan
             * taulukkoon true kaikkiin varaukseen kuuluviin puolen
             * tunnin lokeroihin.
             */
   
            while (rs2.next()) {
     
              String varauksenAlku = rs2.getString("alkuaika");
              String varauksenKesto = rs2.getString("kesto");
              int alkuaika = 0;
              int kesto = 0;
     
      
              /*
               * Muutetaan alkuaika ja varauksen kesto laskettavaan muotoon.
               * Puolituntia on yksi rivi taulukossa. Näin olleen taulukon
               * indeksit saadaan suoraan kertomalla tunnit kahdella ja 
               * jos ajassa on puolta tuntia merkitsevä osa, niin lisätään
               * lukuun yksi.                           
               */
     
              if (varauksenAlku.substring(3,5).equals("30")) {
                alkuaika = 
                   2 * Integer.parseInt(varauksenAlku.substring(0,2)) + 1;
              }
              else {
                alkuaika = 
                   2 * Integer.parseInt(varauksenAlku.substring(0,2));
               }
               
               kesto = 
                  (int)((new Double(rs2.getString("kesto")).doubleValue()) * 2);
              
   
              /* 
               * Muutetaan varauksen ajan viemät lokerot arvoksi true.
               */
   
              for (int j = alkuaika; j < alkuaika + kesto; j++) {
                ryhmänVaraukset[j][i] = true;
              }
     
            }  // end while rs2

            tempKalenteri.add(Calendar.DATE, 1);
  
          }  // end for

          tempKalenteri.add(Calendar.DATE, -7);
 
        }  // end while rs1

     

      } catch (SQLException ee) {
            out.println("Tietokantavirhe "+ee.getMessage());
      } finally {
            try {
               if (rs1!=null) rs1.close(); 
               if (rs2!=null) rs2.close();
               if (stmt1!=null) stmt1.close(); 
               if (stmt2!=null) stmt2.close();
               con.close();
            } catch(SQLException e) { 
               out.println("An SQL Exception was thrown."); 
            }
      }

      

      /*
       * Tulostetaan ryhmän vapaat ajat kalenteriin
       */

      out.println("<table cellpadding='3' cellspacing='1' " +
                  "style='background: #000000'>");

             
      out.println("<tr><td class='kalenteri' colspan='8' align='center'>");
  
      out.println("<a href='?alkupvm=" + viikkoTaakse + 
                  "&omatryhmat=" + omatryhmat + "'>&lt;&lt;</a>&nbsp;");
      out.println("&nbsp;<a href='?alkupvm=" + taakse + 
                  "&omatryhmat=" + omatryhmat + "'>&lt;</a>&nbsp;");
      out.println("&nbsp;<a href='?alkupvm=" + eteen + 
                  "&omatryhmat=" + omatryhmat + "'>&gt;</a>&nbsp;");  
      out.println("&nbsp;<a href='?alkupvm=" + viikkoEteen + 
                  "&omatryhmat=" + omatryhmat + "'>&gt;&gt;</a></td></tr>");

      out.println("<tr><th>klo</th>");

      for (int i = 0; i < 7; i++) {

        int viikonPäivä = tempKalenteri.get(Calendar.DAY_OF_WEEK);       

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
 
        tempKalenteri.add(Calendar.DATE, 1);
  
      }

      out.println("</tr>");

      for (int i = 0; i < ryhmänVaraukset.length; i++) {
        String klo = "";

        /*
         * Tulostetaan ajat rivien alkuun
         */

        if (i % 2 == 0) { 
          if (i < 20) klo = "0" + (i / 2) + ":00"; 
          else klo = (i / 2) + ":00";
        }
        else {
          klo = "&nbsp;";           
        }

  
        /*
         * Varsinainen varausten ja vapaiden aikojen tulostus         
         */

        out.println("<td class='kalenteri' style='font-size: 8pt; " +
                    "padding: 0 2 0 2'>" + klo + "</td>");
        for (int j = 0; j < ryhmänVaraukset[i].length; j++) {
          if (ryhmänVaraukset[i][j] == true) {
            out.println("<td height='13' style='background: #000000; " +
                        "font-size: 4pt'>&nbsp;</td>");      
          }
          else {
            out.println("<td class='kalenteri' height='13' style='" +
                        "font-size: 4pt'>&nbsp;</td>");
          }
        }
        out.println("</tr>");
      }

      out.println("</table></body></html>");

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