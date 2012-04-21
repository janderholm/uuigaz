import java.net.ServerSocket;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
	    int port = 30000;    
		if(args.length > 0)
			port = Integer.parseInt(args[0]);
		
		ServerSocket socket;
		try{
			socket = new ServerSocket(port);

		} catch (IOException e){

		}
		System.out.println("Hello World!");
    }
}
