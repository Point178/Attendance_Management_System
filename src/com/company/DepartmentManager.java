package com.company;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class DepartmentManager {
    private String manager_eno;
    private String manager_dno;
    private Statement stmt;

    public DepartmentManager(int id, Statement stmt) throws Exception {
        this.manager_eno = id + "";
        this.stmt = stmt;
        this.manager_dno = check_dno(manager_eno);
    }

    public void run() {
        try {
            boolean execute = true;
            while (execute) {
                System.out.println("请选择操作：\n" +
                        "1. 查看某员工考勤情况\n" +
                        "2. 定向查找考勤信息\n" +
                        "3. 查看已审批的所有请假或出差申请的历史记录\n" +
                        "4. 审批请假或出差申请\n" +
                        "5. 退出系统\n" +
                        "其他  返回上级菜单");
                Scanner in = new Scanner(System.in);
                String input = in.nextLine();
                switch (input) {
                    case "1":
                        view_attendance();
                        break;
                    case "2":
                        lookup_attendance();
                        break;
                    case "3":
                        view_application();
                        break;
                    case "4":
                        approve_application();
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
            System.out.println("输入不合法！");
        }
    }

    /**
     * 根据姓名或员工编号查看某个员工的考勤信息
     */
    public void view_attendance() throws Exception {
        ResultSet rs;
        System.out.println("请选择按工号查询-1/按姓名查询-2");
        Scanner scanner = new Scanner(System.in);
        String choice = scanner.nextLine();
        String employee_eno = null;
        switch (choice) {
            case "1":
                System.out.println("请输入员工工号");
                scanner = new Scanner(System.in);
                int usrId = scanner.nextInt();
                rs = stmt.executeQuery("SELECT * FROM employee WHERE eno =" + usrId);
                int count = 0;
                while (rs.next()) {
                    count = rs.getInt(1);
                }
                if (count == 0) {
                    System.out.println("不存在工号为" + usrId + "的员工！");
                    return;
                }
                employee_eno = usrId + "";
                break;
            case "2":
                System.out.println("请输入员工姓名");
                scanner = new Scanner(System.in);
                String name = scanner.next();
                rs = stmt.executeQuery("SELECT eno FROM employee WHERE ename ='" + name + "'");
                while (rs.next()) {
                    employee_eno = rs.getString(1);
                }
                if (employee_eno == null) {
                    System.out.println("不存在名字为" + name + "的员工！");
                    return;
                }
                break;
            default:
                System.out.println("您还没有输入工号或姓名: 1/2");
                return;
        }
        //判断这个员工是否是自己部门下的员工，否则没有查看的权限
        if (!check_dno(employee_eno).equals(check_dno(manager_eno))) {
            System.out.println("对不起，这个员工不存在或者不属于您的部门，您不能查看他的考勤信息");
            return;
        }
        java.sql.Date today = new java.sql.Date(new java.util.Date().getTime());
        rs = stmt.executeQuery("SELECT * FROM attendance WHERE adate < '" + today +
                "' AND eno=" + employee_eno);
        while (rs.next()) {
            System.out.print("工号: " + rs.getString(1));
            System.out.print("  日期: " + rs.getDate(2));
            System.out.print("  签到: " + (rs.getTime(3) == null ? "        " : rs.getTime(3)));
            System.out.print("  签退: " + (rs.getTime(4) == null ? "        " : rs.getTime(4)));
            String state = null;
            if (rs.getInt(8) == 2) {
                state = "请假";
            } else if (rs.getInt(8) == 3) {
                state = "出差";
            } else {
                state = "在公司";
                if (rs.getInt(7) == 0) {
                    state = "(旷工)";
                } else {//没有旷工
                    if (rs.getInt(5) == 0) {
                        state += "(迟到)";
                    }
                    if (rs.getInt(6) == 0) {
                        state += "(早退)";
                    }
                }
            }
            System.out.println("  状态： " + state);
        }
        System.out.println();
    }

    /**
     * 根据考勤的不同状态和日期进行查找
     *
     * @throws Exception
     */
    public void lookup_attendance() throws Exception {
        System.out.println("您可以根据日期和上班状态进行考勤信息的筛选和查看，1：日期；2：上班状态");
        Scanner scanner = new Scanner(System.in);
        int choose = scanner.nextInt();
        if (choose == 1) {
            System.out.println("您可以查看某个日期范围内部门里所有员工的早退/迟到天数");
            System.out.println("请输入起始日期：");
            scanner = new Scanner(System.in);
            String start = scanner.nextLine();
            System.out.println("请输入终止日期：");
            scanner = new Scanner(System.in);
            String end = scanner.nextLine();
            //打印早退排序结果
            ResultSet rs = stmt.executeQuery("SELECT eno,COUNT(`leave`) AS early_leave_days FROM attendance NATURAL JOIN belong" +
                    "  WHERE `leave` = '1' AND belong.dno = " + manager_dno +
                    " AND adate > '" + start + "' AND adate < '" + end + "'" +
                    " GROUP BY eno" +
                    " ORDER BY early_leave_days DESC ");
            System.out.println("工号-------早退天数");
            while (rs.next()) {
                System.out.print(rs.getString("eno") + "        ");
                System.out.println(rs.getString("early_leave_days"));
            }
            //打印迟到排序结果
            rs = stmt.executeQuery("SELECT eno,COUNT(arrive) AS arrive_late_days FROM attendance NATURAL JOIN belong" +
                    "  WHERE arrive = '1' AND belong.dno = " + manager_dno +
                    " AND adate > '" + start + "' AND adate < '" + end + "'" +
                    " GROUP BY eno" +
                    " ORDER BY arrive_late_days DESC ");
            System.out.println("工号-------迟到天数");
            while (rs.next()) {
                System.out.print(rs.getString("eno") + "        ");
                System.out.println(rs.getString("arrive_late_days"));
            }
        } else if (choose == 2) {
            System.out.println("请输入筛选条件： 1. 旷工  2. 没有旷工  3. 在公司  4. 请假  5. 出差");
            scanner = new Scanner(System.in);
            String condition1 = scanner.nextLine();
            String condition;
            String condition2;
            switch (condition1) {
                case "1":
                    condition = "absent=0";
                    condition2 = "旷工";
                    break;
                case "2":
                    condition = "absent=1";
                    condition2 = "没有旷工";
                    break;
                case "3":
                    condition = "astate=1";
                    condition2 = "在公司";
                    break;
                case "4":
                    condition = "astate=2";
                    condition2 = "请假";
                    break;
                default:
                    condition = "astate=3";
                    condition2 = "出差";
            }

            if (!condition.isEmpty()) {
                java.sql.Date today = new java.sql.Date(new java.util.Date().getTime());
                ResultSet rs = stmt.executeQuery("SELECT * FROM attendance natural join belong" +
                        " WHERE belong.dno = '" + manager_dno + "' AND  adate < '" + today + "'" +
                        " AND " + condition);
                System.out.println("以下是按照条件  " + condition2 + "  查找的考勤信息：");
                while (rs.next()) {
                    System.out.print("工号: " + rs.getString(1));
                    System.out.print("   日期: " + rs.getDate(2));
                    System.out.print("  签到: " + (rs.getTime(3) == null ? "        " : rs.getTime(3)));
                    System.out.print("  签退: " + (rs.getTime(4) == null ? "        " : rs.getTime(4)));
                    String state = null;
                    if (rs.getInt(8) == 2) {
                        state = " 请假";
                    } else if (rs.getInt(8) == 3) {
                        state = " 出差";
                    } else {
                        state = "在公司";
                        if (rs.getInt(7) == 0) {
                            state = " (旷工)";
                        } else {//没有旷工
                            if (rs.getInt(5) == 0) {
                                state += "(迟到)";
                            }
                            if (rs.getInt(6) == 0) {
                                state += "(早退)";
                            }
                        }
                    }
                    System.out.println("  状态： " + state);
                }
            }
        } else
            System.out.println("输入不合法！");
    }

    /**
     * 查看部门里所有的请假或出差的申请的历史记录
     */
    public void view_application() throws Exception {
        System.out.println("您可以查看您部门下员工所有的请假或出差申请，1：请假，2：出差");
        Scanner scanner = new Scanner(System.in);
        ResultSet rs;
        switch (scanner.nextLine()) {
            case "1":
                String sql = "SELECT * FROM `leave` natural join checkleave" +
                        " WHERE checkleave.dno = " + manager_dno +
                        " AND checkleave.lstate != '1' ";//所有不是待审批状态的记录
                rs = stmt.executeQuery(sql);
                System.out.println("以下是您审批的部门下所有员工的请假信息");
                System.out.println("序号--------员工工号-----开始日期-----结束日期------类型--审批状态-----拒绝理由");
                String line;
                while (rs.next()) {
                    line = (rs.getString("lno") == null ? "无" : rs.getString("lno")) + "   ";
                    line += (rs.getString("eno") == null ? "无" : rs.getString("eno")) + "    ";
                    line += (rs.getString("lsdate") == null ? "无" : rs.getString("lsdate")) + "  ";
                    line += (rs.getString("ledate") == null ? "无" : rs.getString("ledate")) + "    ";
                    line += check_leave_type(rs.getInt("ltype")) + "    ";
                    line += check_state(rs.getInt("lstate")) + "        ";
                    line += (rs.getString("lrefuse") == null ? "无" : rs.getString("lrefuse"));
                    System.out.println(line);
                }
                break;
            case "2":
                sql = "SELECT * FROM trip natural join checktrip" +
                        " WHERE checktrip.dno = " + manager_dno +
                        " AND checktrip.tstate != '1' ";
                rs = stmt.executeQuery(sql);
                System.out.println("以下是您审批的部门下所有员工的出差信息");
                System.out.println("序号--------员工工号-----开始日期-----结束日期--------类型---审批状态-----拒绝理由");
                while (rs.next()) {
                    line = (rs.getString("tno") == null ? "无" : rs.getString("tno")) + "   ";
                    line += (rs.getString("eno") == null ? "无" : rs.getString("eno")) + "    ";
                    line += (rs.getString("tsdate") == null ? "无" : rs.getString("tsdate")) + "  ";
                    line += (rs.getString("tedate") == null ? "无" : rs.getString("tedate")) + "    ";
                    line += check_trip_type(rs.getInt("ttype")) + "   ";
                    line += check_state(rs.getInt("tstate")) + "        ";
                    line += (rs.getString("trefuse") == null ? "无" : rs.getString("trefuse"));
                    System.out.println(line);
                }
                break;
            default:
                System.out.println("您没有输入您的选择————1：请假查询，2：出差查询！");
        }
        System.out.println();
    }

    /**
     * 审批部门里员工的请假和出差申请
     *
     * @throws Exception
     */
    public void approve_application() throws Exception {
        System.out.println("您可以审批您部门里员工的请假和出差申请————1：请假申请；2：出差申请");
        Scanner scanner = new Scanner(System.in);
        switch (scanner.nextLine()) {
            case "1":
                ResultSet rs = stmt.executeQuery("SELECT * FROM checkleave natural join `leave`" +
                        " WHERE dno = " + manager_dno +
                        " AND lstate = 1 ");//待审批的申请
                String line;
                System.out.println("以下是您部门里待处理的请假申请：");
                System.out.println("序号--------员工工号-----开始日期-----结束日期------类型---审批状态-----拒绝理由");
                //System.out.println("120180112---10001----2018-01-12--2018-01-14----产假---待审和--------无");
                while (rs.next()) {
                    line = (rs.getString("lno") == null ? "无" : rs.getString("lno")) + "   ";
                    line += (rs.getString("eno") == null ? "无" : rs.getString("eno")) + "    ";
                    line += (rs.getString("lsdate") == null ? "无" : rs.getString("lsdate")) + "  ";
                    line += (rs.getString("ledate") == null ? "无" : rs.getString("ledate")) + "    ";
                    line += check_leave_type(rs.getInt("ltype")) + "   ";
                    line += check_state(rs.getInt("lstate")) + "        ";
                    line += (rs.getString("lrefuse") == null ? "无" : rs.getString("lrefuse"));
                    System.out.println(line);
                }
                System.out.println("您想要审批他们的申请吗？1：是；0：否");
                scanner = new Scanner(System.in);
                if (scanner.nextInt() == 1) {
                    System.out.println("请输入想要审批的申请的序号:");
                    scanner = new Scanner(System.in);
                    int lno = scanner.nextInt();
                    System.out.println("请输入审批状态(1:待审批2:同意3:驳回):");
                    scanner = new Scanner(System.in);
                    int lstate = scanner.nextInt();
                    System.out.println("请输入拒绝理由(若同意或无理由请不输入):");
                    scanner = new Scanner(System.in);
                    String lrefuse = scanner.nextLine();
                    while (lrefuse.isEmpty()) {
                        if (lstate == 3) {
                            System.out.println("您拒绝了他的申请，必须填写拒绝理由↓");
                            scanner = new Scanner(System.in);
                            lrefuse = scanner.nextLine();
                        } else
                            break;
                    }
                    stmt.execute(" update checkleave set lstate='" + lstate + "' ,lrefuse='" + lrefuse + "' where lno=" + lno);
                    System.out.println("审批成功！");
                    //写入日志
                    if (lstate == 2)
                        writelog("approve_leave");
                    else if (lstate == 3)
                        writelog("reject_leave");
                    if (lstate == 2) {
                        //查询要被更新的请假条目的起始日期和终止日期
                        java.sql.Date lsdate = null, ledate = null;
                        String eno = null;
                        rs = stmt.executeQuery("SELECT lsdate,ledate,eno FROM `leave` WHERE lno=" + lno);
                        while (rs.next()) {
                            lsdate = rs.getDate("lsdate");
                            ledate = rs.getDate("ledate");
                            eno = rs.getString("eno");
                        }
                        //把请假日期之内的每一天的考勤信息都设置成请假状态
                        int days = (int) (ledate.getTime() - lsdate.getTime()) / 86400000 + 1;
                        String sql;
                        for (int i = 0; i < days; i++) {
                            java.sql.Date tmp = new java.sql.Date(lsdate.getTime() + 86400000 * i);
                            //判断这一天是否已经请假或出差
                            sql = "SELECT * FROM attendance WHERE adate = '" + tmp + "' AND astate != 1";
                            rs = stmt.executeQuery(sql);
                            if (rs.next())//next有值返回true
                                sql = "UPDATE attendance SET astate ='" + 2 + "' WHERE adate =" + tmp;
                            else
                                sql = "INSERT INTO attendance(eno,adate,astate) VALUES ('" + eno + "', '" + tmp + "', '" + 2 + "')";
                            stmt.execute(sql);
                        }
                    }
                    System.out.println();
                } else
                    return;
                break;
            case "2":
                rs = stmt.executeQuery("SELECT * FROM checktrip natural join trip" +
                        " WHERE dno = " + manager_dno +
                        " AND tstate = 1 ");//待审批或者被驳回的申请
                System.out.println("以下是您部门里没有成功或待处理的出差申请：");
                System.out.println("序号--------员工工号-----开始日期-----结束日期--------类型---审批状态-----拒绝理由");
                //System.out.println("120180112---10001----2018-01-12--2018-01-14---公司指派---待审和--------无");
                while (rs.next()) {
                    line = (rs.getString("tno") == null ? "无" : rs.getString("tno")) + "   ";
                    line += (rs.getString("eno") == null ? "无" : rs.getString("eno")) + "    ";
                    line += (rs.getString("tsdate") == null ? "无" : rs.getString("tsdate")) + "  ";
                    line += (rs.getString("tedate") == null ? "无" : rs.getString("tedate")) + "    ";
                    line += check_trip_type(rs.getInt("ttype")) + "   ";
                    line += check_state(rs.getInt("tstate")) + "        ";
                    line += (rs.getString("trefuse") == null ? "无" : rs.getString("trefuse"));
                    System.out.println(line);
                }
                System.out.println("您想要审批他们的申请吗？1：是；0：否");
                scanner = new Scanner(System.in);
                if (scanner.nextInt() == 1) {
                    System.out.println("请输入想要审批的申请的序号:");
                    scanner = new Scanner(System.in);
                    int tno = scanner.nextInt();
                    System.out.println("请输入审批状态(1:待审批2:同意3:驳回):");
                    scanner = new Scanner(System.in);
                    int tstate = scanner.nextInt();
                    System.out.println("请输入拒绝理由(若同意或无理由请不输入):");
                    scanner = new Scanner(System.in);
                    String trefuse = scanner.nextLine();
                    while (trefuse.isEmpty()) {
                        if (tstate == 3) {
                            System.out.println("您拒绝了他的申请，必须填写拒绝理由:");
                            scanner = new Scanner(System.in);
                            trefuse = scanner.nextLine();
                        } else
                            break;
                    }
                    stmt.execute(" update checktrip set tstate=" + tstate + " ,trefuse='" + trefuse + "' where tno=" + tno);
                    System.out.println("审批成功！");
                    //写入日志
                    if (tstate == 2)
                        writelog("approve_trip");
                    else if (tstate == 3)
                        writelog("reject_trip");
                    if (tstate == 2) {
                        //查询要被更新的出差条目的起始日期和终止日期
                        java.sql.Date tsdate = null, tedate = null;
                        String eno = null;
                        rs = stmt.executeQuery("SELECT tsdate,tedate,eno FROM trip WHERE tno=" + tno);
                        while (rs.next()) {
                            tsdate = rs.getDate("tsdate");
                            tedate = rs.getDate("tedate");
                            eno = rs.getString("eno");
                        }
                        //把请假日期之内的每一天的考勤信息都设置成请假状态
                        int days = (int) (tedate.getTime() - tsdate.getTime()) / 86400000 + 1;
                        String sql;
                        for (int i = 0; i < days; i++) {
                            java.sql.Date tmp = new java.sql.Date(tsdate.getTime() + 86400000 * i);
                            //判断这一天是否已经请假或出差
                            sql = " SELECT * FROM attendance WHERE adate = '" + tmp + "' AND astate != 1";
                            rs = stmt.executeQuery(sql);
                            if (rs.next())//next有值返回true
                                sql = "UPDATE attendance SET astate ='" + 3 + "' WHERE adate =" + tmp;
                            else
                                sql = "INSERT INTO attendance(eno,adate,astate) VALUES ('" + eno + "', '" + tmp + "', '" + 3 + "')";
                            stmt.execute(sql);
                        }
                    }
                    System.out.println();
                }
                break;
            default:
                System.out.println("输入不合法！");
        }
    }

    void writelog(String log) throws Exception {
        Date date = new java.util.Date();
        java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("HH:mm:ss");
        String nowTime = f.format(date);
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        String sql = "insert  into log(ldate, ltime, eno, operation) VALUES ('" + sqlDate + "', '" + nowTime + "', '" + manager_eno + "' ,'" + log + "')";
        stmt.executeUpdate(sql);
    }

    public String check_dno(String eno) throws Exception {
        ResultSet rs = stmt.executeQuery("SELECT * FROM belong WHERE eno ='" + eno + "'");
        String dno = "";
        while (rs.next())
            dno = rs.getString(2);
        return dno;
    }

    public static String check_leave_type(int type) {
        switch (type) {
            case 1:
                return "事假";
            case 2:
                return "病假";
            case 3:
                return "产假";
            case 4:
                return "婚假";
            case 5:
                return "其他";
            default:
                return "无";
        }
    }

    public static String check_state(int state) {
        switch (state) {
            case 1:
                return "待审核";
            case 2:
                return "同意";
            case 3:
                return "驳回";
            default:
                return "无";
        }
    }

    public static String check_trip_type(int type) {
        switch (type) {
            case 1:
                return "公司指派";
            case 2:
                return "个人申请";
            default:
                return "无";
        }
    }
}