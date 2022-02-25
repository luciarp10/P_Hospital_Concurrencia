
package com.mycompany.plhospital;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 *
 * @author lucyr
 */
public class Hospital {
    private LinkedBlockingQueue colaEspera = new LinkedBlockingQueue();
    private LinkedBlockingQueue colaVacunas = new LinkedBlockingQueue();
    private Semaphore salaVacunacion = new Semaphore(10);
    private Semaphore salaObservacion = new Semaphore(20);
    private Semaphore escribirLog = new Semaphore(1);
    // La recepcion no necesita semáforo porque no tiene aforo limitado y la sala de 
    // descanso del personal tampoco porque solo hay 10 sanitarios, nunca va a haber más de 10
    private Paciente[] pacientePuestoVacunacion = new Paciente[10];
    private Sanitario[] sanitarioPuestoVacunacion = new Sanitario[10];
    private String[] pacientePuestoObservacion = new String[20];
    private String[] sanitarioPuestoObservacion = new String[20];
    private String[] sanitarioSalaDescanso = new String[10];
    private boolean[] alertasObservacion = new boolean[20];
    private Lock cerrojoSalaDescanso= new ReentrantLock();
    private Lock cerrojoRecepcion = new ReentrantLock();
    private Lock cerrojoSalaVacunacion=new ReentrantLock();
    private Lock cerrojoSalaObservacion = new ReentrantLock();
    private String colaEsperaText="";
    private String salaDescansoText="";
    private ListaThreads colaEsperaLista;
    private String[] puestosVacunacion= new String[10];
    private String[] puestosObservacion =new String[20];
    private String vacunasDisponibles="";
    private String aux1="";
    private String aux2="";
    private String pacienteRegistro="";
    
    
    private BufferedWriter bw = null;
    private FileWriter fw = null;
    

    public Hospital() {
        for (int i=0; i<10; i++){
            pacientePuestoVacunacion[i]=null;
            sanitarioPuestoVacunacion[i]=null;
            pacientePuestoObservacion[i]=null;
            sanitarioPuestoObservacion[i]=null;
            sanitarioSalaDescanso[i]=null;
            alertasObservacion[i]=false;
            puestosVacunacion[i]="";
            puestosObservacion[i]="";
        }
        for (int i=10; i<20;i++){
            pacientePuestoObservacion[i]=null;
            sanitarioPuestoObservacion[i]=null;
            alertasObservacion[i]=false;
            puestosObservacion[i]="";
        }
        this.colaEsperaLista=new ListaThreads(this.colaEsperaText);
    }
        
    public void hacerCola(Paciente paciente) {
        cerrojoRecepcion.lock();
        try{
            colaEspera.offer(paciente);
            colaEsperaLista.meter(paciente);
            String text=("El paciente "+ paciente.getId_paciente() +" entra en la cola.");
            escribirEnLog(text, LocalDateTime.now());
            synchronized (colaEspera) {  // notifica que la cola está vacía a los hilos que estaban esperando para hacer poll;
                colaEspera.notifyAll();
            }
        }
        finally{
            cerrojoRecepcion.unlock();
        }

        
    }

    public Paciente sacarDeCola() {
        if (colaEspera.isEmpty()){ // si la cola está vacía, esperar a que haya algun elemento. 
            synchronized (colaEspera){
                try {
                    String text=("La cola esta vacía, esperando que lleguen pacientes...");
                    escribirEnLog(text, LocalDateTime.now());
                    colaEspera.wait();
               } catch (InterruptedException ex) {}
            }
        }
        cerrojoRecepcion.lock();
        try{
          Paciente paciente= (Paciente) colaEspera.poll();
          colaEsperaLista.sacar(paciente);
          String text=("El paciente "+ paciente.getId_paciente()+" sale de la cola.");
          escribirEnLog(text, LocalDateTime.now());
          return paciente;
        }
        finally{
            cerrojoRecepcion.unlock();
        }
        
    }

    public void entrarSalaDescanso(String id_sanitario) {
        cerrojoSalaDescanso.lock();
        try{
            for (int i=0;i<10;i++){
                if(sanitarioSalaDescanso[i]==null){
                    sanitarioSalaDescanso[i]=id_sanitario;
                    break;
                }
            }
            String contenido="";
            for (int i=0;i<10;i++){
                if (sanitarioSalaDescanso[i]!=null){
                    contenido=contenido+" " + sanitarioSalaDescanso[i];
                }
            }
            if(aux1==""){
                contenido = contenido+" A1";
            }
            if(aux2==""){
                contenido=contenido+" A2";
            }
            salaDescansoText=contenido;        
        }
        finally{
            cerrojoSalaDescanso.unlock();
        }
    }

    public void salirSalaDescanso(String id_sanitario) {
        cerrojoSalaDescanso.lock();
        try{
            for (int i=0;i<10;i++){
                if(sanitarioSalaDescanso[i]==id_sanitario){
                    sanitarioSalaDescanso[i]=null;
                    break;
                }
            }
            String contenido="";
            for (int i=0; i<10; i++){
                if (sanitarioSalaDescanso[i]!=null){
                    contenido=contenido+" " + sanitarioSalaDescanso[i];
                }
            }
            if(aux1==""){
                contenido = contenido+" A1";
            }
            if(aux2==""){
                contenido=contenido+" A2";
            }
            salaDescansoText=contenido;
        }
        finally{
            cerrojoSalaDescanso.unlock();
        }
    }

    public int asignarPuestoVacunacionSanitario(Sanitario id_sanitario) {
        cerrojoSalaVacunacion.lock();
        int puesto=-1;
        try{
            for (int i=0;i<10;i++){
                if(sanitarioPuestoVacunacion[i]==null){
                    sanitarioPuestoVacunacion[i]=id_sanitario;
                    puestosVacunacion[i]=id_sanitario.getId_sanitario();
                    puesto=i;
                    break;
                }
            }
        }
        finally{
            cerrojoSalaVacunacion.unlock();
        }
        return puesto;
    }

    public Paciente[] getPacientePuestoVacunacion() {
        return pacientePuestoVacunacion;
    }

    public synchronized Paciente vacunar(int puesto) {
        while(!hayPaciente(puesto)){
            try {
                wait();
            } catch (InterruptedException ex) {}
        }
        Paciente vacunado = pacientePuestoVacunacion[puesto];
        return vacunado; 
    }
    private synchronized boolean hayPaciente(int puesto){
        if(pacientePuestoVacunacion[puesto]==null){
            return false;
        }
        return true;
    }
   

    public void dejarPuestoVacunacionSanitario(int puestoAsignado) {
        cerrojoSalaVacunacion.lock();
        try{
            sanitarioPuestoVacunacion[puestoAsignado]=null;
            puestosVacunacion[puestoAsignado]="";
        }
        finally{
            cerrojoSalaVacunacion.unlock();
        }
    }

    public int hayAlertas() {
        int puestoAlerta=-1;
        cerrojoSalaObservacion.lock();
        try{
            for (int i=0; i<20;i++){
                if(alertasObservacion[i]==true){
                    puestoAlerta=i;
                    break;
                }
            }
        }
        finally{
            cerrojoSalaObservacion.unlock();
        }
        return puestoAlerta; 
    }

    public String atenderAlerta(int puestoAlerta, String id_sanitario) {
        cerrojoSalaObservacion.lock();
        try{
            sanitarioPuestoObservacion[puestoAlerta]=id_sanitario;
            alertasObservacion[puestoAlerta]=false;
            puestosObservacion[puestoAlerta]=puestosObservacion[puestoAlerta]+"->"+id_sanitario;
        }
        finally{
            cerrojoSalaObservacion.unlock();
        }
        return pacientePuestoObservacion[puestoAlerta];
    }

    public synchronized int asignarPuestoVacunacionPaciente(Paciente paciente) {
        try {
            salaVacunacion.acquire();
        } catch (InterruptedException ex) {}
        cerrojoSalaVacunacion.lock();
        int puestoAsignado=-1;
        try{
            for (int i=0; i<10; i++){
                
                if ((sanitarioPuestoVacunacion[i]!=null) && (pacientePuestoVacunacion[i]==null) && (sanitarioPuestoVacunacion[i].getContador()<15)){
                    puestoAsignado=i;
                    pacientePuestoVacunacion[i]=paciente;
                    puestosVacunacion[i]=puestosVacunacion[i]+"->"+ paciente.getId_paciente();
                    notifyAll();
                    break;
                }
                
            } 
        }
        finally{
            cerrojoSalaVacunacion.unlock();
        }
        return puestoAsignado;
    }
    
    private void dejarPuestoSalaVacunacionPaciente(int puesto){
        cerrojoSalaVacunacion.lock();
        try{
            pacientePuestoVacunacion[puesto]=null;
            puestosVacunacion[puesto]=sanitarioPuestoVacunacion[puesto].getId_sanitario();
            salaVacunacion.release();
        }
        finally{
            cerrojoSalaVacunacion.unlock();
        }
    }

    public void asignarPuestoSalaObservacion(String paciente, int puestoVacunacion) {
        try {
            salaObservacion.acquire(); //si consigue un puesto en la sala de observacion, deja la de vacunacion
        } catch (InterruptedException ex) {}
        dejarPuestoSalaVacunacionPaciente(puestoVacunacion);
        cerrojoSalaObservacion.lock();
        try{
            for (int i=0;i<20;i++){
                if(pacientePuestoObservacion[i]==null){
                    pacientePuestoObservacion[i]=paciente;
                    puestosObservacion[i]=paciente;
                    break;
                }
            }
        }
        finally{
            cerrojoSalaObservacion.unlock();
        }
        
        
    }

    public String getSanitarioPuestoVacunacion(int puesto){
        cerrojoSalaVacunacion.lock();
        try{
            return sanitarioPuestoVacunacion[puesto].getId_sanitario();
        }
        finally{
            cerrojoSalaVacunacion.unlock();
        }
        
    }

    public void generarAlerta(String id_paciente) {
        cerrojoSalaObservacion.lock();
        try{
            for (int i=0;i<20;i++){
                if(id_paciente==pacientePuestoObservacion[i]){
                    alertasObservacion[i]=true;
                    String text= ("Alerta activada puesto: "+i);
                    escribirEnLog(text, LocalDateTime.now());
                    break;
                }
            }
        }
        finally{
            cerrojoSalaObservacion.unlock();
        }
    }

    public void anadirVacuna() {
        Object vacunaNueva= new Object(); 
        colaVacunas.offer(vacunaNueva);
        vacunasDisponibles=colaVacunas.size()+"";
        synchronized (colaVacunas) {  // notifica que la cola está vacía a los hilos que estaban esperando para hacer poll;
            colaVacunas.notifyAll();
        }
    }
    
    public void cogerVacuna() {
        if (colaVacunas.isEmpty()){ // si la cola está vacía, esperar a que haya algun elemento. 
            synchronized (colaVacunas){
                try {
                    String text= ("La cola esta vacía, esperando que lleguen vacunas...");
                    escribirEnLog(text, LocalDateTime.now());
                    colaVacunas.wait();
                } catch (InterruptedException ex) {}
            }
        }
        colaVacunas.poll();
        vacunasDisponibles=colaVacunas.size()+"";
    }

    public void dejarPuestoSalaObservacionPaciente(String id_paciente) {
        cerrojoSalaObservacion.lock();
        try{
            for (int i=0;i<20;i++){
                if(pacientePuestoObservacion[i]==id_paciente){
                    pacientePuestoObservacion[i]=null;
                    puestosObservacion[i]="";                }
            }
            salaObservacion.release();
        }
        finally{
            cerrojoSalaObservacion.unlock();
        }
        
    }

    public void dejarPuestoSalaObservacionSanitario(int puestoAlerta) {
        cerrojoSalaObservacion.lock();
        try{
            sanitarioPuestoObservacion[puestoAlerta]=null;
            puestosObservacion[puestoAlerta]="";
        }
        finally{
            cerrojoSalaObservacion.unlock();
        }
    }

    public void salirAuxSalaDescanso(){
        cerrojoSalaDescanso.lock();
        try{
            String contenido="";
            for (int i=0; i<10; i++){
                if (sanitarioSalaDescanso[i]!=null){
                    contenido=contenido+" " + sanitarioSalaDescanso[i];
                }
            }
            if(aux2==""){
                contenido = contenido + " " + "A2";
            }
            if(aux1==""){
                contenido = contenido + " " + "A1";
            }
            salaDescansoText=contenido;
        }
        finally{
            cerrojoSalaDescanso.unlock();
        }
    }

    public boolean[] getAlertasObservacion() {
        return alertasObservacion;
    }

    public String getColaEsperaText() {
        return colaEsperaLista.getCuadroColaEspera();
    }

    public String getSalaDescansoText() {
        return salaDescansoText;
    }

    public String[] getPuestosVacunacion() {
        return puestosVacunacion;
    }

    public String[] getPuestosObservacion() {
        return puestosObservacion;
    }

    public String getVacunasDisponibles() {
        return vacunasDisponibles;
    }

    public String getAux1() {
        return aux1;
    }

    public String getAux2() {
        return aux2;
    }
    
    public String getPacienteRegistro(){
        return pacienteRegistro;
    }
    
    public void setPacienteRegistro(String pacienteRegistro) {
        this.pacienteRegistro = pacienteRegistro;
    }

    public void setAux1(String aux1) {
        this.aux1 = aux1;
    }

    public void setAux2(String aux2) {
        this.aux2 = aux2;
    }

    public void setSalaDescansoText(String salaDescansoText) {
        this.salaDescansoText = salaDescansoText;
    }
    
    
    
    public void escribirEnLog(String texto, LocalDateTime fecha){
        try {
            escribirLog.acquire();
        } catch (InterruptedException ex) {}
        try {
            texto = "\n"+texto+" [ "+fecha+" ]";
            File log = new File("EvolucionHospital.txt");
            // Si el archivo no existe, se crea
            if (!log.exists()) {
                log.createNewFile();
            }
            // flag true, indica adjuntar información al archivo.
            fw = new FileWriter(log.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            bw.write(texto);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //Cierra instancias de FileWriter y BufferedWriter
                if (bw != null){
                    bw.close();
                }
                if (fw != null){
                    fw.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        escribirLog.release();
        
    }

    public synchronized void limpiarPuesto(int puestoCerrado) {
        if(pacientePuestoVacunacion[puestoCerrado]==null){
           Paciente pacienteFicticio=new Paciente("", this);
           pacientePuestoVacunacion[puestoCerrado]=pacienteFicticio;
        }
        sanitarioPuestoVacunacion[puestoCerrado].setCerrarPuesto(true);
    }
    
    
}
