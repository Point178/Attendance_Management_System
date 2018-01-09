package com.company;

import java.sql.ResultSet;
import java.sql.Statement;
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
                System.out.println("请选择操作：\n1. 管理用户信息\n2. 管理考勤信息\n3. 管理日志信息\n4. 退出系统\n其他  返回上级菜单");
                Scanner in = new Scanner(System.in);
                String input = in.nextLine();
                switch (input) {
                    case "1":
                        manageUser();
                        break;
                    case "2":
                        manageAttendance();
                        break;
                    case "3":
                        manageLog();
                        break;
                    case "4":
                        System.exit(0);
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
        String sql=null;
        switch (usrRole){
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
        if (sql!=null){
            stmt.executeUpdate(sql);
        }
        System.out.println("增加用户成功！");
    }

    private void deleteUser() throws Exception{
        System.out.println("请输入用户工号");
        Scanner scanner = new Scanner(System.in);
        int usrId=scanner.nextInt();
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
        System.out.println("删除用户成功！");

    }

    private void modifyUser() throws Exception{
        System.out.println("请输入用户工号");
        Scanner scanner = new Scanner(System.in);
        int usrId=scanner.nextInt();
        ResultSet rs = stmt.executeQuery("SELECT * FROM employee WHERE eno =" + usrId);
        int count = 0;
        while (rs.next()) {
            count = rs.getInt(1);
        }
        if (count == 0) {
            System.out.println("不存在工号为" + usrId + "的用户！");
            return;
        }
        Employee employee=new Employee(usrId,stmt,"");
        employee.changePasswd();
    }

    private void viewUser() throws Exception{
        System.out.println("请输入用户工号");
        Scanner scanner = new Scanner(System.in);
        int usrId=scanner.nextInt();
        ResultSet rs = stmt.executeQuery("SELECT * FROM employee WHERE eno =" + usrId);
        int count = 0;
        while (rs.next()) {
            count = rs.getInt(1);
        }
        if (count == 0) {
            System.out.println("不存在工号为" + usrId + "的用户！");
            return;
        }
        rs = stmt.executeQuery("SELECT * FROM employee WHERE eno=" + usrId);
        while (rs.next()){
            System.out.print("eno: "+rs.getString(1) + "\n");
            System.out.print("ename: "+rs.getString(2) + "\n");
            System.out.print("pasword: "+rs.getString(3) + "\n");
            System.out.print("role: "+rs.getInt(4) + "\n");
            System.out.println();
        }
    }
    private void manageAttendance() {

    }

    private void manageLog() {

    }
}
