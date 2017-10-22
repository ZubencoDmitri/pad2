package model;

/**
 * Created by Vladok on 07.11.2016.
 */
public class NodeInfo {
    private int port;
    private int numberOfNodes;

    public NodeInfo(int port, int numberOfNodes) {
        this.port = port;
        this.numberOfNodes = numberOfNodes;
    }

    public int getPort() {
        return port;
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }
}
