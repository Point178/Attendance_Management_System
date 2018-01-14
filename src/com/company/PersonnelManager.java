package com.company;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.Scanner;

public class PersonnelManager {
    private String manager_eno;
    private Statement stmt;

    public PersonnelManager(int id, Statement stmt) throws Exception{
        this.manager_eno = id+"";
        this.stmt = stmt;
    }

    public void run(){
        try {
            boolean execute = true;
            while (execute) {
                System.out.println("请选择操作：\n" +
                        "1. 按个人（根据姓名或工号）查看某员工考勤情况\n" +
                        "2. 按部门（根据部门名称或编号）查看部门里所有员工考勤情况\n" +
                        "3. 查看所有的已审批的差假申请的历史记录\n" +
                        "4. 审批部门主管的差假申请\n" +
                        "5. 退出系统\n" +
                        "其他  返回上级菜单");
                Scanner in = new Scanner(System.in);
                String input = in.nextLine();
                switch (input) {
                    case "1":
                        view_attendance();
                        break;
                    case "2":
                        view_department_attendance();
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
    public void view_attendance() throws Exception{
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
                String count = null;
                while (rs.next()) {
                    count = rs.getString(1);
                }
                if (count == null) {
                    System.out.println("不存在工号为" + usrId + "的员工！");
                    System.out.println();
                    return;
                }
                employee_eno = usrId+"";
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
                    System.out.println();
                    return;
                }
                break;
            default:
                System.out.println("您还没有输入工号或姓名: 1/2");
                return;
        }
        java.sql.Date today = new java.sql.Date(new java.util.Date().getTime());
        rs = stmt.executeQuery("SELECT * FROM attendance WHERE adate < '" + today+
                "' AND eno=" + employee_eno);
        while (rs.next()) {
            System.out.print("工号: " + rs.getString(1));
            System.out.print("  日期: " + rs.getDate(2));
            System.out.print("  签到: " + (rs.getTime(3)==null?"        ":rs.getTime(3)));
            System.out.print("  签退: " + (rs.getTime(4)==null?"        ":rs.getTime(4)));
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
     * 查看某个部门里所有员工的考勤信息
     * @throws Exception
     */
    public void view_department_attendance()throws Exception{
        System.out.println("您可以查看某个部门里所有员工的考勤情况,请输入部门名称或者编号");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        //判断是否存在这个部门
        ResultSet rs = stmt.executeQuery("SELECT dno FROM department WHERE dno = '"+input +"' OR dname = '"+input+"' ");//
        String dno = null;
        while (rs.next()) {
            dno = rs.getString(1);
        }
        if (dno == null) {
            System.out.println("不存在编号或者名称为" + input + "的部门！");
            System.out.println();
            return;
        }
        System.out.println("您可以按条件筛选考勤信息：1.按照旷班天数排序  2.所有");
        scanner = new Scanner(System.in);
        String sql;

        switch (scanner.nextLine()){
            case "1":
                java.sql.Date today = new java.sql.Date(new java.util.Date().getTime());
                sql = "SELECT eno,COUNT(`leave`) AS absent_days FROM attendance NATURAL JOIN belong" +
                        "  WHERE absent = '1' AND belong.dno = " + dno + " AND adate < '"+ today+
                        "'  GROUP BY eno" +
                        " ORDER BY absent_days DESC ";
                rs = stmt.executeQuery(sql);
                System.out.println("工号-------旷班天数");
                while (rs.next()){
                    System.out.print(rs.getString("eno") +"        ");
                    System.out.println(rs.getString("absent_days"));
                }
                break;
            case "2":
                today = new java.sql.Date(new java.util.Date().getTime());
                sql = "SELECT * FROM attendance natural join belong WHERE adate < '" + today +"'"+
                        " AND dno=" + dno;
                //打印该部门下所有员工的考勤信息
                rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    System.out.print("工号: " + rs.getString(1));
                    System.out.print(" 日期: " + rs.getDate(2));
                    System.out.print("  签到: " + (rs.getTime(3)==null?"        ":rs.getTime(3)));
                    System.out.print("  签退: " + (rs.getTime(4)==null?"        ":rs.getTime(4)));
                    String state = null;
                    if (rs.getInt(8) == 2) {
                        state = " 请假";
                    } else if (rs.getInt(8) == 3) {
                        state = " 出差";
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
                break;
            default:
                System.out.println("您没有选择筛选条件，已退出查询。");
        }
        System.out.println();
    }

    /**
     * 查看所有已批准或拒绝的审批历史记录
     */
    public void view_application() throws Exception {
        System.out.println("您可以查看所有的请假或出差申请，1：请假，2：出差");
        Scanner scanner = new Scanner(System.in);
        ResultSet rs;
        switch (scanner.nextLine()) {
            case "1":
                String sql = "SELECT * FROM `leave` natural join checkleave " +
                        " WHERE lstate != '1' ";
                rs = stmt.executeQuery(sql);
                System.out.println("以下是所有已拒绝或通过的请假申请的历史记录");
                System.out.println("序号--------员工工号-----开始日期-----结束日期------类型--审批状态-----拒绝理由");
                String line;
                while (rs.next()) {
                    line = (rs.getString("lno") == null ? "无" : rs.getString("lno")) +"   ";
                    line += (rs.getString("eno") == null ? "无" : rs.getString("eno")) +"    ";
                    line += (rs.getString("lsdate") == null ? "无" : rs.getString("lsdate")) +"  ";
                    line += (rs.getString("ledate") == null ? "无" : rs.getString("ledate")) +"    ";
                    line += DepartmentManager.check_leave_type(rs.getInt("ltype"))+"    ";
                    line += DepartmentManager.check_state(rs.getInt("lstate"))+"        ";
                    line += (rs.getString("lrefuse") == null ? "无" : rs.getString("lrefuse"));
                    System.out.println(line);
                }
                break;
            case "2":
                sql = "SELECT * FROM trip natural join checktrip" +
                        " WHERE tstate != '1' ";
                rs = stmt.executeQuery(sql);
                System.out.println("以下是已拒绝或通过的所有出差申请的历史记录");
                System.out.println("序号--------员工工号-----开始日期-----结束日期--------类型---审批状态-----拒绝理由");
                while (rs.next()) {line = (rs.getString("tno") == null ? "无" : rs.getString("tno")) +"   ";
                    line += (rs.getString("eno") == null ? "无" : rs.getString("eno")) +"    ";
                    line += (rs.getString("tsdate") == null ? "无" : rs.getString("tsdate")) +"  ";
                    line += (rs.getString("tedate") == null ? "无" : rs.getString("tedate")) +"    ";
                    line += DepartmentManager.check_trip_type(rs.getInt("ttype"))+"   ";
                    line += DepartmentManager.check_state(rs.getInt("tstate"))+"        ";
                    line += (rs.getString("trefuse") == null ? "无" : rs.getString("trefuse"));
                    System.out.println(line);
                }
                break;
            default:
                System.out.println("您没有输入您的选择————1：请假查询，2：出差查询");
        }
        System.out.println();
    }

    /**
     * 处理部门主管待审批的申请
     * @throws Exception
     */
    public void approve_application()throws Exception{
        System.out.println("您可以审批部门主管的请假和出差申请————1：请假申请；2：出差申请");
        Scanner scanner = new Scanner(System.in);
        switch (scanner.nextLine()){
            case "1":
                System.out.println("请输入部门编号/部门名称");
                scanner = new Scanner(System.in);
                String input = scanner.nextLine();
                //判断这个部门是否存在
                String sql = "SELECT mno,dno,dname FROM department WHERE dno = '"+input +"' OR dname = '"+input+"' ";
                ResultSet rs = stmt.executeQuery(sql);
                String dno = null,mno = null,dname = null;
                while (rs.next()) {
                    dno = rs.getString("dno");
                    dname = rs.getString("dname");
                    mno = rs.getString("mno");
                }
                if (dno == null) {
                    System.out.println("不存在编号或者名称为 " + input + " 的部门！");
                    System.out.println();
                    return;
                }
                //查看这个部门对应主管审批的申请
                rs = stmt.executeQuery("SELECT * FROM checkleave natural join `leave`" +
                        " WHERE dno = " + dno+
                        " AND eno = " + mno +
                        " AND lstate = 1 ");
                String line;
                System.out.println("以下是 "+dname+" 部门主管待处理的请假申请");
                System.out.println("序号----------主管工号---开始日期-------结束日期------类型---审批状态-----拒绝理由");
                while (rs.next()){
                    line = rs.getString("lno")+"     ";
                    line += mno+"   ";
                    line += rs.getString("lsdate")+"    ";
                    line += rs.getString("ledate")+"    ";
                    line += DepartmentManager.check_leave_type(rs.getInt("ltype")) +"    ";
                    line += DepartmentManager.check_state(rs.getInt("lstate"))+"        ";
                    line += (rs.getString("lrefuse") == null ? "无" : rs.getString("lrefuse"));
                    System.out.println(line);
                }
                System.out.println();
                System.out.println("您想要修改他的申请吗？1：是；0：否");
                scanner = new Scanner(System.in);
                if(scanner.nextInt() == 1){
                    System.out.println("请输入想要修改的申请的序号:");
                    scanner = new Scanner(System.in);
                    int lno = scanner.nextInt();
                    System.out.println("请输入审批状态(1:待审批2:同意3:驳回):");
                    scanner = new Scanner(System.in);
                    int lstate = scanner.nextInt();
                    System.out.println("请输入拒绝理由(若同意或无理由请不输入):");
                    scanner = new Scanner(System.in);
                    String lrefuse = scanner.nextLine();
                    while (lrefuse.isEmpty()){
                        if(lstate == 3) {
                            System.out.println(" 您拒绝了他的申请，必须填写拒绝理由:");
                            scanner = new Scanner(System.in);
                            lrefuse = scanner.nextLine();
                        }
                        else
                            break;
                    }
                    stmt.execute("update checkleave set lstate="+lstate+" ,lrefuse='"+lrefuse+"' where lno="+lno);
                    System.out.println("审批成功！");
                    //写入日志
                    if(lstate == 2)
                        writelog("approve_leave");
                    else if(lstate == 3)
                        writelog("reject_leave");
                    if(lstate == 2) {
                        //查询要被更新的请假条目的起始日期和终止日期
                        java.sql.Date lsdate = null, ledate = null;
                        sql = "SELECT lsdate,ledate FROM `leave` WHERE lno=" + lno;
                        rs = stmt.executeQuery(sql);
                        while (rs.next()) {
                            lsdate = rs.getDate("lsdate");
                            ledate = rs.getDate("ledate");
                        }
                        //把请假日期之内的每一天的考勤信息都设置成请假状态
                        int days = ((int) ((ledate.getTime() - lsdate.getTime()) / 86400000)) + 1;
                        for (int i = 0; i < days; i++) {
                            java.sql.Date tmp = new java.sql.Date(lsdate.getTime() + 86400000 * i);
                            //判断这一天是否已经请假或出差
                            sql = " SELECT * FROM attendance WHERE adate = '" + tmp + "' AND astate != 1";
                            rs = stmt.executeQuery(sql);
                            if (rs.next())//next有值返回true
                                sql = "UPDATE attendance SET astate ='" + 2 + "' WHERE adate =" + tmp;
                            else
                                sql = "INSERT INTO attendance(eno,adate,astate) VALUES ('" + mno + "', '" + tmp + "', '" + 2 + "')";
                            stmt.execute(sql);
                        }
                    }
                    System.out.println();
                }
                else
                    return;
                break;
            case "2":
                System.out.println("请输入部门编号/部门名称");
                scanner = new Scanner(System.in);
                input = scanner.nextLine();
                //判断这个部门是否存在
                sql = "SELECT mno,dno,dname FROM department WHERE dno = '"+input +"' OR dname = '"+input+"' ";
                rs = stmt.executeQuery(sql);
                dno = null;mno = null;dname = null;
                while (rs.next()) {
                    dno = rs.getString("dno");
                    dname = rs.getString("dname");
                    mno = rs.getString("mno");
                }
                if (dno == null) {
                    System.out.println("不存在编号或者名称为 " + input + " 的部门！");
                    System.out.println();
                    return;
                }
                rs = stmt.executeQuery("SELECT * FROM checktrip natural join trip " +
                        " WHERE dno = " + dno+
                        " AND eno = " + mno +
                        " AND tstate = 1 ");
                System.out.println("以下是 "+dname+" 部门主管待处理的出差申请：");
                System.out.println("序号--------主管工号-----开始日期-----结束日期-------类型---审批状态-----拒绝理由");
                while (rs.next()){
                    line = rs.getString("tno")+"   ";
                    line += mno+"   ";
                    line += rs.getString("tsdate")+"    ";
                    line += rs.getString("tedate")+"    ";
                    line += DepartmentManager.check_leave_type(rs.getInt("ttype")) +"    ";
                    line += DepartmentManager.check_state(rs.getInt("tstate"))+"        ";
                    line += (rs.getString("trefuse") == null ? "无" : rs.getString("trefuse"));
                    System.out.println(line);
                }
                System.out.println();
                System.out.println("您想要修改他们的申请吗？1：是；0：否");
                scanner = new Scanner(System.in);
                if(scanner.nextInt() == 1){
                    System.out.println("请输入想要修改的申请的序号:");
                    scanner = new Scanner(System.in);
                    String tno = scanner.nextLine();
                    System.out.println("请输入审批状态(1:待审批2:同意3:驳回):");
                    scanner = new Scanner(System.in);
                    int tstate = scanner.nextInt();
                    System.out.println("请输入拒绝理由(若同意或无理由请不输入):");
                    scanner = new Scanner(System.in);
                    String trefuse = scanner.nextLine();
                    while (trefuse.isEmpty()){
                        if(tstate == 3) {
                            System.out.println(" 您拒绝了他的申请，必须填写拒绝理由:");
                            scanner = new Scanner(System.in);
                            trefuse = scanner.nextLine();
                        }
                        else
                            break;
                    }
                    stmt.execute("update checktrip set tstate='"+tstate+"' , trefuse = '"+trefuse+"' WHERE tno="+tno);
                    System.out.println("审批成功！");
                    //写入日志
                    if(tstate == 2)
                        writelog("approve_trip");
                    else if(tstate == 3)
                        writelog("reject_trip");
                    if(tstate == 2) {
                        //查询要被更新的出差条目的起始日期和终止日期
                        java.sql.Date tsdate = null, tedate = null;
                        sql = "SELECT tsdate,tedate FROM trip WHERE tno=" + tno;
                        rs = stmt.executeQuery(sql);
                        while (rs.next()) {
                            tsdate = rs.getDate("tsdate");
                            tedate = rs.getDate("tedate");
                        }
                        //把出差日期之内的每一天的考勤信息都设置成出差状态
                        int days = (int) (tedate.getTime() - tsdate.getTime()) / 86400000 + 1;
                        for (int i = 0; i < days; i++) {
                            java.sql.Date tmp = new java.sql.Date(tsdate.getTime() + 86400000 * i);
                            //判断这一天是否已经请假或出差
                            sql = "SELECT * FROM attendance WHERE adate = '" + tmp + "' AND astate != 1";
                            rs = stmt.executeQuery(sql);
                            if (rs.next())//next有值返回truex
                                sql = "UPDATE attendance SET astate ='" + 3 + "' WHERE adate =" + tmp;
                            else
                                sql = "INSERT INTO attendance(eno,adate,astate) VALUES ('" + mno + "', '" + tmp + "', '" + 3 + "')";
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

    /**
     * 写日志
     * @param log 操作内容
     * @throws Exception
     */
    void writelog(String log) throws Exception {
        Date date = new java.util.Date();
        java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("HH:mm:ss");
        String nowTime = f.format(date);
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        String sql = " insert into log(ldate, ltime, eno, operation) VALUES ('" + sqlDate + "', '" + nowTime + "', '" + manager_eno + "' ,'" + log + "')";
        stmt.executeUpdate(sql);
    }
}