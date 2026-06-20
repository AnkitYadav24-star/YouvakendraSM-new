import javafx.beans.property.SimpleStringProperty;

public class Student {
    // 13 Required Fields from Google Sheets/Requirements
    private String studentId;
    private String erpNo;
    private String studentName;
    private String fatherName;
    private String dob;
    private String address;
    private String course;
    private String center;
    private String batchId;
    private String batchTime;
    private String doj;
    private String courseDuration;
    private String status;

    // Compatibility fields (not in Google Sheet but used by DashboardView)
    private String motherName = "";
    private String mobile = "";
    private String altMobile = "";
    private String email = "";
    private String courseId = "";

    // JavaFX Properties for UI Data Binding
    private final SimpleStringProperty studentIdProp = new SimpleStringProperty();
    private final SimpleStringProperty erpNoProp = new SimpleStringProperty();
    private final SimpleStringProperty studentNameProp = new SimpleStringProperty();
    private final SimpleStringProperty fatherNameProp = new SimpleStringProperty();
    private final SimpleStringProperty dobProp = new SimpleStringProperty();
    private final SimpleStringProperty addressProp = new SimpleStringProperty();
    private final SimpleStringProperty courseProp = new SimpleStringProperty();
    private final SimpleStringProperty centerProp = new SimpleStringProperty();
    private final SimpleStringProperty batchIdProp = new SimpleStringProperty();
    private final SimpleStringProperty batchTimeProp = new SimpleStringProperty();
    private final SimpleStringProperty dojProp = new SimpleStringProperty();
    private final SimpleStringProperty courseDurationProp = new SimpleStringProperty();
    private final SimpleStringProperty statusProp = new SimpleStringProperty();

    // Compatibility Properties
    private final SimpleStringProperty motherNameProp = new SimpleStringProperty();
    private final SimpleStringProperty mobileProp = new SimpleStringProperty();
    private final SimpleStringProperty altMobileProp = new SimpleStringProperty();
    private final SimpleStringProperty emailProp = new SimpleStringProperty();
    private final SimpleStringProperty courseIdProp = new SimpleStringProperty();

    // Compatibility Constructor used by DashboardView
    public Student(String id, String name, String fatherName, String motherName, String mobile,
            String altMobile, String email, String address, String course, String batch,
            String admissionDate, String completionDate, String status) {
        setStudentId(id);
        setErpNo("");
        setStudentName(name);
        setFatherName(fatherName);
        setDob("");
        setAddress(address);
        setCourse(course);
        setCenter("");
        setBatchTime(batch);
        if (batch != null && batch.contains(" ")) {
            setBatchId(batch.split(" ")[0]);
        } else {
            setBatchId("");
        }
        setDoj(admissionDate);
        setCourseDuration(completionDate);
        setStatus(status);

        // Compatibility fields
        setMotherName(motherName);
        setMobile(mobile);
        setAltMobile(altMobile);
        setEmail(email);
        setCourseId(course); // Default to course value
    }

    // Factory Method for Google Sheets parsing
    public static Student createFromSheets(String studentId, String erpNo, String studentName, String fatherName,
            String dob, String address, String course, String center, String batchId,
            String batchTime, String doj, String courseDuration, String status, String courseId) {
        Student s = new Student("", "", "", "", "", "", "", "", "", "", "", "", "");
        s.setStudentId(studentId);
        s.setErpNo(erpNo);
        s.setStudentName(studentName);
        s.setFatherName(fatherName);
        s.setDob(dob);
        s.setAddress(address);
        s.setCourse(course);
        s.setCenter(center);
        s.setBatchId(batchId);
        s.setBatchTime(batchTime);
        s.setDoj(doj);
        s.setCourseDuration(courseDuration);
        s.setStatus(status);
        s.setCourseId(courseId);
        return s;
    }

    // Overload for compatibility
    public static Student createFromSheets(String studentId, String erpNo, String studentName, String fatherName,
            String dob, String address, String course, String center, String batchId,
            String batchTime, String doj, String courseDuration, String status) {
        return createFromSheets(studentId, erpNo, studentName, fatherName, dob, address, course, center, batchId,
                batchTime, doj, courseDuration, status, course);
    }

    // Getters, Setters, and Properties for Required Fields
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String value) {
        this.studentId = value;
        this.studentIdProp.set(value);
    }

    public SimpleStringProperty studentIdProperty() {
        return studentIdProp;
    }

    public String getErpNo() {
        return erpNo;
    }

    public void setErpNo(String value) {
        this.erpNo = value;
        this.erpNoProp.set(value);
    }

    public SimpleStringProperty erpNoProperty() {
        return erpNoProp;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String value) {
        this.studentName = value;
        this.studentNameProp.set(value);
    }

    public SimpleStringProperty studentNameProperty() {
        return studentNameProp;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String value) {
        this.fatherName = value;
        this.fatherNameProp.set(value);
    }

    public SimpleStringProperty fatherNameProperty() {
        return fatherNameProp;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String value) {
        this.dob = value;
        this.dobProp.set(value);
    }

    public SimpleStringProperty dobProperty() {
        return dobProp;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String value) {
        this.address = value;
        this.addressProp.set(value);
    }

    public SimpleStringProperty addressProperty() {
        return addressProp;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String value) {
        this.course = value;
        this.courseProp.set(value);
    }

    public SimpleStringProperty courseProperty() {
        return courseProp;
    }

    public String getCenter() {
        return center;
    }

    public void setCenter(String value) {
        this.center = value;
        this.centerProp.set(value);
    }

    public SimpleStringProperty centerProperty() {
        return centerProp;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String value) {
        this.batchId = value;
        this.batchIdProp.set(value);
    }

    public SimpleStringProperty batchIdProperty() {
        return batchIdProp;
    }

    public String getBatchTime() {
        return batchTime;
    }

    public void setBatchTime(String value) {
        this.batchTime = value;
        this.batchTimeProp.set(value);
    }

    public SimpleStringProperty batchTimeProperty() {
        return batchTimeProp;
    }

    public String getDoj() {
        return doj;
    }

    public void setDoj(String value) {
        this.doj = value;
        this.dojProp.set(value);
    }

    public SimpleStringProperty dojProperty() {
        return dojProp;
    }

    public String getCourseDuration() {
        return courseDuration;
    }

    public void setCourseDuration(String value) {
        this.courseDuration = value;
        this.courseDurationProp.set(value);
    }

    public SimpleStringProperty courseDurationProperty() {
        return courseDurationProp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        this.status = value;
        this.statusProp.set(value);
    }

    public SimpleStringProperty statusProperty() {
        return statusProp;
    }

    // Compatibility Getters, Setters, and Properties for DashboardView.java
    public String getId() {
        return getStudentId();
    }

    public void setId(String value) {
        setStudentId(value);
    }

    public SimpleStringProperty idProperty() {
        return studentIdProp;
    }

    public String getName() {
        return getStudentName();
    }

    public void setName(String value) {
        setStudentName(value);
    }

    public SimpleStringProperty nameProperty() {
        return studentNameProp;
    }

    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String value) {
        this.motherName = value;
        this.motherNameProp.set(value);
    }

    public SimpleStringProperty motherNameProperty() {
        return motherNameProp;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String value) {
        this.mobile = value;
        this.mobileProp.set(value);
    }

    public SimpleStringProperty mobileProperty() {
        return mobileProp;
    }

    public String getAltMobile() {
        return altMobile;
    }

    public void setAltMobile(String value) {
        this.altMobile = value;
        this.altMobileProp.set(value);
    }

    public SimpleStringProperty altMobileProperty() {
        return altMobileProp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String value) {
        this.email = value;
        this.emailProp.set(value);
    }

    public SimpleStringProperty emailProperty() {
        return emailProp;
    }

    public String getBatch() {
        return getBatchTime();
    }

    public void setBatch(String value) {
        setBatchTime(value);
    }

    public SimpleStringProperty batchProperty() {
        return batchTimeProp;
    }

    public String getAdmissionDate() {
        return getDoj();
    }

    public void setAdmissionDate(String value) {
        setDoj(value);
    }

    public SimpleStringProperty admissionDateProperty() {
        return dojProp;
    }

    public String getCompletionDate() {
        return getCourseDuration();
    }

    public void setCompletionDate(String value) {
        setCourseDuration(value);
    }

    public SimpleStringProperty completionDateProperty() {
        return courseDurationProp;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String value) {
        this.courseId = value;
        this.courseIdProp.set(value);
    }

    public SimpleStringProperty courseIdProperty() {
        return courseIdProp;
    }
}
