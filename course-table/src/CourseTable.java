import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Scanner;

public class CourseTable {
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final CourseDao DAO = new CourseDao();

    public static void main(String[] args) {
        printBanner();
        while (true) {
            printMenu();
            int choice = readChoice();
            switch (choice) {
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
                    statsByWeekday();
                    break;
                case 5:
                    joinQuery();
                    break;
                case 6:
                    swapCourse();
                    break;
                case 7:
                    System.out.println("再见！");
                    return;
                default:
                    System.out.println("没有这个选项，请输入1~7的数字");
            }
        }
    }

    private static void printBanner() {
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
        System.out.println("  4. 按星期统计课程数");
        System.out.println("  5. 连表查询展示");
        System.out.println("  6. 调课");
        System.out.println("  7. 退出");
    }

    private static int readChoice() {
        while (true) {
            System.out.println("请输入你的选择：");
            String line = SCANNER.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("输入有误，请输入对应数字。");
            }
        }
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

    private static int readId(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = SCANNER.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("请输入数字编号。");
            }
        }
    }

    private static void addCourse() {
        System.out.println();
        System.out.println("【添加课程】");
        try {
            System.out.println("可选时间段：");
            for (String line : DAO.listTimeSlots()) {
                System.out.println("  " + line);
            }
            System.out.println("可选教室：");
            for (String line : DAO.listClassrooms()) {
                System.out.println("  " + line);
            }
        } catch (SQLException e) {
            printDbError(e);
            return;
        }
        String name = readNonEmpty("课程名称：");
        int timeId = readId("时间段编号：");
        int roomId = readId("教室编号：");
        try {
            DAO.add(name, timeId, roomId);
            System.out.println("已添加：" + name + "（time_id=" + timeId + "，room_id=" + roomId + "）");
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("添加失败：时间段或教室编号不存在，请重新输入。");
        } catch (SQLException e) {
            printDbError(e);
        }
    }

    private static void listCourses() {
        System.out.println();
        System.out.println("【所有课程】");
        try {
            List<Course> courses = DAO.findAll();
            if (courses.isEmpty()) {
                System.out.println("暂无课程记录，先去添加一门吧。");
                return;
            }
            for (Course c : courses) {
                System.out.println(c);
            }
        } catch (SQLException e) {
            printDbError(e);
        }
    }

    private static void findCourse() {
        System.out.println();
        System.out.println("【查找课程】");
        String keyword = readNonEmpty("请输入课程名称（支持模糊匹配）：");
        try {
            List<Course> result = DAO.findByName(keyword);
            if (result.isEmpty()) {
                System.out.println("无此课程");
            } else {
                System.out.println("找到 " + result.size() + " 门课程：");
                for (Course c : result) {
                    System.out.println(c);
                }
            }
        } catch (SQLException e) {
            printDbError(e);
        }
    }

    private static void statsByWeekday() {
        System.out.println();
        System.out.println("【按星期统计课程数】");
        try {
            List<CourseDao.StatRow> rows = DAO.countByWeekday();
            if (rows.isEmpty()) {
                System.out.println("暂无课程记录。");
                return;
            }
            for (CourseDao.StatRow row : rows) {
                System.out.println(row.getWeekday() + "：" + row.getCount() + " 门");
            }
        } catch (SQLException e) {
            printDbError(e);
        }
    }

    private static void joinQuery() {
        System.out.println();
        System.out.println("【连表查询展示】");
        try {
            List<CourseDao.JoinRow> rows = DAO.joinQuery();
            if (rows.isEmpty()) {
                System.out.println("暂无课程记录。");
                return;
            }
            System.out.println("课程名 | 星期 | 节次 | 教室");
            for (CourseDao.JoinRow row : rows) {
                System.out.println(row.getName() + " | " + row.getWeekday() + " | "
                        + row.getPeriod() + " | " + row.getRoomName());
            }
        } catch (SQLException e) {
            printDbError(e);
        }
    }

    private static void swapCourse() {
        System.out.println();
        System.out.println("【调课】");
        String name1 = readNonEmpty("第一门课程名称：");
        String name2 = readNonEmpty("第二门课程名称：");
        try {
            if (DAO.swapTime(name1, name2)) {
                System.out.println("调课成功：" + name1 + " 与 " + name2 + " 的上课时间已互换（事务已提交）。");
            } else {
                System.out.println("调课失败：请确认两门课程都存在且名称不同。");
            }
        } catch (SQLException e) {
            System.out.println("调课失败，事务已回滚：" + e.getMessage());
        }
    }

    private static void printDbError(SQLException e) {
        System.out.println("数据库操作失败：" + e.getMessage());
        System.out.println("请检查 MySQL80 服务是否已启动、数据库 xinghuo 是否存在。");
    }
}
