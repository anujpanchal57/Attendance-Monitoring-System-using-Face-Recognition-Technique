/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Attendence;

import Database.DatabaseConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author test
 */
public class Attendence {

    DatabaseConnection dbcon;
    String date;
    String time;
    public List<String> studentAtt;
    int thresholdFactor = 20;
    public static String from = "";
    public static String to = "";

    public Attendence() {
        dbcon = new DatabaseConnection();
        dbcon.dbconnection();
        date = Dategetter.getCurrentDate();
        time = Dategetter.getCurrentTime();
        studentAtt = new ArrayList<>();
    }

    public void MarkAttendence(String sid) {
        try {
            studentAtt.add(sid);
            if (studentAtt.size() >= thresholdFactor) {
                /**
                 * check weather last 10 images are of same sid
                 *
                 */

                int cnter = 0;
                for (String sidnum : studentAtt) {
                    if (sidnum.equals(sid)) {
                        cnter++;
                    }
                }
                if (cnter >= thresholdFactor - 5) {
                    String Date = this.date;
                    this.time = Dategetter.getCurrentTime();
                    String subject = getSubject(time);
                    if (!subject.equals("")) {
                        String fromtime = from;
                        String totime = to;
                        if ((!chkAttendenceExists(sid, Date, subject, fromtime, totime))) {
                            String query = "INSERT INTO attendence(sid,present,attDate,entryTime,subject,fromtime,totime) VALUES(" + sid + ",'p','" + Date + "','" + this.time + "','" + subject + "','" + fromtime + "','" + totime + "')";
                            dbcon.getUpdate(query);
                            JOptionPane.showMessageDialog(null, "ATTENDENCE MARKED FOR STUDENT " + getStudentName(sid) + " At Time " + this.time);
                        }
                    }
                }
                System.out.println("before: " + studentAtt);
                studentAtt.removeAll(studentAtt);
                System.out.println("after: " + studentAtt);
            }
        } catch (Exception e) {
        }
    }

    public boolean chkAttendenceExists(String sid, String Date, String subject, String fromtime, String totime) {
        boolean flag = false;
        try {
            String query = "SELECT * FROM attendence WHERE sid='" + sid + "' AND attDate='" + Date + "' AND subject='" + subject + "' AND fromtime='" + fromtime + "' AND totime='" + totime + "'";
            ResultSet rs = dbcon.getResultSet(query);
            if (rs.next()) {
                flag = true;
            }
        } catch (Exception e) {
        }
        return flag;
    }

    public String getSubject(String currtime) {
        String subject = "";
        System.out.println("CurrTime IS: " + currtime);
        try {
            String day = Dategetter.getDayName();

            float currtimef = Float.parseFloat(currtime);
            String query = "SELECT * FROM timetable WHERE day='" + day + "'";
            ResultSet rs = dbcon.getResultSet(query);
            while (rs.next()) {
                float stTime = Float.parseFloat(rs.getString("Fromtime"));
                float enTime = Float.parseFloat(rs.getString("Totime"));
                if (currtimef >= stTime && currtimef <= enTime) {
                    subject = rs.getString("Subject");
                    from = rs.getString("fromtime");
                    to = rs.getString("totime");
                    break;
                }
            }
        } catch (NumberFormatException | SQLException e) {
        }
        return subject;
    }

    public String getSid(String StudentName, String std) {
        String sid = "";
        try {
            String query = "SELECT sid from student WHERE STUDENT_NAME='" + StudentName + "' AND student_std='" + std + "'";
            ResultSet rs = dbcon.getResultSet(query);
            if (rs.next()) {
                sid = rs.getString(1);
            }
        } catch (SQLException | NumberFormatException e) {
        }
        return sid;
    }

    public String getStudentName(String sid) {
        String name = "";
        try {
            String query = "SELECT STUDENT_NAME from student WHERE sid='" + sid + "'";
            ResultSet rs = dbcon.getResultSet(query);
            if (rs.next()) {
                name = rs.getString(1);
            }
        } catch (Exception e) {
        }
        return name;
    }
}
