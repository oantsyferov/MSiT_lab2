
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.MessageTemplate;
import jade.proto.states.MsgReceiver;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author oleksandr.antsyferov
 */
public class NavigatorAgent extends SelfReportingAgent{
    
    private AID walker;
    private int counter;
    
    protected void requestNewWalker() {
        ++counter;
        DFAgentDescription agentDescription = new DFAgentDescription();
        ServiceDescription factoryService = new ServiceDescription();
        factoryService.setName("factory");
        factoryService.setType("WalkerAgent");
        agentDescription.addServices(factoryService);

        try {
            DFAgentDescription[] factories = DFService.search(this, agentDescription);
            ACLMessage message = new ACLMessage(ACLMessage.PROPOSE);
            message.setContent("walker" + counter);
            String conversationId = "new-walker-request" + System.currentTimeMillis();
            if (factories.length > 0) {
                message.addReceiver(factories[0].getName());
                message.setConversationId(conversationId);
                send(message);
            }

            //Get responses for walker requests
            addBehaviour(new MsgReceiver(this,
                    MessageTemplate.MatchConversationId(conversationId),
                    System.currentTimeMillis() + 1000, 
                    null, null) {
                @Override
                protected void handleMessage(ACLMessage msg) {
                    if (null == msg) {
                        System.out.println("no response for walker request:" + conversationId);
                        requestNewWalker();
                        
                    } else {
                        walker = new AID();
                        walker.setName(message.getContent());
                        System.out.println("got response for walker request:" + conversationId);
                    }
                }
            });
    
        } catch (Exception e) {

        }
    }
    
    @Override
    protected void setup() {
        counter = 0;
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                requestNewWalker();
            }
        });
        
        
        
        addBehaviour(new TickerBehaviour(this, 100) {
            @Override
            protected void onTick() {
                if(null == walker)
                {
                    requestNewWalker();
                }
            }
        });

        
         addBehaviour(new TickerBehaviour(this, 15000) {
            @Override
            protected void onTick() {
                walker = null;
            }
        });
        
    }

}
