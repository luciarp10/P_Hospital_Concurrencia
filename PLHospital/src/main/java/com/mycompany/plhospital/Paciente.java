
package com.mycompany.plhospital;

import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Paciente extends Thread {
    String id_paciente;
    Hospital hospital; 
    boolean vacunado;
    boolean registroCorrecto;


    public Paciente(String id_paciente, Hospital hospital) {
        this.id_paciente = id_paciente;
        this.hospital = hospital;
        this.vacunado=false;
        this.registroCorrecto=true; 

    }
    
    public void run(){
        int aleatorio;
        hospital.hacerCola(this);
        while (!vacunado && registroCorrecto){
            try {
                sleep(10);
            } catch (InterruptedException ex) {}
        }
        if(vacunado){
            try {
                sleep(10000);
            } catch (InterruptedException ex) {}
            aleatorio= (int) (Math.random()*100+1);
            if(aleatorio>0 && aleatorio<=5){
                String text =("El paciente "+this.id_paciente+" está sufriendo una reaccion a la vacuna.");
                hospital.escribirEnLog(text, LocalDateTime.now());
                hospital.generarAlerta(this.id_paciente); // aleatoriamente tendrá problemas con la vacuna o no 
            }
            else{
                String text=("El paciente "+this.id_paciente+" deja la sala de observacion. Se va a casa.");
                hospital.escribirEnLog(text, LocalDateTime.now());
                hospital.dejarPuestoSalaObservacionPaciente(this.id_paciente);
            }
            
        }
    }

    public String getId_paciente() {
        return id_paciente;
    }

    public void setVacunado(boolean vacunado) {
        this.vacunado = vacunado;
    }

    public void setRegistroCorrecto(boolean registroCorrecto) {
        this.registroCorrecto = registroCorrecto;
    }



    
    
}
