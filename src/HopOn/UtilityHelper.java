package HopOn;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.text.DecimalFormat;

@SuppressWarnings("Duplicates")
public class UtilityHelper extends HttpServlet {
    CallableStatement cStmt = null;
    ResultSet rset = null;
    String Query = "";
    Statement stmt = null;

    private static Double calculateDistance(Double pLat, Double pLon, Double dLat, Double dLon) {
        final int R = 6371; // Radious of the earth

        Double latDistance = toRad(dLat - pLat);
        Double lonDistance = toRad(dLon - pLon);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(toRad(pLat)) * Math.cos(toRad(dLat)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        Double distance = R * c;
        DecimalFormat f = new DecimalFormat("##.00");
        distance = Double.valueOf(f.format(distance));
        return distance;

    }

    private static Double toRad(Double value) {
        return value * Math.PI / 180;
    }

    /**
     * Initialize global variables
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    /**
     * Process the HTTP Get request
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = new PrintWriter(response.getOutputStream());
        out.println("<html>");
        out.println("<head><title>SupportService</title></head>");
        out.println("<body>Hello From SupportService doGet()");
        out.println("</body></html>");
        out.close();
    }

    /**
     * Process the HTTP Post request
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = new PrintWriter(response.getOutputStream());
        out.println("<html>");
        out.println("<head><title>SupportService</title></head>");
        out.println("<body>");
        out.println("</body></html>");
        out.close();
    }

    public int UserIndex(HttpServletRequest request, String UserId, Connection conn, ServletContext servletContext) {
        cStmt = null;
        rset = null;
        Query = "";
        int UserIndex = 0;
        try {
            Query = "{CALL SP_GET_UserIndex(?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setString(1, UserId);
            rset = cStmt.executeQuery();
            if (rset.next()) {
                UserIndex = rset.getInt(1);
            }
            rset.close();
            cStmt.close();
        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "UserIndex -- SP -- 001 ", request, Ex, servletContext);
            UserIndex = -1;
        }
        return UserIndex;
    }

    public int ParentIndex(HttpServletRequest request, int UserIndex, Connection conn, ServletContext servletContext) {
        cStmt = null;
        rset = null;
        Query = "";
        int ParentIndex = 0;
        try {
            Query = "{CALL SP_GET_ParentIndex(?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setInt(1, UserIndex);
            rset = cStmt.executeQuery();
            if (rset.next()) {
                ParentIndex = rset.getInt(1);
            }
            rset.close();
            cStmt.close();
        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "ParentIndex -- SP -- 002 ", request, Ex, servletContext);
            ParentIndex = -1;
        }
        return ParentIndex;
    }

    public String SystemUserName(HttpServletRequest request, int UserIndex, Connection conn, ServletContext servletContext) {
        cStmt = null;
        rset = null;
        Query = "";
        String SystemUserName = "";
        try {
            Query = "{CALL SP_GET_SystemUserName(?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setInt(1, UserIndex);
            rset = cStmt.executeQuery();
            if (rset.next()) {
                SystemUserName = rset.getString(2);
            }
            rset.close();
            cStmt.close();
        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "SystemUserName -- SP -- 002-1 ", request, Ex, servletContext);
            SystemUserName = "-1";
        }
        return SystemUserName;
    }

    public StringBuilder SchoolList(HttpServletRequest request, Connection connection, ServletContext servletContext) {
        cStmt = null;
        rset = null;
        Query = "";
        StringBuilder SchoolList = new StringBuilder();

        try {
            Query = "{CALL SP_GET_SchoolList()}";
            cStmt = connection.prepareCall(Query);
            rset = cStmt.executeQuery();
            SchoolList.append("<option value='' selected>Select School</option>");
            while (rset.next()) {
                SchoolList.append("<option value=" + rset.getInt(1) + ">" + rset.getString(2) + "</option>");
            }
            rset.close();
            cStmt.close();
        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "SchoolList -- SP -- 003 ", request, Ex, servletContext);
        }
        return SchoolList;
    }

    public StringBuilder ChildList(HttpServletRequest request, int ParentIndex, Connection connection, ServletContext servletContext) {
        cStmt = null;
        rset = null;
        Query = "";
        StringBuilder ChildList = new StringBuilder();

        try {
            Query = "{CALL SP_GET_ChildList(?)}";
            cStmt = connection.prepareCall(Query);
            cStmt.setInt(1, ParentIndex);
            rset = cStmt.executeQuery();
            ChildList.append("<option value='-1' selected>Select Children</option>");
            while (rset.next()) {
                ChildList.append("<option value=" + rset.getInt(1) + ">" + rset.getString(2) + "</option>");
            }
            rset.close();
            cStmt.close();
        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "ChildList -- SP -- 004 ", request, Ex, servletContext);
        }
        return ChildList;
    }

    public StringBuilder InstructionsList(HttpServletRequest request, Connection connection, ServletContext servletContext) {
        cStmt = null;
        rset = null;
        Query = "";
        StringBuilder InstructionList = new StringBuilder();

        try {
            Query = "{CALL SP_GET_InstructionList()}";
            cStmt = connection.prepareCall(Query);
            rset = cStmt.executeQuery();
            InstructionList.append("<option value='' selected>Select Instructions</option>");
            while (rset.next()) {
                InstructionList.append("<option value=" + rset.getInt(1) + ">" + rset.getString(2) + "</option>");
            }
            rset.close();
            cStmt.close();
        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "InstructionsList -- SP -- 005 ", request, Ex, servletContext);
        }
        return InstructionList;
    }

    public StringBuilder EquipmentsList(HttpServletRequest request, Connection connection, ServletContext servletContext) {
        cStmt = null;
        rset = null;
        Query = "";
        StringBuilder EquipmentList = new StringBuilder();

        try {
            Query = "{CALL SP_GET_EquipmentList()}";
            cStmt = connection.prepareCall(Query);
            rset = cStmt.executeQuery();
            EquipmentList.append("<option value='' selected>Select Equipment</option>");
            while (rset.next()) {
                EquipmentList.append("<option value=" + rset.getInt(1) + ">" + rset.getString(2) + "</option>");
            }
            rset.close();
            cStmt.close();
        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "EquipmentList -- SP -- 006 ", request, Ex, servletContext);
        }
        return EquipmentList;
    }

    public StringBuilder GradeList(HttpServletRequest request, Connection connection, ServletContext servletContext) {
        cStmt = null;
        rset = null;
        Query = "";
        StringBuilder GradeList = new StringBuilder();

        try {
            Query = "{CALL SP_GET_GradeList()}";
            cStmt = connection.prepareCall(Query);
            rset = cStmt.executeQuery();
            GradeList.append("<option value='' selected>Select Grade</option>");
            while (rset.next()) {
                GradeList.append("<option value=" + rset.getInt(1) + ">" + rset.getString(2) + "</option>");
            }
            rset.close();
            cStmt.close();
        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "GradeList -- SP -- 007 ", request, Ex, servletContext);
        }
        return GradeList;
    }

    public StringBuilder AllergyList(HttpServletRequest request, Connection connection, ServletContext servletContext) {
        cStmt = null;
        rset = null;
        Query = "";
        StringBuilder AllergyList = new StringBuilder();

        try {
            Query = "{CALL SP_GET_AllergyList()}";
            cStmt = connection.prepareCall(Query);
            rset = cStmt.executeQuery();
            AllergyList.append("<option value='' selected>Select Allergy</option>");
            while (rset.next()) {
                AllergyList.append("<option value=" + rset.getInt(1) + ">" + rset.getString(2) + "</option>");
            }
            rset.close();
            cStmt.close();
        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "GradeList -- SP -- 008 ", request, Ex, servletContext);
        }
        return AllergyList;
    }

    public String[] getChildInfo(HttpServletRequest request, int ParentIndex, int ChildIndex, Connection conn, ServletContext servletContext) {
        cStmt = null;
        rset = null;
        Query = "";

        String ChildName = "";
        String DOB = "";
        String Age = "";
        String Grade = "";
        String _Child_Allergy = "";
        int InstructionIndex = 0;
        String _Child_Instructions = "";
        int EquipmentIndex = 0;
        String _Child_Equipments = "";
        String Child_Description = "";
        int SchoolIndex = 0;
        String _Child_School = "";
        String ParentName = "";
        String NewAllergy = "";
        String NewGrade = "";

        try {
            Query = "{CALL SP_GET_ChildInfo(?,?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setInt(1, ParentIndex);
            cStmt.setInt(2, ChildIndex);
            rset = cStmt.executeQuery();
            if (rset.next()) {
                ChildName = rset.getString(1);
                DOB = rset.getString(2);
                Age = rset.getString(3);
                Grade = rset.getString(4);
                EquipmentIndex = rset.getInt(5);
                _Child_Equipments = rset.getString(6);
                _Child_Allergy = rset.getString(7);
                Child_Description = rset.getString(8).trim();
                InstructionIndex = rset.getInt(9);
                _Child_Instructions = rset.getString(10);
                SchoolIndex = rset.getInt(11);
                _Child_School = rset.getString(12);
                ParentName = rset.getString(13).trim();
                NewGrade = rset.getString(15).trim();
                NewAllergy = rset.getString(16).trim();
            }
            rset.close();
            cStmt.close();

        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "getChildInfo -- SP -- 009 ", request, Ex, servletContext);
        }

        return new String[]{ChildName, DOB, Age, Grade, String.valueOf(EquipmentIndex), _Child_Equipments, _Child_Allergy, Child_Description,
                String.valueOf(InstructionIndex), _Child_Instructions, String.valueOf(SchoolIndex), _Child_School, ParentName, NewGrade, NewAllergy};

    }

    public StringBuilder DriverList(HttpServletRequest request, Connection connection, ServletContext servletContext) {
        cStmt = null;
        rset = null;
        Query = "";
        StringBuilder DriverList = new StringBuilder();

        try {
            Query = "{CALL SP_GET_DriversList()}";
            cStmt = connection.prepareCall(Query);
            rset = cStmt.executeQuery();
            DriverList.append("<option value='' selected>Select Driver</option>");
            while (rset.next()) {
                DriverList.append("<option value=" + rset.getInt(1) + ">" + rset.getString(2) + "</option>");
            }
            rset.close();
            cStmt.close();
        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "DriverList -- SP -- 010 ", request, Ex, servletContext);
        }
        return DriverList;
    }

    public StringBuilder RouteList(HttpServletRequest request, Connection connection, ServletContext servletContext) {
        cStmt = null;
        rset = null;
        Query = "";
        StringBuilder RouteList = new StringBuilder();

        try {
            Query = "{CALL SP_GET_RouteList()}";
            cStmt = connection.prepareCall(Query);
            rset = cStmt.executeQuery();
            RouteList.append("<option value='-1' selected>Select Route</option>");
            while (rset.next()) {
                RouteList.append("<option value=" + rset.getInt(1) + ">" + rset.getString(2) + "</option>");
            }
            rset.close();
            cStmt.close();
        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "RouteList -- SP -- 011 ", request, Ex, servletContext);
        }
        return RouteList;
    }

    public String[] getRouteInfo(HttpServletRequest request, int RouteIndex, Connection conn, ServletContext servletContext) {
        cStmt = null;
        rset = null;
        Query = "";

        String RouteNum = "";
        String DriverName = "";

        try {
            Query = "{CALL SP_GET_RouteInfo(?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setInt(1, RouteIndex);
            rset = cStmt.executeQuery();
            if (rset.next()) {
                RouteNum = rset.getString(1);
                DriverName = rset.getString(2);

            }
            rset.close();
            cStmt.close();

        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "getRouteInfo -- SP -- 012 ", request, Ex, servletContext);
        }

        return new String[]{RouteNum, DriverName};

    }

    public int DeleteRouteInfo(HttpServletRequest request, int RouteIndex, int StatusIndex, Connection conn, ServletContext servletContext) {
        cStmt = null;
        rset = null;
        Query = "";
        int Result = 0;
        try {
            Query = "{CALL SP_GET_DelRouteInfo(?,?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setInt(1, RouteIndex);
            cStmt.setInt(2, StatusIndex);
            rset = cStmt.executeQuery();

            rset.close();
            cStmt.close();
        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "DeleteRouteInfo -- SP -- 013 ", request, Ex, servletContext);
            Result = -1;
        }
        return Result;
    }

    public String RouteID(HttpServletRequest request, int RouteIndex, Connection conn, ServletContext servletContext) {
        cStmt = null;
        rset = null;
        Query = "";
        String RouteID = "";
        try {
            Query = "{CALL SP_GET_RouteID(?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setInt(1, RouteIndex);
            rset = cStmt.executeQuery();
            if (rset.next()) {
                RouteID = rset.getString(1);
            }
            rset.close();
            cStmt.close();
        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "RouteID -- SP -- 0014 ", request, Ex, servletContext);
            RouteID = "-1";
        }
        return RouteID;
    }

    public int[] RunCountInfo(HttpServletRequest request, int RouteIndex, Connection conn, ServletContext servletContext) {
        cStmt = null;
        rset = null;
        Query = "";
        int RunCount = 0;
        int TotalDistance = 0;
        try {
            Query = "{CALL SP_GET_RunCount(?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setInt(1, RouteIndex);
            rset = cStmt.executeQuery();
            if (rset.next()) {
                RunCount = rset.getInt(1);
                TotalDistance = rset.getInt(2);
            }
            rset.close();
            cStmt.close();
        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "RunCount -- SP -- 0015 ", request, Ex, servletContext);
            RunCount = -1;
        }
        return new int[]{RunCount, TotalDistance};
    }

    public StringBuilder RunInfo(HttpServletRequest request, int RouteIndex, Connection connection, ServletContext servletContext) {
        cStmt = null;
        rset = null;
        Query = "";
        String Query1 = "";
        Statement stmt1 = null;
        ResultSet rset1 = null;
        StringBuilder RunInfo = new StringBuilder();
        int StudentRunCount = 0;
        double calculateDistance = 0.0d;
        DecimalFormat f = new DecimalFormat("##.00");
        try {

            Query = "{CALL SP_GET_RunInfo(?)}";
            cStmt = connection.prepareCall(Query);
            cStmt.setInt(1, RouteIndex);
            rset = cStmt.executeQuery();
            while (rset.next()) {
                RunInfo.append("<tr>");
                RunInfo.append("<td width=18%>" + rset.getString(3) + "</td>");//RunID
                RunInfo.append("<td width=07%>" + rset.getString(4) + "</td>");//Rate
                RunInfo.append("<td width=12%>" + rset.getString(5) + "</td>"); //StartDate
                calculateDistance = distanceCalculator(request, rset.getInt(10), connection, servletContext);
                RunInfo.append("<td width=07%>" + f.format(calculateDistance) + " km </td>"); //Distance

                if (rset.getString(7).equals("M"))
                    RunInfo.append("<td width=10% style=\"color:#40BBEA\"><b>" + rset.getString(8) + "</b></td>"); //RunType
                if (rset.getString(7).equals("A"))
                    RunInfo.append("<td width=10% style=\"color:#005E80\"><b>" + rset.getString(8) + "</b></td>"); //RunType
                if (rset.getString(7).equals("L"))
                    RunInfo.append("<td width=10% style=\"color:#DAAD0B\"><b>" + rset.getString(8) + "</b></td>"); //RunType

                Query1 = "SELECT COUNT(*) FROM StudentWiseRun WHERE RunIndex = " + rset.getInt(10) + " AND Status = 0";
                stmt1 = connection.createStatement();
                rset1 = stmt1.executeQuery(Query1);
                if (rset1.next()) {
                    StudentRunCount = rset1.getInt(1);
                }
                rset1.close();
                stmt1.close();

                if (rset.getString(7).equals("M"))
                    RunInfo.append("<td width=07%><a class='label label-info' href = \"/HopOn/HopOn.AddRun?ActionID=ShowStudent&RunIndex=" + rset.getInt(10) + "&CountRecord=" + StudentRunCount + "&RouteIndex=" + RouteIndex + " \" target=\"_self\">" + StudentRunCount + "</a></td>");
                if (rset.getString(7).equals("A"))
                    RunInfo.append("<td width=07%><a class='label label-primary' href = \"/HopOn/HopOn.AddRun?ActionID=ShowStudent&RunIndex=" + rset.getInt(10) + "&CountRecord=" + StudentRunCount + "&RouteIndex=" + RouteIndex + " \" target=\"_self\">" + StudentRunCount + "</a></td>");
                if (rset.getString(7).equals("L"))
                    RunInfo.append("<td width=07%><a class='label label-warning' href = \"/HopOn/HopOn.AddRun?ActionID=ShowStudent&RunIndex=" + rset.getInt(10) + "&CountRecord=" + StudentRunCount + "&RouteIndex=" + RouteIndex + " \" target=\"_self\">" + StudentRunCount + "</a></td>");

                //RunInfo.append("<td width=07%>" + StudentRunCount + "</td>"); //StudentCount
                RunInfo.append("<td width=42%> ");
                RunInfo.append("<button id=saveStdBtn onclick=\"setRunIndex(this.value)\" class=\"btn btn-primary btn-sm\" data-toggle=\"modal\" data-target=\"#myModal\" value=" + rset.getInt(10) + "><i class=\"fa fa-plus\"></i> Add Student</button>&nbsp;&nbsp;");
                RunInfo.append("<a href=\"#\" class=\"btn btn-info btn-sm\"><i class=\"fa fa-pencil\"></i> Edit </a>&nbsp;&nbsp;");
                RunInfo.append("<button id=deleteBtn onclick=\"addFields(this.value)\" class=\"btn btn-danger btn-sm myLink\" value=" + rset.getInt(10) + " target=NewFrame1> <font color = \"FFFFFF\"> <i class=\"fa fa-trash-o\"></i> [Delete] </font></button>&nbsp;&nbsp;");
//                RunInfo.append("<button class=\"btn btn-success \" type=\"button\"><i class=\"fa fa-map-marker\"></i>&nbsp;&nbsp;Map</button>");

                RunInfo.append("<a href=\"/HopOn/HopOn.AddRun?ActionID=showLocation&RunIndex=" + rset.getInt(10) + "&RouteIndex=" + RouteIndex + " \" target=_\"self\" class=\"btn btn-success btn-sm\"><i class=\"fa fa-map-marker\"></i> Map </a>&nbsp;&nbsp;");
                RunInfo.append("</td>");
                RunInfo.append("</tr>");
            }
            rset.close();
            cStmt.close();
        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "RunInfo -- SP -- 016 ", request, Ex, servletContext);
        }
        return RunInfo;
    }

    public int DeleteRunInfo(HttpServletRequest request, int RunIndex, int StatusIndex, Connection conn, ServletContext servletContext) {
        cStmt = null;
        rset = null;
        Query = "";
        int Result = 0;
        try {
            Query = "{CALL SP_GET_DelRunInfo(?,?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setInt(1, RunIndex);
            cStmt.setInt(2, StatusIndex);
            rset = cStmt.executeQuery();

            rset.close();
            cStmt.close();
        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "DeleteRunInfo -- SP -- 017 ", request, Ex, servletContext);
            Result = -1;
        }
        return Result;
    }

    public int RunCountInStudentWiseRunTable(HttpServletRequest request, int RouteIndex, Connection conn, ServletContext servletContext) {
        stmt = null;
        rset = null;
        Query = "";
        int Result = 0;
        try {
            Query = "SELECT COUNT(a.Id) AS CountRecord " +
                    " FROM Runs a " +
                    " WHERE a.RouteIndex = " + RouteIndex + " AND " +
                    " a.Id NOT IN (SELECT x.RunIndex FROM StudentWiseRun x WHERE x.RouteIndex = a.RouteIndex) ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                Result = rset.getInt(1);
            }
            rset.close();
            stmt.close();
        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "RunCountInStudentWiseRunTable -- SP -- 018 ", request, Ex, servletContext);
            Result = -1;
        }
        return Result;
    }

    public String CityName(HttpServletRequest request, int CityIndex, Connection conn, ServletContext servletContext) {
        cStmt = null;
        rset = null;
        Query = "";
        String CityName = "";
        try {
            Query = "{CALL SP_GET_CityName(?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setInt(1, CityIndex);
            rset = cStmt.executeQuery();
            if (rset.next()) {
                CityName = rset.getString(1);
            }
            rset.close();
            cStmt.close();
        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "CityName -- SP -- 0019 ", request, Ex, servletContext);
            CityName = "-1";
        }
        return CityName;
    }

    public String getCurrDate(HttpServletRequest request, Connection conn) {
        String CurrDate = "";
        Query = "{CALL CurrentDate()}";
        try {
            cStmt = conn.prepareCall(Query);
            rset = cStmt.executeQuery();
            if (rset.next())
                CurrDate = rset.getString(1).trim();
            rset.close();
            cStmt.close();
        } catch (SQLException Ex) {
            Supportive.DumException("UtilityHelper", "getCurrDate -- SP -- 0020 ", request, Ex, this.getServletContext());
            CurrDate = "-1";
        }
        return CurrDate;
    }

    public String getFormattedCurrDate(HttpServletRequest request, Connection conn) {
        String CurrDate = "";
        Query = "{CALL CurrentDateFormat()}";
        try {
            cStmt = conn.prepareCall(Query);
            rset = cStmt.executeQuery();
            if (rset.next())
                CurrDate = rset.getString(1).trim();
            rset.close();
            cStmt.close();
        } catch (SQLException Ex) {
            Supportive.DumException("UtilityHelper", "getFormattedCurrDate -- SP -- 0021 ", request, Ex, this.getServletContext());
            CurrDate = "-1";
        }
        return CurrDate;
    }

    public String getAutomateRouteID(HttpServletRequest request, Connection conn) {
        String RouteID = "";
        Query = "{CALL getAutomateRouteID()}";
        try {
            cStmt = conn.prepareCall(Query);
            rset = cStmt.executeQuery();
            if (rset.next())
                RouteID = rset.getString(1).trim();
            rset.close();
            cStmt.close();
        } catch (SQLException Ex) {
            Supportive.DumException("UtilityHelper", "getAutomateRouteID -- SP -- 0022 ", request, Ex, this.getServletContext());
            RouteID = "-1";
        }
        return RouteID;
    }

    public String getCalculatedDate(HttpServletRequest request, Connection conn) {
        String add8Days = "";
        Query = "SELECT DATE_FORMAT(DATE_ADD(NOW(), INTERVAL 1 YEAR),'%m-%d-%Y') ";
        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next())
                add8Days = rset.getString(1);
            rset.close();
            stmt.close();
        } catch (SQLException Ex) {
            Supportive.DumException("UtilityHelper", "getCalculatedDate -- 0023 ", request, Ex, this.getServletContext());
            add8Days = "-1";
        }
        return add8Days;
    }

    public int DeleteHolidayInfo(HttpServletRequest request, int HolidayIndex, int StatusIndex, Connection conn, ServletContext servletContext) {
        cStmt = null;
        rset = null;
        Query = "";
        int Result = 0;
        try {
            Query = "{CALL SP_DeleteHoliday(?,?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setInt(1, HolidayIndex);
            cStmt.setInt(2, StatusIndex);
            rset = cStmt.executeQuery();

            rset.close();
            cStmt.close();
        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "DeleteHolidayInfo -- SP -- 024 ", request, Ex, servletContext);
            Result = -1;
        }
        return Result;
    }

    public int RunCount(HttpServletRequest request, int RouteIndex, Connection conn, ServletContext servletContext) {
        stmt = null;
        rset = null;
        Query = "";
        int RunCount = 0;
        try {
            Query = "SELECT IFNULL(COUNT(*),0) AS RunCount, IFNULL(SUM(Distance),0) AS Distance " +
                    "FROM Runs " +
                    "WHERE RouteIndex = ltrim(rtrim(" + RouteIndex + ")) AND Status=0 ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                RunCount = rset.getInt(1);
            }
            rset.close();
            stmt.close();
        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "RunCount -- SP -- 025 ", request, Ex, servletContext);
            RunCount = -1;
        }
        return RunCount;
    }

    public double distanceCalculator(HttpServletRequest request, int RunIndex, Connection conn, ServletContext servletContext) {
        String Query1 = "";
        Statement stmt1 = null;
        ResultSet rset1 = null;
        Double myLoc;
        Double calculateDistance = 0.0d;
        DecimalFormat f = new DecimalFormat("##.00");
        try {
            Query1 = "SELECT a.ChildName, a.SchoolIndex, b.SchoolName, a.PickupLat, a.PickupLon, a.DropOffLat, a.DropOffLon, d.RunID\n" +
                    "FROM Childrens a " +
                    "INNER JOIN School b ON a.SchoolIndex = b.Id " +
                    "INNER JOIN StudentWiseRun c ON a.Id = c.StudentIndex " +
                    "INNER JOIN Runs d ON c.RunIndex = d.Id AND d.Id = " + RunIndex + " ";
            Supportive.PrintMessages(Query1, "distanceCalculator", request, servletContext);
            stmt1 = conn.createStatement();
            rset1 = stmt1.executeQuery(Query1);
            while (rset1.next()) {
                myLoc = 0.0;
                myLoc = calculateDistance(rset1.getDouble(4), rset1.getDouble(5), rset1.getDouble(6), rset1.getDouble(7));
                Supportive.PrintMessages(String.valueOf(myLoc), "distanceCalculator", request, servletContext);
                calculateDistance += myLoc;
            }
            rset1.close();
            stmt1.close();
        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "distanceCalculator -- SP -- 026 ", request, Ex, servletContext);
            Supportive.PrintMessages(String.valueOf(calculateDistance), "distanceCalculator", request, servletContext);
            calculateDistance = -1.0;
        }
        return Double.valueOf(f.format(calculateDistance));
    }

    public double getRouteWiseDistance(HttpServletRequest request, int RouteIndex, Connection conn, ServletContext servletContext) {
        double calculateDistance = 0.0d;
        double totalDistance = 0.0d;
        cStmt = null;
        stmt = null;
        rset = null;
        Query = "";
        DecimalFormat f = new DecimalFormat("##.00");

        Query = "SELECT Id FROM Runs WHERE RouteIndex = " + RouteIndex;
        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                calculateDistance = distanceCalculator(request, rset.getInt(1), conn, servletContext);
                totalDistance += calculateDistance;
            }
            rset.close();
            stmt.close();
        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "getRouteWiseDistance -- SP -- 027 ", request, Ex, servletContext);
        }
        return Double.valueOf(f.format(totalDistance));
    }

    public StringBuilder CityList(HttpServletRequest request, Connection connection, ServletContext servletContext) {
        cStmt = null;
        rset = null;
        Query = "";
        StringBuilder CityList = new StringBuilder();

        try {
            Query = "{CALL SP_GET_CityList()}";
            cStmt = connection.prepareCall(Query);
            rset = cStmt.executeQuery();
            CityList.append("<option value='' selected>Select City</option>");
            while (rset.next()) {
                CityList.append("<option value=" + rset.getInt(1) + ">" + rset.getString(2) + "</option>");
            }
            rset.close();
            cStmt.close();
        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "CityList -- SP -- 028 ", request, Ex, servletContext);
        }
        return CityList;
    }

    public String DriverName(HttpServletRequest request, int DriverIndex, Connection conn, ServletContext servletContext) {
        cStmt = null;
        rset = null;
        Query = "";
        String DriverName = "";
        try {
            Query = "{CALL SP_GET_DriverName(?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setInt(1, DriverIndex);
            rset = cStmt.executeQuery();
            if (rset.next()) {
                DriverName = rset.getString(1);
            }
            rset.close();
            cStmt.close();
        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "DriverName -- SP -- 0029 ", request, Ex, servletContext);
            DriverName = "-1";
        }
        return DriverName;
    }

    public String saveMobileUser(HttpServletRequest request, int SelectedDriverIndex, String UserName, String UserId, String Password,
                                 String Phone, int Status, String CreatedDate, String CreatedBy, String UserType, Connection conn, ServletContext servletContext) {
        cStmt = null;
        rset = null;
        Query = "";
        String Result = "";
        try {
            Query = "{CALL SP_SAVE_MobileUser(?,?,?,?,?,?,?,?,?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setInt(1, SelectedDriverIndex);
            cStmt.setString(2, UserName);
            cStmt.setString(3, UserId);
            cStmt.setString(4, Password);
            cStmt.setString(5, Phone);
            cStmt.setInt(6, Status);
            cStmt.setString(7, CreatedDate);
            cStmt.setString(8, CreatedBy);
            cStmt.setString(9, UserType);
            rset = cStmt.executeQuery();
            rset.close();
            cStmt.close();

            Result = "Success";

        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "saveMobileUser -- SP -- 0030", request, Ex, servletContext);
            Result = "-1";
        }
        return Result;
    }

    public int DriverIndex(HttpServletRequest request, int SystemUserIndex, Connection conn, ServletContext servletContext) {
        cStmt = null;
        rset = null;
        Query = "";
        int driverIndex = 0;
        try {
            Query = "{CALL SP_GET_DriverIndex(?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setInt(1, SystemUserIndex);
            rset = cStmt.executeQuery();
            if (rset.next()) {
                driverIndex = rset.getInt(1);
            }
            rset.close();
            cStmt.close();
        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "DriverIndex -- SP -- 031 ", request, Ex, servletContext);
            driverIndex = -1;
        }
        return driverIndex;
    }

    public boolean isMobileUserExist(HttpServletRequest request, int DriverIndex, Connection conn, ServletContext servletContext) {
        cStmt = null;
        rset = null;
        Query = "";
        boolean isExist = false;
        int count = 0;
        try {
            Query = "{CALL SP_GET_MobileUserCount(?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setInt(1, DriverIndex);
            rset = cStmt.executeQuery();
            if (rset.next()) {
                count = rset.getInt(1);
            }
            rset.close();
            cStmt.close();

            if (count > 0)
                isExist = true;
            else
                isExist = false;

        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "isMobileUserExist -- SP -- 032 ", request, Ex, servletContext);
            isExist = false;
        }
        return isExist;
    }

    public boolean isDriverExist(HttpServletRequest request, int DriverIndex, Connection conn, ServletContext servletContext) {
        cStmt = null;
        rset = null;
        Query = "";
        boolean isExist = false;
        int count = 0;
        try {
            Query = "{CALL SP_GET_DriverCount(?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setInt(1, DriverIndex);
            rset = cStmt.executeQuery();
            if (rset.next()) {
                count = rset.getInt(1);
            }
            rset.close();
            cStmt.close();

            if (count > 0)
                isExist = true;
            else
                isExist = false;

        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "isDriverExist -- SP -- 033 ", request, Ex, servletContext);
            isExist = false;
        }
        return isExist;
    }

    public boolean isParentExist(HttpServletRequest request, int ParentIndex, Connection conn, ServletContext servletContext) {
        cStmt = null;
        rset = null;
        Query = "";
        boolean isExist = false;
        int count = 0;
        try {
            Query = "{CALL SP_GET_ParentCount(?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setInt(1, ParentIndex);
            rset = cStmt.executeQuery();
            if (rset.next()) {
                count = rset.getInt(1);
            }
            rset.close();
            cStmt.close();

            if (count > 0)
                isExist = true;
            else
                isExist = false;

        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "isParentExist -- SP -- 034 ", request, Ex, servletContext);
            isExist = false;
        }
        return isExist;
    }

    public String UserType(HttpServletRequest request, int UserIndex, Connection conn, ServletContext servletContext) {
        cStmt = null;
        rset = null;
        Query = "";
        String UserType = "";
        try {
            Query = "{CALL SP_GET_UserType(?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setInt(1, UserIndex);
            rset = cStmt.executeQuery();
            if (rset.next()) {
                UserType = rset.getString(1);
            }
            rset.close();
            cStmt.close();
        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "UserType -- SP -- 0035 ", request, Ex, servletContext);
            UserType = "-1";
        }
        return UserType;
    }

    public int MobileUserIndex(HttpServletRequest request, String UserId, Connection conn, ServletContext servletContext) {
        cStmt = null;
        rset = null;
        Query = "";
        int UserIndex = 0;
        try {
            Query = "{CALL SP_GET_mobUserIndex(?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setString(1, UserId);
            rset = cStmt.executeQuery();
            if (rset.next()) {
                UserIndex = rset.getInt(1);
            }
            rset.close();
            cStmt.close();
        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "MobileUserIndex -- SP -- 036 ", request, Ex, servletContext);
            UserIndex = -1;
        }
        return UserIndex;
    }

    public String saveInspectionData(HttpServletRequest request, Connection conn, ServletContext servletContext,
                                     String DentFound, String AccessoriesMissing, String Documents, String Remarks,
                                     String Pic1, String Pic2, String Pic3, String Pic4, String Pic5, int Status,
                                     String CreatedDate, String CreatedBy, int MobUserIndex) {
        cStmt = null;
        rset = null;
        Query = "";
        String Result = "";
        try {
            Query = "{CALL SP_SAVE_InspectionDriverData(?,?,?,?,?,?,?,?,?,?,?,?,?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setString(1, DentFound);
            cStmt.setString(2, AccessoriesMissing);
            cStmt.setString(3, Documents);
            cStmt.setString(4, Remarks);
            cStmt.setString(5, Pic1);
            cStmt.setString(6, Pic2);
            cStmt.setString(7, Pic3);
            cStmt.setString(8, Pic4);
            cStmt.setString(9, Pic5);
            cStmt.setInt(10, Status);
            cStmt.setString(11, CreatedDate);
            cStmt.setString(12, CreatedBy);
            cStmt.setInt(13, MobUserIndex);
            rset = cStmt.executeQuery();
            rset.close();
            cStmt.close();

            Result = "Success";

        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "saveInspectionData -- SP -- 0037", request, Ex, servletContext);
            Result = "-1";
        }
        return Result;
    }

    public int StudentCountRouteWise(HttpServletRequest request, int RouteIndex, Connection conn, ServletContext servletContext) {
        stmt = null;
        rset = null;
        Query = "";
        int StdCount = 0;
        try {
            Query = "SELECT COUNT(*) FROM StudentWiseRun WHERE RouteIndex = " + RouteIndex + " AND Status = 0";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                StdCount = rset.getInt(1);
            }
            rset.close();
            stmt.close();
        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "StudentCountRouteWise -- SP -- 038 ", request, Ex, servletContext);
            StdCount = -1;
        }
        return StdCount;
    }

    public Object[] getStudentInfoRunWise(HttpServletRequest request, int RouteIndex, int RunIndex, Connection conn, ServletContext servletContext) {
        stmt = null;
        rset = null;
        Query = "";

        int StudentIndex = 0;
        int SchoolIndex = 0;
        String ChildName = "";
        int Age = 0;
        String DOB = "";
        String PickupLat = "";
        String PickupLon = "";
        String DropOffLat = "";
        String DropOffLon = "";
        String SchoolName = "";

        try {
            Query = "{CALL SP_GET_LatLonStdWise(?,?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setInt(1, RouteIndex);
            cStmt.setInt(2, RunIndex);
            rset = cStmt.executeQuery();
            while (rset.next()) {
                StudentIndex = rset.getInt(1); //
                SchoolIndex = rset.getInt(2); //
                ChildName = rset.getString(3);
                Age = rset.getInt(4);
                DOB = rset.getString(5);
                PickupLat = rset.getString(6);
                PickupLon = rset.getString(7);
                DropOffLat = rset.getString(8);
                DropOffLon = rset.getString(9);
                SchoolName = rset.getString(10);

            }
            rset.close();
            cStmt.close();

        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "StudentInfoRunWise -- SP -- 039 ", request, Ex, servletContext);
        }

        //return new String[]{String.valueOf(StudentIndex), String.valueOf(SchoolIndex), ChildName, String.valueOf(Age), DOB, PickupLat, PickupLon, DropOffLat, DropOffLon, SchoolName};
        return new Object[]{StudentIndex, SchoolIndex, ChildName, Age, DOB, PickupLat, PickupLon, DropOffLat, DropOffLon, SchoolName};
    }

    public int StudentCountRunWise(HttpServletRequest request, int RunIndex, Connection conn, ServletContext servletContext) {
        stmt = null;
        rset = null;
        Query = "";
        int StdCount = 0;
        try {
            Query = "SELECT COUNT(*) FROM StudentWiseRun WHERE RunIndex = " + RunIndex + " AND Status = 0";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                StdCount = rset.getInt(1);
            }
            rset.close();
            stmt.close();
        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "StudentWiseRun -- SP -- 040 ", request, Ex, servletContext);
            StdCount = -1;
        }
        return StdCount;
    }


    public int checkDriverInspectionData(HttpServletRequest request, int MobUserIdx, String CurrDate, Connection conn, ServletContext servletContext) {
        cStmt = null;
        rset = null;
        Query = "";
        int count = 0;
        try {
            Query = "{CALL SP_GET_driverInspectionCount(?,?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setInt(1, MobUserIdx);
            cStmt.setString(2, CurrDate);
            rset = cStmt.executeQuery();
            if (rset.next()) {
                count = rset.getInt(1);
            }
            rset.close();
            cStmt.close();
        } catch (Exception Ex) {
            Supportive.DumException("UtilityHelper", "checkDriverInspectionData -- SP -- 041 ", request, Ex, servletContext);
            count = -1;
        }
        return count;
    }

    public String getDefaultCurrDate(HttpServletRequest request, Connection conn) {
        String CurrDate = "";
        Query = "{CALL SP_GET_DefaultDateFormat()}";
        try {
            cStmt = conn.prepareCall(Query);
            rset = cStmt.executeQuery();
            if (rset.next())
                CurrDate = rset.getString(1).trim();
            rset.close();
            cStmt.close();
        } catch (SQLException Ex) {
            Supportive.DumException("UtilityHelper", "getDefaultCurrDate -- SP -- 0042 ", request, Ex, this.getServletContext());
            CurrDate = "-1";
        }
        return CurrDate;
    }

    public String getClientIp(HttpServletRequest request) {

        String remoteAddr = "";

        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }

        return remoteAddr;
    }
}
