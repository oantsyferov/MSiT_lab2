/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

/**
 *
 * @author oleksandr.antsyferov
 */
public class SelfReportingAgent extends Agent {
    
    private void reportMethod() {
         StackTraceElement[] stackTrace = new Throwable().getStackTrace();
         System.out.println("Agent("+getAID().getName() + "):" + stackTrace[1].getMethodName()+"()");
    }
    
    @Override protected void setup() {
        reportMethod();
    }
    
    void registerService(String name, String type)
    {
        
        DFAgentDescription sellerDescription = new DFAgentDescription();
        sellerDescription.setName(getAID());
        ServiceDescription pongService = new ServiceDescription();
        pongService.setType(type);
        pongService.setName(name);
        sellerDescription.addServices(pongService);
        
        try {
            DFService.register(this, sellerDescription);
        }
        catch (Exception e) {}
    }
    
    @Override protected void takeDown() {
        reportMethod();
        try {
            DFService.deregister(this);
        }
        catch(Exception e) {}
    }
  
}
