import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class OmatRyhmat extends HttpServlet {

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
    String submitLisays = req.getParameter("submitLisays");
    String submitPoisto = req.getParameter("submitPoisto");
    String submitPoistaRyhma = req.getParameter("submitPoistaRyhma");    
    String sessionTunnus = (String)session.getValue("sessionTunnus");
    String[] jasenet = req.getParameterValues("jasenet");
    String[] henkilot = req.getParameterValues("henkilot");
    String ryhmä = req.getParameter("ryhma");
    String omatryhmat = req.getParameter("omatryhmat");
    String virhe;

    out.println("<html><head><title></title>" +
       "<link rel='stylesheet' type='text/css' href='http://" +
       "db.cs.helsinki.fi/u/avtanska/tsoha/perus.css' /><body>" +
       "<form method='post' action='http://db.cs.helsinki.fi/" +
       "s/avtanska/OmatRyhmat'>");

    if (omatryhmat.equals("tyhjä")) {
      out.println("<h3>Valitse ryhmäsi</h3>");
    }
    else {
      Connection con = null;


      /*
       * Jos lisätään henkilöitä ryhmään
       */

      if (submitLisays != null && henkilot != null) {

        con= createDbConnection(dbDriver,dbServer,dbUser,dbPassword,out);

        if (con==null) {
          out.println("</body></html>");
          return;
        }

        Statement stmt = null;
        ResultSet rs = null;
        try {
           stmt = con.createStatement();

           for (int i = 0; i < henkilot.length; i++) {
             String insertJäsenyys =
                "INSERT INTO jäsenyys VALUES ('" + henkilot[i] +
                "','" + omatryhmat + "')";
             int count = stmt.executeUpdate(insertJäsenyys);
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

      } // loppu if submitLisays ei null


      /*
       * Jos poistetaan jäseniä ryhmästä
       */

      if (submitPoisto != null && jasenet != null) {

        con= createDbConnection(dbDriver,dbServer,dbUser,dbPassword,out);

        if (con==null) {
          out.println("</body></html>");
          return;
        }

        Statement stmt = null;
        ResultSet rs = null;
        try {
           stmt = con.createStatement();

           for (int i = 0; i < jasenet.length; i++) {
             String deleteJäsenyys =
                "DELETE FROM jäsenyys where " + "tunnus='" + jasenet[i] +
                "' and ryhmä='" + omatryhmat + "'";
             int count = stmt.executeUpdate(deleteJäsenyys);
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

      } // loppu if submitPoisto ei null


      /*
       * Jos koko ryhmä poistetaan
       */

      if (submitPoistaRyhma != null) {
        con= createDbConnection(dbDriver,dbServer,dbUser,dbPassword,out);

        if (con==null) {
          out.println("</body></html>");
          return;
        }

        Statement stmt = null;
        ResultSet rs = null;
        try {
           stmt = con.createStatement();
           int count = 0;


           /*
            * Poistetaan ensin jäsenyys ryhmään kuuluvilta
            */

           String deleteJäsenyys = "DELETE FROM jäsenyys where " +
                                   "ryhmä='" + omatryhmat + "'";
           count = stmt.executeUpdate(deleteJäsenyys);


           /*
            * Lopuksi poistetaan itse ryhmä
            */

           String deleteRyhmä = "DELETE FROM ryhmä where nimi='" +
                                 omatryhmat + "'";
           count = stmt.executeUpdate(deleteRyhmä);

           out.println("<h3>Ryhmän poisto onnistui.</h3>" +
                       "<p>Ryhmä näkyy vielä omien ryhmiesi listassa,<br />" +
                       "mutta se on poistettu tietokannasta. Seuraavan<br />" +
                       "kerran kun kirjoittaudut sisään, sitä ei enää<br />" +
                       "listassakaan näy.</p></body></html>");

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

      }  // loppu if submitRyhmanPoisto ei null


      /*
       * Jos tullaan 'Omat ryhmät'-valintalaatikon kautta
       */

      else {

        out.println("<h3>Ryhmän hallinta</h3>");

        out.println("<table border='0'><tr><td align='left' valign='center' " +
                    "colspan='3'>" +
                    "Ryhmän nimi:&nbsp;&nbsp;<b>" + omatryhmat + "</b>" +
                    "</td></tr>");

        con=null;
        con= createDbConnection(dbDriver,dbServer,dbUser,dbPassword,out);
        if (con==null) {
           out.println("</body></html>");
           return;
        }

        Statement stmt = null;
        ResultSet rs = null;
        try {
          stmt = con.createStatement();

          String selectOmistaja = 
             "SELECT omistaja FROM ryhmä WHERE nimi='" + 
             omatryhmat + "'";

          rs = stmt.executeQuery(selectOmistaja);

          while(rs.next()) {
               out.println("<tr><td colspan='3'>Ryhmän omistaa:&nbsp;&nbsp;" +
                           "<b>" + rs.getString("omistaja") + "</b></td></tr>" +
                           "<tr><td>&nbsp;</td></tr>");
                  
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
      

        /*
         * Haetaan ryhmän jäsenet tietokannasta
         */

        out.println("<tr><td valign='top' width='120' align='center'>" +
                    "<p>Ryhmän jäsenet:</p><select multiple name='jasenet'>");

        con=null;
        con= createDbConnection(dbDriver,dbServer,dbUser,dbPassword,out);
        if (con==null) {
           out.println("</body></html>");
           return;
        }

        stmt = null;
        rs = null;
        try {
          stmt = con.createStatement();

          String selectJäsenet =
             "SELECT * FROM henkilö WHERE tunnus in " +
             "(SELECT tunnus FROM jäsenyys WHERE ryhmä='" +
             omatryhmat + "') ORDER BY tunnus";

          rs = stmt.executeQuery(selectJäsenet);

          while(rs.next()) {
               out.println("<option value='" + rs.getString("tunnus") +
                   "'>" + rs.getString("etunimi") + " " +
                   rs.getString("sukunimi"));
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

        out.println("</select>");


        out.println("</td>");

        out.println("<td align='center' valign='bottom'>" +
                    "<input type='submit' name='submitLisays' " +
                    "value='&nbsp;&lt;&lt;&nbsp;'>" +
                    "<br /><br />" +
                    "<input type='submit' name='submitPoisto' " +
                    "value='&nbsp;&gt;&gt;&nbsp;'>" +
                    "<input type='hidden' name='omatryhmat' " +
                    "value='" + omatryhmat + "'>" +
                    "</td>");

        /*
         * Haetaan muut kuin ryhmään kuuluvat henkilöt tietokannasta
         */

        out.println("<td valign='top' width='120' align='center'>" +
                    "<p>Loput henkilöt:</p><select multiple name='henkilot'>");

        stmt = null;
        rs = null;

        try {
          stmt = con.createStatement();

          String selectLoput =
            "SELECT * FROM henkilö WHERE tunnus IN " +
            "(SELECT tunnus FROM henkilö WHERE tunnus NOT IN " +
               "(SELECT tunnus FROM jäsenyys WHERE ryhmä='" +
                 omatryhmat + "')" +
            ") ORDER BY tunnus";

          rs = stmt.executeQuery(selectLoput);

          while(rs.next()) {
            if (!(rs.getString("tunnus")).equals("admin")) {
               out.println("<option value='" + rs.getString("tunnus") +
                   "'>" + rs.getString("etunimi") + " " +
                   rs.getString("sukunimi"));;
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

        out.println("</select></td>");

        out.println("</tr><tr><td colspan='3' height='40' align='center'>" +
                    "<input type='reset' value='Tyhjennä'>" +
                    "</td></table>");

        out.println("<table><tr><td colspan='2'>" +
                    "<input type='submit' name='submitPoistaRyhma' " +
                    "value='Poista ryhmä'>" +
                    "</td></tr></table></form>");

        out.println("<form method='post' action='RyhmanAjat'>" +
                    "<table><tr><td colspan='2'>" +
                    "<input type='submit' name='submit' value='Ryhmän ajat'>" +
                    "<input type='hidden' name='omatryhmat' value='" +
                    omatryhmat + "'>" +
                    "</td></tr></table></form></body></html>");

      } // loppu else ryhmän hallinnan perussivu

    } // loppu else omatryhmat ei tyhjä
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
