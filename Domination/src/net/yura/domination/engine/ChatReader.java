// Yura Mamyrin, Group D

package risk.engine;

import java.net.*;
import java.io.*;

/**
 * <p> Chat Reader </p>
 * @author Yura Mamyrin
 */

// The ChatReader thread reads incomming socket data and puts it into the
// Chat Area so that all outbound threads can send it out

public class ChatReader extends Thread{
   BufferedReader mySocketInput;
   int myIndex;
   ChatArea myChatArea;

    ChatReader(BufferedReader in,  ChatArea cArea, int index) {
       super("ChatReaderThread");
       mySocketInput = in;
       myIndex = index;
       myChatArea = cArea;
   }

    public void run() {

	String inputLine;

	try {
		while ((inputLine = mySocketInput.readLine()) != null) {

		    myChatArea.putString(myIndex, inputLine);

		}
	}
	catch (IOException e) {

		//System.out.println("ChatReader IOException: "+
		//    e.getMessage());
		//e.printStackTrace();

	}
	//System.out.println("ChatReader Terminating: " + myIndex);
   }
}
