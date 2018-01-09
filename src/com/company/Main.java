package com.company;

import java.sql.*;
import java.util.Objects;
import java.util.Scanner;


public class Main {
    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

    // Database credentials
    static final String USER = "root";
    static final String PASS = "123456";
    public static void main(String[] args) {
        int id = 0;
        String passwd;
        int character = -1;
        String name = "";
        boolean login = false;

        try {
            // Register JDBC driver
            Class.forName(JDBC_DRIVER);

            // Execute a query
            Connection ss = DriverManager.getConnection("jdbc:mysql://localhost:3306/attendance_management_system?useUnicode=true&characterEncoding=UTF-8", USER, PASS);
            Statement stmt = ss.createStatement();

            Scanner in = new Scanner(System.in);
            System.out.println("欢迎来到员工考勤系统！请登陆系统！");

            while(!login) {
                System.out.println("请输入您的工号！（提示：5位数字）");
                id = Integer.parseInt(in.nextLine());
                System.out.println("请输入密码：");
                passwd = in.nextLine();
                String passwdDB;

                String sql = "SELECT ename,password,role FROM employee WHERE eno=" + id;
                ResultSet rs = stmt.executeQuery(sql);
                if(rs.next()){
                    passwdDB = rs.getString("password");
                    if(Objects.equals(passwdDB, passwd)){
                        login = true;
                        name = rs.getString("ename");
                        character = rs.getInt("role");
                    }else{
                        System.out.println("账号或密码错误!");
                    }
                }else{
                    System.out.println("工号不存在！");
                }
            }

            // 登陆成功
            java.sql.Date sqlDate=new java.sql.Date(new java.util.Date().getTime());
            String sql = "SELECT vacation FROM vacation WHERE vdate="+sqlDate;
            ResultSet rs = stmt.executeQuery(sql);

            String state="";
            if(rs.next()) {
                if (rs.getInt("vacation") == 0) {
                    state = "工作日";
                } else {
                    state = "公休日";
                }
            }
            System.out.println("您好 " + name +" ! 今天是"+sqlDate+" "+state);

            while(true) {
                System.out.println("请选择身份操作：（输入数字）");
                int exeIdentity = -1;//1-员工 2-部门经理 3-人事经理 4-系统管理员
                switch (character) {
                    case 1:
                        while(exeIdentity == -1) {
                            System.out.println("1. 员工系统\r\n2. 退出系统");
                            int input = in.nextInt();
                            if (input == 1) {
                                exeIdentity = 1;
                            }else if(input == 2){
                                System.exit(0);
                            }else{
                                System.out.println("不合法输入！");
                            }
                        }
                        break;
                    case 2:
                        while(exeIdentity == -1) {
                            System.out.println("1. 员工系统\r\n2. 部门经理\r\n3. 退出");
                            int input = in.nextInt();
                            if(input == 1){
                                exeIdentity = 1;
                            }else if(input == 2){
                                exeIdentity = 2;
                            }else if(input == 3){
                                System.exit(0);
                            }else{
                                System.out.println("不合法输入!");
                            }
                        }
                        break;
                    case 3:
                        while(exeIdentity == -1) {
                            System.out.println("1. 员工系统\r\n2. 人事经理\r\n3. 退出");
                            int input = in.nextInt();
                            if(input == 1){
                                exeIdentity = 1;
                            }else if(input == 2){
                                exeIdentity = 3;
                            }else if(input == 3){
                                System.exit(0);
                            }else{
                                System.out.println("不合法输入！");
                            }
                        }
                        break;
                    case 4:
                        while(exeIdentity == -1) {
                            System.out.println("1. 系统管理员\r\n2. 退出");
                            int input = in.nextInt();
                            if(input == 1){
                                exeIdentity = 4;
                            }else if(input == 2){
                                System.exit(0);
                            }else{
                                System.out.println("不合法输入！");
                            }
                        }
                        break;
                }

                switch(exeIdentity){
                    case 1:
                        Employee employee = new Employee(id, stmt, state);
                        employee.run();
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                    case 4:
                        Administrator administrator=new Administrator(id, stmt);
                        administrator.run();
                        break;
                }
            }
        }catch(Exception e){
            System.out.println("输入不合法！");
            e.printStackTrace();
        }
    }
}
