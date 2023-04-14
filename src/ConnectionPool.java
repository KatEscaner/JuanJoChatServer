import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionPool {
    private static DataSource dataSource;

    //create a connection with the database server and can return connections to use in another objects
    public static DataSource getDataSource(){
        if(dataSource == null){
            MysqlConnectionPoolDataSource cpds = new MysqlConnectionPoolDataSource();
            cpds.setUrl("jdbc:mysql://localhost/mailbox");
            cpds.setUser("root");
            cpds.setPassword("");

            dataSource = cpds;
        }
        return dataSource;
    }

    public static Connection getConnection() throws SQLException{
        return getDataSource().getConnection();
    }
}
