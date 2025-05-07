package servidor;

import java.io.*;
import java.net.*;

public class ServidorArchivos {
    private static final int PUERTO = 5002;
    private static final String CARPETA_DESTINO = "archivos_recibidos/";

    public static void main(String[] args) {
        File carpeta = new File(CARPETA_DESTINO);
        if (!carpeta.exists()) {
            boolean creada = carpeta.mkdir();
            System.out.println("Carpeta creada: " + creada);
        }

        try (ServerSocket servidor = new ServerSocket(PUERTO)) {
            System.out.println("Servidor esperando archivos en el puerto " + PUERTO);

            while (true) {
                Socket cliente = servidor.accept();
                System.out.println("Cliente conectado: " + cliente.getInetAddress());

                new Thread(() -> manejarCliente(cliente)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void manejarCliente(Socket cliente) {
        try (DataInputStream entrada = new DataInputStream(cliente.getInputStream());
             DataOutputStream salida = new DataOutputStream(cliente.getOutputStream())) {
            
            while (true) {
                // Recibir nombre del archivo
                String nombreArchivo = entrada.readUTF();
                
                // Verificar si es mensaje de finalización
                if (nombreArchivo.equals("FIN")) {
                    System.out.println("Cliente finalizó la sesión: " + cliente.getInetAddress());
                    break;
                }
                
                // Recibir tamaño del archivo
                long tamañoArchivo = entrada.readLong();
                File archivoDestino = new File(CARPETA_DESTINO + nombreArchivo);
                
                System.out.println("Recibiendo archivo: " + nombreArchivo + " (" + tamañoArchivo + " bytes)");
                System.out.println("Guardando en: " + archivoDestino.getAbsolutePath());

                // Recibir contenido del archivo
                try (FileOutputStream fos = new FileOutputStream(archivoDestino)) {
                    byte[] buffer = new byte[4096];
                    int bytesLeidos;
                    long bytesRecibidos = 0;

                    while ((bytesLeidos = entrada.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesLeidos);
                        bytesRecibidos += bytesLeidos;
                        if (bytesRecibidos >= tamañoArchivo) break;
                    }
                }

                // Confirmar recepción
                System.out.println("Archivo recibido con éxito: " + nombreArchivo);
                salida.writeUTF("Archivo recibido correctamente.");
            }
            
        } catch (IOException e) {
            System.out.println("Error al manejar el cliente: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                cliente.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
