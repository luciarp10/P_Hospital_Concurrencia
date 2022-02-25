
package com.mycompany.plhospital;

import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Sanitario extends Thread {
    private String id_sanitario;
    private Hospital hospital;
    private int contador=0;
    private boolean cerrarPuesto=false; 

    public Sanitario(String id_sanitario, Hospital hospital) {
        this.id_sanitario = id_sanitario;
        this.hospital = hospital;
    }
    
    public void run(){
        //Cuando llegan al hospital van entre 1 y 3 segundos a cambiarse en la sala de descanso
        hospital.entrarSalaDescanso(this.id_sanitario);
        int tiempoAleatorio;
        try{
            tiempoAleatorio= (int) (Math.random()*3+1)*1000;
            sleep(tiempoAleatorio);
        } catch (InterruptedException ex) {}
        hospital.salirSalaDescanso(this.id_sanitario);
        
        while(true){
            int puestoAlerta=hospital.hayAlertas();
            if(puestoAlerta!=-1){
                int tiempoObservacion = (int)(Math.random()*4+2)*1000;
                String paciente= hospital.atenderAlerta(puestoAlerta, this.id_sanitario);
                String text =("El sanitario "+ this.id_sanitario+" acude a atender una reaccion del paciente "+paciente);
                hospital.escribirEnLog(text, LocalDateTime.now());
                try {
                    sleep(tiempoObservacion);
                } catch (InterruptedException ex) {}
                text=("El paciente "+ paciente+ " ya se ha recuperado, se va a casa.");
                hospital.escribirEnLog(text, LocalDateTime.now());
                hospital.dejarPuestoSalaObservacionPaciente(paciente);
                hospital.dejarPuestoSalaObservacionSanitario(puestoAlerta);
                
            }
            int puestoAsignado= hospital.asignarPuestoVacunacionSanitario(this);
            String text= ("Al sanitario "+this.id_sanitario+" se le asigna el puesto: "+puestoAsignado);
            hospital.escribirEnLog(text, LocalDateTime.now());
            while(contador<15 && !cerrarPuesto){
                hospital.cogerVacuna();
                Paciente pacienteVacunar= hospital.vacunar(puestoAsignado);
                if(cerrarPuesto){
                    break;
                }
                tiempoAleatorio=(int)(Math.random()*3+3)*1000;
                try {
                    sleep(tiempoAleatorio);
                } catch (InterruptedException ex) {}
                text= ("Paciente "+pacienteVacunar.getId_paciente()+" vacunado, se va a la sala de observacion.");
                hospital.escribirEnLog(text, LocalDateTime.now());
                hospital.asignarPuestoSalaObservacion(pacienteVacunar.getId_paciente(), puestoAsignado);
                pacienteVacunar.setVacunado(true);
                contador++;         
            }
            hospital.dejarPuestoVacunacionSanitario(puestoAsignado);
            tiempoAleatorio= (int) (Math.random()*4+5)*1000;
            if(cerrarPuesto){
                text= ("El sanitario "+this.id_sanitario+" va a descansar "+tiempoAleatorio+" segundos porque cierran su puesto para limpiarlo");
                hospital.escribirEnLog(text, LocalDateTime.now());
                cerrarPuesto=false;
            }
            else{
                text= ("El sanitario "+this.id_sanitario+" ha vacunado a 15 pacientes, va a descansar "+tiempoAleatorio+" segundos");
                hospital.escribirEnLog(text, LocalDateTime.now());
                
            }
            
            hospital.entrarSalaDescanso(id_sanitario);
            try {
                sleep(tiempoAleatorio);
            }catch (InterruptedException ex) {}
            hospital.salirSalaDescanso(id_sanitario);
            contador=0;
        }
    }

    public int getContador() {
        return contador;
    }

    public String getId_sanitario() {
        return id_sanitario;
    }



    public void setCerrarPuesto(boolean cerrarPuesto) {
        this.cerrarPuesto = cerrarPuesto;
    }
    
    
    
}

