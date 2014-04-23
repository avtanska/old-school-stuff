import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.Calendar;

public class VuosiKalenteri extends HttpServlet {

  final String dbDriver="org.postgresql.Driver";
  final String dbServer ="jdbc:postgresql://localhost:10388/tsoha";
  final String dbUser= "avtanska";        // replace with your db user account
  final String dbPassword ="postgres"; // replace with your password


  public void service(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
    ServletOutputStream out;
    res.setContentType("text/html");
    out= res.getOutputStream();

    HttpSession session = req.getSession(false);
    String sessionTunnus = (String)session.getValue("sessionTunnus");
    String henkilo = req.getParameter("henkilo");
    String ryhma = req.getParameter("ryhma");
    String vuosi = req.getParameter("vuosi");

    out.println("<html><head><title></title>" +
                "<link rel='stylesheet' type='text/css' href='http://" +
                "db.cs.helsinki.fi/u/avtanska/tsoha/vuosi.css' /><body>");


    /*
     * Luodaan kalenteri ja asetetaan se osoittamaan vuoden 
     * ensimm‰iseen p‰iv‰‰n.
     */

    Calendar vuosiKalenteri = Calendar.getInstance();

    vuosiKalenteri.set(Calendar.YEAR, Integer.parseInt(vuosi));
    vuosiKalenteri.set(Calendar.DAY_OF_YEAR, 1);

    int p‰ivi‰Rivill‰ = 0;

    Connection con=null;
    con= createDbConnection(dbDriver,dbServer,dbUser,dbPassword,out);
    if (con==null) {
       out.println("</body></html>");
       return;
    }

    Statement stmt = null;
    ResultSet rs = null;

    out.println("<h3>" + vuosiKalenteri.get(Calendar.YEAR) + "</h3>");

 
    /*
     * K‰yd‰‰n kaikki 12 kuukautta l‰pi
     */

    for (int k = 0; k < 12; k++) {

      out.println("<table cellpadding='0' cellspacing='1' " +
               "style='background: #000000'><tr>");


      /*
       * Kuukauden nimi
       */

      switch (vuosiKalenteri.get(Calendar.MONTH)) {
       case Calendar.JANUARY:
          out.println("<td class='kuukausi' colspan='8'>Tammikuu</td>"); break;
       case Calendar.FEBRUARY:
          out.println("<td class='kuukausi'colspan='8'>Helmikuu</td>"); break;
       case Calendar.MARCH:
          out.println("<td class='kuukausi' colspan='8'>Maaliskuu</td>"); break;
       case Calendar.APRIL:
          out.println("<td class='kuukausi' colspan='8'>Huhtikuu</th>"); break;
       case Calendar.MAY:
          out.println("<td class='kuukausi' colspan='8'>Toukokuu</td>"); break;
       case Calendar.JUNE:
          out.println("<td class='kuukausi' colspan='8'>Kes‰kuu</td>"); break;
       case Calendar.JULY:
          out.println("<td class='kuukausi' colspan='8'>Hein‰kuu</td>"); break;
       case Calendar.AUGUST:
          out.println("<td class='kuukausi' colspan='8'>Elokuu</td>"); break;
       case Calendar.SEPTEMBER:
          out.println("<td class='kuukausi' colspan='8'>Syyskuu</td>"); break;
       case Calendar.OCTOBER:
          out.println("<td class='kuukausi' colspan='8'>Lokakuu</td>"); break;
       case Calendar.NOVEMBER:
          out.println("<td class='kuukausi' colspan='8'>Marraskuu</td>"); break;
       case Calendar.DECEMBER:
          out.println("<td class='kuukausi' colspan='8'>Joulukuu</td>"); break;

      }

      out.println("</tr><tr><td>&nbsp;</td><th>Ma</th><th>Ti</th><th>Ke</th>" +
                  "<th>To</th><th>Pe</th><th>La</th><th>Su</th></tr><tr>");


      /* 
       * Otetaan talteen k‰sittelyss‰ olevan kuukauden p‰ivien 
       * lukum‰‰r‰
       */

      int maxKuunP‰ivi‰ = 
        vuosiKalenteri.getActualMaximum(Calendar.DAY_OF_MONTH);
      int kuunP‰iv‰ = 1;
      String varattu = "";
      String linkki = "";
      String pvm = "";
      String p‰iv‰ = "";

 
      /*
       * K‰yd‰‰n kuukauden p‰iv‰t l‰pi.
       */

      while (kuunP‰iv‰ <= maxKuunP‰ivi‰) {
        if (p‰ivi‰Rivill‰ == 0) {
          out.println("<th class='viikko'>" + 
                      vuosiKalenteri.get(Calendar.WEEK_OF_YEAR) + "</th>");
        }

 
        /*
         * Jos kuukauden ensimm‰inen viikko ei ala maanantaista, tulostetaan
         * tyhji‰ soluja sen viikon riville 
         */

        if (vuosiKalenteri.get(Calendar.DAY_OF_MONTH) == 1) {
          Calendar temp = (Calendar)vuosiKalenteri.clone();
          while (temp.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            out.println("<td>&nbsp;</td>");
            temp.add(Calendar.DAY_OF_WEEK, -1);
            p‰ivi‰Rivill‰++;
          }
        }

        
        /*
         * P‰iv‰-muuttujan arvo tulostetaan kalenteriin jokaisen p‰iv‰n
         * kohdalle. Jos p‰iv‰lle on varauksia, niin lis‰t‰‰n p‰iv‰n
         * numeron yhteyteen linkki viikkokalenteriin.
         */       

        p‰iv‰ = "" + vuosiKalenteri.get(Calendar.DAY_OF_MONTH);


        pvm = CheckDate.c(vuosiKalenteri.get(Calendar.DAY_OF_MONTH)) + "." +
              CheckDate.c((vuosiKalenteri.get(Calendar.MONTH)+1)) + "." +
              vuosiKalenteri.get(Calendar.YEAR);

        stmt = null;
        rs = null;

 
        /*
         * Tarkistetaan jokaisen p‰iv‰n kohdalla lˆytyykˆ henkilˆn
         * kalenterista yht‰k‰‰n varausta sille p‰iv‰lle
         */
         
        try {
          stmt = con.createStatement();
          if (!henkilo.equals("null")) {
            rs = stmt.executeQuery(
              "SELECT 1 as true FROM varaus WHERE tunnus='" + henkilo + 
              "' AND pvm=to_date('" + pvm + "', 'DD.MM.YYYY')");
          }
          else if (!ryhma.equals("null")) {
            rs = stmt.executeQuery(
              "SELECT 1 as true FROM varaus WHERE ryhm‰='" + ryhma + 
              "' AND pvm=to_date('" + pvm + "', 'DD.MM.YYYY')");
          }

          if(rs.next()) {
            varattu = "class='varaus'";
            if (!henkilo.equals("null")) {
              p‰iv‰ = "<a href='http://db.cs.helsinki.fi/s/avtanska/" +
                      "Kalenteri?alkupvm=" + pvm + "&henkilo=" + henkilo + 
                      "&viikkoAlusta=true'>" + p‰iv‰ + "</a>";
            }
            else if (!ryhma.equals("null")) {
              p‰iv‰ = "<a href='http://db.cs.helsinki.fi/s/avtanska/" +
                      "Kalenteri?alkupvm=" + pvm + "&ryhma=" + ryhma + 
                      "&viikkoAlusta=true'>" + p‰iv‰ + "</a>";
            }
          }
        } catch (SQLException ee) {
          out.println("Tietokantavirhe "+ee.getMessage());
        }


        out.println("<td " + varattu + ">" + p‰iv‰  + "</td>");

        varattu = "";

        p‰ivi‰Rivill‰++;

        /*
         * Toiminta viikon viimeisen p‰iv‰n kohdalla. Jos ollaan 
         * sunnuntaissa, joka on myˆs kuukauden viimeinen p‰iv‰,
         * ei en‰‰ aloiteta uutta rivi‰ taulukossa.
         *
         * Jos p‰iv‰ on sunnuntai, mutta kuukausi ei ole lopussa,
         * aloitetaan uusi rivi seuraavaa viikkoa varten.
         */

        if (vuosiKalenteri.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY &&
            vuosiKalenteri.get(Calendar.DAY_OF_MONTH) ==
            vuosiKalenteri.getActualMaximum(Calendar.DAY_OF_MONTH)) {
          out.println("</tr>");
          p‰ivi‰Rivill‰ = 0;
        }
        else if (vuosiKalenteri.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
          out.println("</tr><tr>");
          p‰ivi‰Rivill‰ = 0;
        }


        /*
         * Jos kuukauden viimeinen p‰iv‰ ei ole sunnuntai, t‰ytet‰‰n
         * loppuviikko tyhjill‰ soluilla
         */

        if (vuosiKalenteri.get(Calendar.DAY_OF_MONTH) ==
            vuosiKalenteri.getActualMaximum(Calendar.DAY_OF_MONTH) &&
            vuosiKalenteri.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {

          Calendar temp2 = (Calendar)vuosiKalenteri.clone();
          while (p‰ivi‰Rivill‰ < 7) {
            out.println("<td>&nbsp;</td>");
            temp2.add(Calendar.DAY_OF_WEEK, 1);
            p‰ivi‰Rivill‰++;
          }
          p‰ivi‰Rivill‰ = 0;
          out.println("</tr>");
        }


        vuosiKalenteri.add(Calendar.DAY_OF_MONTH, 1);
        kuunP‰iv‰++;
      } // end while

      out.println("</table><br /><br />");

    } // end for k

  
    /*
     * Suljetaan tietokantayhteys
     */

    try {
      if (rs!=null) rs.close();
      if (stmt!=null) stmt.close();
      con.close();
    } catch(SQLException e) {
       out.println("An SQL Exception was thrown.");
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
