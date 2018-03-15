
import jade.core.AgentContainer;
import jade.core.behaviours.OneShotBehaviour;
import jade.wrapper.AgentController;
import jade.core.AID;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.introspection.AddedBehaviour;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author oleksandr.antsyferov
 */
public class InitializationAgent extends SelfReportingAgent {
    
    private AID startNewAgent(String nickname, String className, Object[] arguments) {
        AID result = null;
        try {
            AgentController agent = getContainerController().createNewAgent(nickname, className, arguments);
            agent.start();
            result = new AID();
            result.setName(agent.getName());
        } catch (Exception e) {
        }
        
        return result;
    }
    
    @Override protected void setup() {
        super.setup();
        
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                //GUI watcher for convinience
                startNewAgent("rmaWatcher", "WatchDogAgent", null);
                
                //navigator for guiding a walker in the world
                startNewAgent("navigator", "NavigatorAgent", null);
                //the navigator is one of, terminate if not available
                startNewAgent("navigatorWatcher", "WatchDogAgent", new String[]{"navigator"});
             
                
                //setup walker creation service
                registerService("factory", "WalkerAgent");
            }
        });
        
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
               ACLMessage message = receive();
                if (null != message) {
                    AID walker = startNewAgent(message.getContent(), "WalkerAgent", null);
                    ACLMessage response = message.createReply();
                    response.setPerformative(ACLMessage.CONFIRM);
                    response.setContent(walker.getName());
                    send(response);
                } else {
                    block();
                }
            }
        });
    }
    
}
