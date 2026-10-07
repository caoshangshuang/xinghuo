import java.sql.SQLOutput;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class CourseTable {
    private static final Scanner SCANNER=new Scanner(System.in);
    private static final List<Course> COURSES = new ArrayList<>();
    public static void main(String[] args){
        printBanner();
        while(true){
            printMenu();
            int choice=readChoice();
            switch(choice){
                case 1:
                    addCourse();
                    break;
                case 2:
                    listCourses();
                    break;
                case 3:
                    findCourse();
                    break;
                case 4:
                    System.out.println("再见！");
                    return;
                default:
                    System.out.println("没有这个选项，请输入1~4的数字");
            }
        }
    }
    private static void printBanner(){
        System.out.println("========================================");
        System.out.println("  星火人的个人课程表 · 付桂豪");
        System.out.println("========================================");
    }
    private static void printMenu() {
        System.out.println();
        System.out.println("---------- 菜单 ----------");
        System.out.println("  1. 添加课程");
        System.out.println("  2. 查看所有课程");
        System.out.println("  3. 查找课程");
        System.out.println("  4. 退出");
    }
    private static int readChoice(){
        while (true){
            System.out.println("请输入你的选择：");
            String line = SCANNER.nextLine().trim();
            try {
                return Integer.parseInt(line);
            }
            catch (NumberFormatException e){
                System.out.println("输入有误，请输入对应数字。");
            }
        }
    }
    private static void addCourse(){
        System.out.println();
        System.out.println("【添加课程】");
        String lesson=readNonEmpty("课程名称：");
        String time = readNonEmpty("上课时间（如 Monday 3-4）：");
        String classroom = readNonEmpty("教室：");
        Course course = new Course(lesson, time ,classroom);
        COURSES.add(course);
        System.out.println("已添加：" + course);
    }
    private static String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = SCANNER.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("内容不能为空，请重新输入。");
        }
    }
    private static void listCourses() {
        System.out.println();
        System.out.println("【所有课程】");
        if (COURSES.isEmpty()) {
            System.out.println("暂无课程记录，先去添加一门吧。");
            return;
        }
        for (int i = 0; i < COURSES.size(); i++) {
            System.out.println((i + 1) + ". " + COURSES.get(i));
        }
    }
    private static void findCourse() {
        System.out.println();
        System.out.println("【查找课程】");
        System.out.print("请输入课程名称（支持模糊匹配）：");
        String keyword = SCANNER.nextLine().trim();
        if (keyword.isEmpty()) {
            System.out.println("查找内容不能为空。");
            return;
        }
        List<Course> result = new ArrayList<>();
        for (Course c : COURSES) {
            if (c.getLesson().toLowerCase().contains(keyword.toLowerCase())) {
                result.add(c);
            }
        }
        if (result.isEmpty()) {
            System.out.println("无此课程");
        } else {
            System.out.println("找到 " + result.size() + " 门课程：");
            for (Course c : result) {
                System.out.println(c);
            }
        }
    }
}
