public class StudentLoginProfile {
    private final String studentId;
    private final String erpNo;
    private final String password;
    private final String imageUrl;

    public StudentLoginProfile(String studentId, String erpNo, String password, String imageUrl) {
        this.studentId = studentId;
        this.erpNo = erpNo;
        this.password = password;
        this.imageUrl = imageUrl;
    }

    public String getStudentId() { return studentId; }
    public String getErpNo() { return erpNo; }
    public String getPassword() { return password; }
    public String getImageUrl() { return imageUrl; }
}
