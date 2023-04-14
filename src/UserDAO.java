import model.User;
import model.UserCredentials;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    // ask database if a user exists
    public boolean checkUser(UserCredentials userCredentials, Connection con){
        final String query =
                "SELECT * " +
                    "FROM user " +
                        "WHERE email = ? AND password = ?";
        int count = 0;
        try (PreparedStatement pst = con.prepareStatement(query)){
            pst.setString(1, userCredentials.getEmail());
            pst.setString(2, userCredentials.getPassword());
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    count++;
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return count > 0;
    }

    // ask database about user information
    public User getUser(UserCredentials userCredentials, Connection con){
        final String query =
                "SELECT * " +
                    "FROM user " +
                        "WHERE email = ? AND password = ?";
        User user = null;
        try (PreparedStatement pst = con.prepareStatement(query)){
            pst.setString(1, userCredentials.getEmail());
            pst.setString(2, userCredentials.getPassword());
            try(ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    user = new User();
                    user.setId(rs.getLong("id"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return user;
    }

    public User getUserById(Long userID, Connection con){
        final String query =
                "SELECT * " +
                        "FROM user " +
                        "WHERE id = ?";
        User user = null;
        try (PreparedStatement pst = con.prepareStatement(query)){
            pst.setString(1, String.valueOf(userID));
            try(ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    user = new User();
                    user.setId(rs.getLong("id"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return user;
    }

    public List<Long> getUserGroupsID(long userID, Connection con){
        List<Long> groupsID = new ArrayList<>();
        final String query =
                "SELECT * " +
                    "FROM member INNER JOIN `group` " +
                        "ON member.group = `group`.id " +
                            "WHERE member.user = ? ";
        try(PreparedStatement pst = con.prepareStatement(query)){
            pst.setString(1, String.valueOf(userID));
            try(ResultSet rs = pst.executeQuery()){
                while (rs.next()) {
                    groupsID.add(rs.getLong("group"));
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return groupsID;
    }
}
