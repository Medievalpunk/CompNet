package packets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Extracts information from a given TFTP-packet. This Class may be used to identify received packets in the TFTP-Client and Server.
 * Note: You may want to add tests for the parameters of a method.
 * @author Sebastian Becker, sbecker@hm.edu
 *
 */
public class PacketExtractor {

	/** Extract the opCode from the given TFTP-packet.
	 * @param packet TFTP-packet to extract the opCode from.
	 * @return Returns the opCode as a short.
	 */
	public short extractOPCode(final byte[] packet) {
		short ret =-1;
		byte[] temp=new byte[2];
		for (short i =0;i<2;i++)
			temp[i]=packet[i];
		ret=bytesToShort(temp);





		return ret;
	}
	
	/**
	 * Extract the file-name from the given TFTP-packet.
	 * @param packet RRQ- or WRQ-packet to extract the file-name from.
	 * @return The file-name as a String.
	 */
	public String extractFileName(final byte[] packet) {

		if (extractOPCode(packet)==1||extractOPCode(packet)==2)
		{
			short c=2;
			for (;packet[c]!=0;c++);

			int nameLength=c-2;
			byte[]temp=new byte[nameLength];


			for (short i =2;i<nameLength+2;i++)
				temp[i]=packet[i];


			return new String(temp);


		}
		return null;


	}
	
	/** Extract the blockNumber from the given TFTP-packet.
	 * @param packet TFTP-packet to extract the blockNumber from.
	 * @return The blockNumber as a short.
	 */
	public short extractBlockNumber(final byte[] packet) {

	    if(extractOPCode(packet)!=3||extractOPCode(packet)!=4)
	        return -1;




        byte[] temp=new byte[2];
        for (short i =2;i<4;i++)
            temp[i]=packet[i];
        return bytesToShort(temp);

	}
	
	/** Extract the data from a data-packet.
	 * @param packet The data-packet to extract the data from.
	 * @return A byte-array containing the data.
	 */
	public byte[] extractData(final byte[] packet){

        if(extractOPCode(packet)!=3)
            return null;



        int dataLength=packet.length-4;
        byte[]temp=new byte[dataLength];


        for (short i =4;i<dataLength+4;i++)
            temp[i]=packet[i];



        return temp;
	}
	
	/**
	 * Extract the error-number from an error-packet.
	 * @param packet The error-packet to extract the error-number from.
	 * @return The error-number as a short.
	 */
	public short extractErrorNumber(final byte[] packet){


        if(extractOPCode(packet)!=5)
            return -1;




        byte[] temp=new byte[2];
        for (short i =2;i<4;i++)
        temp[i]=packet[i];
        return bytesToShort(temp);


	}
	
	/**
	 * Extract the error-message from an error-packet.
	 * @param packet The error-packet to extract the error-message from.
	 * @return The error-message as a String.
	 */
	public String extractErrorMessage(final byte[] packet) {



        if(extractOPCode(packet)!=3)
            return null;



        int messageLength=packet.length-5;
        byte[]temp=new byte[messageLength];


        for (short i =4;i<messageLength + 4;i++)
            temp[i]=packet[i];



        return new String(temp);

	}





	public short bytesToShort(byte[] bytes) {
		return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
	}
	public byte[] shortToBytes(short value) {
		return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array();
	}




}
