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

    private String getSafeValue(List<Object> row, int index) {
        if (index < row.size() && row.get(index) != null) {
            return row.get(index).toString().trim();
        }
        return "";
    }
}
