
package com.mycompany.plhospital;

import java.util.ArrayList;

public class ListaThreads {
    ArrayList<Paciente> listaPacientes;
    String cuadroColaEspera;
    public ListaThreads(String cuadroColaEspera)
    {
        listaPacientes=new ArrayList<Paciente>();
        this.cuadroColaEspera=cuadroColaEspera;
    }
    
    public synchronized void meter(Paciente t)
    {
        listaPacientes.add(t);
        imprimir();
    }
    public synchronized void sacar(Paciente t)
    {
        listaPacientes.remove(t);
        imprimir();
    } 
    public void imprimir()
    {
        String contenido="";
        for(int i=0; i<listaPacientes.size(); i++)
        {
           contenido=contenido+listaPacientes.get(i).getId_paciente()+" ";
        }
        cuadroColaEspera=contenido;
    }

    public String getCuadroColaEspera() {
        return cuadroColaEspera;
    }

}
