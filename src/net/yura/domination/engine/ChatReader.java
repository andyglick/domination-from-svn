// Yura Mamyrin, Group D

package net.yura.domination.engine;

import java.io.BufferedReader;
import java.io.IOException;

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
		//RiskUtil.printStackTrace(e);

	}
	//System.out.println("ChatReader Terminating: " + myIndex);
   }
}
