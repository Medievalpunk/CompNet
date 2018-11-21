

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

import packets.PacketCreator;
import packets.PacketExtractor;

/**
 * This is the template for the TFTP-server. Please refactor the name to TFTPServer before adjusting and using it in your own project.
 * You do not have to use this template for your project. It is merely a starting point for you. Feel free to implement it on your own.
 * Note: You may want to add tests for the parameters of a method.
 * @author Sebastian Becker, sbecker@hm.edu
 * 
 */
public class TFTPServer {
	//This variable is used to create the TFTP-packets
	private PacketCreator pCreator;
	//This variable is used to extract data from the TFTP-packets
	private PacketExtractor pExtractor;
	//This variable is used to receive incoming TFTP-requests
	private DatagramSocket serverSocket;
	//Store the default server port here
	private int serverPort;
	
	public static void main(String...strings) throws Exception {
		new TFTPServer().startServer();
	}
	
	/**
	 * Default Constructor for the TFTP-Server.
	 * @throws SocketException Throw every exception.
	 */
	TFTPServer() throws SocketException{
		serverPort = 1337;
		serverSocket = new DatagramSocket(serverPort);
		pCreator = new PacketCreator();
		pExtractor = new PacketExtractor();
	}
	
	/** 
	 * Starts the logic of the TFTP-server.
	 * @throws IOException Throw every exception.
	 */
	public void startServer() throws Exception {
		//Store the incoming packet in this variable
		byte[] packet;
		//This variable is the buffer to store the data of incoming requests in
		byte[] requestBuffer;
		//This variable stores incoming requests
		DatagramPacket request;
		
		while(true) {
			//Clear the old buffer
			requestBuffer = new byte[516];
			request = new DatagramPacket(requestBuffer, requestBuffer.length);
			//Wait for an incoming request
			serverSocket.receive(request);
			packet = request.getData();
			//Execute a request depending on the opCode
			switch(pExtractor.extractOPCode(packet)) {
			case 1:
				executeRRQ(request);
				break;
			case 2:
			    System.out.println("WRITE REQUEST");
				executeWRQ(serverSocket,3000,pExtractor.extractFileName(packet));
				break;
			case 3:
				System.err.println("Received a data-packet but expected a rrq- or wrq-packet.");
				break;
			case 4:
				System.err.println("Received an ack-packet but expected a rrq- or wrq-packet.");
				break;
			case 5:
				int errorNumber = pExtractor.extractErrorNumber(packet);
				String errorMsg = pExtractor.extractErrorMessage(packet);
				System.err.println("Received an error-packet with the content: errorNumber = " + errorNumber + " | errorMsg = " + errorMsg);
			}
		}
	}
	
	/**
	 * Executes the Read-Request on the server-side of the TFTP-Protocol.
	 * @param rrqPacket The rrq-packet received by the server.
	 * @throws IOException Throw every exception.
	 */
	public void executeRRQ(final DatagramPacket rrqPacket) throws IOException {
		//Is it the last packet?
		boolean isLastPacket = false;
		//Variable for the blockNumber
		short blockNumber = 1;
		//Store the port of the client here
		int clientPort = rrqPacket.getPort();
		//Store the IP-Address of the client here
		InetAddress clientIP = rrqPacket.getAddress();
		//Store the data to be sent here
		byte[] currentData;
		//This is the buffer for the data-packet
		byte[] dataPacketBuffer;
		//Store the data-packet here
		DatagramPacket dataPacket;
		//Open a new DatagramSocket for this connection
		DatagramSocket newServerSocket = new DatagramSocket();
		//The complete data to send is stored here
		byte[][] data;
		
		//Create some random data, this is only for dummy purposes -> adjust it in your own code
		data = new byte[3][512];
		data[2] = new byte[1];
		for(byte[] d : data)
			for(int i = 0; i < d.length; i++)
				d[i] = 6;
		
		System.out.println("Starting the rrq-protocol on the server.");
		
		//Execute the rrq in this while-loop
		while(!isLastPacket) {
			//The blockNumber starts at 1 but the index need is blockNumber-1
			currentData = data[blockNumber - 1];
			//Create a new data-packet
			dataPacketBuffer = pCreator.createDataPacket(currentData,blockNumber);
			//Encapsulate the data-packet into a UDP-packet
			dataPacket = new DatagramPacket(dataPacketBuffer, dataPacketBuffer.length, clientIP, clientPort);
			//Send the data-packet to the server
			newServerSocket.send(dataPacket);
			//Prepare the ack-packet to receive the ack from the Client
			
			//Wait for the ack-packet to arrive
			
			
			//If the current data is less than 512 bytes it is the last packet
			if(currentData.length < 512)
				isLastPacket = true;
			
			System.out.println("Sending packet " + blockNumber + " with the length = " + currentData.length);
			blockNumber++;
		}
		
		newServerSocket.close();
	}
	
	/**
	 * Executes the Write-Request on the server-side of the TFTP-Protocol.
	 * @param dataSocket The wrq-packet received by the server.
	 */
	public void executeWRQ(DatagramSocket dataSocket,int timeout,String filename)throws Exception
	{
		File file=new File(filename);
		FileOutputStream os=new FileOutputStream(file);
		ArrayList<byte[]> receivedFile= new ArrayList<>();
		byte[] receivedData = new byte[516];
		DatagramPacket receivedPacket=new DatagramPacket(receivedData,receivedData.length);
		short blocknum=-1;
		boolean transferComplete=false;
		try {
			dataSocket.setSoTimeout(timeout);
		} catch (SocketException e)
		{
			e.printStackTrace();
		}

		while(!transferComplete)
		{



			try {
				dataSocket.receive(receivedPacket);
			} catch (SocketTimeoutException e) {
                continue;
			}
			byte []data=receivedPacket.getData();

			if(pExtractor.extractOPCode(data) == 3 && pExtractor.extractBlockNumber(data) > blocknum)
			{
				receivedFile.add(pExtractor.extractData(data));
				blocknum=pExtractor.extractBlockNumber(data);
				dataSocket.send(new DatagramPacket(pCreator.createACKPacket(blocknum), 4));
			}
			if((data.length - 4) < 512) transferComplete = true;
		}

		for(byte[] section : receivedFile)
		{
			os.write(section);

		}





		return;
	}

}