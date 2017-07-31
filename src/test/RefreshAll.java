/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import Database.DatabaseConnection;
import java.io.File;
import java.io.FileOutputStream;

/**
 *
 * @author test
 */
public class RefreshAll {

    public void refresh() {
        refreshTextFiles();
        deleteDataFiles();
        updateDB();
    }

    /**
     * Deleting Data Files
     */
    public void deleteDataFiles() {
        try {
            File[] lstFiles = new File("data").listFiles();
            for (File f : lstFiles) {
                System.out.println("deleting: " + f.getName());
                f.delete();
            }
            System.out.println("All Files Are Deleted");
        } catch (Exception e) {
        }
    }

    public void updateDB() {
        try {
            String query = "Delete from student";
            DatabaseConnection dbcon = new DatabaseConnection();
            dbcon.dbconnection();
            dbcon.getUpdate(query);
        } catch (Exception e) {
        }
    }

    /**
     * Refreshing Text Files ......
     */

    public void refreshTextFiles() {
        try {
            File file1 = new File("train.txt");
            FileOutputStream fos1 = new FileOutputStream(file1);
            fos1.write("".getBytes());
            fos1.close();
        } catch (Exception e) {
        }
    }

    /**
     *
     */
    public static void main(String[] args) {
        RefreshAll rfrshall = new RefreshAll();
        rfrshall.refresh();
    }
}
