import java.net.ServerSocket;

public class Main {
    public static void main(String[] args) {
	    int port = 0x7530;    
		if(args.length > 0)
			port = Integer.parseInt(args[0]);
		
		System.out.println("Hello World!");
    }
}
