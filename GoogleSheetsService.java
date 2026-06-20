import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GoogleSheetsService {
    private static final String APPLICATION_NAME = "YouvakendraSM";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private static Sheets getSheetsService() throws IOException, GeneralSecurityException {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(Config.CREDENTIALS_PATH))
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));
        return new Sheets.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public List<Student> readStudents() throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        String range = Config.SHEET_NAME + "!A:M";
        ValueRange response = service.spreadsheets().values()
                .get(Config.SPREADSHEET_ID, range)
                .execute();
        List<List<Object>> values = response.getValues();
        List<Student> students = new ArrayList<>();

        if (values == null || values.isEmpty()) {
            return students;
        }

        // Row 0 is the header row, start loop from 1 to read actual data rows
        for (int i = 1; i < values.size(); i++) {
            List<Object> row = values.get(i);

            // Check if the row is completely empty
            boolean isEmptyRow = true;
            for (Object cell : row) {
                if (cell != null && !cell.toString().trim().isEmpty()) {
                    isEmptyRow = false;
                    break;
                }
            }
            if (isEmptyRow) {
                continue;
            }

            // Convert row into Student object safely
            String studentId = getSafeValue(row, 0);
            String erpNo = getSafeValue(row, 1);
            String studentName = getSafeValue(row, 2);
            String fatherName = getSafeValue(row, 3);
            String dob = getSafeValue(row, 4);
            String address = getSafeValue(row, 5);
            String course = getSafeValue(row, 6);
            String center = getSafeValue(row, 7);
            String batchId = getSafeValue(row, 8);
            String batchTime = getSafeValue(row, 9);
            String doj = getSafeValue(row, 10);
            String courseDuration = getSafeValue(row, 11);
            String status = getSafeValue(row, 12);

            students.add(Student.createFromSheets(
                    studentId, erpNo, studentName, fatherName, dob, address,
                    course, center, batchId, batchTime, doj, courseDuration, status));
        }

        return students;
    }

    public List<Attendance> getAttendanceRecords() throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        String range = Config.ATTENDANCE_SHEET_NAME + "!A:G";
        ValueRange response = service.spreadsheets().values()
                .get(Config.SPREADSHEET_ID, range)
                .execute();
        List<List<Object>> values = response.getValues();
        List<Attendance> records = new ArrayList<>();

        if (values == null || values.isEmpty()) {
            return records;
        }

        // Row 0 is the header row, start loop from 1 to read actual data rows
        for (int i = 1; i < values.size(); i++) {
            List<Object> row = values.get(i);
            
            // Check if the row is completely empty
            boolean isEmptyRow = true;
            for (Object cell : row) {
                if (cell != null && !cell.toString().trim().isEmpty()) {
                    isEmptyRow = false;
                    break;
                }
            }
            if (isEmptyRow) {
                continue;
            }

            // Convert row into Attendance object safely
            String attendanceId = getSafeValue(row, 0);
            String date = getSafeValue(row, 1);
            String studentId = getSafeValue(row, 2);
            String batchId = getSafeValue(row, 3);
            String status = getSafeValue(row, 4);
            String markedBy = getSafeValue(row, 5);
            String markedTime = getSafeValue(row, 6);

            records.add(new Attendance(
                attendanceId, date, studentId, batchId, status, markedBy, markedTime
            ));
        }

        return records;
    }

    public List<Course> readCourses() throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        String range = Config.COURSES_SHEET_NAME + "!A:B";
        ValueRange response = service.spreadsheets().values()
                .get(Config.SPREADSHEET_ID, range)
                .execute();
        List<List<Object>> values = response.getValues();
        List<Course> courses = new ArrayList<>();

        if (values == null || values.isEmpty()) {
            return courses;
        }

        for (int i = 1; i < values.size(); i++) {
            List<Object> row = values.get(i);
            boolean isEmptyRow = true;
            for (Object cell : row) {
                if (cell != null && !cell.toString().trim().isEmpty()) {
                    isEmptyRow = false;
                    break;
                }
            }
            if (isEmptyRow) {
                continue;
            }

            String courseId = getSafeValue(row, 0);
            String courseName = getSafeValue(row, 1);

            courses.add(new Course(courseId, courseName));
        }
        return courses;
    }

    public List<Batch> readBatches() throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        String range = Config.BATCHES_SHEET_NAME + "!A:F";
        ValueRange response = service.spreadsheets().values()
                .get(Config.SPREADSHEET_ID, range)
                .execute();
        List<List<Object>> values = response.getValues();
        List<Batch> batches = new ArrayList<>();

        if (values == null || values.isEmpty()) {
            return batches;
        }

        for (int i = 1; i < values.size(); i++) {
            List<Object> row = values.get(i);
            boolean isEmptyRow = true;
            for (Object cell : row) {
                if (cell != null && !cell.toString().trim().isEmpty()) {
                    isEmptyRow = false;
                    break;
                }
            }
            if (isEmptyRow) {
                continue;
            }

            String batchId = getSafeValue(row, 0);
            String courseId = getSafeValue(row, 1);
            String batchNo = getSafeValue(row, 2);
            String startDate = getSafeValue(row, 3);
            String endDate = getSafeValue(row, 4);
            String batchTime = getSafeValue(row, 5);

            batches.add(new Batch(batchId, courseId, batchNo, startDate, endDate, batchTime));
        }
        return batches;
    }

    public List<Company> readCompanies() throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        String range = Config.COMPANIES_SHEET_NAME + "!A:E";
        ValueRange response = service.spreadsheets().values()
                .get(Config.SPREADSHEET_ID, range)
                .execute();
        List<List<Object>> values = response.getValues();
        List<Company> companies = new ArrayList<>();

        if (values == null || values.isEmpty()) {
            return companies;
        }

        for (int i = 1; i < values.size(); i++) {
            List<Object> row = values.get(i);
            boolean isEmptyRow = true;
            for (Object cell : row) {
                if (cell != null && !cell.toString().trim().isEmpty()) {
                    isEmptyRow = false;
                    break;
                }
            }
            if (isEmptyRow) {
                continue;
            }

            String companyId = getSafeValue(row, 0);
            String companyName = getSafeValue(row, 1);
            String hrName = getSafeValue(row, 2);
            String companyAddress = getSafeValue(row, 3);
            String contactInfo = getSafeValue(row, 4);

            companies.add(new Company(companyId, companyName, hrName, companyAddress, contactInfo));
        }
        return companies;
    }

    public List<StudentPlacement> readPlacements() throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        String range = Config.PLACEMENTS_SHEET_NAME + "!A:G";
        ValueRange response = service.spreadsheets().values()
                .get(Config.SPREADSHEET_ID, range)
                .execute();
        List<List<Object>> values = response.getValues();
        List<StudentPlacement> placements = new ArrayList<>();

        if (values == null || values.isEmpty()) {
            return placements;
        }

        for (int i = 1; i < values.size(); i++) {
            List<Object> row = values.get(i);
            boolean isEmptyRow = true;
            for (Object cell : row) {
                if (cell != null && !cell.toString().trim().isEmpty()) {
                    isEmptyRow = false;
                    break;
                }
            }
            if (isEmptyRow) {
                continue;
            }

            String placementId = getSafeValue(row, 0);
            String studentId = getSafeValue(row, 1);
            String erpNo = getSafeValue(row, 2);
            String companyId = getSafeValue(row, 3);
            String placementStatus = getSafeValue(row, 4);
            String selectionDate = getSafeValue(row, 5);
            String remark = getSafeValue(row, 6);

            placements.add(new StudentPlacement(placementId, studentId, erpNo, companyId, placementStatus, selectionDate, remark));
        }
        return placements;
    }

    private String getSafeValue(List<Object> row, int index) {
        if (index < row.size() && row.get(index) != null) {
            return row.get(index).toString().trim();
        }
        return "";
    }

    public List<AdminProfile> readAdminProfiles() throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        String range = Config.ADMINS_SHEET_NAME + "!A:F";
        ValueRange response = service.spreadsheets().values()
                .get(Config.SPREADSHEET_ID, range)
                .execute();
        List<List<Object>> values = response.getValues();
        List<AdminProfile> profiles = new ArrayList<>();

        if (values == null || values.isEmpty()) {
            return profiles;
        }

        for (int i = 1; i < values.size(); i++) {
            List<Object> row = values.get(i);
            if (row == null || row.isEmpty()) continue;
            String id = getSafeValue(row, 0);
            String name = getSafeValue(row, 1);
            String picUrl = getSafeValue(row, 2);
            String center = getSafeValue(row, 3);
            String designation = getSafeValue(row, 4);
            String password = getSafeValue(row, 5);
            profiles.add(new AdminProfile(id, name, picUrl, center, designation, password));
        }
        return profiles;
    }

    public List<TrainerProfile> readTrainerProfiles() throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        String range = Config.TRAINERS_SHEET_NAME + "!A:F";
        ValueRange response = service.spreadsheets().values()
                .get(Config.SPREADSHEET_ID, range)
                .execute();
        List<List<Object>> values = response.getValues();
        List<TrainerProfile> profiles = new ArrayList<>();

        if (values == null || values.isEmpty()) {
            return profiles;
        }

        for (int i = 1; i < values.size(); i++) {
            List<Object> row = values.get(i);
            if (row == null || row.isEmpty()) continue;
            String id = getSafeValue(row, 0);
            String name = getSafeValue(row, 1);
            String picUrl = getSafeValue(row, 2);
            String center = getSafeValue(row, 3);
            String designation = getSafeValue(row, 4);
            String password = getSafeValue(row, 5);
            profiles.add(new TrainerProfile(id, name, picUrl, center, designation, password));
        }
        return profiles;
    }

    public List<StudentLoginProfile> readStudentLoginProfiles() throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        String range = Config.STUDENT_LOGIN_SHEET_NAME + "!A:D";
        ValueRange response = service.spreadsheets().values()
                .get(Config.SPREADSHEET_ID, range)
                .execute();
        List<List<Object>> values = response.getValues();
        List<StudentLoginProfile> profiles = new ArrayList<>();

        if (values == null || values.isEmpty()) {
            return profiles;
        }

        for (int i = 1; i < values.size(); i++) {
            List<Object> row = values.get(i);
            if (row == null || row.isEmpty()) continue;
            String studentId = getSafeValue(row, 0);
            String erpNo = getSafeValue(row, 1);
            String password = getSafeValue(row, 2);
            String imageUrl = getSafeValue(row, 3);
            profiles.add(new StudentLoginProfile(studentId, erpNo, password, imageUrl));
        }
        return profiles;
    }

    public void addStudent(Student s) throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        String range = Config.SHEET_NAME + "!A:M";
        List<Object> row = List.of(
            s.getStudentId(),
            s.getErpNo(),
            s.getStudentName(),
            s.getFatherName(),
            s.getDob(),
            s.getAddress(),
            s.getCourse(),
            s.getCenter(),
            s.getBatchId(),
            s.getBatchTime(),
            s.getDoj(),
            s.getCourseDuration(),
            s.getStatus()
        );
        ValueRange body = new ValueRange().setValues(List.of(row));
        service.spreadsheets().values().append(Config.SPREADSHEET_ID, range, body)
                .setValueInputOption("RAW")
                .execute();
    }

    public void updateStudent(Student s) throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        String range = Config.SHEET_NAME + "!A:M";
        ValueRange response = service.spreadsheets().values()
                .get(Config.SPREADSHEET_ID, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            throw new IOException("No student records found in sheet");
        }
        int rowIndex = -1;
        for (int i = 1; i < values.size(); i++) {
            List<Object> row = values.get(i);
            if (row.size() > 0 && s.getStudentId().equals(row.get(0).toString().trim())) {
                rowIndex = i + 1; // 1-based index
                break;
            }
        }
        if (rowIndex == -1) {
            throw new IOException("Student not found in sheet: " + s.getStudentId());
        }
        
        String updateRange = Config.SHEET_NAME + "!A" + rowIndex + ":M" + rowIndex;
        List<Object> rowData = List.of(
            s.getStudentId(),
            s.getErpNo(),
            s.getStudentName(),
            s.getFatherName(),
            s.getDob(),
            s.getAddress(),
            s.getCourse(),
            s.getCenter(),
            s.getBatchId(),
            s.getBatchTime(),
            s.getDoj(),
            s.getCourseDuration(),
            s.getStatus()
        );
        ValueRange body = new ValueRange().setValues(List.of(rowData));
        service.spreadsheets().values().update(Config.SPREADSHEET_ID, updateRange, body)
                .setValueInputOption("RAW")
                .execute();
    }

    public void deleteStudent(String studentId) throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        String range = Config.SHEET_NAME + "!A:M";
        ValueRange response = service.spreadsheets().values()
                .get(Config.SPREADSHEET_ID, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            throw new IOException("No student records found in sheet");
        }
        int rowIndex = -1;
        for (int i = 1; i < values.size(); i++) {
            List<Object> row = values.get(i);
            if (row.size() > 0 && studentId.equals(row.get(0).toString().trim())) {
                rowIndex = i + 1; // 1-based index
                break;
            }
        }
        if (rowIndex == -1) {
            throw new IOException("Student not found in sheet: " + studentId);
        }

        com.google.api.services.sheets.v4.model.Spreadsheet spreadsheet = service.spreadsheets().get(Config.SPREADSHEET_ID).execute();
        Integer sheetId = null;
        for (com.google.api.services.sheets.v4.model.Sheet sheet : spreadsheet.getSheets()) {
            if (sheet.getProperties().getTitle().equals(Config.SHEET_NAME)) {
                sheetId = sheet.getProperties().getSheetId();
                break;
            }
        }
        if (sheetId == null) {
            throw new IOException("Sheet " + Config.SHEET_NAME + " not found");
        }

        com.google.api.services.sheets.v4.model.DeleteDimensionRequest deleteDimensionRequest = new com.google.api.services.sheets.v4.model.DeleteDimensionRequest()
            .setRange(new com.google.api.services.sheets.v4.model.DimensionRange()
                .setSheetId(sheetId)
                .setDimension("ROWS")
                .setStartIndex(rowIndex - 1)
                .setEndIndex(rowIndex)
            );

        com.google.api.services.sheets.v4.model.Request request = new com.google.api.services.sheets.v4.model.Request()
            .setDeleteDimension(deleteDimensionRequest);

        com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest batchRequest = new com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest()
            .setRequests(Collections.singletonList(request));

        service.spreadsheets().batchUpdate(Config.SPREADSHEET_ID, batchRequest).execute();
    }

    private void deleteRowFromSheet(String sheetName, int rowIndex) throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        com.google.api.services.sheets.v4.model.Spreadsheet spreadsheet = service.spreadsheets().get(Config.SPREADSHEET_ID).execute();
        Integer sheetId = null;
        for (com.google.api.services.sheets.v4.model.Sheet sheet : spreadsheet.getSheets()) {
            if (sheet.getProperties().getTitle().equals(sheetName)) {
                sheetId = sheet.getProperties().getSheetId();
                break;
            }
        }
        if (sheetId == null) {
            throw new IOException("Sheet " + sheetName + " not found");
        }

        com.google.api.services.sheets.v4.model.DeleteDimensionRequest deleteDimensionRequest = new com.google.api.services.sheets.v4.model.DeleteDimensionRequest()
            .setRange(new com.google.api.services.sheets.v4.model.DimensionRange()
                .setSheetId(sheetId)
                .setDimension("ROWS")
                .setStartIndex(rowIndex - 1)
                .setEndIndex(rowIndex)
            );

        com.google.api.services.sheets.v4.model.Request request = new com.google.api.services.sheets.v4.model.Request()
            .setDeleteDimension(deleteDimensionRequest);

        com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest batchRequest = new com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest()
            .setRequests(Collections.singletonList(request));

        service.spreadsheets().batchUpdate(Config.SPREADSHEET_ID, batchRequest).execute();
    }

    public void addCourse(Course c) throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        String range = Config.COURSES_SHEET_NAME + "!A:B";
        List<Object> row = List.of(c.getCourseId(), c.getCourseName());
        ValueRange body = new ValueRange().setValues(List.of(row));
        service.spreadsheets().values().append(Config.SPREADSHEET_ID, range, body)
                .setValueInputOption("RAW")
                .execute();
    }

    public void updateCourse(Course c) throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        String range = Config.COURSES_SHEET_NAME + "!A:B";
        ValueRange response = service.spreadsheets().values()
                .get(Config.SPREADSHEET_ID, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            throw new IOException("No courses found in sheet");
        }
        int rowIndex = -1;
        for (int i = 1; i < values.size(); i++) {
            List<Object> row = values.get(i);
            if (row.size() > 0 && c.getCourseId().equalsIgnoreCase(row.get(0).toString().trim())) {
                rowIndex = i + 1;
                break;
            }
        }
        if (rowIndex == -1) {
            throw new IOException("Course not found in sheet: " + c.getCourseId());
        }
        String updateRange = Config.COURSES_SHEET_NAME + "!A" + rowIndex + ":B" + rowIndex;
        List<Object> rowData = List.of(c.getCourseId(), c.getCourseName());
        ValueRange body = new ValueRange().setValues(List.of(rowData));
        service.spreadsheets().values().update(Config.SPREADSHEET_ID, updateRange, body)
                .setValueInputOption("RAW")
                .execute();
    }

    public void deleteCourse(String courseId) throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        String range = Config.COURSES_SHEET_NAME + "!A:B";
        ValueRange response = service.spreadsheets().values()
                .get(Config.SPREADSHEET_ID, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            throw new IOException("No courses found in sheet");
        }
        int rowIndex = -1;
        for (int i = 1; i < values.size(); i++) {
            List<Object> row = values.get(i);
            if (row.size() > 0 && courseId.equalsIgnoreCase(row.get(0).toString().trim())) {
                rowIndex = i + 1;
                break;
            }
        }
        if (rowIndex == -1) {
            throw new IOException("Course not found in sheet: " + courseId);
        }
        deleteRowFromSheet(Config.COURSES_SHEET_NAME, rowIndex);
    }

    public void addBatch(Batch b) throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        String range = Config.BATCHES_SHEET_NAME + "!A:F";
        List<Object> row = List.of(b.getBatchId(), b.getCourseId(), b.getBatchNo(), b.getStartDate(), b.getEndDate(), b.getBatchTime());
        ValueRange body = new ValueRange().setValues(List.of(row));
        service.spreadsheets().values().append(Config.SPREADSHEET_ID, range, body)
                .setValueInputOption("RAW")
                .execute();
    }

    public void updateBatch(Batch b) throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        String range = Config.BATCHES_SHEET_NAME + "!A:F";
        ValueRange response = service.spreadsheets().values()
                .get(Config.SPREADSHEET_ID, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            throw new IOException("No batches found in sheet");
        }
        int rowIndex = -1;
        for (int i = 1; i < values.size(); i++) {
            List<Object> row = values.get(i);
            if (row.size() > 0 && b.getBatchId().equalsIgnoreCase(row.get(0).toString().trim())) {
                rowIndex = i + 1;
                break;
            }
        }
        if (rowIndex == -1) {
            throw new IOException("Batch not found in sheet: " + b.getBatchId());
        }
        String updateRange = Config.BATCHES_SHEET_NAME + "!A" + rowIndex + ":F" + rowIndex;
        List<Object> rowData = List.of(b.getBatchId(), b.getCourseId(), b.getBatchNo(), b.getStartDate(), b.getEndDate(), b.getBatchTime());
        ValueRange body = new ValueRange().setValues(List.of(rowData));
        service.spreadsheets().values().update(Config.SPREADSHEET_ID, updateRange, body)
                .setValueInputOption("RAW")
                .execute();
    }

    public void deleteBatch(String batchId) throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        String range = Config.BATCHES_SHEET_NAME + "!A:F";
        ValueRange response = service.spreadsheets().values()
                .get(Config.SPREADSHEET_ID, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            throw new IOException("No batches found in sheet");
        }
        int rowIndex = -1;
        for (int i = 1; i < values.size(); i++) {
            List<Object> row = values.get(i);
            if (row.size() > 0 && batchId.equalsIgnoreCase(row.get(0).toString().trim())) {
                rowIndex = i + 1;
                break;
            }
        }
        if (rowIndex == -1) {
            throw new IOException("Batch not found in sheet: " + batchId);
        }
        deleteRowFromSheet(Config.BATCHES_SHEET_NAME, rowIndex);
    }

    public void addCompany(Company c) throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        String range = Config.COMPANIES_SHEET_NAME + "!A:E";
        List<Object> row = List.of(c.getCompanyId(), c.getCompanyName(), c.getHrName(), c.getCompanyAddress(), c.getContactInfo());
        ValueRange body = new ValueRange().setValues(List.of(row));
        service.spreadsheets().values().append(Config.SPREADSHEET_ID, range, body)
                .setValueInputOption("RAW")
                .execute();
    }

    public void updateCompany(Company c) throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        String range = Config.COMPANIES_SHEET_NAME + "!A:E";
        ValueRange response = service.spreadsheets().values()
                .get(Config.SPREADSHEET_ID, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            throw new IOException("No companies found in sheet");
        }
        int rowIndex = -1;
        for (int i = 1; i < values.size(); i++) {
            List<Object> row = values.get(i);
            if (row.size() > 0 && c.getCompanyId().equalsIgnoreCase(row.get(0).toString().trim())) {
                rowIndex = i + 1;
                break;
            }
        }
        if (rowIndex == -1) {
            throw new IOException("Company not found in sheet: " + c.getCompanyId());
        }
        String updateRange = Config.COMPANIES_SHEET_NAME + "!A" + rowIndex + ":E" + rowIndex;
        List<Object> rowData = List.of(c.getCompanyId(), c.getCompanyName(), c.getHrName(), c.getCompanyAddress(), c.getContactInfo());
        ValueRange body = new ValueRange().setValues(List.of(rowData));
        service.spreadsheets().values().update(Config.SPREADSHEET_ID, updateRange, body)
                .setValueInputOption("RAW")
                .execute();
    }

    public void deleteCompany(String companyId) throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        String range = Config.COMPANIES_SHEET_NAME + "!A:E";
        ValueRange response = service.spreadsheets().values()
                .get(Config.SPREADSHEET_ID, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            throw new IOException("No companies found in sheet");
        }
        int rowIndex = -1;
        for (int i = 1; i < values.size(); i++) {
            List<Object> row = values.get(i);
            if (row.size() > 0 && companyId.equalsIgnoreCase(row.get(0).toString().trim())) {
                rowIndex = i + 1;
                break;
            }
        }
        if (rowIndex == -1) {
            throw new IOException("Company not found in sheet: " + companyId);
        }
        deleteRowFromSheet(Config.COMPANIES_SHEET_NAME, rowIndex);
    }

    public void addPlacement(StudentPlacement p) throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        String range = Config.PLACEMENTS_SHEET_NAME + "!A:G";
        List<Object> row = List.of(p.getPlacementId(), p.getStudentId(), p.getErpNo(), p.getCompanyId(), p.getPlacementStatus(), p.getSelectionDate(), p.getRemark());
        ValueRange body = new ValueRange().setValues(List.of(row));
        service.spreadsheets().values().append(Config.SPREADSHEET_ID, range, body)
                .setValueInputOption("RAW")
                .execute();
    }

    public void updatePlacement(StudentPlacement p) throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        String range = Config.PLACEMENTS_SHEET_NAME + "!A:G";
        ValueRange response = service.spreadsheets().values()
                .get(Config.SPREADSHEET_ID, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            throw new IOException("No placements found in sheet");
        }
        int rowIndex = -1;
        for (int i = 1; i < values.size(); i++) {
            List<Object> row = values.get(i);
            if (row.size() > 0 && p.getPlacementId().equalsIgnoreCase(row.get(0).toString().trim())) {
                rowIndex = i + 1;
                break;
            }
        }
        if (rowIndex == -1) {
            throw new IOException("Placement not found in sheet: " + p.getPlacementId());
        }
        String updateRange = Config.PLACEMENTS_SHEET_NAME + "!A" + rowIndex + ":G" + rowIndex;
        List<Object> rowData = List.of(p.getPlacementId(), p.getStudentId(), p.getErpNo(), p.getCompanyId(), p.getPlacementStatus(), p.getSelectionDate(), p.getRemark());
        ValueRange body = new ValueRange().setValues(List.of(rowData));
        service.spreadsheets().values().update(Config.SPREADSHEET_ID, updateRange, body)
                .setValueInputOption("RAW")
                .execute();
    }

    public void deletePlacement(String placementId) throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        String range = Config.PLACEMENTS_SHEET_NAME + "!A:G";
        ValueRange response = service.spreadsheets().values()
                .get(Config.SPREADSHEET_ID, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            throw new IOException("No placements found in sheet");
        }
        int rowIndex = -1;
        for (int i = 1; i < values.size(); i++) {
            List<Object> row = values.get(i);
            if (row.size() > 0 && placementId.equalsIgnoreCase(row.get(0).toString().trim())) {
                rowIndex = i + 1;
                break;
            }
        }
        if (rowIndex == -1) {
            throw new IOException("Placement not found in sheet: " + placementId);
        }
        deleteRowFromSheet(Config.PLACEMENTS_SHEET_NAME, rowIndex);
    }
}
