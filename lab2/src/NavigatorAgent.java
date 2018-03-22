
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
import java.util.LinkedList;

import java.util.Queue;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author oleksandr.antsyferov
 */
public class NavigatorAgent extends SelfReportingAgent {

    private AID walker;
    private int counter;
    private Queue<String> actions;

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
                        walker.setLocalName(message.getContent());
                        System.out.println("got response for walker request:" + conversationId);
                    }
                }
            });

        } catch (Exception e) {

        }
    }
    
    private void addWalkerRequests(String request) {
        //start the behaviour each time to avoid unnesseary polling on empty queue
        if (null == actions) {
            actions = new LinkedList<String>();
        }
        if (actions.size() <= 0) {
            addBehaviour(new Behaviour() {

                boolean active_communication = false;
                int communication_attempts = 0;
                final int communication_attempts_max = 3;

                @Override
                public void action() {
                    
                    if (false == active_communication) {
                        if (null != walker && null != actions.peek())  {
                            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                            message.setConversationId("navigator-to-walker-request" + System.currentTimeMillis());
                            message.addReceiver(walker);
                            message.setContent(actions.peek());
                            send(message);
                            block(1000);
                            active_communication = true;
                        }
                    } else {
                        ACLMessage message = receive();
                        if (null != message) {
                            communication_attempts = 0;
                            active_communication = false;
                            
                            if(actions.peek().equals(WalkerAgent.ACT_FEEL)) {
                                System.out.println("Navigator got Walker feelings:" + message.getContent());
                            }
                            
                            actions.poll();
                            
                            //verify conversiation id, handle result
                            
                        } else {
                            if(communication_attempts < communication_attempts_max) {
                                block(1000);
                            } else {
                                active_communication = false;
                            }
                            
                            ++communication_attempts;
                        }

                    }
                    //schedule message handling
                }

                public boolean done() {
                    if (0 == actions.size()) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }
        
        actions.add(request);
    }

    @Override
    protected void setup() {
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                requestNewWalker();
            }
        });

        addBehaviour(new TickerBehaviour(this, 100) {
            @Override
            protected void onTick() {
                if (null == walker) {
                    requestNewWalker();
                }
            }
        });
        
        
        
        
        test();        
    }
    
    
    private void test() {
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                addWalkerRequests(WalkerAgent.ACT_WALK);
                addWalkerRequests(WalkerAgent.ACT_TURN);
                addWalkerRequests(WalkerAgent.ACT_WALK);
                addWalkerRequests(WalkerAgent.ACT_WALK);
                addWalkerRequests(WalkerAgent.ACT_WALK);
                addWalkerRequests(WalkerAgent.ACT_WALK);
                addWalkerRequests(WalkerAgent.ACT_WALK);
                addWalkerRequests(WalkerAgent.ACT_TURN);
                addWalkerRequests(WalkerAgent.ACT_WALK);
                addWalkerRequests(WalkerAgent.ACT_WALK);
                addWalkerRequests(WalkerAgent.ACT_WALK);
                addWalkerRequests(WalkerAgent.ACT_WALK);
                addWalkerRequests(WalkerAgent.ACT_FEEL);    
               }
        });
    }

}
