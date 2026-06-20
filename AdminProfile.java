public class AdminProfile {
    private final String adminId;
    private final String adminName;
    private final String adminPictureUrl;
    private final String center;
    private final String designation;
    private final String password;

    public AdminProfile(String adminId, String adminName, String adminPictureUrl, String center, String designation, String password) {
        this.adminId = adminId;
        this.adminName = adminName;
        this.adminPictureUrl = adminPictureUrl;
        this.center = center;
        this.designation = designation;
        this.password = password;
    }

    public String getAdminId() { return adminId; }
    public String getAdminName() { return adminName; }
    public String getAdminPictureUrl() { return adminPictureUrl; }
    public String getCenter() { return center; }
    public String getDesignation() { return designation; }
    public String getPassword() { return password; }
}
