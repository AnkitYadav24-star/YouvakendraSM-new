import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
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
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS_READONLY));
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
}
