import java.sql.*;

public class UserRepository {
    private Connection conn;

    public void deleteUser(String username) throws SQLException {
        String sql = "DELETE FROM users WHERE username = '" + username + "'";
        Statement st = conn.createStatement();
        st.executeUpdate(sql);
    }
}
