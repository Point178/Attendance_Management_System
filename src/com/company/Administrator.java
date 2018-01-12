package com.company;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Scanner;

public class Administrator {
    private int id;
    private Statement stmt;

    public Administrator(int id, Statement stmt) {
        this.id = id;
        this.stmt = stmt;
    }

    public void run() {
        try {
            boolean execute = true;
            while (execute) {
                System.out.println("请选择操作：\n1. 管理用户信息\n2. 查看考勤信息\n3. 管理日志信息\n4. 系统设置\n5. 退出系统\n其他  返回上级菜单");
                Scanner in = new Scanner(System.in);
                String input = in.nextLine();
                switch (input) {
                    case "1":
                        manageUser();
                        break;
                    case "2":
                        viewAttendance();
                        break;
                    case "3":
                        manageLog();
                    case "4":
                        setSystem();
                        break;
                    case "5":
                        System.exit(0);
                        break;
                    default:
                        execute = false;
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("输入不合法！");
        }

    }

    /**
     * 管理用户信息
     *
     * @throws Exception
     */
    private void manageUser() throws Exception {
        boolean execute = true;
        while (execute) {
            System.out.println("请选择操作：\n1. 增加用户\n2. 删除用户\n3. 修改用户信息\n4. 查询用户信息\n5. 退出系统\n其他  返回上级菜单");
            Scanner in = new Scanner(System.in);
            String input = in.nextLine();
            switch (input) {
                case "1":
                    addUser();
                    break;
                case "2":
                    deleteUser();
                    break;
                case "3":
                    modifyUser();
                    break;
                case "4":
                    viewUser();
                    break;
                case "5":
                    System.exit(0);
                default:
                    execute = false;
                    break;
            }
        }
    }

    /**
     * 增加新用户
     *
     * @throws Exception
     */
    private void addUser() throws Exception {
        int usrId;
        String usrName;
        int usrRole;
        System.out.println("请输入用户工号、用户名、用户身份（数字表示）");
        Scanner scanner = new Scanner(System.in);
        usrId = Integer.parseInt(scanner.next());
        usrName = scanner.next();
        usrRole = Integer.parseInt(scanner.next());
        ResultSet rs = stmt.executeQuery("SELECT * FROM employee WHERE eno =" + usrId);
        int count = 0;
        while (rs.next()) {
            count = rs.getInt(1);
        }
        if (count > 0) {
            System.out.println("已存在工号为" + usrId + "的用户！");
            return;
        }
        String sql = null;
        switch (usrRole) {
            case 1:
                System.out.println("请输入部门编号");
                scanner = new Scanner(System.in);
                int dno = Integer.parseInt(scanner.next());
                rs = stmt.executeQuery("SELECT * FROM department WHERE dno = " + dno);
                count = 0;
                while (rs.next()) {
                    count = rs.getInt(1);
                }
                if (count == 0) {
                    System.out.println("不存在部门编号为" + dno + "的部门！");
                    return;
                }
                sql = "INSERT INTO belong(dno, eno) VALUES ('" + dno + "',' " + usrId + "')";
                break;
            case 2:
                System.out.println("每个部门只能有一个部门经理！请在增加部门时增加部门经理。");
                return;
            case 3:
                break;
            case 4:
                System.out.println("系统中只能有一个系统管理员！");
                return;
            default:
                System.out.println("用户身份必须从 1 ～ 4 中选择！");
                return;

        }
        String sql1 = "INSERT INTO employee(eno, ename, role) VALUES ('" + usrId + "', '" + usrName + "',' " + usrRole + "')";
        stmt.executeUpdate(sql1);
        if (sql != null) {
            stmt.executeUpdate(sql);
        }
        System.out.println("增加用户成功！");
        writelog("add_usr");
    }

    /**
     * 删除用户
     *
     * @throws Exception
     */
    private void deleteUser() throws Exception {
        System.out.println("请输入用户工号");
        Scanner scanner = new Scanner(System.in);
        int usrId = scanner.nextInt();
        ResultSet rs = stmt.executeQuery("SELECT * FROM employee WHERE eno =" + usrId);
        int count = 0;
        while (rs.next()) {
            count = rs.getInt(1);
        }
        if (count == 0) {
            System.out.println("不存在工号为" + usrId + "的用户！");
            return;
        }
        String sql = "DELETE FROM employee WHERE eno=" + usrId;

        stmt.executeUpdate(sql);
        rs = stmt.executeQuery("SELECT * FROM belong WHERE eno =" + usrId);
        count = 0;
        while (rs.next()) {
            count = rs.getInt(1);
        }
        if (count > 0) {
            stmt.executeUpdate("DELETE FROM belong WHERE eno=" + usrId);

        }
        System.out.println("删除用户成功！");
        writelog("del_user");
    }

    /**
     * 修改用户密码
     *
     * @throws Exception
     */
    private void modifyUser() throws Exception {
        System.out.println("请输入用户工号");
        Scanner scanner = new Scanner(System.in);
        int usrId = scanner.nextInt();
        ResultSet rs = stmt.executeQuery("SELECT * FROM employee WHERE eno =" + usrId);
        int count = 0;
        while (rs.next()) {
            count = rs.getInt(1);
        }
        if (count == 0) {
            System.out.println("不存在工号为" + usrId + "的用户！");
            return;
        }
        String passwd;
        Scanner in = new Scanner(System.in);
        do {
            System.out.println("请输入新密码：（不超过20位）");
            passwd = in.nextLine();
            System.out.println("请再次输入新密码：");
        } while (!Objects.equals(passwd, in.nextLine()));

        //update database
        String sql = "update employee set password='" + passwd + "' where eno='" + usrId + "'";
        stmt.execute(sql);
        System.out.println("修改密码成功!");

        //write log
        writelog("modify_user");
    }

    /**
     * 查看用户基本信息
     *
     * @throws Exception
     */
    private void viewUser() throws Exception {
        ResultSet rs = null;
        System.out.println("请选择按工号查询-1/按姓名查询-2");
        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                System.out.println("请输入用户工号");
                scanner = new Scanner(System.in);
                int usrId = scanner.nextInt();
                rs = stmt.executeQuery("SELECT * FROM employee WHERE eno =" + usrId);
                int count = 0;
                while (rs.next()) {
                    count = rs.getInt(1);
                }
                if (count == 0) {
                    System.out.println("不存在工号为" + usrId + "的用户！");
                    return;
                }
                rs = stmt.executeQuery("SELECT * FROM employee WHERE eno=" + usrId);

                break;
            case 2:
                System.out.println("请输入用户姓名");
                scanner = new Scanner(System.in);
                String name = scanner.next();
                rs = stmt.executeQuery("SELECT * FROM employee WHERE ename ='" + name + "'");
                count = 0;
                while (rs.next()) {
                    count = rs.getInt(1);
                }
                if (count == 0) {
                    System.out.println("不存在名字为" + name + "的用户！");
                    return;
                }
                rs = stmt.executeQuery("SELECT * FROM employee WHERE ename='" + name + "'");
                break;
            default:
                System.out.println("请输入1/2");
                return;
        }
        while (rs.next()) {
            System.out.println("工号: " + rs.getString(1));
            System.out.println("姓名: " + rs.getString(2));
            System.out.println("密码: " + rs.getString(3));
            int role = rs.getInt(4);
            String roleStr;
            switch (role) {
                case 1:
                    roleStr = "普通员工";
                    break;
                case 2:
                    roleStr = "部门经理";
                    break;
                case 3:
                    roleStr = "人事主管";
                    break;
                case 4:
                    roleStr = "系统管理员";
                    break;
                default:
                    return;
            }
            System.out.println("身份: " + roleStr);
            System.out.println();
        }
    }

    /**
     * 查看考勤信息
     *
     * @throws Exception
     */
    private void viewAttendance() throws Exception {
        ResultSet rs = null;
        System.out.println("请选择按工号查询-1/按姓名查询-2");
        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                System.out.println("请输入用户工号");
                scanner = new Scanner(System.in);
                int usrId = scanner.nextInt();
                rs = stmt.executeQuery("SELECT * FROM employee WHERE eno =" + usrId);
                int count = 0;
                while (rs.next()) {
                    count = rs.getInt(1);
                }
                if (count == 0) {
                    System.out.println("不存在工号为" + usrId + "的用户！");
                    return;
                }
                rs = stmt.executeQuery("SELECT * FROM attendance WHERE eno=" + usrId);
                break;
            case 2:
                System.out.println("请输入用户姓名");
                scanner = new Scanner(System.in);
                String name = scanner.next();
                rs = stmt.executeQuery("SELECT eno FROM employee WHERE ename ='" + name + "'");
                String eno = null;
                while (rs.next()) {
                    eno = rs.getString(1);
                }
                if (eno == null) {
                    System.out.println("不存在名字为" + name + "的用户！");
                    return;
                }
                rs = stmt.executeQuery("SELECT * FROM attendance WHERE eno=" + eno);

                break;
            default:
                System.out.println("请输入1/2");
                return;
        }
        while (rs.next()) {
            System.out.println("工号: " + rs.getString(1));
            System.out.println("日期: " + rs.getDate(2));
            System.out.println("签到时间: " + rs.getTime(3));
            System.out.println("签退时间: " + rs.getTime(4));
            String state = null;
            if (rs.getInt(8) == 2) {
                state = "请假";
            } else if (rs.getInt(8) == 3) {
                state = "出差";
            } else {
                if (rs.getInt(7) == 0) {
                    state = "旷工";
                } else {
                    if (rs.getInt(5) == 0) {
                        state += "迟到";
                    }
                    if (rs.getInt(6) == 0) {
                        state += "早退";
                    }
                }
            }
            System.out.println("状态： " + state);
            System.out.println();
        }
    }

    /**
     * 管理日志信息
     *
     * @throws Exception
     */
    private void manageLog() throws Exception {
        boolean execute = true;
        while (execute) {
            System.out.println("请选择操作：\n1. 查看日志\n2. 删除日志\n3. 退出系统\n其他  返回上级菜单");
            Scanner in = new Scanner(System.in);
            String input = in.nextLine();
            switch (input) {
                case "1":
                    viewLog();
                    break;
                case "2":
                    deleteLog();
                    break;
                case "3":
                    System.exit(0);
                    break;
                default:
                    return;
            }
        }
    }

    /**
     * 查看日志信息
     *
     * @throws Exception
     */
    private void viewLog() throws Exception {
        ResultSet rs = stmt.executeQuery("SELECT * FROM log");
        System.out.println("日期        时间      工号   操作");

        while (rs.next()) {
            System.out.print(rs.getDate(1) + " ");
            System.out.print(rs.getTime(2) + " ");
            System.out.print(rs.getString(3) + " ");
            System.out.print(rs.getString(4) + "\n");
        }
        System.out.println();
    }

    /**
     * 删除日志信息
     *
     * @throws Exception
     */
    private void deleteLog() throws Exception {
        System.out.println("请输入日期、时间、工号");
        Scanner scanner = new Scanner(System.in);
        String date = scanner.next();
        String time = scanner.next();
        String eno = scanner.next();
        /*Date jdate = new java.util.Date();
        java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("HH:mm:ss");
        String time1 = f.format(jdate);*/
        ResultSet rs = stmt.executeQuery("SELECT * FROM log WHERE ldate ='" + date + "' AND ltime='" + time + "' AND eno='" + eno + "'");
        int count = 0;
        while (rs.next()) {
            count = rs.getInt(1);
        }
        if (count == 0) {
            System.out.println("不存在该日志！");
            return;
        }
        String sql = "DELETE FROM log WHERE ldate ='" + date + "' AND ltime='" + time + "' AND eno='" + eno + "'";
        stmt.executeUpdate(sql);
        System.out.println("删除日志成功！");
    }

    /**
     * 写日志
     *
     * @param log 操作内容
     * @throws Exception
     */
    void writelog(String log) throws Exception {
        //write log
        Date date = new java.util.Date();
        java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("HH:mm:ss");
        String nowTime = f.format(date);
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        String sql = "insert into log(ldate, ltime, eno, operation) VALUES ('" + sqlDate + "', '" + nowTime + "', '" + id + "' ,'" + log + "')";
        stmt.executeUpdate(sql);
    }

    /**
     * 系统设置管理
     *
     * @throws Exception
     */
    private void setSystem() throws Exception {
        boolean execute = true;
        while (execute) {
            System.out.println("请选择操作：\n1. 上下班时间管理\n2. 放假日期管理\n3. 退出系统\n其他  返回上级菜单");
            Scanner in = new Scanner(System.in);
            String input = in.nextLine();
            switch (input) {
                case "1":
                    manageTime();
                    break;
                case "2":
                    manageVacation();
                    break;
                case "3":
                    System.exit(0);
                    break;
                default:
                    return;
            }
        }
    }

    /**
     * 上下班时间管理
     *
     * @throws Exception
     */
    private void manageTime() throws Exception {
        boolean execute = true;
        while (execute) {
            System.out.println("请选择操作：\n1. 查看上下班时间\n2. 修改上班时间\n3. 修改下班时间\n4. 退出系统\n其他  返回上级菜单");
            Scanner in = new Scanner(System.in);
            String input = in.nextLine();
            switch (input) {
                case "1":
                    viewTime();
                    break;
                case "2":
                    modifyTime(1);
                    break;
                case "3":
                    modifyTime(2);
                    break;
                case "4":
                    System.exit(0);
                    break;
                default:
                    return;
            }
        }
    }

    /**
     * 查看上下班时间
     *
     * @throws Exception
     */
    private void viewTime() throws Exception {
        ResultSet rs = stmt.executeQuery("SELECT stime, etime FROM time");
        if (rs.next()) {
            System.out.println("上班时间：" + rs.getTime(1));
            System.out.println("下班时间：" + rs.getTime(2));
        }
        System.out.println();
    }

    /**
     * 修改上下班时间
     *
     * @param num 1：上班 2：下班
     * @throws Exception
     */
    private void modifyTime(int num) throws Exception {
        String choice = null;
        String out = null;
        if (num == 1) {
            choice = "stime";
            out = "上班时间";
        } else if (num == 2) {
            choice = "etime";
            out = "下班时间";
        }
        System.out.println("请输入修改后的时间（HH:mm:ss格式）");
        Scanner in = new Scanner(System.in);
        String input = in.nextLine();
        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
        java.util.Date d = format.parse(input);
        java.sql.Time time = new java.sql.Time(d.getTime());

        String sql = "update time SET " + choice + "='" + time + "'";
        stmt.execute(sql);
        System.out.println(out + "修改成功!");
    }

    /**
     * 管理放假时间
     *
     * @throws Exception
     */
    private void manageVacation() throws Exception {
        boolean execute = true;
        while (execute) {
            System.out.println("请选择操作：\n1. 查看放假日期\n2. 设置放假日期\n3. 设置上班日期\n4. 退出系统\n其他  返回上级菜单");
            Scanner in = new Scanner(System.in);
            String input = in.nextLine();
            switch (input) {
                case "1":
                    viewVacation();
                    break;
                case "2":
                    modifyVacation(1);
                    break;
                case "3":
                    modifyVacation(2);
                    break;
                case "4":
                    System.exit(0);
                    break;
                default:
                    return;
            }
        }
    }

    /**
     * 查看放假日期
     *
     * @throws Exception
     */
    private void viewVacation() throws Exception {
        System.out.println("放假日期：");
        ResultSet rs = stmt.executeQuery("SELECT vdate FROM vacation WHERE vacation=1");
        while (rs.next()) {
            System.out.println(rs.getDate(1));
        }
        System.out.println();
    }

    /**
     * 设置放假/上班日期
     *
     * @param num 1：设置放假日期 2：设置上班日期
     */
    private void modifyVacation(int num) throws Exception {
        int choice = -1;
        String out = null;
        if (num == 1) {
            choice = 1;
            out = "放假日期";
        } else if (num == 2) {
            choice = 0;
            out = "上班日期";
        }
        System.out.println("请输入" + out + "（yyyy-MM-dd格式）");
        Scanner in = new Scanner(System.in);
        String input = in.nextLine();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date d = format.parse(input);
        java.sql.Date vdate = new java.sql.Date(d.getTime());
        String sql = "update vacation SET vacation ='" + choice + "'WHERE vdate = '" + vdate + "'";
        stmt.execute(sql);
        System.out.println(out + "设置成功!");
    }
}
