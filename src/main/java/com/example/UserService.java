public class UserService {
    public String getUserDisplayName(String user) {
        // Bug: No null check on user or user.getName()
        return user.trim();
    }
}
