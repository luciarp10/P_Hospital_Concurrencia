
package com.mycompany.plhospital;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {

    
    public static void main(String[] args) {
        String[] puestosVacunacion= new String[10];
        String[] puestosObservacion = new String[20];
        ServerSocket servidor;
        Socket conexion;
        DataOutputStream salida; 
        DataInputStream entrada; 
        
        Hospital hospital = new Hospital();
        HiloCargarDatos inicializador = new HiloCargarDatos(hospital);
        inicializador.start();
        try{
            servidor = new ServerSocket(5000); // Creamos un ServerSocket en el puerto 5000
            System.out.println("Servidor Arrancado....");
            while (true)
            {
                conexion = servidor.accept();     // Esperamos una conexión
                salida  = new DataOutputStream(conexion.getOutputStream());
                entrada = new DataInputStream(conexion.getInputStream());
                salida.writeUTF(hospital.getAux1());  
                salida.writeUTF(hospital.getAux2());
                salida.writeUTF(hospital.getVacunasDisponibles());
                salida.writeUTF(hospital.getColaEsperaText());
                puestosVacunacion=hospital.getPuestosVacunacion();
                for (int i=0;i<10;i++){
                    salida.writeUTF(puestosVacunacion[i]);
                }
                puestosObservacion=hospital.getPuestosObservacion();
                for (int i=0;i<20;i++){
                    salida.writeUTF(puestosObservacion[i]);
                }
                salida.writeUTF(hospital.getSalaDescansoText());
                salida.writeUTF(hospital.getPacienteRegistro());
                int puestoCerrado =entrada.readInt();
                if(puestoCerrado!=-1){
                    
                    hospital.limpiarPuesto(puestoCerrado);
                }
                conexion.close();// Y cerramos la conexión
            }
        } catch (IOException e) {}
 
    }
    
}
