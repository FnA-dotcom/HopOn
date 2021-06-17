package TAMAPP;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.sql.*;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by Siddiqui on 9/15/2017.
 */
@SuppressWarnings("Duplicates")
public class MobileServices extends HttpServlet {
    private Statement stmt = null;
    private ResultSet rset = null;
    private String Query = "";
    private PreparedStatement pStmt = null;
    private CallableStatement cStmt = null;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        ServicesRequest(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        ServicesRequest(request, response);
    }

    public void ServicesRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Connection conn;
        String ActionID = request.getParameter("ActionID").trim();
        ServletContext context = null;

        response.setContentType("text/html");
        PrintWriter out = new PrintWriter(response.getOutputStream());

        Supportive supp = new Supportive();
        String connect_string = supp.GetConnectString();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(connect_string);
        } catch (Exception excp) {
            conn = null;
            out.println("Exception excp conn: " + excp.getMessage());
        }

        if (ActionID.equals("CustomerData")) {
            CustomerData(request, out, conn);
        } else if (ActionID.equals("LocationGet")) {
            MarkLocation(request, out, conn);
        } else if (ActionID.equals("InspectionForm")) {
            InspectionForm(request, out, conn);
        } else if (ActionID.equals("QAForm")) {
            QAForm(request, out, conn);
        } else if (ActionID.equals("SavePicture")) {
            SavePicture(request, out);
        } else if (ActionID.equals("UpdateTime")) {
            UpdateTime(request, out, conn);
        } else if (ActionID.equals("PostponedReasonInsertion")) {
            PostponedReasonInsertion(request, out, conn);
        } else if (ActionID.equals("RevertStatus")) {
            RevertStatus(request, out, conn);
        } else if (ActionID.equals("TestingService")) {
            TestingService(request, out, conn);
        }
        try {
            assert conn != null;
            conn.close();
        } catch (Exception ignored) {
        }

        out.flush();
        out.close();
    }

    private void CustomerData(HttpServletRequest request, PrintWriter out, Connection conn) {
        rset = null;
        Query = " ";
        String Query1 = "";
        CallableStatement cStmt2 = null;
        ResultSet rset2 = null;
        String UserId = request.getParameter("UserId").trim();
        try {

            DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            Element root = doc.createElement("gform");
            doc.appendChild(root);

            try {
                Query = "{CALL MobileDataAssignment(?)}";
                    cStmt = conn.prepareCall(Query);
                    cStmt.setString(1, UserId);
                    rset = cStmt.executeQuery();
                    while (rset.next()) {
                    Element root1 = doc.createElement("client");
                    root.appendChild(root1);

                    if (rset.getString(1).length() == 0) {
                        Element child = doc.createElement("RegNo");
                        root1.appendChild(child);
                        Text text = doc.createTextNode("-");
                        child.appendChild(text);
                    } else {
                        Element child = doc.createElement("RegNo");
                        root1.appendChild(child);
                        Text text = doc.createTextNode(rset.getString(1));
                        child.appendChild(text);
                    }

                    if (rset.getString(2).length() == 0) {
                        Element child = doc.createElement("CustName");
                        root1.appendChild(child);
                        Text text = doc.createTextNode("-");
                        child.appendChild(text);
                    } else {
                        Element child = doc.createElement("CustName");
                        root1.appendChild(child);
                        Text text = doc.createTextNode(rset.getString(2));
                        child.appendChild(text);
                    }

                    if (rset.getString(3).length() == 0) {
                        Element child = doc.createElement("CellNo");
                        root1.appendChild(child);
                        Text text = doc.createTextNode("-");
                        child.appendChild(text);
                    } else {
                        Element child = doc.createElement("CellNo");
                        root1.appendChild(child);
                        Text text = doc.createTextNode(rset.getString(3));
                        child.appendChild(text);
                    }

                    if (rset.getString(4).length() == 0) {
                        Element child = doc.createElement("PhNo");
                        root1.appendChild(child);
                        Text text = doc.createTextNode("-");
                        child.appendChild(text);
                    } else {
                        Element child = doc.createElement("PhNo");
                        root1.appendChild(child);
                        Text text = doc.createTextNode(rset.getString(4));
                        child.appendChild(text);
                    }

                    if (rset.getString(5).length() == 0) {
                        Element child = doc.createElement("Make");
                        root1.appendChild(child);
                        Text text = doc.createTextNode("-");
                        child.appendChild(text);
                    } else {
                        Element child = doc.createElement("Make");
                        root1.appendChild(child);
                        Text text = doc.createTextNode(rset.getString(5));
                        child.appendChild(text);
                    }

                    if (rset.getString(6).length() == 0) {
                        Element child = doc.createElement("Model");
                        root1.appendChild(child);
                        Text text = doc.createTextNode("-");
                        child.appendChild(text);
                    } else {
                        Element child = doc.createElement("Model");
                        root1.appendChild(child);
                        Text text = doc.createTextNode(rset.getString(6));
                        child.appendChild(text);
                    }

                    if (rset.getString(7).length() == 0) {
                        Element child = doc.createElement("Color");
                        root1.appendChild(child);
                        Text text = doc.createTextNode("-");
                        child.appendChild(text);
                    } else {
                        Element child = doc.createElement("Color");
                        root1.appendChild(child);
                        Text text = doc.createTextNode(rset.getString(7));
                        child.appendChild(text);
                    }

                    if (rset.getString(8).length() == 0) {
                        Element child = doc.createElement("ChNo");
                        root1.appendChild(child);
                        Text text = doc.createTextNode("-");
                        child.appendChild(text);
                    } else {
                        Element child = doc.createElement("ChNo");
                        root1.appendChild(child);
                        Text text = doc.createTextNode(rset.getString(8));
                        child.appendChild(text);
                    }

                    if (rset.getString(9).length() == 0) {
                        Element child = doc.createElement("Type");
                        root1.appendChild(child);
                        Text text = doc.createTextNode("-");
                        child.appendChild(text);
                    } else {
                        Element child = doc.createElement("Type");
                        root1.appendChild(child);
                        Text text = doc.createTextNode(rset.getString(9));
                        child.appendChild(text);
                    }

                    if (rset.getString(10).length() == 0) {
                        Element child = doc.createElement("Address");
                        root1.appendChild(child);
                        Text text = doc.createTextNode("-");
                        child.appendChild(text);
                    } else {
                        Element child = doc.createElement("Address");
                        root1.appendChild(child);
                        Text text = doc.createTextNode(rset.getString(10));
                        child.appendChild(text);
                    }

                    if (rset.getString(11).length() == 0) {
                        Element child = doc.createElement("DeviceNo");
                        root1.appendChild(child);
                        Text text = doc.createTextNode("-");
                        child.appendChild(text);
                    } else {
                        Element child = doc.createElement("DeviceNo");
                        root1.appendChild(child);
                        Text text = doc.createTextNode(rset.getString(11));
                        child.appendChild(text);
                    }

                    if (rset.getString(12).length() == 0) {
                        Element child = doc.createElement("Insurance");
                        root1.appendChild(child);
                        Text text = doc.createTextNode("-");
                        child.appendChild(text);
                    } else {
                        Element child = doc.createElement("Insurance");
                        root1.appendChild(child);
                        Text text = doc.createTextNode(rset.getString(12));
                        child.appendChild(text);
                    }

                    if (rset.getString(13).length() == 0) {
                        Element child = doc.createElement("CaseId");
                        root1.appendChild(child);
                        Text text = doc.createTextNode("-");
                        child.appendChild(text);
                    } else {
                        Element child = doc.createElement("CaseId");
                        root1.appendChild(child);
                        Text text = doc.createTextNode(rset.getString(13));
                        child.appendChild(text);
                    }
                    if (rset.getString(16).length() == 0) {
                        Element child = doc.createElement("CreatedDate");
                        root1.appendChild(child);
                        Text text = doc.createTextNode("-");
                        child.appendChild(text);
                    } else {
                        Element child = doc.createElement("CreatedDate");
                        root1.appendChild(child);
                        Text text = doc.createTextNode(rset.getString(16));
                        child.appendChild(text);
                    }
                    if (rset.getString(17).length() == 0) {
                        Element child = doc.createElement("ServerDate");
                        root1.appendChild(child);
                        Text text = doc.createTextNode("-");
                        child.appendChild(text);
                    } else {
                        Element child = doc.createElement("ServerDate");
                        root1.appendChild(child);
                        Text text = doc.createTextNode(rset.getString(17));
                        child.appendChild(text);
                    }


/*                    Query1 = "{CALL UpdateJobStatus(?,?)}";
                    cStmt2 = conn.prepareCall(Query1);
                    cStmt2.setString(1, rset.getString(13));
                    cStmt2.setInt(2, 1);
                    rset2 = cStmt2.executeQuery();
                    rset2.close();
                    cStmt2.close();*/
                }
                rset.close();
                cStmt.close();
            } catch (Exception e) {
                Supportive.doLog(this.getServletContext(), "Data Assigning to Mobile", e.getMessage(), e);
                out.println("Data Assigning to Mobile -- " + e.getMessage());
                out.close();
                out.flush();
                return;
            }

            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = transfac.newTransformer();
            trans.setOutputProperty("omit-xml-declaration", "yes");
            trans.setOutputProperty("indent", "yes");


            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            DOMSource source = new DOMSource(doc);
            trans.transform(source, result);
            String xmlString = sw.toString();

            out.println(xmlString);

            String filename = GenerateXMLFileCase(xmlString, UserId, getServletContext());
            if (filename.equals("Error in FileName.")) {
                try {
                    conn.rollback();
                } catch (Exception ignored) {
                }
                out.println("Error in Data ... Unable to generate filename... ");
                out.flush();
                out.close();
                return;
            }
            //  out.println("XML File Generated Successfully.<br>");
            //out.println("FileName & FilePath : " + filename + "<br>");


            // rset.close();
            //stmt.close();

            //conn.commit();
        } catch (Exception e) {
            Supportive.doLog(this.getServletContext(), "Error In XML Creation Main Function", e.getMessage(), e);
            out.println("Error In XML Creation Main Function");
            out.println("Error No.: 20021");
            out.println("Unable to process request.." + e.getMessage());
            out.flush();
            out.close();
            return;
        }
    }

    private static String GenerateXMLFileCase(String xmlString, String UserId, ServletContext context) {
        try {
            String FileName = Supportive.GetXMLPath(context) + "/CustomerData/CustomerData_" + UserId + ".xml";

            FileWriter FW = new FileWriter(FileName, false);
            FW.write(xmlString);
            FW.flush();
            FW.close();

            return FileName;
        } catch (Exception ignored) {
        }
        return "Error in FileName.";
    }

    private void MarkLocation(HttpServletRequest request, PrintWriter out, Connection conn) {
        stmt = null;
        rset = null;
        Query = " ";
        pStmt = null;

        String UserId = request.getParameter("Name").trim();
        String Latitude = request.getParameter("Lat").trim();
        Latitude = Latitude.substring(0, 9);
        String Longitude = request.getParameter("Lon").trim();
        Longitude = Longitude.substring(0, 9);
        try {
            if (!Latitude.equals("0.0") || !Longitude.equals("0.0")) {
                pStmt = conn.prepareStatement(
                        "INSERT INTO TechnicianLocation (Longtitude, Latitude, UserId, Status, CreatedDate) " +
                                "VALUES (?,?,?,0,NOW())");
                pStmt.setString(1, Longitude);
                pStmt.setString(2, Latitude);
                pStmt.setString(3, UserId);

                pStmt.executeUpdate();
                pStmt.close();
            }
            out.println("true");
            out.close();
            out.flush();

        } catch (Exception e) {
            Supportive.doLog(this.getServletContext(), "Mark Location", e.getMessage(), e);
            out.println("false");
            out.close();
            out.flush();
        }

    }

    private void InspectionForm(HttpServletRequest request, PrintWriter out, Connection conn) {
        stmt = null;
        rset = null;
        Query = " ";
        pStmt = null;
        String UserId = request.getParameter("UserId").trim();
        String ComplainNumber = request.getParameter("CaseID").trim();

        String Horn = request.getParameter("Horn").trim();
        String InteriorLights = request.getParameter("InteriorLights").trim();
        String SideMirrors = request.getParameter("SideMirrors").trim();
        String PowerWindows = request.getParameter("PowerWindows").trim();
        String CentralLockingSystem = request.getParameter("CentralLockingSystem").trim();
        String AC = request.getParameter("AC").trim();
        String Radio = request.getParameter("Radio").trim();
        String TempGauge = request.getParameter("TempGauge").trim();
        String FuelGauge = request.getParameter("FeulGauge").trim();
        String ABSLight = request.getParameter("ABSLight").trim();
        String AirBagLight = request.getParameter("AirBagLight").trim();
        String EngineCheckLight = request.getParameter("EngineCheckLight").trim();
        String HandBrake = request.getParameter("HandBrake").trim();
        String CleanInterior = request.getParameter("CleanInterior").trim();
        String DashBoard = request.getParameter("DashBoard").trim();
        String GearShift = request.getParameter("GearShift").trim();
        String CustBelongings = request.getParameter("CustBelongings").trim();
        String ExteriorLights = request.getParameter("ExteriorLights").trim();
        String BodyDamage = request.getParameter("BodyDamage").trim();
        String IntExtScratches = request.getParameter("IntExtScratches").trim();
        String Windscreen = request.getParameter("Windscreen").trim();
        String ToolKit = request.getParameter("ToolKit").trim();
        String Carjack = request.getParameter("Carjack").trim();
        String Pic1 = request.getParameter("Pic1").trim();
        String Pic2 = request.getParameter("Pic2").trim();
        String Pic3 = request.getParameter("Pic3").trim();
        String Pic4 = request.getParameter("Pic4").trim();

        try {

            pStmt = conn.prepareStatement(
                    "INSERT INTO FaultInspection (Horn, InteriorLights, SideMirrors, PowerWindows, CentralLockingSystem, AC, " +
                            "Radio, TempGauge, FuelGauge, ABSLight, AirBagLight, EngineCheckLight, HandBrake, CleanInterior, " +
                            "DashBoard, GearShift, CustBelongings, ExteriorLights, BodyDamage, IntExtScratches, Windscreen, " +
                            "ToolKit, Carjack, Pic1, Pic2, Pic3, Pic4, UserId, Status, CreatedDate,ComplainNumber) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,0,NOW(),?) ");
            pStmt.setString(1, Horn);
            pStmt.setString(2, InteriorLights);
            pStmt.setString(3, SideMirrors);
            pStmt.setString(4, PowerWindows);
            pStmt.setString(5, CentralLockingSystem);
            pStmt.setString(6, AC);
            pStmt.setString(7, Radio);
            pStmt.setString(8, TempGauge);
            pStmt.setString(9, FuelGauge);
            pStmt.setString(10, ABSLight);
            pStmt.setString(11, AirBagLight);
            pStmt.setString(12, EngineCheckLight);
            pStmt.setString(13, HandBrake);
            pStmt.setString(14, CleanInterior);
            pStmt.setString(15, DashBoard);
            pStmt.setString(16, GearShift);
            pStmt.setString(17, CustBelongings);
            pStmt.setString(18, ExteriorLights);
            pStmt.setString(19, BodyDamage);
            pStmt.setString(20, IntExtScratches);
            pStmt.setString(21, Windscreen);
            pStmt.setString(22, ToolKit);
            pStmt.setString(23, Carjack);
            pStmt.setString(24, Pic1);
            pStmt.setString(25, Pic2);
            pStmt.setString(26, Pic3);
            pStmt.setString(27, Pic4);
            pStmt.setString(28, UserId);
            pStmt.setString(29, ComplainNumber);

            pStmt.executeUpdate();
            pStmt.close();

            out.println("true");
        } catch (Exception e) {
            Supportive.doLog(this.getServletContext(), "Inspection Form Data", e.getMessage(), e);
            out.println("Error while saving inspection data. Please contact system administrator!!");
            out.close();
            out.flush();
            return;
        }

    }

    private void QAForm(HttpServletRequest request, PrintWriter out, Connection conn) {
        stmt = null;
        rset = null;
        Query = "";
        pStmt = null;

        String UserId = request.getParameter("UserId").trim();

        String Ans = request.getParameter("Ans").trim();
        String Ans1 = request.getParameter("Ans1").trim();
        String Ans2 = request.getParameter("Ans2").trim();
        String Ans3 = request.getParameter("Ans3").trim();
        String Ans4 = request.getParameter("Ans4").trim();
        String Ans5 = request.getParameter("Ans5").trim();
        String Pic1 = request.getParameter("Pic1").trim();
        String Pic2 = request.getParameter("Pic2").trim();
        String Pic3 = request.getParameter("Pic3").trim();
        String Pic4 = request.getParameter("Pic4").trim();
        String ComplainNumber = request.getParameter("CaseID").trim();

        try {
            pStmt = conn.prepareStatement(
                    "INSERT INTO QAForm (Ans, Ans1, Ans2, Ans3, Ans4, Ans5, Pic1, Pic2, Pic3, Pic4, ComplainNumber, UserId,Status,CreatedDate) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,0,NOW()) ");
            pStmt.setString(1, Ans);
            pStmt.setString(2, Ans1);
            pStmt.setString(3, Ans2);
            pStmt.setString(4, Ans3);
            pStmt.setString(5, Ans4);
            pStmt.setString(6, Ans5);
            pStmt.setString(7, Pic1);
            pStmt.setString(8, Pic2);
            pStmt.setString(9, Pic3);
            pStmt.setString(10, Pic4);
            pStmt.setString(11, ComplainNumber);
            pStmt.setString(12, UserId);

            pStmt.executeUpdate();
            pStmt.close();

            out.println("true");
        } catch (Exception Ex) {
            Supportive.doLog(this.getServletContext(), "QA Form Data", Ex.getMessage(), Ex);
            out.println("Error while saving inspection data. Please contact system administrator!!");
            out.close();
            out.flush();
            return;
        }

    }

    private void SavePicture(HttpServletRequest request, PrintWriter out) {
        try {
            FileOutputStream fout = new FileOutputStream(new File("/opt/TAMAPP.txt"), true);

            Enumeration e = request.getParameterNames();
            String Param;
            while (e.hasMoreElements()) {
                Param = (String) e.nextElement();
                fout.write(("Modified Code Param =  " + Param + "  Value = " + request.getParameter(Param) + "\r\n").getBytes());
            }
            fout.close();

            DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();

            fileItemFactory.setSizeThreshold(1024 * 1024); // 1 MB

            File tmpDir = null;
            tmpDir = new File(getPicsFolder());
            fileItemFactory.setRepository(tmpDir);

            ServletFileUpload uploadHandler = new ServletFileUpload(
                    fileItemFactory);
            try {
                List items = uploadHandler.parseRequest(request);

                for (Object item1 : items) {
                    FileItem item = (FileItem) item1;
                    if (!item.isFormField()) {
                        File file = new File(getPicsFolder(), item.getName());
                        item.write(file);
                    }
                }
                out.close();
            } catch (FileUploadException ex) {
                log("Error encountered while parsing the request", ex);
            } catch (Exception ex) {
                log("Error encountered while uploading file", ex);
            }
        } catch (Exception e) {
            Supportive.doLog(this.getServletContext(), "Picture Saving Error!!", e.getMessage(), e);
            out.println("Exception : " + e.getMessage());
            out.flush();
            out.close();
            return;
        }
    }

    @SuppressWarnings("SameReturnValue")
    private String getPicsFolder() {
        return "/opt/Htmls/TAMAPP/pics/";
    }

    private void UpdateTime(HttpServletRequest request, PrintWriter out, Connection conn) {
        stmt = null;
        rset = null;
        Query = " ";

        String UserId = request.getParameter("Name").trim();
        String UniqueID = request.getParameter("UniqueID").trim();
        String EndTime = request.getParameter("EndTime").trim();
        int ComplainId = 0;
        int TechId = 0;
        try {
            Query = "SELECT Id FROM CustomerData WHERE ComplainNumber = '" + UniqueID + "' ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next())
                ComplainId = rset.getInt(1);
            rset.close();
            stmt.close();

            Query = "SELECT Id FROM MobileUsers WHERE UserId = '" + UserId + "' ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next())
                ComplainId = rset.getInt(1);
            rset.close();
            stmt.close();

            Query = "UPDATE Assignment SET TotalTime='" + EndTime + "' WHERE ComplaintId = " + ComplainId + " AND UserId = " + TechId + " ";
            stmt = conn.createStatement();
            stmt.executeUpdate(Query);
            stmt.close();

            out.println("true");
            out.close();
            out.flush();

        } catch (Exception e) {
            out.println("false");
            out.close();
            out.flush();
        }
    }

    private void PostponedReasonInsertion(HttpServletRequest request, PrintWriter out, Connection conn) {
        stmt = null;
        rset = null;
        Query = " ";
        pStmt = null;

        String UserId = request.getParameter("Name").trim();
        String Status = request.getParameter("Status").trim();
        Status = Status.replace("-", " ");
        String ComplainNumber = request.getParameter("UniqueID").trim();
        ComplainNumber = ComplainNumber.replace("^", "#");
        String PostponedReason = request.getParameter("PostponedReason");

        int CustDataIndex = 0;
        try {
            Query = "SELECT  Id FROM CustomerData WHERE ComplainNumber='" + ComplainNumber + "' ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                CustDataIndex = rset.getInt(1);
            }
            rset.close();
            stmt.close();

            if (CustDataIndex == 0) {
                Supportive.doLogMethodMessage(this.getServletContext(), "PostponedReason Check - 0001", "No Complain Found.Please try again!!");
                out.println("No Complain Found.Please try again!!");
                out.close();
                out.flush();
                return;
            }
            int AssignmentIndex = 0;
            Query = "SELECT Max(Id) FROM Assignment WHERE ComplaintId = " + CustDataIndex;
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next())
                AssignmentIndex = rset.getInt(1);
            rset.close();
            stmt.close();

            int TechnicianId = 0;
            Query = "SELECT Id FROM MobileUsers WHERE upper(trim(UserId))='" + UserId.toUpperCase().trim() + "' ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                TechnicianId = rset.getInt(1);
            }
            rset.close();
            stmt.close();

            if (TechnicianId == 0) {
                Supportive.doLogMethodMessage(this.getServletContext(), "Postponed Reason - Second Check", "No User Record Found.Please try again!!");
                out.println("No User Record Found.Please try again!!");
                out.close();
                out.flush();
                return;
            }

            pStmt = conn.prepareStatement(
                    "INSERT INTO PostponedReasons(PostponedReason, JobId, UserId, Status, CreatedDate, AssignmentIndex) " +
                            "VALUES (?,?,?,0,NOW(),?)");

            pStmt.setString(1, PostponedReason);
            pStmt.setInt(2, CustDataIndex);
            pStmt.setInt(3, TechnicianId);
            pStmt.setInt(4, AssignmentIndex);
            pStmt.executeUpdate();
            pStmt.close();

            out.println("true");
            out.close();
            out.flush();
        } catch (Exception Ex) {
            out.println("Exception Message in Postponed Reason" + Ex.getMessage());
            out.close();
            out.flush();
        }
    }

    private void RevertStatus(HttpServletRequest request, PrintWriter out, Connection conn) {
        stmt = null;
        rset = null;
        Query = " ";
        pStmt = null;

        String UserId = request.getParameter("Name").trim();
        String Status = request.getParameter("Status").trim();
        Status = Status.replace("-", " ");
        String ComplainNumber = request.getParameter("UniqueID").trim();
        ComplainNumber = ComplainNumber.replace("^", "#");
        String StatusVal = request.getParameter("StatusVal").trim();
        String AssignmentStatus = request.getParameter("AssignmentStatus").trim();

        int CustDataIndex = 0;
        try {
            Query = "SELECT  Id FROM CustomerData WHERE ComplainNumber='" + ComplainNumber + "' ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                CustDataIndex = rset.getInt(1);
            }
            rset.close();
            stmt.close();

            if (CustDataIndex == 0) {
                Supportive.doLogMethodMessage(this.getServletContext(), "Revert Status Check - 0001", "No Complain Found.Please try again!!");
                out.println("No Complain Data Found.Please try again!!");
                out.close();
                out.flush();
                return;
            }
            int AssignmentIndex = 0;
            Query = "SELECT Max(Id) FROM Assignment WHERE ComplaintId = " + CustDataIndex;
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next())
                AssignmentIndex = rset.getInt(1);
            rset.close();
            stmt.close();

            int TechnicianId = 0;
            Query = "SELECT Id FROM MobileUsers WHERE upper(trim(UserId))='" + UserId.toUpperCase().trim() + "' ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                TechnicianId = rset.getInt(1);
            }
            rset.close();
            stmt.close();

            if (TechnicianId == 0) {
                Supportive.doLogMethodMessage(this.getServletContext(), "Revert Status - Second Check", "No User Record Found.Please try again!!");
                out.println("No User Record Found.Please try again!!");
                out.close();
                out.flush();
                return;
            }

            Query = "UPDATE Assignment SET ComplaintStatus=" + StatusVal + ",AssignmentStatus=" + AssignmentStatus + "," +
                    "CreatedDate = DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s') " +
                    " WHERE Id = " + AssignmentIndex;
            stmt = conn.createStatement();
            stmt.executeUpdate(Query);
            stmt.close();

            out.println("true");
            out.close();
            out.flush();
        } catch (Exception Ex) {
            out.println("Exception Message Revert Status" + Ex.getMessage());
            out.close();
            out.flush();
        }
    }

    private void TestingService(HttpServletRequest request, PrintWriter out, Connection conn){
        pStmt = null;
        String Test1 = request.getParameter("Test1").trim();
        String Test2 = request.getParameter("Test2").trim();

        try {
            pStmt = conn.prepareStatement(
                    "INSERT INTO TestingTable(Test1,Test2) " +
                            "VALUES (?,?)");

            pStmt.setString(1, Test1);
            pStmt.setString(2, Test2);
            pStmt.executeUpdate();
            pStmt.close();

            out.println("true");
            out.close();
            out.flush();
        }
        catch (Exception e){

        }
    }

}
