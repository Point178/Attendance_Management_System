import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.Scanner;

public class DepartmentManager {
    private String manager_eno;
    private String manager_dno;
    private Statement stmt;

    public DepartmentManager(int id, Statement stmt) throws Exception{
        this.manager_eno = id+"";
        this.stmt = stmt;
        this.manager_dno = check_dno(manager_eno);
        writelog("login_in");
    };

    public void run(){
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
            e.printStackTrace();
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
                int count = 0;
                while (rs.next()) {
                    count = rs.getInt(1);
                }
                if (count == 0) {
                    System.out.println("不存在工号为" + usrId + "的员工！");
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
                    return;
                }
                break;
            default:
                System.out.println("您还没有输入工号或姓名: 1/2");
                return;
        }
        //判断这个员工是否是自己部门下的员工，否则没有查看的权限
        if(!check_dno(employee_eno).equals(check_dno(manager_eno))){
            System.out.println("对不起，这个员工不存在或者不属于您的部门，您不能查看他的考勤信息");
            return ;
        }
        rs = stmt.executeQuery("SELECT * FROM attendance WHERE eno=" + employee_eno);
        while (rs.next()) {
            System.out.print("工号: " + rs.getString(1));
            System.out.print("  日期: " + rs.getDate(2));
            System.out.print("  签到时间: " + rs.getTime(3));
            System.out.print("  签退时间: " + rs.getTime(4));
            String state = null;
            if (rs.getInt(8) == 2) {
                state = "请假";
            }
            else if (rs.getInt(8) == 3) {
                state = "出差";
            }
            else {
                state = "在公司";
                if (rs.getInt(7) == 0) {
                    state = "(旷工)";
                }
                else {//没有旷工
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
     * @throws Exception
     */
    public void lookup_attendance()throws Exception{
        System.out.println("您可以输入不同的条件进行考勤信息的筛选和查看，比如：\n" +
                "absent=0 （旷工）   absent=1 （没有旷工）\n" +
                "astate=1  (在公司)  astate=2  (请假)  astate=3  (出差)\n" +
                "adate<2018-01-02  （考勤时间范围）" +"\n");
        Scanner scanner = new Scanner(System.in);
        String condition  = scanner.nextLine();
        if(!condition.isEmpty()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM attendance natural join belong" +
                    " WHERE belong.dno = " + manager_dno+ " AND " + condition);

            System.out.println("以下是按照条件  " + condition + "  查找的考勤信息：");
            while (rs.next()) {
                System.out.print("工号: " + rs.getString(1));
                System.out.print("  日期: " + rs.getDate(2));
                System.out.print("  签到: " + rs.getTime(3));
                System.out.print("  签退: " + rs.getTime(4));
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
        else
            System.out.println("请输入您要查找的条件！");
    }

    /**
     * 查看部门里所有的请假或出差的申请的历史记录
     */
    public void view_application() throws Exception{
        System.out.println("您可以查看您部门下员工所有的请假或出差申请，1：请假，2：出差");
        Scanner scanner = new Scanner(System.in);
        ResultSet rs;
        switch (scanner.nextLine()){
            case "1":
                String sql = "SELECT lno,eno,lsdate,ledate,ltype,lreason FROM `leave` natural join checkleave" +
                        " WHERE checkleave.dno = "+manager_dno+
                        " AND checkleave.lstate != '1' ";//所有不是待审批状态的记录
                rs = stmt.executeQuery(sql);
                System.out.println("以下是您审批的部门下所有员工的请假信息");
                System.out.println("序号--------员工工号-----开始日期-----结束日期-----审批状态-----拒绝理由");
                String line;
                while (rs.next()){
                    line = rs.getString("lno") +"     ";
                    line += rs.getString("eno")+"     ";
                    int lstate = rs.getInt("lstate");
                    switch (lstate){
                        case 1:
                            line += "待审核  ";break;
                        case 2:
                            line += "同意   ";break;
                        case 3:
                            line += "驳回   ";break;
                    }
                    line += rs.getDate("ledate")+"     ";
                    int type = rs.getInt("ltype");
                    if(type == 1)
                        line += "事假  ";
                    else if(type == 2)
                        line += "病假  ";
                    else if(type == 3)
                        line += "产假  ";
                    else if(type == 4)
                        line += "婚假  ";
                    else
                        line += "其他  ";
                    line += rs.getString("lreason");
                    System.out.println(line);
                }
                break;
            case "2":
                sql = "SELECT tno,eno,tsdate,tedate,ttype,treason FROM trip natural join checktrip" +
                        " WHERE checktrip.dno = "+manager_dno+
                        " AND checktrip.tstate != '1' ";
                rs = stmt.executeQuery(sql);
                System.out.println("以下是您审批的部门下所有员工的出差信息");
                System.out.println("序号--------员工工号-----开始日期-----结束日期-----审批状态-----拒绝理由");
                while (rs.next()){
                    line = rs.getString("tno") +"     ";
                    line += rs.getString("teo")+"     ";
                    int tstate = rs.getInt("tstate");
                    switch (tstate){
                        case 1:
                            line += "待审核  ";break;
                        case 2:
                            line += "同意   ";break;
                        case 3:
                            line += "驳回   ";break;
                    }
                    line += rs.getDate("tedate")+"     ";
                    int type = rs.getInt("ttype)");
                    if(type == 1)
                        line += "公司指派";
                    else if(type == 2)
                        line += "个人申请";
                    line += rs.getString("treason");
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
     * @throws Exception
     */
    public void approve_application() throws Exception{
        System.out.println("您可以审批您部门里员工的请假和出差申请————1：请假申请；2：出差申请");
        Scanner scanner = new Scanner(System.in);
        switch (scanner.nextLine()){
            case "1":
                ResultSet rs = stmt.executeQuery("SELECT * FROM checkleave natural join `leave`" +
                        " WHERE dno = " + manager_dno+
                        " AND lstate = 1 ");//待审批或者被驳回的申请
                String line;
                System.out.println("以下是您部门里没有成功或待处理的请假申请：");
                System.out.println("序号--------员工工号-----开始日期-----结束日期-----审批状态-----拒绝理由");
                while (rs.next()){
                    line = rs.getString("lno")+"     ";
                    line += rs.getString("eno")+"        ";
                    line += rs.getInt("lstate")+"       ";
                    line += rs.getString("lrefuse");
                    System.out.println(line);
                }
                System.out.println("您想要审批他们的申请吗？1：是；0：否");
                scanner = new Scanner(System.in);
                if(scanner.nextInt() == 1){
                    System.out.println("请输入想要审批的申请的序号:");
                    scanner = new Scanner(System.in);
                    int lno = scanner.nextInt();
                    System.out.println("请输入审批状态(1:待审批2:同意3:驳回):");
                    scanner = new Scanner(System.in);
                    int lstate = scanner.nextInt();
                    System.out.println("请输入拒绝理由(若同意或无理由请输入null):");
                    scanner = new Scanner(System.in);
                    String lrefuse = scanner.nextLine();
                    stmt.execute(" update checkleave set lstate="+lstate+" ,lrefuse="+lrefuse+" where lno="+lno);
                    System.out.println("审批成功！");
                    //写入日志
                    if(lstate == 2)
                        writelog("approve_leave");
                    else if(lstate == 3)
                        writelog("reject_leave");
                    //查询要被更新的请假条目的起始日期和终止日期
                    java.sql.Date lsdate = null,ledate = null;
                    String eno = null;
                    rs = stmt.executeQuery("SELECT lsdate,ledate,eno FROM `leave` WHERE lno="+lno);
                    while (rs.next()){
                        lsdate = rs.getDate("lsdate");
                        ledate = rs.getDate("ledate");
                        eno = rs.getString("eno");
                    }
                    //把请假日期之内的每一天的考勤信息都设置成请假状态
                    int days = (int)(ledate.getTime() - lsdate.getTime())/86400000+1;
                    String sql;
                    for(int i = 0;i < days;i++){
                        java.sql.Date tmp = new java.sql.Date((lsdate.getTime()+86400000*i)*1000);
                        //判断这一天是否已经请假或出差
                        sql = "SELECT * FROM attendance WHERE adate = '"+tmp+"' AND astate != 1";
                        rs = stmt.executeQuery(sql);
                        if (rs.next())//next有值返回true
                            sql = "UPDATE attendance SET astate ='"+ 2 + "' WHERE adate ="+tmp;
                        else
                            sql = "INSERT INTO attendance(eno,adate,astate) VALUES ('"+eno + "', '" + tmp + "', '" + 2 + "')";
                        stmt.execute(sql);
                    }
                    System.out.println();
                }
                else
                    return;
                break;
            case "2":
                rs = stmt.executeQuery("SELECT * FROM checktrip natural join trip" +
                        " WHERE dno = " + manager_dno+
                        " AND tstate = 1 ");//待审批或者被驳回的申请
                System.out.println("以下是您部门里没有成功或待处理的出差申请：");
                System.out.println("序号--------员工工号-----开始日期-----结束日期-----审批状态-----拒绝理由");
                while (rs.next()){
                    line = rs.getString("tno")+"     ";
                    line += rs.getString("eno")+"        ";
                    line += rs.getInt("tstate ")+"        ";
                    line += rs.getString("trefuse");
                    System.out.println(line);
                }
                System.out.println("您想要审批他们的申请吗？1：是；0：否");
                scanner = new Scanner(System.in);
                if(scanner.nextInt() == 1){
                    System.out.println("请输入想要审批的申请的序号:");
                    scanner = new Scanner(System.in);
                    int tno = scanner.nextInt();
                    System.out.println("请输入审批状态(1:待审批2:同意3:驳回):");
                    scanner = new Scanner(System.in);
                    int tstate = scanner.nextInt();
                    System.out.println("请输入拒绝理由(若同意或无理由请输入null):");
                    scanner = new Scanner(System.in);
                    String trefuse = scanner.nextLine();
                    stmt.execute(" update checktrip set tstate="+tstate+" ,trefuse="+trefuse+" where lno="+tno);
                    System.out.println("审批成功！");
                    //写入日志
                    if(tstate == 2)
                        writelog("approve_trip");
                    else if(tstate == 3)
                        writelog("reject_trip");
                    //查询要被更新的出差条目的起始日期和终止日期
                    java.sql.Date tsdate = null,tedate = null;
                    String eno = null;
                    rs = stmt.executeQuery("SELECT tsdate,tedate FROM trip WHERE tno="+tno);
                    while (rs.next()){
                        tsdate = rs.getDate("tsdate");
                        tedate = rs.getDate("tedate");
                        eno = rs.getString("eno");
                    }
                    //把请假日期之内的每一天的考勤信息都设置成请假状态
                    int days = (int)(tedate.getTime() - tsdate.getTime())/86400000+1;
                    String sql;
                    for(int i = 0;i < days;i++){
                        java.sql.Date tmp = new java.sql.Date((tsdate.getTime()+86400000*i)*1000);
                        //判断这一天是否已经请假或出差
                        sql = "SELECT * FROM attendance WHERE adate = '"+tmp+"' AND astate != 1";
                        rs = stmt.executeQuery(sql);
                        if (rs.next())//next有值返回true
                            sql = "UPDATE attendance SET astate ='"+ 3 + "' WHERE adate ="+tmp;
                        else
                            sql = "INSERT INTO attendance(eno,adate,astate) VALUES ('"+eno + "', '" + tmp + "', '" + 3 + "')";
                        stmt.execute(sql);
                    }
                    System.out.println();
                }
                break;
            default:
                System.out.println("输入不合法！");
        }
    }

    public String check_dno(String eno) throws Exception{
        ResultSet rs = stmt.executeQuery("SELECT * FROM belong WHERE eno ='" + eno + "'");
        String dno = "";
        while (rs.next())
            dno = rs.getString(2);
        return dno;
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
        String sql = "insert into log(ldate, ltime, eno, operation) VALUES ('" + sqlDate + "', '" + nowTime + "', '" + manager_eno + "' ,'" + log + "')";
        stmt.executeUpdate(sql);
    }
}