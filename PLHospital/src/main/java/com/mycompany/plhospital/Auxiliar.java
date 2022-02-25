package com.mycompany.plhospital;

import java.time.LocalDateTime;


public class Auxiliar extends Thread{
    private String id_auxiliar;
    private Hospital hospital;

    public Auxiliar(String id_auxiliar, Hospital hospital) {
        this.id_auxiliar = id_auxiliar;
        this.hospital = hospital;
    }
    
    public void run(){
        int contador=0;
        int tiempoDescanso;
        int tiempoVacuna;
        while(true){
            if(this.id_auxiliar == "A1"){
                hospital.setAux1(id_auxiliar);
                Paciente paciente = (Paciente) hospital.sacarDeCola();
                hospital.setPacienteRegistro(paciente.getId_paciente());
                if(es_registro_correcto()){
                    String text= ("El paciente "+paciente.getId_paciente()+" se ha registrado "
                            + "correctamente.");
                    hospital.escribirEnLog(text, LocalDateTime.now());
                    int puestoAsignado=-1;
                    while(puestoAsignado==-1){
                        puestoAsignado= hospital.asignarPuestoVacunacionPaciente(paciente);
                    }
                    String sanitario = hospital.getSanitarioPuestoVacunacion(puestoAsignado);
                    text= ("El paciente "+paciente.getId_paciente()+" será vacunado por el sanitario "+sanitario+ " en el puesto PV0"+puestoAsignado);
                    hospital.escribirEnLog(text, LocalDateTime.now());
                }
                else{
                    String text =("El paciente "+paciente.getId_paciente()+" se ha registrado "
                            + "incorrectamente, debe irse a casa.");
                    hospital.escribirEnLog(text, LocalDateTime.now());
                    paciente.setRegistroCorrecto(false);
                    
                }
                contador++;
                if(contador==10){
                    tiempoDescanso=(int)(Math.random()*3+2)*1000;
                    String text= ("El auxiliar A1 ha registrado 10 pacientes,"
                            + "descansa "+tiempoDescanso+".");
                    hospital.escribirEnLog(text, LocalDateTime.now());
                    hospital.setAux1("");
                    String salaDescanso=hospital.getSalaDescansoText()+" "+id_auxiliar;
                    hospital.setSalaDescansoText(salaDescanso);
                    try {
                        sleep(tiempoDescanso);
                    } catch (InterruptedException ex) {}
                    hospital.setAux1(id_auxiliar);
                    hospital.salirAuxSalaDescanso();
                    contador=0;
                }
            }
            else{
                hospital.setAux2(id_auxiliar);
                hospital.anadirVacuna();
                tiempoVacuna=(int) (Math.random()*501 + 500);
                try {
                    sleep(tiempoVacuna);
                } catch (InterruptedException ex) {}
                contador++;
                if (contador==20){
                    tiempoDescanso = (int) (Math.random()*4 +1)*1000;
                    String text = ("El auxiliar A2 ha creado 20 vacunas,"
                            + "descansa "+tiempoDescanso+".");
                    hospital.escribirEnLog(text, LocalDateTime.now());
                    hospital.setAux2("");
                    String salaDescanso=hospital.getSalaDescansoText()+" "+id_auxiliar;
                    hospital.setSalaDescansoText(salaDescanso);
                    try{
                        sleep(tiempoDescanso);
                    }
                    catch(InterruptedException ie){}
                    hospital.setAux2(id_auxiliar);
                    hospital.salirAuxSalaDescanso();
                    contador=0;
                }
            }
        }

    }
    
    private boolean es_registro_correcto(){
        boolean resultado=true; 
        int tiempoComprobacion = (int) (Math.random()*501+500);
        try {
            sleep(tiempoComprobacion);
        } catch (InterruptedException ex) {}
        int aleatorio = (int) (Math.random()*100+1); //numero aleatorio entre 1 y 100
        if (aleatorio==1){ //1 % de pacientes no estaban citados.
            resultado=false;
        }
        return resultado;
    }
}
