# Attendance Monitoring System using Face Recognition

It will be a two step mechanism. First, prior to face recognition we have to do face detection. Once, face detection has been performed successfully then only face recognition will be performed by using Principal Component Analysis (PCA).

## How to run the system

In order to run the system, you'll require to install Netbeans 8.0 or higher .


**For Windows:** https://netbeans.org/downloads/


**For Ubuntu:** https://askubuntu.com/questions/75549/how-do-i-install-netbeans


After completing above step, you'll need to download SQL Yog to setup the database required for the system to mark and store the attendance.


> https://sqlyog.en.softonic.com/


## Compiling and Running the Project

After completing all the above mentioned steps, Open **Netbeans** and setup the connection port of **SQL Yog**(In my case, the port number is 3306)

1. Run `Attendance.java` in Netbeans, then register yourself with the help of a form that appears on the page.
2. Then, after registering successfully, if you want to mark your attendance for a particular subject, run `MarkAttendance.java`
3. As soon as you run `MarkAttendance.java`, a login page will appear in front of you **(that page is only for the professor and not for the student)**, then the professor will login and a window will appear where the student has to place his/her Face in front of it and the camera will scan, match and identify the face by comparing it with the HAAR Classifiers.
4. If that particular Student's Face is present in the Database, then his/her attendance will be marked simultaneously.

## Other Additional Functionalities

Some of the other additional functionalities include : 

1. If you're identified as a defaulter, then a mail and a message as well will be sent to your Parent's Mail ID as well as to their Contact No which was given at the time of registration.
2. It also displays a Pie Chart of your attendance report.
