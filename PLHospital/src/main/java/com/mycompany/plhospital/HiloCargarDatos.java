
package com.mycompany.plhospital;

import java.time.LocalDateTime;

public class HiloCargarDatos extends Thread {
    private Hospital hospital;
    public HiloCargarDatos (Hospital hospital){
        this.hospital=hospital; 
    }
    public void run(){
        hospital.escribirEnLog("\n --------------------------NUEVA EJECUCIÓN DEL HOSPITAL --> ", LocalDateTime.now());
        String id_sanitario;
        for(int i=0; i<10; i++){
            id_sanitario = "S0"+i;
            Sanitario s = new Sanitario(id_sanitario, hospital);
            s.start();
        }
        Auxiliar a1 = new Auxiliar("A1", hospital);
        Auxiliar a2 = new Auxiliar ("A2", hospital);
        a1.start();
        a2.start();
        try {
            sleep(1500);
        } catch (InterruptedException ex) {}
        String id_paciente;
        Paciente p;
        int tiempoAleatorio;
        for (int i=0; i<10; i++){
            id_paciente = "P000"+i;
            p = new Paciente(id_paciente, hospital);
            p.start();
            tiempoAleatorio=(int) (Math.random()*3+1)*1000;
            try {
                sleep(tiempoAleatorio);
            } catch (InterruptedException ex) {}
        }
        for (int i=10; i<100; i++){
            id_paciente = "P00"+i;
            p = new Paciente(id_paciente, hospital);
            p.start();
            tiempoAleatorio=(int) (Math.random()*3+1)*1000;
            try {
                sleep(tiempoAleatorio);
            } catch (InterruptedException ex) {}
        }
        for (int i=100; i<1000; i++){
            id_paciente = "P0"+i;
            p = new Paciente(id_paciente, hospital);
            p.start();
            tiempoAleatorio=(int) (Math.random()*3+1)*1000;
            try {
                sleep(tiempoAleatorio);
            } catch (InterruptedException ex) {}
        }
        for (int i=1000; i<2000; i++){
            id_paciente = "P"+i;
            p = new Paciente(id_paciente, hospital);
            p.start();
            tiempoAleatorio=(int) (Math.random()*3+1)*1000;
            try {
                sleep(tiempoAleatorio);
            } catch (InterruptedException ex) {}
        }
    }
}
