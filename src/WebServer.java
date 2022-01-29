import java.awt.Desktop;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.StringTokenizer;

public class WebServer implements Runnable {

	static final File ROOT = new File(new File("src/").getAbsolutePath());
	static final File WEB_ROOT = new File(new File("src/assets/").getAbsolutePath());
	static final String DEFAULT_FILE = "homepage.html";
	static final int PORT = 80;
	static boolean canRunThread = true;

	// Client Connection via Socket Class
	private Socket connect;

	public WebServer(Socket c) {
		connect = c;
	}

	public static void main(String[] args) {
		compileProgram();
		canRunThread = true;
		startServerThread();
	}

	public static void startServerThread() {
		Thread serverThread = new Thread(new Runnable() {
			public void run() {
				try {
					ServerSocket serverConnect = new ServerSocket(PORT);

					System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");

					// we listen until user halts server execution

					try {
						Desktop.getDesktop().browse(new URI("http://localhost:80"));
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					while (canRunThread) {
						WebServer myServer = new WebServer(serverConnect.accept());

						System.out.println("Connecton opened. (" + new Date() + ")");

						// create dedicated thread to manage the client connection
						Thread thread = new Thread(myServer);
						thread.start();
					}
					serverConnect.close();
				} catch (IOException e) {
					System.err.println("Server Connection error : " + e.getMessage());
				}
			}
		});
		serverThread.setPriority(Thread.MAX_PRIORITY);
		serverThread.setName("Webserver Thread");
		serverThread.start();
	}

	public static void compileProgram() {
//		Object retval = Compiler.command("javac Test.java");
		try {
			Runtime rt = Runtime.getRuntime();
			Process pr = rt.exec("cmd /c start \"\" compilExecute.bat");
			BufferedReader in = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
			String line;
			while ((line = in.readLine()) != null) {
				System.out.println(line);
			}
			in.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() { // manage client connection
		BufferedReader in = null;
		PrintWriter out = null;
		BufferedOutputStream dataOut = null;
		String fileRequested = null;

		try {
			// read characters from client from the socket
			in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			// get character output stream to client (for headers)
			out = new PrintWriter(connect.getOutputStream());
			// get binary output stream to client (for requested data)
			dataOut = new BufferedOutputStream(connect.getOutputStream());

			// get first line of the request from the client
			String input = in.readLine();
			// parse the request with a string tokenizer
			StringTokenizer parse = new StringTokenizer(input);
			System.out.println("Input: " + input);
			String method = parse.nextToken().toUpperCase(); // get the HTTP method of client

			fileRequested = parse.nextToken().toLowerCase();

			if (method.equals("GET") || method.equals("HEAD")) {
				if (fileRequested.endsWith("/")) {
					fileRequested += DEFAULT_FILE;
				}

				File file = new File(WEB_ROOT, fileRequested);
				int fileLength = (int) file.length();
				String content = getContentType(fileRequested);

				if (method.equals("GET")) { // GET method so we return content
					byte[] fileData = readFileData(file, fileLength);

					// send HTTP Headers
					out.println("HTTP/1.1 200 OK");
					out.println("Server: VRS Java Server");
					out.println("Date: " + new Date());
					out.println("Content-type: " + content);
					out.println("Content-length: " + fileLength);
					out.println(); // blank line between headers and content **Very Important**
					out.flush(); // flush output stream buffer

					dataOut.write(fileData, 0, fileLength);
					dataOut.flush();
				}
			}

		} catch (FileNotFoundException fnfe) {
			try {
				fileNotFound(out, dataOut, fileRequested);
			} catch (IOException ioe) {
				System.err.println("Error with file not found exception : " + ioe.getMessage());
			}

		} catch (IOException ioe) {
			System.err.println("Server error : " + ioe);
		} finally {
			try {
				in.close();
				out.close();
				dataOut.close();
				connect.close(); // close socket connection
			} catch (Exception e) {
				System.err.println("Error closing stream : " + e.getMessage());
			}

			System.out.println("Connection closed.\n");
		}

	}

	private byte[] readFileData(File file, int fileLength) throws IOException {
		FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLength];

		try {
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);
		} finally {
			if (fileIn != null)
				fileIn.close();
		}

		return fileData;
	}

	// return supported MIME Types
	private String getContentType(String fileRequested) {
		if (fileRequested.endsWith(".htm") || fileRequested.endsWith(".html"))
			return "text/html";
		else
			return "text/plain";
	}

	private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {
		File file = new File(ROOT, "404.html");
		int fileLength = (int) file.length();
		String content = "text/html";
		byte[] fileData = readFileData(file, fileLength);

		out.println("HTTP/1.1 404 File Not Found");
		out.println("Server: VRS Java Server");
		out.println("Date: " + new Date());
		out.println("Content-type: " + content);
		out.println("Content-length: " + fileLength);
		out.println(); // blank line between headers and content **Very Important**
		out.flush(); // flush output stream buffer

		dataOut.write(fileData, 0, fileLength);
		dataOut.flush();
	}
}
