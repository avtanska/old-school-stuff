import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class OmatTiedot extends HttpServlet {

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


    /*
     * Jos sivulle tullaan 'Omat tiedot'-linkin kautta
     */

    if (submit == null) {

      out.println("<html><head><title></title>" +
                  "<link rel='stylesheet' type='text/css' href='http://" +
                  "db.cs.helsinki.fi/u/avtanska/tsoha/perus.css' /><body>" +
                  "<form method='post' action='http://db.cs.helsinki.fi/" +
                  "s/avtanska/OmatTiedot'>" +
                  "<table>");

      out.println("<h3>Henkilötietojen ylläpito:</h3>");
      out.println("<tr><td align='right'>Tunnus:</td><td><b>" +
                  sessionTunnus + "</b></td></tr>");
     

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

        String selectHenkilo =
            "SELECT * FROM henkilö WHERE tunnus='" + sessionTunnus  + "'";

        rs = stmt.executeQuery(selectHenkilo);

        while(rs.next()) {
            out.println("<tr><td align='right'>Etunimi:</td><td>" +
                        "<input name='etunimi' type='text' value='" +
                        rs.getString("etunimi") + "' /></td></tr>");

            out.println("<tr><td align='right'>Sukunimi:</td><td>" +
                        "<input name='sukunimi' type='text' value='" +
                        rs.getString("sukunimi") + "' /></td></tr>");

            out.println("<tr><td align='right'>Sähköposti:</td><td>" +
                        "<input name='sposti' type='text' value='" +
                        rs.getString("sposti") + "' /></td></tr>");

            out.println("<tr><td align='right'>Puhelin:</td><td>" +
                        "<input name='puh' type='text' value='" +
                        rs.getString("puh") + "' /></td></tr>");
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

      out.println("<tr><td>&nbsp;</td></tr><tr>" +
                  "<td colspan='2' align='center'>" +
                  "<input name='submit' type='submit' " +
                  "value='Tallenna muutokset' /></td></tr>" +
                  "</table></form></body></html>");


    } // if submit == null


    /*
     * Jos on painettu 'Tallenna tiedot'-nappia
     */

    else {      
      String etunimi = req.getParameter("etunimi");
      String sukunimi = req.getParameter("sukunimi");
      String sposti = req.getParameter("sposti");
      String puh = req.getParameter("puh");

      out.println("<h3>Tiedot päivitetty</h3>");

      Connection con = null;
      con= createDbConnection(dbDriver,dbServer,dbUser,dbPassword,out);

      if (con==null) {
         out.println("</body></html>");
         return;
      }

      Statement stmt = null;
      ResultSet rs = null;


      /*
       * Päivitetään tiedot tietokantaan
       */

      String updateHenkilo = "UPDATE henkilö SET etunimi='" + etunimi +
          "', sukunimi='" + sukunimi + "', sposti='" + sposti +
          "', puh='" + puh + "' WHERE tunnus='" + sessionTunnus + "'";

      try {
          stmt = con.createStatement();
          int count = stmt.executeUpdate(updateHenkilo);
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

      out.println("" +
        "<html><head><title>l</title><link rel='stylesheet' type='text/css'" +
        "href='http://db.cs.helsinki.fi/u/avtanska/tsoha/perus.css' /></head>" +
        "<body>" +
        "<p><b>Tietosi:</b></p><p>Tunnus: <b>" + sessionTunnus + "</b></p>" +
        "<p>Sukunimi: <b>" + sukunimi + "</b></p><p>Etunimi: <b>" + etunimi +
        "</b></p><p>Sähköposti: <b>" + sposti + "</b></p>" +
        "<p>Puhelin: <b>" + puh + "</b></p></body></html>");
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