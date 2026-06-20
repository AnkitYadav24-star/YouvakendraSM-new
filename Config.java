import java.io.File;

public class Config {
    public static final String SPREADSHEET_ID = "1AwdGxIz2-lY3txhugK4Dd-FRcNp34PpnevD1-LwcaGA";
    public static final String SHEET_NAME = "Students";
    public static final String ATTENDANCE_SHEET_NAME = "Attendance";
    public static final String COURSES_SHEET_NAME = "Courses";
    public static final String BATCHES_SHEET_NAME = "Batches";
    public static final String COMPANIES_SHEET_NAME = "Companies";
    public static final String PLACEMENTS_SHEET_NAME = "Student_Placements";
    public static final String ADMINS_SHEET_NAME = "Admins";
    public static final String TRAINERS_SHEET_NAME = "Trainers";
    public static final String STUDENT_LOGIN_SHEET_NAME = "Student_login";
    
    public static final String CREDENTIALS_PATH = getCredentialsPath();

    private static String getCredentialsPath() {
        // 1. Check for credencial.json at the root directory of execution
        File rootCred = new File("credencial.json");
        if (rootCred.exists() && rootCred.isFile()) {
            return rootCred.getAbsolutePath();
        }

        // 2. Check inside the packaged 'app' directory
        File appCred = new File("app/credencial.json");
        if (appCred.exists() && appCred.isFile()) {
            return appCred.getAbsolutePath();
        }

        // 3. Check inside dev workspace subdirectory 'YouvakendraSM'
        File devCred = new File("YouvakendraSM/credencial.json");
        if (devCred.exists() && devCred.isFile()) {
            return devCred.getAbsolutePath();
        }

        // 4. Default fallback
        return "E:\\New folder\\Softwares\\Attendance Youvakendra\\YouvakendraSM\\credencial.json";
    }
}
