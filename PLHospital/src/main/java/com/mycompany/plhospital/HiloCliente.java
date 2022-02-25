
package com.mycompany.plhospital;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;


public class HiloCliente extends Thread {
    private VentanaHospital ventana;
    private Socket cliente;
    private DataInputStream entrada;
    private DataOutputStream salida;

    
    
    public HiloCliente(VentanaHospital ventana){
        this.ventana=ventana;
    }
    
    public void run(){
        String aux1, aux2, vacunasDisponibles, colaEspera, salaDescanso, pacienteRegistro;
        String[] puestosVacunacion = new String[10];
        String [] puestosObservacion = new String[20]; 
        while(true){
            try {
                sleep(1000);
            } catch (InterruptedException ex) {}
            try {
                cliente = new Socket(InetAddress.getLocalHost(),5000);
                entrada = new DataInputStream(cliente.getInputStream());  //Creamos los canales de entrada/salida
                salida = new DataOutputStream(cliente.getOutputStream());
                aux1 = entrada.readUTF();
                aux2 = entrada.readUTF();
                vacunasDisponibles = entrada.readUTF();
                colaEspera= entrada.readUTF();
                for (int i=0;i<10;i++){
                    puestosVacunacion[i]=entrada.readUTF();
                }
                for (int i=0;i<20;i++){
                    puestosObservacion[i]=entrada.readUTF();
                }
                salaDescanso=entrada.readUTF();
                pacienteRegistro=entrada.readUTF();
                salida.writeInt(-1);
                cliente.close();  
                ventana.rellenarVentana(aux1, aux2, vacunasDisponibles, colaEspera, puestosVacunacion, puestosObservacion, salaDescanso, pacienteRegistro);
            } catch (IOException ex) {}
            
        }
    }
}
