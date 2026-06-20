import javafx.beans.property.SimpleStringProperty;

public class StudentPlacement {
    private String placementId;
    private String studentId;
    private String erpNo;
    private String companyId;
    private String placementStatus;
    private String selectionDate;
    private String remark;

    private final SimpleStringProperty placementIdProp = new SimpleStringProperty();
    private final SimpleStringProperty studentIdProp = new SimpleStringProperty();
    private final SimpleStringProperty erpNoProp = new SimpleStringProperty();
    private final SimpleStringProperty companyIdProp = new SimpleStringProperty();
    private final SimpleStringProperty placementStatusProp = new SimpleStringProperty();
    private final SimpleStringProperty selectionDateProp = new SimpleStringProperty();
    private final SimpleStringProperty remarkProp = new SimpleStringProperty();

    public StudentPlacement(String placementId, String studentId, String erpNo, String companyId,
            String placementStatus, String selectionDate, String remark) {
        setPlacementId(placementId);
        setStudentId(studentId);
        setErpNo(erpNo);
        setCompanyId(companyId);
        setPlacementStatus(placementStatus);
        setSelectionDate(selectionDate);
        setRemark(remark);
    }

    public String getPlacementId() {
        return placementId;
    }

    public void setPlacementId(String value) {
        this.placementId = value;
        this.placementIdProp.set(value);
    }

    public SimpleStringProperty placementIdProperty() {
        return placementIdProp;
    }

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

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String value) {
        this.companyId = value;
        this.companyIdProp.set(value);
    }

    public SimpleStringProperty companyIdProperty() {
        return companyIdProp;
    }

    public String getPlacementStatus() {
        return placementStatus;
    }

    public void setPlacementStatus(String value) {
        this.placementStatus = value;
        this.placementStatusProp.set(value);
    }

    public SimpleStringProperty placementStatusProperty() {
        return placementStatusProp;
    }

    public String getSelectionDate() {
        return selectionDate;
    }

    public void setSelectionDate(String value) {
        this.selectionDate = value;
        this.selectionDateProp.set(value);
    }

    public SimpleStringProperty selectionDateProperty() {
        return selectionDateProp;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String value) {
        this.remark = value;
        this.remarkProp.set(value);
    }

    public SimpleStringProperty remarkProperty() {
        return remarkProp;
    }
}
