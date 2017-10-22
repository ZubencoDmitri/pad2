import discovery.DiscoveryClient;
import model.Employee;
import transport.TransportClient;

import java.io.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Vladok on 28.10.2016.
 */
public class ClientApp {
    public static void main(String[] args) throws IOException {

        System.out.println("----------Client started----------");
        System.out.println("--------------UDP-----------------");
        DiscoveryClient discoveryClient=new DiscoveryClient();
        discoveryClient.connect();
        int maxNodesTcpPort=discoveryClient.getMaxNodesTcpPort();               //get the TCP PORT of the node,which
        System.out.println("Was chosen node with maximum nodes.It's port is "
                +maxNodesTcpPort+" to get all data");                           //contains MAX Connected nodes

        System.out.println("--------------TCP-----------------");
        TransportClient transportClient=new TransportClient(maxNodesTcpPort);
        transportClient.connect();
        //transportClient.getData();                                              //get the whole data from this node

        System.out.println("--------------FILTER--------------");
        filter(transportClient.getData());
    }
    public static void filter(List<Employee>employeeList) throws IOException {
        BufferedReader bf=new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Input the minimum salary");
        int salary= Integer.parseInt(bf.readLine());
        System.out.println("Discovered employees: " +
                employeeList.stream()
                        .filter(e -> e.getSalary() > salary)
                        .sorted(Comparator.comparing(Employee::getLastName))
                        .collect(Collectors.groupingBy(Employee::getDepartment))
                        .toString()
        );
    }
}
