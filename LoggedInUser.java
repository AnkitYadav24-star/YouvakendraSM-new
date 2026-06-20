public class LoggedInUser {
    public enum Role { ADMIN, TRAINER, STUDENT }
    
    private final Role role;
    private final String id;
    private final String name;
    private final String pictureUrl;
    private final String center;
    private final String designation;
    private final String erpNo; // Student specific
    
    public LoggedInUser(Role role, String id, String name, String pictureUrl, String center, String designation, String erpNo) {
        this.role = role;
        this.id = id;
        this.name = name;
        this.pictureUrl = pictureUrl;
        this.center = center;
        this.designation = designation;
        this.erpNo = erpNo;
    }
    
    public Role getRole() { return role; }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getPictureUrl() { return pictureUrl; }
    public String getCenter() { return center; }
    public String getDesignation() { return designation; }
    public String getErpNo() { return erpNo; }
}
