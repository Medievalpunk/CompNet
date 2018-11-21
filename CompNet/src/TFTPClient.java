import packets.PacketCreator;
import packets.PacketExtractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;


public class TFTPClient {



    PacketCreator m_packeteer = new PacketCreator();
    PacketExtractor m_exctractor= new PacketExtractor();

    short port=69;


    public static void main(String[]args) throws Exception
    {

        TFTPClient tftp = new TFTPClient();


        tftp.m_main();
    }



    boolean live=true;

    public void m_main() throws Exception{
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress adr=InetAddress.getLocalHost();


        Scanner input = new Scanner(System.in);
        String[] command;
        while (live)
        {
            System.out.print(">");
            command=input.nextLine().split("\\s+");
            System.out.println(command.length);
            if(command[0].equals("connect")&&command.length==2)
            {
                adr=InetAddress.getByName(command[1]);
            }
            else if(command[0].equals("get")&&command.length==3)
            {
               byte[] packet=m_packeteer.createRRQPacket(command[1]);
               clientSocket.send(new DatagramPacket(packet,packet.length,adr,69));
               receiveData(clientSocket,3000,command[2]);



            }
            else if(command[0].equals("put")&&command.length==3)
            {
                byte[] packet=m_packeteer.createWRQPacket(command[1]);
                clientSocket.send(new DatagramPacket(packet,packet.length,adr,69));
                sendData(clientSocket,3000,adr,command[2]);

            }
            else if(command[0].equals("quit"))
            {
                live=false;
            }


        }







       /* for (int i=0;i<packet.length;i++) {
            clientSocket.send(packet[i]);
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
        }




        //Close the socket at the end*/



        clientSocket.close();

    }

    boolean receiveData(DatagramSocket dataSocket,int timeout,String filename)throws Exception
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
            } catch (IOException e) {

            }
            byte []data=receivedPacket.getData();

            if(m_exctractor.extractOPCode(data) == 3 && m_exctractor.extractBlockNumber(data) > blocknum)
            {
                receivedFile.add(m_exctractor.extractData(data));
                blocknum=m_exctractor.extractBlockNumber(data);
                dataSocket.send(new DatagramPacket(m_packeteer.createACKPacket(blocknum), 4));
            }
            if((data.length - 4) < 512) transferComplete = true;
        }

        for(byte[] section : receivedFile)
        {
            os.write(section);

        }





    return true;
    }


    public void sendData(DatagramSocket socket,int timeout,InetAddress ip, String filename) throws Exception {

        boolean lastPackage = false;

        short blocknum = 1;

        byte[] currentData;

        byte[] dataPacketBuffer;

        DatagramPacket dataPacket;
        DatagramPacket ACK;




        File file = new File(filename);
        FileInputStream is=new FileInputStream(file);
        long filelength=file.length();
        long numOfSegments=(filelength/512)+1;
        long lastSegmentLength=filelength-(512*numOfSegments);
        long offsetCounter=0;


        System.out.println("Starting the rrq-protocol on the server.");
        try {
            socket.setSoTimeout(timeout);
        } catch (SocketException e)
        {
            e.printStackTrace();
        }


        while(blocknum<=numOfSegments) {
            System.out.println((int)lastSegmentLength);
            if(blocknum==numOfSegments&&lastSegmentLength>0)
            {
                currentData=new byte[(int)lastSegmentLength];
                is.read(currentData,(int)offsetCounter,(int)lastSegmentLength);
            }else{

                currentData = new byte[512];

                is.read(currentData, (int) offsetCounter, 512);
            }
            dataPacketBuffer = m_packeteer.createDataPacket(currentData,blocknum);

            dataPacket = new DatagramPacket(dataPacketBuffer, dataPacketBuffer.length, ip, port);

            socket.send(dataPacket);
            System.out.println(blocknum);
            byte[] ack=new byte[4];
            ACK= new DatagramPacket(ack,4);
            try {
                socket.receive(ACK);
            }catch (SocketTimeoutException e)
            {
                continue;
            }



            blocknum++;
            offsetCounter+=512;


            System.out.println("Sending packet " + blocknum + " with the length = " + currentData.length);



        }

    }

}
