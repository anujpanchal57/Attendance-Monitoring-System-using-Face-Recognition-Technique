/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Attendence;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author kp
 */
public class Dategetter 
{
    public static String getCurrentDate() 
    {
        String date = "";
        Calendar cal = Calendar.getInstance();

        int day = cal.get(Calendar.DATE);
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);

        date = year + "-" + month + "-" + day;
        return date;
    }
    
    public static String getCurrentTime()
    {
        String time = "";
        SimpleDateFormat sdf=new SimpleDateFormat("HH.mm");
        time=sdf.format(new Date());
        return time;
    } 
    
    public static String getDayName() 
    {
        String day = "";
        SimpleDateFormat dtformat = new SimpleDateFormat("EEEEEEEEEE");
        day = dtformat.format(new Date());
        return day;
    }

     public static void main(String[] args) 
     {
         System.out.println(getDayName());
         System.out.println(getCurrentTime());
         System.out.println(getCurrentDate());
     }
 }