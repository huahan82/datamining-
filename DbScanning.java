
package data.mining;

import java.sql.*;
/**
 *This is a separate class for database scanning.
 * A SQL database is used.
 * @author huahan
 */
	
public class DbScanning {

	String error;	
	private Connection conn = null;
        String database;
		
    public DbScanning () {                         
            }
    /**
    * set up connection to database	
     * @throws SQLException 
    */

    public void connect() throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //Create the connection Object To Communicate With The Database
            //conn=DriverManager.getConnection("jdbc:mysql://sql.njit.edu:3306/hh95", "hh95", "o1xMVuevR");
            conn=DriverManager.getConnection("jdbc:mysql://localhost/test", "root", "hanhan");		
            	} catch (ClassNotFoundException e) {
                	e.printStackTrace();
                    } catch (InstantiationException e) {
                    e.printStackTrace();
                    } catch (IllegalAccessException e) {
                    e.printStackTrace();
            }
    }
	
    /**
     * disconnect with database
     * @throws SQLException 
     */

    public void disconnect() throws SQLException{
	try {
            if (conn !=null) {
                conn.close();
            }
	}catch (SQLException e1) {
            error = "SQL exception : unable to close connection";
            throw new SQLException (error);
	}
			
    }
	/**
         * scan the database line by line
         * @param database
         * @return ResultSet A line of the data in a row
         * @throws SQLException 
         */	

    public ResultSet transaction(String database) throws SQLException {
        ResultSet rs = null;       
        PreparedStatement stmt = null;     
              if (conn!=null) {
                   try {
                       stmt = conn.prepareStatement("SELECT * From " + database);
                       rs =stmt.executeQuery();
                   }catch (SQLException e1) {
                    error = "SQL exception : could not execute query";
                    throw new SQLException(error);
                    }
            }
	return rs;
     }
}

     /**   
        public void aTransactionData (String database) {
            DbScanning scanning = new DbScanning();
            String transaction[] = new String[10];
            Set<String> itemsInALine= new HashSet<String> ();
            scanning.connect();
            ResultSet rs = scanning.transaction(database);
                while (rs.next()){
                    for (int i=1;i<10; i++) {
                        transaction[i] = rs.getString(i);
                        if (transaction [i] == null)
                            break;
                        if (i>=2) itemsInALine.add(transaction[i]);
                        }
                    System.out.println(itemsInALine.size());

                }
        }
        */
        
    	/*public static void main (String[] args) throws Exception {
    		DbScanning manager = new DbScanning();
                Set<String> items= new HashSet<String> ();
                //if (items.add("test")) System.out.println("test added");
                int countTransaction = 0;
                String transaction[] = new String[10];
    		manager.connect();
                ResultSet rs=manager.transaction("bjs");
    		while (rs.next()){
                    for (int i=1;i<10; i++) {
                        transaction[i] = rs.getString(i);
                        if (transaction [i] == null)
                            break;
                    if (i>=2) {
                        if (items.add(transaction[i]))
                        System.out.println(transaction[i]);
                    }
                    }
                    countTransaction ++;                            
                }
               // System.out.println(items.size());
                System.out.println("Total no. of transactions in database"+
                        countTransaction);

    	}*/

