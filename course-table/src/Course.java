public class Course {
    private final String lesson;
    private final String time;
    private final String classroom;

    public Course(String lesson, String time, String classroom) {
        this.lesson = lesson;
        this.time = time;
        this.classroom = classroom;
    }
    public String getLesson(){ return lesson; }
    public String getTime(){ return time; }
    public String getClassroom() { return classroom; }

    @Override
    public String toString(){
        return lesson + " - "+ time + " - " + classroom;
    }

}
