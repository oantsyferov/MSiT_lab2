/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author oleksandr.antsyferov
 */
public class WatchDogAgent extends SelfReportingAgent {

    private String targetAgentName = "rma";
    
    @Override
    protected void setup() {
        super.setup();
        
        Object[] arguments = getArguments();
     
        if (null != arguments && arguments.length > 0) {
            targetAgentName = (String) arguments[0];
        }

        addBehaviour(new TickerBehaviour(this, 1000) {
            @Override
            protected void onTick() {
                //System.out.println("watching...");

                if (!lookupAgent(targetAgentName)) {
                    stopPlatform();
                }
            }

            protected boolean lookupAgent(String name) {
                AMSAgentDescription[] agents = null;

                try {
                    SearchConstraints c = new SearchConstraints();
                    c.setMaxResults(new Long(-1));
                    AMSAgentDescription searchFor = new AMSAgentDescription();
                    searchFor.setName(new AID(name, AID.ISLOCALNAME));
                    agents = AMSService.search(getAgent(), searchFor, c);
                } catch (Exception e) {
                }
                for (int i = 0; i < agents.length; i++) {
                    AID agentID = agents[i].getName();
                    //System.out.println("found:" + agentID.getName());
                }

                if (agents.length > 0) {
                    return true;
                } else {
                    return false;
                }
            }

            protected void stopPlatform() {
                Codec codec = new SLCodec();
                Ontology jmo = JADEManagementOntology.getInstance();
                getContentManager().registerLanguage(codec);
                getContentManager().registerOntology(jmo);
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.addReceiver(getAMS());
                msg.setLanguage(codec.getName());
                msg.setOntology(jmo.getName());
                try {
                    getContentManager().fillContent(msg, new Action(getAID(), new ShutdownPlatform()));
                    send(msg);
                } catch (Exception e) {
                }
            }
        });
    }

}
