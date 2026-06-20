public class TrainerProfile {
    private final String trainerId;
    private final String trainerName;
    private final String trainerPictureUrl;
    private final String center;
    private final String designation;
    private final String password;

    public TrainerProfile(String trainerId, String trainerName, String trainerPictureUrl, String center, String designation, String password) {
        this.trainerId = trainerId;
        this.trainerName = trainerName;
        this.trainerPictureUrl = trainerPictureUrl;
        this.center = center;
        this.designation = designation;
        this.password = password;
    }

    public String getTrainerId() { return trainerId; }
    public String getTrainerName() { return trainerName; }
    public String getTrainerPictureUrl() { return trainerPictureUrl; }
    public String getCenter() { return center; }
    public String getDesignation() { return designation; }
    public String getPassword() { return password; }
}
