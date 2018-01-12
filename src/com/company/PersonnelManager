import java.sql.ResultSet;
import java.sql.Statement;
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
                        "1. 根据姓名或工号查看某员工考勤情况\n" +
                        "2. 查看某个部门里所有员工考勤情况\n" +
                        "3. 查看已审批的差假申请的历史记录\n" +
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

        rs = stmt.executeQuery("SELECT * FROM attendance WHERE eno=" + employee_eno);
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
        //查询该部门下所有员工的考勤信息
        rs = stmt.executeQuery("SELECT * FROM attendance natural join belong WHERE dno =" + dno);
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

    /**
     * 查看所有已批准或拒绝的审批历史记录
     */
    public void view_application() throws Exception {
        System.out.println("您可以查看所有的请假或出差申请，1：请假，2：出差");
        Scanner scanner = new Scanner(System.in);
        ResultSet rs;
        switch (scanner.nextLine()) {
            case "1":
                //根据这个部门的编号dno和部门经理的编号mno查看他的相关请假申请
                String sql = "SELECT lno,eno,lsdate,ledate,ltype,lreason FROM `leave` natural join checkleave " +
                        " WHERE lstate != '1' ";
                rs = stmt.executeQuery(sql);
                System.out.println("以下是所有已拒绝或通过的请假申请的历史记录");
                System.out.println("序号-----部门经理工号-----开始日期------结束日期-----请假类型-----请假理由");
                String line;
                while (rs.next()) {
                    line = rs.getString("lno") + "     ";
                    line += rs.getString("eno") + "     ";
                    line += rs.getDate("lsdate" + "     ");
                    line += rs.getDate("ledate" + "     ");
                    int type = rs.getInt("ltype" + "     ");
                    if (type == 1)
                        line += "事假";
                    else if (type == 2)
                        line += "病假";
                    else if (type == 3)
                        line += "产假";
                    else if (type == 4)
                        line += "婚假";
                    else
                        line += "其他";
                    line += rs.getString("lreason");
                    System.out.println(line);
                }
                break;
            case "2":
                //根据这个部门经理的工号mno查看他的相关请假申请
                sql =  sql = "SELECT tno,tsdate,tedate,ttype,treason FROM trip natural join checktrip" +
                        " WHERE 'checktrip.tstate' != '1' ";
                rs = stmt.executeQuery(sql);
                System.out.println("以下是已拒绝或通过的所有出差申请的历史记录");
                System.out.println("序号-----员工序号-----开始日期------结束日期-----出差类型-----出差理由");
                while (rs.next()) {
                    line = rs.getString("tno") + "      ";
                    line += rs.getString("eno") + "      ";
                    line += rs.getDate("tsdate" + "      ");
                    line += rs.getDate("tedate" + "      ");
                    int type = rs.getInt("ttype" + "      ");
                    if (type == 1)
                        line += "公司指派";
                    else if (type == 2)
                        line += "个人申请";
                    line += rs.getString("lreason");
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
                System.out.println("序号-----主管工号-----审批状态-----拒绝理由");
                while (rs.next()){
                    line = rs.getString("lno     ");
                    line = mno;
                    line += rs.getInt("lstate     ");
                    line += rs.getString("lrefuse");
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
                    System.out.println("请输入拒绝理由(若同意或无理由请输入null):");
                    scanner = new Scanner(System.in);
                    String lrefuse = scanner.nextLine();
                    stmt.execute("update checkleave set lstate="+lstate+" ,lrefuse="+lrefuse+" where lno="+lno);
                    System.out.println("审批成功！");
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
                        " AND 'checktrip.tstate' = 1 ");
                System.out.println("以下是 "+dname+" 部门主管待处理的出差申请：");
                System.out.println("序号-----主管工号-----审批状态-----拒绝理由");
                while (rs.next()){
                    line = rs.getString("tno     ");
                    line = mno;
                    line += rs.getInt("tstate     ");
                    line += rs.getString("trefuse");
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
                    System.out.println("请输入拒绝理由(若同意或无理由请输入null):");
                    scanner = new Scanner(System.in);
                    String trefuse = scanner.nextLine();
                    stmt.execute("update checktrip set tstate='"+tstate+"' , trefuse = "+trefuse+" WHERE tno="+tno);
                    System.out.println("审批成功！");
                    System.out.println();
                }
                break;
            default:
                System.out.println("输入不合法！");
        }
    }
}

