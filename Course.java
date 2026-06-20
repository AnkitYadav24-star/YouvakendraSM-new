import javafx.beans.property.SimpleStringProperty;

public class Course {
    private String courseId;
    private String courseName;

    private final SimpleStringProperty courseIdProp = new SimpleStringProperty();
    private final SimpleStringProperty courseNameProp = new SimpleStringProperty();

    public Course(String courseId, String courseName) {
        setCourseId(courseId);
        setCourseName(courseName);
    }

    public String getCourseId() { return courseId; }
    public void setCourseId(String value) { this.courseId = value; this.courseIdProp.set(value); }
    public SimpleStringProperty courseIdProperty() { return courseIdProp; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String value) { this.courseName = value; this.courseNameProp.set(value); }
    public SimpleStringProperty courseNameProperty() { return courseNameProp; }
}
