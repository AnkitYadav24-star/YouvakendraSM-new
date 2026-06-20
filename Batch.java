import javafx.beans.property.SimpleStringProperty;

public class Batch {
    private String batchId;
    private String courseId;
    private String batchNo;
    private String startDate;
    private String endDate;
    private String batchTime;

    private final SimpleStringProperty batchIdProp = new SimpleStringProperty();
    private final SimpleStringProperty courseIdProp = new SimpleStringProperty();
    private final SimpleStringProperty batchNoProp = new SimpleStringProperty();
    private final SimpleStringProperty startDateProp = new SimpleStringProperty();
    private final SimpleStringProperty endDateProp = new SimpleStringProperty();
    private final SimpleStringProperty batchTimeProp = new SimpleStringProperty();

    public Batch(String batchId, String courseId, String batchNo, String startDate, String endDate, String batchTime) {
        setBatchId(batchId);
        setCourseId(courseId);
        setBatchNo(batchNo);
        setStartDate(startDate);
        setEndDate(endDate);
        setBatchTime(batchTime);
    }

    public String getBatchId() { return batchId; }
    public void setBatchId(String value) { this.batchId = value; this.batchIdProp.set(value); }
    public SimpleStringProperty batchIdProperty() { return batchIdProp; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String value) { this.courseId = value; this.courseIdProp.set(value); }
    public SimpleStringProperty courseIdProperty() { return courseIdProp; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String value) { this.batchNo = value; this.batchNoProp.set(value); }
    public SimpleStringProperty batchNoProperty() { return batchNoProp; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String value) { this.startDate = value; this.startDateProp.set(value); }
    public SimpleStringProperty startDateProperty() { return startDateProp; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String value) { this.endDate = value; this.endDateProp.set(value); }
    public SimpleStringProperty endDateProperty() { return endDateProp; }

    public String getBatchTime() { return batchTime; }
    public void setBatchTime(String value) { this.batchTime = value; this.batchTimeProp.set(value); }
    public SimpleStringProperty batchTimeProperty() { return batchTimeProp; }
}
