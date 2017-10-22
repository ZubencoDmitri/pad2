package discovery;

import model.NodeInfo;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by Vladok on 19.10.2016.
 */
public class DiscoveryClient {

    private static final String udpMulticastAdress="224.10.10.5";
    private static final String udpGetNodesMessage="get nodes";
    private static final int nodesCount=3;

    private List<NodeInfo>nodeInfoList;
    private byte[] sendData;
    private byte[] receiveData;
    private InetAddress IPAddress;
    private DatagramSocket clientSocket;

    public DiscoveryClient() throws SocketException, UnknownHostException {
        clientSocket = new DatagramSocket();
        clientSocket.setSoTimeout((int) SECONDS.toMillis(10));
        IPAddress = InetAddress.getByName(udpMulticastAdress);
        sendData= new byte[1024];
        receiveData= new byte[1024];
        nodeInfoList=new ArrayList<>();
    }
    public void connect() throws IOException {
        sendData = udpGetNodesMessage.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 12345);
        clientSocket.send(sendPacket);
        for(int i=0;i<nodesCount;i++)
        {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);
            String modifiedSentence = new String(receivePacket.getData(), 0, receivePacket.getLength(), "US-ASCII");
            int port=Integer.parseInt(modifiedSentence.substring(modifiedSentence.indexOf("=")+1,modifiedSentence.indexOf(" and")));
            int nodes=Integer.parseInt(modifiedSentence.substring(modifiedSentence.lastIndexOf("=")+1,modifiedSentence.indexOf(".")));
            System.out.println("Port is "+port +" and nodes count is "+nodes);
            nodeInfoList.add(new NodeInfo(port,nodes));
        }
        clientSocket.close();
    }
    public int getMaxNodesTcpPort() {
        int max=0;
        int port=3002;                      //default port
        for (NodeInfo n:nodeInfoList) {
            int nodes=n.getNumberOfNodes();
            if(nodes>max)
            {
                max=nodes;
                port=n.getPort();
            }
        }
        return port;
    }
}
