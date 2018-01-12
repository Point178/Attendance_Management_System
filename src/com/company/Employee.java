package com.company;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Scanner;

public class Employee {
    private int id;
    private Statement stmt = null;
    boolean isVacation = false;
    private String stime;
    private String etime;
    private String state;

    Employee(int id, Statement stmt, String vacation) {
        this.id = id;
        this.stmt = stmt;
        state = "";
        if (Objects.equals(vacation, "工作日")) {
            this.isVacation = false;
        } else {
            this.isVacation = true;
        }

        try {
            String sql = "SELECT stime, etime FROM time";
            ResultSet rs = stmt.executeQuery(sql);
            java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("HH:mm:ss");
            if (rs.next()) {
                this.stime = f.format(rs.getTime("stime"));
                this.etime = f.format(rs.getTime("etime"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("输入不合法！");
        }
    }

    public void run() {
        try {
            Date date = new java.util.Date();
            java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("HH:mm:ss");
            String nowTime = f.format(date);

            //从数据库中取出出勤状态
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());
            String sql = "SELECT intime, outtime, arrive, `leave`, absent, astate FROM attendance WHERE eno='" + id + "' and adate='" + sqlDate + "'";
            ResultSet rs = stmt.executeQuery(sql);
            if (!rs.next()) {
                //没记录  添加当天的默认结果进入系统
                if (isVacation) {
                    sql = "INSERT INTO attendance(eno, adate, intime, outtime, arrive, `leave`, absent, astate) " +
                            "VALUES ('" + id + "', '" + sqlDate + "', null, null, '1', '1', '1', '1')";
                    stmt.executeUpdate(sql);
                } else {
                    sql = "INSERT INTO attendance(eno, adate, intime, outtime, arrive, `leave`, absent, astate) " +
                            "VALUES ('" + id + "', '" + sqlDate + "', null, null, '0', '0', '0', '1')";
                    stmt.executeUpdate(sql);
                }
                sql = "SELECT intime, outtime, arrive, `leave`, absent, astate FROM attendance WHERE eno='" + id + "' and adate='" + sqlDate + "'";
                rs = stmt.executeQuery(sql);
                rs.next();
            }

            //判断是否出差/请假/在公司
            if (rs.getInt("astate") == 3) {
                state = "出差";
            } else if (rs.getInt("astate") == 2) {
                state = "请假";
            } else if (isVacation) {
                state = "放假";
            } else {
                //在公司 且不放假
                if (rs.getTime("intime") != null) {
                    if (rs.getInt("arrive") == 0) {
                        state += "迟到 ";
                    } else {
                        state += "已签到 ";
                    }
                } else {
                    state += "未签到 ";
                }

                if (rs.getTime("outtime") != null) {
                    if (rs.getInt("leave") == 0) {
                        state += "早退 ";
                    } else {
                        state += "已签退 ";
                    }
                } else {
                    state += "未签退 ";
                }

                if ((rs.getInt("absent") == 0) &&
                        isLate(nowTime, etime)) {
                    state = "旷班";
                    // 加入旷班记录
                    sql = "UPDATE attendance SET arrive = '1' ,`leave` = '1', absent = '0' WHERE eno = '" + id + "' and adate = '" + sqlDate + "'";
                    stmt.executeUpdate(sql);
                }
            }

            boolean execute = true;
            while (execute) {
                System.out.println("现在是" + nowTime + "   您的状态是 " + state);
                System.out.println("请选择操作：\n1. 个人信息维护\n2. 出差事务\n3. 休假事务\n4. 查看历史考勤信息\n5. 签到\n6. 签退\n7. 退出系统\n其他  返回上级菜单");
                Scanner in = new Scanner(System.in);
                String input = in.nextLine();
                switch (input) {
                    case "1":
                        changePasswd();
                        break;
                    case "2":
                        bussiness();
                        break;
                    case "3":
                        leave();
                        break;
                    case "4":
                        searchAttendance();
                        break;
                    case "5":
                        if (state == "出差" || state == "请假" || state == "放假") {
                            System.out.println(state + "无需签到！");
                        } else {
                            if (!state.contains("未签到") && !Objects.equals(state, "旷班")) {
                                System.out.println("今日已签到!");
                            } else if (Objects.equals(state, "旷班")) {
                                System.out.println("今日已旷班 签到无效!");
                            } else {
                                //签到
                                checkin();
                            }
                        }
                        break;
                    case "6":
                        if (state == "出差" || state == "请假" || state == "放假") {
                            System.out.println(state + "无需签退！");
                        } else {
                            if (Objects.equals(state, "旷班")) {
                                System.out.println("今日已旷班 签退无效！");
                            } else if (!state.contains("未签退")) {
                                System.out.println("今日已签退!");
                            } else if (state.contains("未签到")) {
                                System.out.println("今日未签到，无法进行签退");
                            } else {
                                //签退
                                checkout();
                            }
                        }
                        break;
                    case "7":
                        System.exit(0);
                    default:
                        execute = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("输入不合法！");
        }
    }

    boolean isLate(String now, String time) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        long nowTime = sdf.parse(now).getTime();
        long compare = sdf.parse(time).getTime();

        return nowTime > compare;
    }

    void changePasswd() throws Exception {
        String passwd;
        Scanner in = new Scanner(System.in);
        do {
            System.out.println("请输入新密码：（不超过20位）");
            passwd = in.nextLine();
            System.out.println("请再次输入新密码：");
        } while (!Objects.equals(passwd, in.nextLine()));

        //update database
        String sql = "update employee set password='" + passwd + "' where eno='" + id + "'";
        stmt.execute(sql);
        System.out.println("修改密码成功!");

        //write log
        Date date = new java.util.Date();
        java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("HH:mm:ss");
        String nowTime = f.format(date);
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        sql = "insert into log(ldate, ltime, eno, operation) VALUES ('" + sqlDate + "', '" + nowTime + "', '" + id + "' ,'modify_user')";
        stmt.executeUpdate(sql);
    }

    void bussiness() throws Exception {
        boolean legalinput = true;
        do {
            System.out.println("1. 提交新出差申请\n2. 查询/修改出差申请\n3. 返回上级菜单");
            Scanner in = new Scanner(System.in);
            String input = in.nextLine();
            if (Objects.equals(input, "1")) { //提交新出差申请
                System.out.println("请选择出差类型：1. 公司指派  2. 个人申请");
                int type = Integer.parseInt(in.nextLine());
                System.out.println("请填写具体出差理由：");
                String reason = in.nextLine();
                System.out.println("请输入出差开始日期：（格式：YYYY-MM-DD）");
                String sdate = in.nextLine();
                System.out.println("请输入出差结束日期：（格式：YYYY-MM-DD）");
                String edate = in.nextLine();
                int tno = Integer.parseInt(((id % 10000) + sdate).replace("-", ""));

                String sql = "SELECT dno from belong WHERE eno=" + id;
                ResultSet rs = stmt.executeQuery(sql);
                int dno;
                if (rs.next()) {
                    dno = rs.getInt("dno");
                } else {
                    dno = 0;
                }

                sql = "SELECT tno FROM trip WHERE tno = '"+tno+"'";
                rs = stmt.executeQuery(sql);
                if (!rs.next()) {
                    sql = "INSERT INTO trip(tno, eno, tsdate, tedate, ttype, treason) " +
                            "VALUES ('" + tno + "', '" + id + "', '" + java.sql.Date.valueOf(sdate) + "', '" + java.sql.Date.valueOf(edate) + "', '" + type + "', '" + reason + "')";
                    stmt.executeUpdate(sql);

                    sql = "INSERT INTO checktrip(dno, tno, tstate, trefuse) VALUES ('" + dno + "', '" + tno + "', '1', null)";
                    stmt.executeUpdate(sql);

                    System.out.println("新出差申请提交成功！");

                    // 写入日志
                    Date date = new java.util.Date();
                    java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("HH:mm:ss");
                    String nowTime = f.format(date);
                    java.sql.Date sqlDate = new java.sql.Date(date.getTime());
                    sql = "insert into log(ldate, ltime, eno, operation) VALUES ('" + sqlDate + "', '" + nowTime + "', '" + id + "' ,'submit_trip')";
                    stmt.executeUpdate(sql);
                } else {
                    System.out.println("申请提交失败!此时间范围已经存在出差申请!");
                }
            } else if (Objects.equals(input, "2")) { //查询/修改出差申请
                System.out.println("查询依据：1. 日期范围  2. 天数  3. 审核状态  4. 类型");
                int searchType = Integer.parseInt(in.nextLine());

                //搜索结果
                String sql;
                if (searchType == 1) {  //按日期范围
                    System.out.println("请输入日期范围 查询顺序：（例：2018-01-01 2018-01-02 desc）");
                    String[] parameter = in.nextLine().split(" ");
                    sql = "SELECT tno,tsdate,tedate,ttype,tstate FROM trip natural join checktrip " +
                            "WHERE eno='" + id + "' and tsdate>'" + parameter[0] + "' and tedate<'" + parameter[1] +
                            "' ORDER BY tsdate " + parameter[2];
                } else if (searchType == 2) { //按天数
                    System.out.println("请输入出差天数：（例：>2）");
                    String duration = in.nextLine();
                    sql = "SELECT tno,tsdate,tedate,ttype,tstate FROM trip natural join checktrip " +
                            "WHERE eno='" + id + "' and tedate - tsdate " + duration + "";
                } else if (searchType == 3) { //按审核状态
                    System.out.println("请输入查询状态值：1. 待审批 2. 已通过 3. 未通过 4. 已放弃");
                    int searchstate = Integer.parseInt(in.nextLine());
                    sql = "SELECT tno,tsdate,tedate,ttype,tstate FROM trip natural join checktrip " +
                            "WHERE eno='" + id + "' and tstate='" + searchstate + "'";
                } else { //按出差类型
                    System.out.println("请输入查询类型：1. 公司指派 2. 个人申请");
                    int searchtype = Integer.parseInt(in.nextLine());
                    sql = "SELECT tno,tsdate,tedate,ttype,tstate FROM trip natural join checktrip " +
                            "WHERE eno='" + id + "' and ttype='" + searchtype + "'";
                }

                //显示结果
                ResultSet rs = stmt.executeQuery(sql);
                System.out.println("   编号     起始日期    结束日期     类型   审核状态");
                while (rs.next()) {
                    String output = rs.getInt("tno") + " " + rs.getDate("tsdate") + " " +
                            rs.getDate("tedate") + " ";
                    if (rs.getInt("ttype") == 1) {
                        output += "公司指派 ";
                    } else {
                        output += "个人申请  ";
                    }
                    switch (rs.getInt("tstate")) {
                        case 1:
                            output += "待审批";
                            break;
                        case 2:
                            output += "已通过";
                            break;
                        case 3:
                            output += "未通过";
                            break;
                        case 4:
                            output += "已放弃";
                    }
                    System.out.println(output);
                }
                System.out.println();

                //修改出差申请
                boolean isModify = true;
                while (isModify) {
                    System.out.println("是否修改申请？Y修改，N返回上级菜单");
                    if (Objects.equals(in.nextLine(), "Y")) {
                        System.out.println("请输入要修改申请的编号：");
                        int no = Integer.parseInt(in.nextLine());
                        sql = "SELECT tsdate,tedate,ttype,treason,tstate FROM trip natural join checktrip " +
                                "WHERE tno='" + no + "'";
                        rs = stmt.executeQuery(sql);
                        if (rs.next()) {
                            if (rs.getInt("tstate") == 2) {
                                System.out.println("此申请已通过，无法修改！");
                            } else {
                                String tsdate = rs.getString("tsdate");
                                String tedate = rs.getString("tedate");
                                int ttype = rs.getInt("ttype");
                                String treason = rs.getString("treason");
                                boolean modifying = true;
                                while (modifying) {
                                    System.out.println("请选择具体的修改操作：1. 开始日期 2. 结束日期 3. 出差类型 4. 具体理由 5. 保存 6. 删除申请  7. 退出修改");
                                    switch (in.nextLine()) {
                                        case "1":
                                            System.out.println("请输入出差开始日期：（格式：YYYY-MM-DD）");
                                            tsdate = in.nextLine();
                                            break;
                                        case "2":
                                            System.out.println("请输入出差结束日期：（格式：YYYY-MM-DD）");
                                            tedate = in.nextLine();
                                            break;
                                        case "3":
                                            System.out.println("请选择出差类型：1. 公司指派  2. 个人申请");
                                            ttype = Integer.parseInt(in.nextLine());
                                            break;
                                        case "4":
                                            System.out.println("请填写具体出差理由：");
                                            treason = in.nextLine();
                                            break;
                                        case "5":
                                            int tno = Integer.parseInt(((id % 10000) + tsdate).replace("-", ""));
                                            if(tno != no){
                                                sql = "SELECT * FROM trip WHERE tno = '"+tno+"'";
                                                rs = stmt.executeQuery(sql);
                                                if (!rs.next()) {
                                                    sql = "SELECT dno FROM checktrip WHERE tno = '"+no+"'";
                                                    rs = stmt.executeQuery(sql);
                                                    rs.next();
                                                    int dno = rs.getInt("dno");

                                                    sql = "DELETE FROM checktrip where tno = '"+no+"'";
                                                    stmt.executeUpdate(sql);
                                                    sql = "DELETE from trip where tno='"+no+"'";
                                                    stmt.executeUpdate(sql);

                                                    sql = "INSERT INTO trip(tno, eno, tsdate, tedate, ttype, treason) " +
                                                            "VALUES ('" + tno + "', '" + id + "', '" + java.sql.Date.valueOf(tsdate) + "', '" + java.sql.Date.valueOf(tedate) + "', '" + ttype + "', '" + treason + "')";
                                                    stmt.executeUpdate(sql);

                                                    sql = "INSERT INTO checktrip(dno, tno, tstate, trefuse) VALUES ('" + dno + "', '" + tno + "', '1', null)";
                                                    stmt.executeUpdate(sql);

                                                    System.out.println("修改已保存！");
                                                }else{
                                                    System.out.println("修改提交失败！此时间段已存在其他出差申请！");
                                                }
                                            }else {
                                                    sql = "update trip set tsdate='" + tsdate + "' ,tedate='" + tedate + "' ,ttype='" + ttype + "' ,treason='" + treason + "' where tno='" + no + "'";
                                                    stmt.executeUpdate(sql);

                                                    sql = "update checktrip set tstate='1' ,trefuse=null where tno='" + no + "'";
                                                    stmt.executeUpdate(sql);
                                                    System.out.println("修改已保存！");
                                            }
                                            break;
                                        case "6":
                                            sql = "DELETE FROM checktrip where tno = '"+no+"'";
                                            stmt.executeUpdate(sql);
                                            sql = "DELETE from trip where tno='"+no+"'";
                                            stmt.executeUpdate(sql);
                                            System.out.println("请求已删除！");
                                            break;
                                        default:
                                            modifying = false;
                                    }
                                }
                            }
                        } else {
                            System.out.println("无此出差记录！");
                        }
                    } else {
                        isModify = false;
                    }
                }


            } else if (Objects.equals(input, "3")) { //返回上级菜单
                return;
            } else {
                legalinput = false;
                System.out.println("不合法输入");
            }
        } while (!legalinput);
    }

    void leave() throws Exception {
        boolean legalinput = true;
        do {
            System.out.println("1. 提交新休假申请\n2. 查询/修改休假申请\n3. 返回上级菜单");
            Scanner in = new Scanner(System.in);
            String input = in.nextLine();
            if (Objects.equals(input, "1")) { //提交新休假申请
                System.out.println("请选择休假类型：1.事假  2.病假  3.产假  4.婚假  5.其他");
                int type = Integer.parseInt(in.nextLine());
                System.out.println("请填写具体请假理由：");
                String reason = in.nextLine();
                System.out.println("请输入请假开始日期：（格式：YYYY-MM-DD）");
                String sdate = in.nextLine();
                System.out.println("请输入请假结束日期：（格式：YYYY-MM-DD）");
                String edate = in.nextLine();
                int tno = Integer.parseInt(((id % 10000) + sdate).replace("-", ""));

                String sql = "SELECT dno FROM belong WHERE eno=" + id;
                ResultSet rs = stmt.executeQuery(sql);
                int dno;
                if (rs.next()) {
                    dno = rs.getInt("dno");
                } else {
                    dno = 0;
                }

                sql = "SELECT lno FROM `leave` WHERE lno = '"+tno+"'";
                rs = stmt.executeQuery(sql);
                if(!rs.next()) {
                    sql = "INSERT INTO `leave`(lno, eno, lsdate, ledate, ltype, lreason) " +
                            "VALUES ('" + tno + "', '" + id + "', '" + java.sql.Date.valueOf(sdate) + "', '" + java.sql.Date.valueOf(edate) + "', '" + type + "', '" + reason + "')";

                    stmt.executeUpdate(sql);

                    sql = "INSERT INTO checkleave(dno, lno, lstate, lrefuse) VALUES ('" + dno + "', '" + tno + "', '1', null)";
                    stmt.executeUpdate(sql);

                    System.out.println("新请假申请提交成功！");

                    // 写入日志
                    Date date = new java.util.Date();
                    java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("HH:mm:ss");
                    String nowTime = f.format(date);
                    java.sql.Date sqlDate = new java.sql.Date(date.getTime());
                    sql = "insert into log(ldate, ltime, eno, operation) VALUES ('" + sqlDate + "', '" + nowTime + "', '" + id + "' ,'submit_leave')";
                    stmt.executeUpdate(sql);
                }else{
                    System.out.println("请假请求提交失败！此时间段已存在请假申请！");
                }
            } else if (Objects.equals(input, "2")) { //查询/修改请假申请
                System.out.println("查询依据：1. 日期范围  2. 天数  3. 审核状态  4. 类型");
                int searchType = Integer.parseInt(in.nextLine());

                //搜索结果
                String sql;
                if (searchType == 1) {  //按日期范围
                    System.out.println("请输入日期范围 查询顺序：（例：2018-01-01 2018-01-02 desc）");
                    String[] parameter = in.nextLine().split(" ");
                    sql = "SELECT lno,lsdate,ledate,ltype,lstate FROM `leave` natural join checkleave " +
                            "WHERE eno='" + id + "' and lsdate>'" + parameter[0] + "' and ledate<'" + parameter[1] +
                            "' ORDER BY lsdate " + parameter[2];
                } else if (searchType == 2) { //按天数
                    System.out.println("请输入请假天数：（例：>2）");
                    String duration = in.nextLine();
                    sql = "SELECT lno,lsdate,ledate,ltype,lstate FROM `leave` natural join checkleave " +
                            "WHERE eno='" + id + "' and ledate - lsdate " + duration;
                } else if (searchType == 3) { //按审核状态
                    System.out.println("请输入查询状态值：1. 待审批 2. 已通过 3. 未通过 4. 已放弃");
                    int searchstate = Integer.parseInt(in.nextLine());
                    sql = "SELECT lno,lsdate,ledate,ltype,lstate from `leave` natural join checkleave " +
                            "WHERE eno='" + id + "' and lstate='" + searchstate + "'";
                } else { //按请假类型
                    System.out.println("请输入查询类型：1.事假  2.病假  3.产假  4.婚假  5.其他");
                    int searchtype = Integer.parseInt(in.nextLine());
                    sql = "SELECT lno,lsdate,ledate,ltype,lstate FROM `leave` natural join checkleave " +
                            "WHERE eno='" + id + "' and ltype='" + searchtype + "'";
                }

                //显示结果
                ResultSet rs = stmt.executeQuery(sql);
                System.out.println("   编号     起始日期    结束日期     类型   审核状态");
                while (rs.next()) {
                    String output = rs.getInt("lno") + " " + rs.getDate("lsdate") + " " +
                            rs.getDate("ledate") + " ";
                    switch (rs.getInt("ltype")) {
                        case 1:
                            output += "  事假   ";
                            break;
                        case 2:
                            output += "  病假   ";
                            break;
                        case 3:
                            output += "  产假   ";
                            break;
                        case 4:
                            output += "  婚假   ";
                            break;
                        default:
                            output += "  其他   ";
                    }

                    switch (rs.getInt("lstate")) {
                        case 1:
                            output += "待审批";
                            break;
                        case 2:
                            output += "已通过";
                            break;
                        case 3:
                            output += "未通过";
                            break;
                        default:
                            output += "已放弃";
                    }
                    System.out.println(output);
                }
                System.out.println();

                //修改放假申请
                boolean isModify = true;
                while (isModify) {
                    System.out.println("是否修改申请？Y修改，N返回上级菜单");
                    if (Objects.equals(in.nextLine(), "Y")) {
                        System.out.println("请输入要修改申请的编号：");
                        int no = Integer.parseInt(in.nextLine());
                        sql = "SELECT lsdate,ledate,ltype,lreason,lstate FROM `leave` natural join checkleave " +
                                "WHERE lno='" + no + "'";
                        rs = stmt.executeQuery(sql);
                        if (rs.next()) {
                            if (rs.getInt("lstate") == 2) {
                                System.out.println("此申请已通过，无法修改！");
                            } else {
                                String tsdate = rs.getString("lsdate");
                                String tedate = rs.getString("ledate");
                                int ttype = rs.getInt("ltype");
                                String treason = rs.getString("lreason");
                                boolean modifying = true;
                                while (modifying) {
                                    System.out.println("请选择具体的修改操作：1. 开始日期 2. 结束日期 3. 请假类型 4. 具体理由 5. 保存 6. 删除申请  7. 退出修改");
                                    switch (in.nextLine()) {
                                        case "1":
                                            System.out.println("请输入请假开始日期：（格式：YYYY-MM-DD）");
                                            tsdate = in.nextLine();
                                            break;
                                        case "2":
                                            System.out.println("请输入请假结束日期：（格式：YYYY-MM-DD）");
                                            tedate = in.nextLine();
                                            break;
                                        case "3":
                                            System.out.println("请选择请假类型：1.事假  2.病假  3.产假  4.婚假  5.其他");
                                            ttype = Integer.parseInt(in.nextLine());
                                            break;
                                        case "4":
                                            System.out.println("请填写具体请假理由：");
                                            treason = in.nextLine();
                                            break;
                                        case "5":
                                            int tno = Integer.parseInt(((id % 10000) + tsdate).replace("-", ""));
                                            if(tno != no) {
                                                sql = "SELECT * FROM `leave` WHERE lno = '" + tno + "'";
                                                rs = stmt.executeQuery(sql);
                                                if (!rs.next()) {
                                                    sql = "SELECT dno FROM checkleave WHERE lno = '" + no + "'";
                                                    rs = stmt.executeQuery(sql);
                                                    rs.next();
                                                    int dno = rs.getInt("dno");

                                                    sql = "DELETE  checkleave where lno = '" + no + "'";
                                                    stmt.executeUpdate(sql);
                                                    sql = "DELETE from `leave` where lno='" + no + "'";
                                                    stmt.executeUpdate(sql);

                                                    sql = "INSERT INTO `leave`(lno, eno, lsdate, ledate, ltype, lreason) " +
                                                            "VALUES ('" + tno + "', '" + id + "', '" + java.sql.Date.valueOf(tsdate) + "', '" + java.sql.Date.valueOf(tedate) + "', '" + ttype + "', '" + treason + "')";
                                                    stmt.executeUpdate(sql);

                                                    sql = "INSERT INTO checkleave(dno, lno, lstate, lrefuse) VALUES ('" + dno + "', '" + tno + "', '1', null)";
                                                    stmt.executeUpdate(sql);

                                                    System.out.println("修改已保存！");
                                                } else {
                                                    System.out.println("修改提交失败！此时间段已存在其他出差申请！");
                                                }
                                            }else {
                                                sql = "update `leave` set lsdate='" + tsdate + "' ,ledate='" + tedate + "' ,ltype='" + ttype + "' ,lreason='" + treason + "' where lno='" + no + "'";
                                                stmt.executeUpdate(sql);

                                                sql = "update checkleave set lstate='1' ,lrefuse=null where lno='" + no + "'";
                                                stmt.executeUpdate(sql);
                                                System.out.println("修改已保存！");
                                            }
                                            break;

                                        case "6":
                                            sql = "DELETE checkleave where lno = '" + no + "'";
                                            stmt.executeUpdate(sql);
                                            sql = "DELETE `leave` from `leave` where lno='" + no + "'";
                                            stmt.executeUpdate(sql);
                                            System.out.println("申请已删除！");
                                            break;
                                        default:
                                            modifying = false;
                                    }
                                }
                            }
                        } else {
                            System.out.println("无此请假记录！");
                        }
                    } else {
                        isModify = false;
                    }
                }
            } else if (Objects.equals(input, "3")) { //返回上级菜单
                return;
            } else {
                legalinput = false;
                System.out.println("不合法输入");
            }
        } while (!legalinput);
    }

    void searchAttendance() throws Exception {
        System.out.println("请选择查询依据：1.日期范围  2.迟到  3.早退  4.迟到早退  5.旷班  6.请假  7.出差  8. 正常上班  9.全部");
        Scanner in = new Scanner(System.in);
        String sql;
        switch (Integer.parseInt(in.nextLine())) {
            case 1:
                System.out.println("请输入日期范围 显示顺序：（例：2018-01-01 2018-01-02 desc）");
                String[] parameter = in.nextLine().split(" ");
                sql = "select adate, intime, outtime, arrive, `leave`, absent, astate, vacation " +
                        "from attendance, vacation " +
                        "where eno = '" + id + "' and adate < '" + parameter[1] + "' and adate > '" + parameter[0] + "' and vacation.vdate = attendance.adate order by adate " + parameter[2];
                System.out.println(sql);
                break;
            case 2:
                sql = "select adate, intime, outtime, arrive, `leave`, absent, astate, vacation " +
                        "from attendance, vacation where eno = '" + id + "' and `leave` = '0' and vacation.vdate = attendance.adate order by adate desc";
                break;
            case 3:
                sql = "select adate, intime, outtime, arrive, `leave`, absent, astate, vacation " +
                        "from attendance, vacation where eno = '" + id + "' and arrive = '0' and vacation.vdate = attendance.adate order by adate desc";
                break;
            case 4:
                sql = "select adate, intime, outtime, arrive, `leave`, absent, astate, vacation " +
                        "from attendance, vacation where eno = '" + id + "' and `leave` = '0' and arrive = '0' and vacation.vdate = attendance.adate order by attendance.adate desc";
                break;
            case 5:
                sql = "select adate, intime, outtime, arrive, `leave`, absent, astate, vacation " +
                        "from attendance, vacation where eno = '" + id + "' and absent = '0' and vacation.vdate = attendance.adate order by adate desc";
                break;
            case 6:
                sql = "select adate, intime, outtime, arrive, `leave`, absent, astate, vacation " +
                        "from attendance, vacation where eno = '" + id + "' and astate = '2' and vacation.vdate = attendance.adate order by adate desc";
                break;
            case 7:
                sql = "select adate, intime, outtime, arrive, `leave`, absent, astate, vacation " +
                        "from attendance, vacation where eno = '" + id + "' and astate = '3' and vacation.vdate = attendance.adate order by adate desc";
                break;
            case 8:
                sql = "select adate, intime, outtime, arrive, `leave`, absent, astate, vacation " +
                        "from attendance, vacation where eno = '" + id + "' and `leave` = '1' and arrive = '1' and vacation.vdate = attendance.adate order by adate desc";
                break;
            case 9:
                sql = "select adate, intime, outtime, arrive, `leave`, absent, astate, vacation " +
                        "from attendance, vacation where eno = '" + id + "' and vacation.vdate = attendance.adate order by adate desc";
                break;
            default:
                return;
        }

        //打印结果
        ResultSet rs = stmt.executeQuery(sql);
        java.sql.Date sqlDate = new java.sql.Date(new java.util.Date().getTime());
        System.out.println("   日期     签到时间   签退时间   状态 ");
        while (rs.next()) {
            java.sql.Date date = rs.getDate("adate");
            int astate = rs.getInt("astate");
            int arrive = rs.getInt("arrive");
            int leave = rs.getInt("leave");
            String intime = rs.getString("intime");
            String outtime = rs.getString("outtime");
            int absent = rs.getInt("absent");
            int vacation = rs.getInt("vacation");

            if (sqlDate.before(date)) {
                if (intime == null) {
                    intime = "        ";
                }
                if (outtime == null) {
                    outtime = "        ";
                }
                String thisState;
                if (astate == 2) {
                    thisState = "  出差 ";
                } else if (astate == 3) {
                    thisState = "  休假 ";
                } else {
                    if (vacation == 1) {
                        thisState = "  公休 ";
                    } else {
                            if (absent == 0) {
                                thisState = "  旷班 ";
                            } else {
                                if ((arrive == 0) && (leave == 0)) {
                                    thisState = "迟到早退";
                                } else if ((arrive == 1) && (leave == 1)) {
                                    thisState = "正常出勤";
                                } else if (arrive == 0) {
                                    thisState = "  早退 ";
                                } else {
                                    thisState = "  迟到 ";
                                }
                            }
                    }
                }
                System.out.println(date + " " + intime + " " + outtime + " " + thisState);
            }
        }
        System.out.println();
    }

    void checkin() throws Exception {
        Date date = new java.util.Date();
        java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("HH:mm:ss");
        String nowTime = f.format(date);
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());

        String sql;
        if (isLate(nowTime, stime)) {
            //迟到
            sql = "update attendance set intime = '" + nowTime + "' ,arrive = '0', absent = '1' where eno = '" + id + "' and adate = '" + sqlDate + "'";
            this.state = state.replace("未签到", "迟到");
        } else {
            sql = "update attendance set intime = '" + nowTime + "' ,arrive = '1', absent = '1' where eno = '" + id + "' and adate = '" + sqlDate + "'";
            this.state = state.replace("未签到", "已签到");
        }
        stmt.executeUpdate(sql);
        System.out.println("签到成功！您的状态是 " + state);

        //write log
        sql = "insert into log(ldate, ltime, eno, operation) VALUES ('" + sqlDate + "', '" + nowTime + "', '" + id + "' ,'checkin')";
        stmt.executeUpdate(sql);
    }

    void checkout() throws Exception {
        Date date = new java.util.Date();
        java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("HH:mm:ss");
        String nowTime = f.format(date);
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());

        String sql;
        if (!isLate(nowTime, etime)) {
            //早退
            sql = "update attendance set outtime = '" + nowTime + "' ,`leave` = '0' where eno = '" + id + "' and adate = '" + sqlDate + "'";
            this.state = state.replace("未签退", "早退");
        } else {
            sql = "update attendance set outtime = '" + nowTime + "' ,`leave` = '1' where eno = '" + id + "' and adate = '" + sqlDate + "'";
            this.state = state.replace("未签退", "已签退");
        }
        stmt.executeUpdate(sql);
        System.out.println("签退成功！您的状态是 " + state);

        //write log
        sql = "insert into log(ldate, ltime, eno, operation) VALUES ('" + sqlDate + "', '" + nowTime + "', '" + id + "' ,'checkout')";
        stmt.executeUpdate(sql);
    }
}
