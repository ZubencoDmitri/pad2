package model;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static org.apache.commons.lang3.SerializationUtils.deserialize;
import static org.apache.commons.lang3.SerializationUtils.serialize;
/**
 * Created by Vladok on 28.10.2016.
 */
public class ServerNode {
    private List<Employee> employeesList;
    private List<Integer> connectedPortsList;
    private MulticastSocket udpServerSocket;
    private ServerSocket tcpClientServerSocket;
    private ServerSocket tcpNodeServerSocket;
    private int tcpClientPort;
    private int tcpNodePort;
    private byte[] receiveData;
    private byte[] sendData;
    private static final int udpMulticastPort = 12345;
    private static final String udpMulticastIp = "224.10.10.5";
    private InetAddress mcIPAddress;
    public ServerNode(int tcpClientPort,
                      int tcpNodePort,
                      List<Integer> connectedPortsList,
                      List<Employee> employeesList
    ) throws IOException {
        receiveData = new byte[1024];
        sendData = new byte[1024];
        this.tcpClientPort=tcpClientPort;
        this.tcpNodePort=tcpNodePort;
        mcIPAddress = InetAddress.getByName(udpMulticastIp);
        udpServerSocket = new MulticastSocket(udpMulticastPort);
        udpServerSocket.joinGroup(mcIPAddress);
        tcpClientServerSocket = new ServerSocket(tcpClientPort);
        tcpNodeServerSocket=new ServerSocket(tcpNodePort);
        this.connectedPortsList=connectedPortsList;
        this.employeesList=employeesList;
    }
    /** UDP MULTICAST message = "get nodes".
     *  uses MulticastSocket
     *  returns the number of connected nodes
     */
    public void udpNodesCountRequest(){
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                while(true)
                {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    try {
                        udpServerSocket.receive(receivePacket);
                        InetAddress IPAddress = receivePacket.getAddress();
                        int port = receivePacket.getPort();
                        String sentence=new String( receivePacket.getData(), 0, receivePacket.getLength(), "US-ASCII");
                        System.out.println("Connected");
                        if(sentence.equals("get nodes"))
                        {
                            sendData = ("Port="+tcpClientPort+" and nodesCount="+connectedPortsList.size()+".").getBytes();
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                            udpServerSocket.send(sendPacket);

                        }
                        System.out.println("Received the message = "+sentence+". And sent port = "+tcpClientPort+" and nodesCount = "+connectedPortsList.size());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //udpServerSocket.leaveGroup(mcIPAddress);
                //udpServerSocket.close();
            }
        });
        thread.start();
    }
    /*  TCP UNICAST message = "get special data"
     *  uses Socket
     *  returns node employeeList
     */
    public void tcpSpecialEmployeeListRequest(){
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                while(true)
                {
                    InputStream istream = null;
                    StringBuffer externalResult = new StringBuffer();
                    try {
                        Socket connectionSocket = tcpNodeServerSocket.accept();
                        istream = connectionSocket.getInputStream();
                        BufferedReader receiveRead = new BufferedReader(new InputStreamReader(istream));
                        String partlyTransData;
                        while(!(partlyTransData=receiveRead.readLine()).isEmpty())
                            externalResult.append(partlyTransData.trim());
                        String message=externalResult.toString();
                        if(message.equals("get special data"))
                        {
                            System.out.println("Server with external port = " + tcpClientPort
                                    + " has got a message : "+ message);
                            OutputStream ostream = connectionSocket.getOutputStream();
                            Employee[] s = new Employee[employeesList.size()];
                            serialize((Employee[]) employeesList.toArray(s), ostream);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }
    /*  TCP UNICAST message = "get all data"
     *  uses Socket
     *  returns all connected nodes' employeeLists
     */
    public void tcpAllEmployeeListRequest(){
        List<Employee>theWholeList=new ArrayList<>();
        theWholeList.addAll(employeesList);
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                while(true)
                {
                    String fullDataRequest="";
                    InputStream istream = null;
                    StringBuffer externalResult = new StringBuffer();
                    try {
                        Socket connectionSocket = tcpClientServerSocket.accept();
                        istream = connectionSocket.getInputStream();
                        BufferedReader receiveRead = new BufferedReader(new InputStreamReader(istream));
                        String partlyTransData;
                        while(!(partlyTransData=receiveRead.readLine()).isEmpty())
                            externalResult.append(partlyTransData.trim());
                        String message= externalResult.toString();
                        if(message.equals("get all data"))
                        {
                            System.out.println("Server with external port = " + tcpClientPort
                                    + " has got a message : "+ message);
                            ExecutorService executorService= Executors.newCachedThreadPool();
                            System.out.println("Port used to connect to special nodes = "+ tcpClientPort);
                            Semaphore semaphore=new Semaphore(connectedPortsList.size()+1);
                            for (int port:connectedPortsList) {
                                System.out.println("Current port is "+port);
                                Callable<Employee[]> callableThread=new Callable<Employee[]>() {
                                    @Override
                                    public Employee[] call() {
                                        Socket sock = null;
                                        Employee[] employees=null;
                                        try {
                                            sock = new Socket("localhost", port);
                                            //send
                                            OutputStream ostream = sock.getOutputStream();
                                            PrintWriter pwrite = new PrintWriter(ostream, true);
                                            //receive
                                            InputStream istream = sock.getInputStream();
                                            BufferedReader receiveRead = new BufferedReader(new InputStreamReader(istream));
                                            //message
                                            String sendMessage = "get special data\n";
                                            //send
                                            pwrite.println(sendMessage);       // sending to server
                                            pwrite.flush();                    // flush the data
                                            employees = (Employee[]) deserialize(istream);

                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        System.out.println("Returned data from Server with internal port = "
                                                +port+" to Server with external port = " + tcpClientPort);
                                        return employees;
                                    }
                                };
                                Future<Employee[]> future = executorService.submit(callableThread);
                                while(!future.isDone());
                                try{
                                    Employee[] employees=future.get();
                                    theWholeList.addAll(new ArrayList<Employee>(Arrays.asList(employees)));
                                    semaphore.release();
                                } catch (InterruptedException ie) {
                                    ie.printStackTrace(System.err);
                                } catch (ExecutionException ee) {
                                    ee.printStackTrace(System.err);
                                }
                            }
                            executorService.shutdown();
                            semaphore.acquire();
                            System.out.println("Full data from Server Node with External port = "+tcpClientPort+" is next:");
                            for (Employee e:theWholeList) {
                                System.out.println(e.toString());
                            }
                            OutputStream ostream = connectionSocket.getOutputStream();
                            Employee[] s = new Employee[theWholeList.size()];
                            serialize((Employee[]) theWholeList.toArray(s), ostream);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }
}
