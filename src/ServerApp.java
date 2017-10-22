import model.Employee;
import model.ServerNode;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vladok on 28.10.2016.
 */
public class ServerApp {
    public static void main(String[] args) throws IOException {
        //node1 - maven
        List<Integer> ports1=new ArrayList<>();
        ports1.add(2001);//internal port of node 2
        ports1.add(4001);//internal port of node 3
        int tcpPort1External=3002;
        int tcpPort1Internal=3001;
        List<Employee>employeeList1=new ArrayList<>();
        employeeList1.add(new Employee("John","Zxc","D1",555.0));
        employeeList1.add(new Employee("Bill","Asd","D1",255.0));
        employeeList1.add(new Employee("Ed","Qwerty","D1",500.0));
        employeeList1.add(new Employee("Ann","JJ","D1",660.0));
        //node2
        List<Integer> ports2=new ArrayList<>();
        ports2.add(3001);//internal port of node 1
        int tcpPort2External=2002;
        int tcpPort2Internal=2001;
        List<Employee>employeeList2=new ArrayList<>();
        employeeList2.add(new Employee("Andy","QQ","D2",543.0));
        employeeList2.add(new Employee("Billy","WW","D2",798.0));
        employeeList2.add(new Employee("Vladok","L","D2",5000.0));
        employeeList2.add(new Employee("David","DD","D2",650.0));
        //node3
        List<Integer> ports3=new ArrayList<>();
        ports3.add(4001);//internal port of node 1
        int tcpPort3External=4002;
        int tcpPort3Internal=4001;
        List<Employee>employeeList3=new ArrayList<>();
        employeeList3.add(new Employee("Sylvana","A33","D3",9999.0));
        employeeList3.add(new Employee("Varian","Rynn","D3",9999.0));
        employeeList3.add(new Employee("Grom","Hellscream","D3",9999.0));
        employeeList3.add(new Employee("Orgrym","Doomhammer","D3",9999.0));
        //Server Nodes Start
        System.out.println("----------Servers started----------");

        ServerNode s1= new ServerNode(tcpPort1External,tcpPort1Internal,ports1,employeeList1);
        ServerNode s2=new ServerNode(tcpPort2External,tcpPort2Internal,ports2,employeeList2);
        ServerNode s3=new ServerNode(tcpPort3External,tcpPort3Internal,ports3,employeeList3);
        //server 1 start
        s1.tcpAllEmployeeListRequest();
        s1.tcpSpecialEmployeeListRequest();
        s1.udpNodesCountRequest();
        //server 2 start
        s2.tcpSpecialEmployeeListRequest();
        s2.udpNodesCountRequest();
        s2.tcpAllEmployeeListRequest();
        //server 3 start
        s3.tcpSpecialEmployeeListRequest();
        s3.udpNodesCountRequest();
        s3.tcpAllEmployeeListRequest();

    }
}
