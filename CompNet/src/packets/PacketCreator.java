package packets;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Creates the TFTP-packets as specfied by RFC 1350. These include: RRQ, WRQ, Data, ACK, Error.
 * The method to create a RRQ-packet is already implemented. The students are supposed to implement the methods to create
 * a WRQ-packet, Data-packet, ACK-packet and Error-packet. They will be used later to create the packets on
 * the server- and client-side.
 * Note: You may want to add tests for the parameters of a method.
 * @author Sebastian Becker, sbecker@hm.edu
 * 
 */
public class PacketCreator {
	/** Creates a RRQ-packet as specified by RFC 1350.
	 * @param fileName The name of the file.
	 * @return Returns an array which contains the data for a RRQ-packet.
	 */
	public byte[] createRRQPacket(final String fileName){
		//OPCode for the RRQ is 1
		byte[] opCode = {0, 1};
		//Get the Bytes of the fileName
		byte[] fileNameInBytes = fileName.getBytes();
		//For this project you only need to use the mode "netascii"
		byte[] modeInBytes = "netascii".getBytes();

		//Create an Index for filling the rrq-array
		int rrqIndex = 0;
		//Calculate the length of the array to store the information in. 2 = length of OPCode, 1+1 = length of two zero-bytes
		int rrqLength = 2 + fileName.length() + 1 + modeInBytes.length + 1;

		//Create an array for the rrq
		byte[] rrq = new byte[rrqLength];

		//Copy the opCode into the rrq-message-array
		System.arraycopy(opCode, 0, rrq, rrqIndex, opCode.length);
		rrqIndex += 2;

		//Copy the fileName into the rrq-message-array
		System.arraycopy(fileNameInBytes, 0, rrq, rrqIndex, fileNameInBytes.length);
		rrqIndex += fileNameInBytes.length;

		//Copy the zero-byte (used to differ between fileName and mode) into the rrq-message-array
		rrq[rrqIndex++] = 0;

		//Copy the mode into the rrq-message-array
		System.arraycopy(modeInBytes, 0, rrq, rrqIndex, modeInBytes.length);
		rrqIndex += modeInBytes.length;

		//Copy the second zero-byte (used to indicate the end of the message) into the rrq-message-array
		rrq[rrqIndex] = 0;

		//Return the created Array
		return rrq;
	}

	public byte[] createWRQPacket(final String fileName){
		byte[] opCode = {0, 2};
		byte[] fileNameInBytes = fileName.getBytes();
		byte[] modeInBytes = "netascii".getBytes();

		int wrqIndex = 0;
		//Calculate the length of the array to store the information in. 2 = length of OPCode, 1+1 = length of two zero-bytes
		int wrqLength = 2 + fileName.length() + 1 + modeInBytes.length + 1;

		//Create an array for the wrq
		byte[] wrq = new byte[wrqLength];

		//Copy the opCode into the wrq-message-array
		System.arraycopy(opCode, 0, wrq, wrqIndex, opCode.length);
		wrqIndex += 2;

		//Copy the fileName into the wrq-message-array
		System.arraycopy(fileNameInBytes, 0, wrq, wrqIndex, fileNameInBytes.length);
		wrqIndex += fileNameInBytes.length;

		//Copy the zero-byte (used to differ between fileName and mode) into the wrq-message-array
		wrq[wrqIndex++] = 0;

		//Copy the mode into the wrq-message-array
		System.arraycopy(modeInBytes, 0, wrq, wrqIndex, modeInBytes.length);
		wrqIndex += modeInBytes.length;

		//Copy the second zero-byte (used to indicate the end of the message) into the wrq-message-array
		wrq[wrqIndex] = 0;

		//Return the created Array
		return wrq;
	}

	/** Creates a Data-packet as specified by RFC 1350.
	 * @return Returns an array which contains the data for a Data-packet.
	 */
	public byte[] createDataPacket(byte[] datapacket,short num){
		byte[] opCode = {0, 3};
        byte[] blocknum=shortToBytes(num);

        System.out.println(blocknum.length);
		int dataIndex = 0;


		int dataLength = 2 + 2 + datapacket.length;

		byte[] data = new byte[dataLength];

		System.arraycopy(opCode, 0, data, dataIndex, opCode.length);
		dataIndex += 2;

		System.arraycopy(blocknum, 0, data, dataIndex, 2);
		dataIndex += 2;


		System.arraycopy(datapacket, 0, data, dataIndex, datapacket.length);
		dataIndex += datapacket.length;

		return data;
	}

	/** Creates an ACK-packet as specified by RFC 1350.
	 * @return Returns an array which contains the data for an ACK-packet.
	 */
	public byte[] createACKPacket(short num){
		byte[] opCode = {0, 4};
        byte[] blocknum=shortToBytes(num);

		int ackIndex = 0;
		int ackLength = 2 + 2;

		byte[] ack = new byte[ackLength];

		System.arraycopy(opCode, 0, ack, ackIndex, opCode.length);
		ackIndex += 2;

		System.arraycopy(blocknum, 0, ack, ackIndex, 2);
		ackIndex += 2;

		return ack;
	}

	/** Creates an Error-packet as specified by RFC 1350.
	 * @return Returns an array which contains the data for an Error-packet.
	 */
	public byte[] createErrorPacket(short errorNum){
		//OPCode for the RRQ is 1
		byte[] opCode = {0, 5};
		byte[] errorCode=ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(errorNum).array();
		//Get the Bytes of the fileName
		String[] errorMessages=new String[]{    "Not defined, see error message( if any).",
				"File not found.",
				"Access violation.",
				"Disk full or allocation exceeded.",
				"Illegal TFTP operation.",
				"Unknown transfer ID.",
				"File already exists.",
				"No such user.",
		};



		ByteBuffer wrapped = ByteBuffer.wrap(errorCode);
		short num = wrapped.getShort();

		byte[] error = errorMessages[(int)num].getBytes();

		int errorIndex = 0;
		int errorLength = 2 + 2 + error.length + 1;

		byte[] errorPacket = new byte[errorLength];

		System.arraycopy(opCode, 0, errorPacket, errorIndex, opCode.length);
		errorIndex += 2;

		System.arraycopy(errorCode, 0, errorPacket, errorIndex, errorCode.length);
		errorIndex += 2;


		System.arraycopy(error, 0, errorPacket, errorIndex, error.length);
		errorIndex += error.length;

		errorPacket[errorIndex] = 0;

		return errorPacket;
	}




	public byte[] fileToBytes(String path)throws Exception
    {
        File file = new File(path);
        FileInputStream is = new FileInputStream(file);
        byte[] block = new byte[(int) file.length()];
        is.read(block);



        return block;

    }






    public short bytesToShort(byte[] bytes) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }
    public byte[] shortToBytes(short value) {
        return ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN).putShort(value).array();
    }









}
