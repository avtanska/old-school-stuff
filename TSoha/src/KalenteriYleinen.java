import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.Calendar;
import java.text.DateFormat;
import java.util.Date;


public class KalenteriYleinen extends HttpServlet {

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
     * T�m� muuttuja m��rittelee kuinka monta pikseli� yhden 
     * tunnin korkeus kalenterissa on. HUOM! Jos muutat, muista
     * vaihtaa my�s tuntiviivoja esitt�v� kuva kale_bg.gif.
     */ 

    final int TUNNIN_KORKEUS = 26;

    String henkilo = req.getParameter("henkilo");
    String ryhma = req.getParameter("ryhma");
    String alkupvm = req.getParameter("alkupvm");
    String henkil�TaiRyhm� = "";
    
    
    if (henkilo != null && henkilo.equals("tyhj�")) {
      henkilo = null;
    }
    if (ryhma != null && ryhma.equals("tyhj�")) {
      ryhma = null;
    }
    
    out.println("<html><head><title>Database query from DB (tsoha)</title>" +
    "<link rel='stylesheet' style='text/css' " +
    "href='http://db.cs.helsinki.fi/u/avtanska/tsoha/perus.css'></head>" +
    "<body>");
  
  
  
  
    if (ryhma == null && henkilo == null) {
      out.println("<h3>Valitse ryhm� tai henkil�.</h3>");
    }
    else {
  
  
      if (henkilo != null) {
        henkil�TaiRyhm� = "henkilo=" + henkilo;
        out.println("<h3>Varaukset henkil�lle '" + henkilo + "'</h3>");
      }
      else if (ryhma != null) {
        henkil�TaiRyhm� = "ryhma=" + ryhma;
        out.println("<h3>Varaukset ryhm�lle '" + ryhma + "'</h3>");
      }
  
      out.println("<form method='post' " + 
         "action='http://db.cs.helsinki.fi/s/avtanska/Kalenteri'>");
  
      
  
      /*
       * Alustetaan kalenteri osoittamaan k�ynniss� olevan
       * viikon ensimm�iseen p�iv��n (Euroopassa maanantai)
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
       * T�ss� py�ritell��n kalenteria edestakaisin, jotta saadaan
       * oikeat p�iv�m��r�t sen tulostusta ohjaaville nuolille, sek�
       * p�iv�m��r�t tulostettavan aikav�lin p��tepisteille
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
  
      out.println("<p>Varaukset ajalle: <b>" + viikonAlku + " - ");
  
      kalenteri.add(Calendar.DATE, 6);
      String viikonLoppu = CheckDate.c(kalenteri.get(Calendar.DATE)) +
                           "." + CheckDate.c(kalenteri.get(Calendar.MONTH)+1) +
                           "." + kalenteri.get(Calendar.YEAR);
  
      out.println(viikonLoppu + "</b></p>");
  
      kalenteri.add(Calendar.DATE, -6);
  
  
      out.println("<table cellpadding='0' cellspacing='1' " + 
                  "style='background: #000000'>");
  
  
      out.println("<tr><td class='kalenteri' style='padding: 4 0 4 0' " +
                  "colspan='8' align='center'>");
  
      out.println("<a href='?alkupvm=" + viikkoTaakse + "&" + henkil�TaiRyhm� +
                  "'>&lt;&lt;</a>&nbsp;");
      out.println("&nbsp;&nbsp;<a href='?alkupvm=" + taakse + "&" + 
                  henkil�TaiRyhm� + "'>&lt;</a>&nbsp;");
      out.println("&nbsp;<a href='?alkupvm=" + eteen + "&" + henkil�TaiRyhm� +
                  "'>&gt;</a>&nbsp;");
      out.println("&nbsp;&nbsp;<a href='?alkupvm=" + viikkoEteen + "&" + 
                  henkil�TaiRyhm� + "'>&gt;&gt;</a></td></tr><tr>");
    
    
      /*
       * P�ivien otsikot kalenteriin
       */
    
      out.println("<th>klo</th>");
  
      for (int i = 0; i < 7; i++) {  
        int viikonP�iv� = kalenteri.get(Calendar.DAY_OF_WEEK);
  
        switch (viikonP�iv�) {
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
       * Varauksien k�sittely alkaa
       */  
  
      Connection con=null;
      con= createDbConnection(dbDriver,dbServer,dbUser,dbPassword,out);
      if (con==null) {
         out.println("</body></html>");
         return;
      }
  
      int num = 0;
      int aikaisinVaraus = 0;
  
  
      Statement stmt = null;
      ResultSet rs = null;
  
      try {
        stmt = con.createStatement();
  
        
        /* 
         *  Haetaan viikon aikaisimman varauksen alkuaika, jotta voidaan
         *  halutessa trimmata turhat tyhj�t pois aamuista kalenterissa
         */
  
        if (henkilo != null) {
          rs = stmt.executeQuery(
              "select min(alkuaika) as aikaisin from varaus where tunnus='" +
              henkilo + "' and pvm between to_date('" + viikonAlku +
              "', 'DD.MM.YYYY') and to_date('" + viikonLoppu +
              "', 'DD.MM.YYYY') group by alkuaika");
        }
        else if (ryhma != null) {
          rs = stmt.executeQuery(
              "select min(alkuaika) as aikaisin from varaus where ryhm�='" +
              ryhma + "' and pvm between to_date('" + viikonAlku +
              "', 'DD.MM.YYYY') and to_date('" + viikonLoppu +
              "', 'DD.MM.YYYY') group by alkuaika");
        }
  
  
        while (rs.next()) {
          aikaisinVaraus = 
             Integer.parseInt((rs.getString("aikaisin")).substring(0,2));
        }
  
  
        /*
         *  Kommentoi 'aikaisinVaraus = 0' rivi pois, jos
         *  haluat, ett� tila ennen viikon aikaisinta
         *  varausta trimmataan pois.
         */
  
        aikaisinVaraus = 0;
  

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
       * K�yd��n seitsem�n p�iv�� kalenterin t�m�nhetkisest� p�iv�m��r�st�
       * l�htien. 
       */
  
  
      for (int i = 0; i < 7; i++) {  // forin alku
  
        out.println("<td class='kalenteri' style='background-image: " +
           "url(http://db.cs.helsinki.fi/u/avtanska/tsoha/img/kale_bg.gif)' " +
           "valign='top'>");
   
        String k�sitelt�v�P�iv� = 
            "" + CheckDate.c(kalenteri.get(Calendar.DATE)) + "." + 
            CheckDate.c((kalenteri.get(Calendar.MONTH)+1)) + "." + 
            kalenteri.get(Calendar.YEAR);
   
   
        try {
          stmt = con.createStatement();
          double edellisenLoppu = 0;
          boolean p�iv�nEnsimm�inen = true;
 
  
          /*
           *  Haetaan joko henkil�n tai ryhm�n varaukset
           */
 
          if (henkilo != null) {
            rs = stmt.executeQuery(
                "select * from varaus where tunnus='" + henkilo +
                "' and pvm=to_date('" + k�sitelt�v�P�iv� +
                "', 'DD.MM.YYYY') order by alkuaika");
          }
          else if (ryhma != null) {
            rs = stmt.executeQuery(
                "select * from varaus where ryhm�='" + ryhma +
                "' and pvm=to_date('" + k�sitelt�v�P�iv� +
                "', 'DD.MM.YYYY') order by alkuaika");
          }
 
    
 
          /*
           *  K�yd��n yhden p�iv�n kaikki varaukset l�pi yksi kerrallaan
           */
 
          while(rs.next()) {
 
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
             *  Lasketaan ja tulostellaan tyhj� tila ennen
             *  varausta
             */

            if (p�iv�nEnsimm�inen) {
              tyhjaTila = alkuAika - aikaisinVaraus;
            } 
            else {
              tyhjaTila = alkuAika - edellisenLoppu;
            }

            edellisenLoppu = alkuAika + kesto;
 

            out.println("<table cellspacing='0' cellpadding='0' " +
                        "width='100' class='varaus'>");

            
            if (p�iv�nEnsimm�inen && tyhjaTila != 0) {
              out.println("<tr><td width='100' class='kalenteri' " +
                 "style='background: url(http://db.cs.helsinki.fi/u/avtanska" +
                 "/tsoha/img/transparent.gif)' height='" + 
                 ((tyhjaTila*TUNNIN_KORKEUS)-1) + "'></td></tr>");
            } else if (tyhjaTila != 0) {
              out.println("<tr><td width='100' class='kalenteri' " +
                 "style='background: url(http://db.cs.helsinki.fi/u/avtanska" +
                 "/tsoha/img/transparent.gif)' height='" + 
                 ((tyhjaTila*TUNNIN_KORKEUS)-1) + "'></td></tr>");
            }

            if ((p�iv�nEnsimm�inen && tyhjaTila != 0) || tyhjaTila != 0) {
              out.println("<tr><td height='1' style='background: #000000'>" +
                          "</td></tr>");
            }

            
            /*
             * Sitten itse varauksen tulostus
             */

            out.println("<tr><td valign='top' class='varaus'" +            
                        " height='" + ((kesto*TUNNIN_KORKEUS)-1) + "'>");
                 
            out.println("</td></tr>");
 
            if (alkuAika + kesto < 24) {
              out.println("<tr><td height='1' style='background: #000000'>" +
                          "</td></tr>");
            }

            out.println("</table>");
 
            p�iv�nEnsimm�inen = false;
            num++;
            
          } // loppu while
   
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
        
      out.println("</tr><tr><th colspan='8'>Varausten lukum��r�: " +
                  num + "</th></tr></table></form></body></html>");
     
    } // loppu else ryhm� ja henkil� ei null
  
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