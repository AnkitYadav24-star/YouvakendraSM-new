import javafx.beans.property.SimpleStringProperty;

public class Attendance {
    private String attendanceId;
    private String date;
    private String studentId;
    private String batchId;
    private String status;
    private String markedBy;
    private String markedTime;
    private String courseId = "";

    private final SimpleStringProperty attendanceIdProp = new SimpleStringProperty();
    private final SimpleStringProperty dateProp = new SimpleStringProperty();
    private final SimpleStringProperty studentIdProp = new SimpleStringProperty();
    private final SimpleStringProperty batchIdProp = new SimpleStringProperty();
    private final SimpleStringProperty statusProp = new SimpleStringProperty();
    private final SimpleStringProperty markedByProp = new SimpleStringProperty();
    private final SimpleStringProperty markedTimeProp = new SimpleStringProperty();
    private final SimpleStringProperty courseIdProp = new SimpleStringProperty();

    public Attendance(String attendanceId, String date, String studentId, String batchId,
                      String status, String markedBy, String markedTime, String courseId) {
        setAttendanceId(attendanceId);
        setDate(date);
        setStudentId(studentId);
        setBatchId(batchId);
        setStatus(status);
        setMarkedBy(markedBy);
        setMarkedTime(markedTime);
        setCourseId(courseId);
    }

    public Attendance(String attendanceId, String date, String studentId, String batchId,
                      String status, String markedBy, String markedTime) {
        this(attendanceId, date, studentId, batchId, status, markedBy, markedTime, "");
    }

    // Getters, Setters, and Properties
    public String getAttendanceId() { return attendanceId; }
    public void setAttendanceId(String value) { this.attendanceId = value; this.attendanceIdProp.set(value); }
    public SimpleStringProperty attendanceIdProperty() { return attendanceIdProp; }

    public String getDate() { return date; }
    public void setDate(String value) { this.date = value; this.dateProp.set(value); }
    public SimpleStringProperty dateProperty() { return dateProp; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String value) { this.studentId = value; this.studentIdProp.set(value); }
    public SimpleStringProperty studentIdProperty() { return studentIdProp; }

    public String getBatchId() { return batchId; }
    public void setBatchId(String value) { this.batchId = value; this.batchIdProp.set(value); }
    public SimpleStringProperty batchIdProperty() { return batchIdProp; }

    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; this.statusProp.set(value); }
    public SimpleStringProperty statusProperty() { return statusProp; }

    public String getMarkedBy() { return markedBy; }
    public void setMarkedBy(String value) { this.markedBy = value; this.markedByProp.set(value); }
    public SimpleStringProperty markedByProperty() { return markedByProp; }

    public String getMarkedTime() { return markedTime; }
    public void setMarkedTime(String value) { this.markedTime = value; this.markedTimeProp.set(value); }
    public SimpleStringProperty markedTimeProperty() { return markedTimeProp; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String value) { this.courseId = value; this.courseIdProp.set(value); }
    public SimpleStringProperty courseIdProperty() { return courseIdProp; }
}
