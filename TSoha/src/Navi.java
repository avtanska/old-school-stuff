import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class Navi extends HttpServlet {

   final String dbDriver="org.postgresql.Driver";
   final String dbServer ="jdbc:postgresql://localhost:10388/tsoha";
   final String dbUser= "avtanska";        // replace with your db user account
   final String dbPassword ="postgres"; // replace with your password


   public void service(HttpServletRequest req, HttpServletResponse res)
     throws ServletException, IOException {
     ServletOutputStream out;
     res.setContentType("text/html");
     out= res.getOutputStream();

     HttpSession session = req.getSession(true);

     String submit = req.getParameter("submit");
     String ulos = req.getParameter("ulos");
     String tunnus = req.getParameter("tunnus");
     String salasana = req.getParameter("salasana");

     String salasanaQuery = "SELECT * FROM salasana where tunnus='" +
                             tunnus + "' and salasana='" + salasana + "'";

     String virhe = null;


     Connection con=null;
     con= createDbConnection(dbDriver,dbServer,dbUser,dbPassword,out);
     if (con==null) {
        out.println("</body></html>");
        return;
     }

     // connection established

     Statement stmt = null;
     ResultSet rs = null;
     try {
         stmt = con.createStatement();
         rs = stmt.executeQuery(salasanaQuery);
         if (!rs.next() && submit != null) {
           virhe = "<span style='font-weight: bold; color: #ff0000'>" +
                   "Tunnus ja salasana eivät täsmää.</span>";
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



     /*
      * Rekisteröitymättömän käyttäjän navigointipalkki
      */

     if (submit == null || (submit != null && virhe != null) || ulos != null) {

       if (ulos != null) {
         String temp = "";
         session.putValue("sessionTunnus", temp);
       }

       out.println("<html><head><title></title>" +
           "<link rel='stylesheet' type='text/css' href='http://" +
           "db.cs.helsinki.fi/u/avtanska/tsoha/navi.css' />" +
           "</head><body bgcolor='#cccccc' style='margin: 2'>" +
           "<form method='post' action='http://db.cs.helsinki.fi/" +
           "s/avtanska/KalenteriYleinen' target='main'>" +
           "<table width='175' cellpadding='5' border='0'><tr>" +
           "<td height='100' valign='top'><a href='http://" +
           "db.cs.helsinki.fi/u/avtanska/tsoha/' target='_top'>" +
           "apua?</a><br /><hr />" +
           "</td></tr><tr><td><b>Kalenterit:</b></td></tr><tr>");

       out.println("<td>Ryhmä:<br /><select " +
                   "onFocus=document.forms[0].henkilo.value='tyhjä' " +
                   "name='ryhma'>" +
                   "<option value='tyhjä'>-- valitse --");

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
         rs = stmt.executeQuery("select nimi from ryhmä order by nimi");
         while(rs.next()) {
            out.println("<option>" + rs.getString("nimi"));
         }
       } catch (SQLException ee) {
           out.println("Tietokantavirhe "+ee.getMessage());
       } finally {
          try {
            if (rs!=null) rs.close();
            if (stmt!=null) stmt.close();
            // con.close();
           } catch(SQLException e) {
              out.println("An SQL Exception was thrown.");
           }
       }
       out.println("</select>&nbsp;&nbsp;tai</td></tr>");

       out.println("<tr><td>Henkilö:<br /><select " +
                   "onFocus=document.forms[0].ryhma.value='tyhjä' " +
                   "name='henkilo'>" +
                   "<option value='tyhjä'>-- valitse --");


       if (con==null) {
          out.println("</body></html>");
          return;
       }

       stmt = null;
       rs = null;
       try {
           stmt = con.createStatement();
           rs = stmt.executeQuery("select tunnus from henkilö order by tunnus");
           while(rs.next()) {
             if (!(rs.getString("tunnus").equals("admin"))) {
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

       out.println("</select><p><input name='submit' type='submit' " +
                   "value='Katso'></p></form><br /><hr /></td></tr>");


       out.println("<tr><td height='50'>");

       if (virhe != null) {
         out.println("<p>" + virhe + "</p>");
       }

       out.println("</td></tr><tr><td><form method='post' action='Navi'>" +
           "tunnus:<br />" +
           "<input name='tunnus' type='text'></td></tr>" +
           "<tr><td>salasana:<br />" +
           "<input name='salasana' type='password'></td></tr>" +
           "<tr><td align='left'>" +
           "<input name='submit' type='submit' value='Sisään'>" +
           "</td></tr><tr><td><hr /></td></tr><tr><td>" +
           "<a href='http://db.cs.helsinki.fi/u/avtanska/tsoha/" +
           "luo_tunnus.htm' target='main'>Luo tunnus</a></td></tr>" +
           "<tr><td><hr /></td></tr></table></form></body></html>");


     } // loppu if submit null


     /*
      * Sisäänkirjautuneen käyttäjän navigointipalkki
      */

     else {

       tunnus = req.getParameter("tunnus");
       salasana = req.getParameter("salasana");


       /*
        * Luodaan uusi sessio ja tallennetaan kirjautuneen käyttäjän
        * tunnus sessio-olioon
        */

       session = req.getSession(true);
       session.putValue("sessionTunnus", tunnus);
       String sessionTunnus = (String)session.getValue("sessionTunnus");


       out.println("<html><head><title></title>" +
          "<link rel='stylesheet' type='text/css' href='http://" +
          "db.cs.helsinki.fi/u/avtanska/tsoha/navi.css' />" +
          "</head><body bgcolor='#cccccc' style='margin: 2'>" +
          "<form method='post' action='http://db.cs.helsinki.fi/" +
          "s/avtanska/Kalenteri' target='main'>" +
          "<table width='175' cellpadding='5' border='0'><tr>" +
          "<td height='100' valign='top'><a href='http://" +
          "db.cs.helsinki.fi/u/avtanska/tsoha/apua.htm' target='main'>" +
          "apua?</a><br /><hr />" +
          "<p>Tervetuloa, <b>" + tunnus + "</b></p>" +
          "</td></tr><tr><td><b>Kalenterit:</b></td></tr><tr>");


       /*
        * Haetaan ryhmien nimet tietokannasta
        */

       out.println("<td>Ryhmä:<br /><select " +
                   "onFocus=document.forms[0].henkilo.value='tyhjä' " +
                   "name='ryhma'>" +
                   "<option value='tyhjä'>-- valitse --");

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
             // con.close();
       } catch(SQLException e) {
           out.println("An SQL Exception was thrown.");
       }
       }
       out.println("</select>&nbsp;&nbsp;tai</td></tr>");


       /*
        * Haetaan kaikki henkilöt tietokannasta, mutta admin-ylläpitäjää
        * ei tulosteta valintalaatikkoon
        */

       out.println("<tr><td>Henkilö:<br /><select " +
                   "onFocus=document.forms[0].ryhma.value='tyhjä' " +
                   "name='henkilo'>" +
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
             // con.close();
          } catch(SQLException e) {
             out.println("An SQL Exception was thrown.");
          }
       }

       out.println("</select><p><input name='submit' type='submit' " +
                   "value='Katso'></p></form><br /><hr /></td></tr>");


       out.println("<tr><td height='100' valign='bottom'>");


       /*
        * Jos käyttäjällä on luotuja ryhmiä, ne tulostetaan tässä.
        * Admin-ylläpitokäyttäjälle tulostetaan kaikki ryhmät.
        */

       if (con==null) {
          out.println("</body></html>");
          return;
       }

       stmt = null;
       rs = null;
       try {
           stmt = con.createStatement();
           rs = stmt.executeQuery("SELECT nimi FROM ryhmä WHERE omistaja='" +  
                                  sessionTunnus + "'");
           if (sessionTunnus.equals("admin")) {
             rs = stmt.executeQuery("SELECT nimi FROM ryhmä ORDER BY nimi");
             out.println("<form method='post' action='OmatRyhmat' " +
                         "target='main'>omat ryhmät:<br />" +
                         "<select name='omatryhmat'>" +
                         "<option value='tyhjä'>-- valitse --");

             while(rs.next()) {
                out.println("<option>" + rs.getString("nimi"));
             }

             out.println("</select><p><input type='submit' name='submit' " +
                         "value='Mene'></p>");
           }
           else if (rs.next()) {
             rs = stmt.executeQuery("SELECT nimi FROM ryhmä WHERE omistaja='" + 
                                    sessionTunnus + "' ORDER BY nimi");
             out.println("<form method='post' action='OmatRyhmat' " +
                         "target='main'>omat ryhmät:<br /><select " +
                         "name='omatryhmat'>" +
                         "<option value='tyhjä'>-- valitse --");

             while(rs.next()) {
                out.println("<option>" + rs.getString("nimi"));
             }

             out.println("</select><p><input type='submit' name='submit' " +
                         "value='Mene'></p>");
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


       out.println("</td></tr><tr><td><hr /></td></tr><tr><td>" +
           "<a href='http://db.cs.helsinki.fi/s/avtanska/TeeVaraus' " +
           "target='main'>Tee varaus</a><br />" +
           "<a href='LuoRyhma' target='main'>Luo ryhmä</a><br />" +
           "<a href='OmatTiedot' target='main'>Omat tiedot</a><br />" +
           "</td></tr><tr><td><hr />" +
           "<a href='http://db.cs.helsinki.fi/u/avtanska/tsoha/index.html' " +
           "target='_top'>Kirjaudu ulos</a></td></tr></table></body></html>");

     } // loppu else

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