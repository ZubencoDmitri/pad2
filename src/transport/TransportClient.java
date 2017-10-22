package transport;

import model.Employee;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.SerializationUtils.deserialize;

/**
 * Created by Vladok on 19.10.2016.
 */
public class TransportClient {

    private static final String tcpUnicastAdress="localhost";
    private static int tcpUnicastPort=3002;                                 //default
    private static final String tcpGetAllNodesEmployeeLists="get all data";

    private Socket serverSocketToConnect;
    private PrintWriter pwrite;
    private OutputStream ostream;
    private InputStream istream;
    private List<Employee>employeesList;

    public TransportClient(int tcpUnicastPort) throws IOException {
        this.tcpUnicastPort=tcpUnicastPort;
        serverSocketToConnect= new Socket(tcpUnicastAdress, tcpUnicastPort);
        ostream = serverSocketToConnect.getOutputStream();
        pwrite = new PrintWriter(ostream, true);
        istream = serverSocketToConnect.getInputStream();
    }
    public void connect() throws IOException {
        pwrite.println(tcpGetAllNodesEmployeeLists+"\n");
        pwrite.flush();
        Employee[] employees = (Employee[]) deserialize(istream);
        employeesList=new ArrayList<Employee>(Arrays.asList(employees));
        System.out.println("Data received:");
        for (Employee e:employeesList) {
            System.out.println(e.toString());
        }
    }
    public List<Employee> getData()
    {
        return employeesList;
    }
}
