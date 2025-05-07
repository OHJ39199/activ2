package cliente;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class TransferenciaArchivosCliente2 {
		private static final String HOST = "10.140.132.36";
	    private static final int PUERTO = 5002;
	    
	    public static void main(String[] args) {
	        try (Socket socket = new Socket(HOST, PUERTO)) {
	            // Confirmación conexión servidor
	            System.out.println("Conectado al servidor: " + HOST);
	            
	            // Input output streams
	            DataInputStream dis = new DataInputStream(socket.getInputStream());
	            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
	            BufferedReader tc = new BufferedReader(new InputStreamReader(System.in));
	            
	            while (true) {
	                // Ruta archivo local
	                System.out.println("Introduce la ruta del archivo (o 'FIN' para salir): ");
	                String filePath = tc.readLine();
	                
	                // Verificar si el usuario quiere terminar
	                if (filePath.equalsIgnoreCase("FIN")) {
	                    dos.writeUTF("FIN");
	                    System.out.println("Sesión terminada.");
	                    break;
	                }
	                
	                // Comprobar existe archivo
	                File file = new File(filePath);
	                if (!file.exists() || !file.isFile()) {
	                    System.out.println("Error: el documento no existe o no es un formato válido");
	                    continue;
	                }
	                
	                // Enviar archivo
	                dos.writeUTF(file.getName());
	                dos.writeLong(file.length());
	                
	                try (FileInputStream fis = new FileInputStream(file)) {
	                    byte[] buffer = new byte[4096];
	                    int bytesLeidos;
	                    
	                    while ((bytesLeidos = fis.read(buffer)) != -1) {
	                        dos.write(buffer, 0, bytesLeidos);
	                    }
	                }
	                
	                // Confirmación servidor
	                String respuesta = dis.readUTF();
	                System.out.println("Servidor: " + respuesta);
	                
	                // Preguntar si desea enviar otro archivo
	                System.out.println("¿Desea enviar otro archivo? (S/N): ");
	                String respuestaUsuario = tc.readLine();
	                if (!respuestaUsuario.equalsIgnoreCase("S")) {
	                    dos.writeUTF("FIN");
	                    System.out.println("Sesión terminada.");
	                    break;
	                }
	            }
	            
	        } catch (UnknownHostException e) {
	            System.err.println("Error: No se pudo encontrar el host " + HOST);
	            e.printStackTrace();
	        } catch (IOException e) {
	            System.err.println("Error de entrada/salida al comunicarse con el servidor.");
	            e.printStackTrace();
	        }
	    }
	}
