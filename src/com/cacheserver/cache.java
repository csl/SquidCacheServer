package com.cacheserver;

import java.io.* ;
import java.net.* ;


public class cache {

public static final int MTPORT = 54321;
public static final int NUM_THREADS = 100;
public static Socket clientSocket;

public String URL;  

public cache(int port, int numThreads)
{
	ServerSocket servSock;
	URL = "";
	
	try {
		servSock = new ServerSocket(MTPORT);
	
	} catch(IOException e) {

		System.err.println("Could not create ServerSocket " + e);
		System.exit(1);
		return;
	}
	
	for (int i=0; i<NUM_THREADS; i++)
		new Thread(new Handler(servSock, i)).start();
}

	class Handler extends Thread 
	{
		ServerSocket servSock;
		int threadNumber;
		
		Handler(ServerSocket s, int i) {
			super();
			servSock = s;
			threadNumber = i;
			setName("Thread " + threadNumber);
		}
	
		@SuppressWarnings("deprecation")
		public void run()
		{
			while (true)
			{
				try {
					Socket clientSocket;
					synchronized(servSock) {
						clientSocket = servSock.accept();
					}
					//System.out.println(getName() + " starting, IP=" +clientSocket.getInetAddress());
	
					//repose
					DataInputStream in = new DataInputStream(clientSocket.getInputStream());
				
					String line;
					line = in.readUTF();
					
					if(line.equals("URL"))
					{
						line = in.readUTF();
						URL = line;
				        try {
				            	String s = "squidclient -r -p 80 -m PURGE " + URL;
				                Process p = Runtime.getRuntime().exec(s);
				            }
				            catch (IOException e) {
				                System.out.println("exception happened - here's what I know: ");
				                e.printStackTrace();
				                System.exit(-1);
				            }						
						
						//RepData
						DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
						out.writeUTF("OK");
						out.flush();
					}

			       in.close();
				   clientSocket.close();
				} catch (IOException ex) {
					System.out.println(getName() + ": IO Error on socket " + ex);
					return;
				}
			}	
		}
	}
	
	public static void main(String[] av)
	{
		new cache(MTPORT, NUM_THREADS);
	}
}

