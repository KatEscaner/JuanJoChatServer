package dao;

import model.Group;
import model.Message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class GroupDAO {
    private final UserDAO userDAO = new UserDAO();
    public boolean checkIfUserIsMember(Message message, Connection con){
        int count = 0;
        final String query =
                "SELECT * " +
                    "FROM member INNER JOIN `group` " +
                        "ON member.group = `group`.id " +
                            "WHERE member.user = ? " +
                            "AND member.group = ?";
        try(PreparedStatement pst = con.prepareStatement(query)){
            pst.setString(1, String.valueOf(message.getSender()));
            pst.setString(2, String.valueOf(message.getGroup()));
            try(ResultSet rs = pst.executeQuery()){
                while (rs.next()) {
                    count++;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return count > 0;
    }

    public List<Long> getMembersOfAGroup(long groupID, Connection con) {
        List<Long> users = new ArrayList<>();
        final String query =
                "SELECT * " +
                    "FROM member JOIN user " +
                        "ON member.user = user.id " +
                            "WHERE member.group = ?";
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, String.valueOf(groupID));
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    users.add((long) rs.getInt("user"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }

    public Group getGroup(long groupID, Connection con){
        final String query =
                "SELECT * " +
                    "FROM `group` " +
                        "WHERE id = ?";
        Group group = null;
        try (PreparedStatement pst = con.prepareStatement(query)){
            pst.setString(1, String.valueOf(groupID));
            try(ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    group = new Group();
                    group.setId(rs.getLong("id"));
                    group.setName(rs.getString("name"));
                    group.setType(rs.getString("type"));
                    group.setFilter(rs.getInt("filter"));
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        if (group != null) {
            Group finalGroup = group;
            getMembersOfAGroup(groupID, con)
                    .forEach(e -> finalGroup.members.add(userDAO.getUserById(e, con)));
            finalGroup.members.forEach(e -> e.setPassword(""));
            return finalGroup;
        }
        return null;
    }
}