
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.EnumSet;
import java.util.BitSet;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author oleksandr.antsyferov
 */
public class WalkerAgent extends SelfReportingAgent {

    final static public String ACT_WALK = "walk";
    final static public String ACT_FEEL = "feel";
    final static public String ACT_TURN = "turn";
    final static public String ACT_SHOOT = "shoot";
    final static public String ACT_AMMO = "ammo";

    public enum Feels {
        GOLD_NEAR, PIT_NEAR, WAMPUS_NEAR,
        GOLD_FOUND, PIT_FOUND,
        WAMPUS_FOUND_ALIVE, WAMPUS_FOUND_DEAD
    };

    final String[][] map = new String[][]{
        //0   1   2   3   4   5   6   7   8
        {" ", " ", " ", " ", " ", " ", "P", " ", " "},//0
        {" ", " ", "G", " ", " ", " ", " ", " ", " "},//1
        {" ", "P", " ", " ", "P", " ", "G", " ", " "},//2
        {" ", " ", "G", " ", " ", " ", " ", "P", " "},//3
        {" ", " ", " ", " ", "M", "P", " ", " ", " "},//4
        {"P", " ", " ", "G", " ", " ", " ", "G", " "} //5
    };

    int x = 0;
    int y = 0;
    int direction = 0; //0-top, 1-right, 2-down, 3-left

    int ammo = 1;

    void turn() {
        direction += 1;
        if (4 == direction) {
            direction = 0;
        }
    }

    boolean walk() {
        //move checks
        if ((0 == direction) && (y > 0)) {
            --y;
        } else if (1 == direction
                && x < map[0].length - 1) {
            ++x;

        } else if (2 == direction
                && y < map.length - 1) {
            ++y;
        } else if (3 == direction
                && x > 0) {
            --x;
        } else {
            return false;
        }

        //checks after moving in the new cell
        if (0 == x && 0 == y) {
            ammo = 1;
        }

        return true;
    }

    BitSet feelRoom(int y, int x) {
        BitSet result = new BitSet(8);

        String content = map[y][x];
        if ("P" == content) {
            result.set(Feels.PIT_FOUND.ordinal()); //pit found
        } else if ("G" == content) {
            result.set(Feels.GOLD_FOUND.ordinal()); //gold found
        } else if ("M" == content) {
            result.set(Feels.WAMPUS_FOUND_ALIVE.ordinal()); //alive wampus
        } else if ("C" == content) {
            result.set(Feels.WAMPUS_FOUND_DEAD.ordinal()); //dead wampus
        }

        return result;
    }

    BitSet weakenFeels(BitSet feels) {
        BitSet result = new BitSet(feels.size());

        if (feels.get(Feels.GOLD_FOUND.ordinal())) {
            result.set(Feels.GOLD_NEAR.ordinal());
        }
        if (feels.get(Feels.PIT_FOUND.ordinal())) {
            result.set(Feels.PIT_NEAR.ordinal());
        }
        if (feels.get(Feels.WAMPUS_FOUND_ALIVE.ordinal())) {
            result.set(Feels.WAMPUS_NEAR.ordinal());
        }

        return result;
    }

    BitSet feel() {
        BitSet result = new BitSet(8);

        result.or(feelRoom(y, x));
        if (x > 0) {
            result.or(weakenFeels(feelRoom(y, x - 1)));
        }
        if (y > 0) {
            result.or(weakenFeels(feelRoom(y - 1, x)));
        }
        if (x + 1 < map[0].length) {
            result.or(weakenFeels(feelRoom(y, x + 1)));
        }
        if (y + 1 < map.length) {
            result.or(weakenFeels(feelRoom(y + 1, x)));
        }

        return result;
    }

    boolean shoot() {

        if (ammo > 0) {
            --ammo;
            //kill calculation here
            //kill confirmation in feel()
            return true;
        } else {
            return false;
        }
    }

    int ammo() {
        return ammo;
    }

    void print() {
        System.out.println("=====MAP=====");
        for (int row = 0; row < map.length; ++row) {
            for (int column = 0; column < map[0].length; ++column) {
                if (y == row
                        && x == column) {
                    System.out.print("H ");
                } else {
                    if (" " == map[row][column]) {
                        System.out.print("- ");
                    } else {
                        System.out.print(map[row][column] + " ");
                    }

                }
            }
            System.out.println("");
        }
        System.out.println();
    }
    
    private void test()
    {
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                walk();
                System.out.println("walker feels:" + feel());
                print();

                turn();

                walk();
                System.out.println("walker feels:" + feel());
                print();

                walk();
                System.out.println("walker feels:" + feel());
                print();

                walk();
                System.out.println("walker feels:" + feel());
                print();

                walk();
                System.out.println("walker feels:" + feel());
                print();

                walk();
                System.out.println("walker feels:" + feel());
                print();

                turn();

                walk();
                System.out.println("walker feels:" + feel());
                print();

                walk();
                System.out.println("walker feels:" + feel());
                print();

                walk();
                System.out.println("walker feels:" + feel());
                print();

                walk();
                System.out.println("walker feels:" + feel());
                print();

            }
        });
    }

    @Override
    protected void setup() {
        super.setup();

        registerService("walker", MSG_QUEUE_CLASS);

        //test();

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage message = receive();
                if (null != message) {
                    String command = message.getContent();
                    ACLMessage response = message.createReply();
                    System.out.println("navigator command:" + command);
                    if (null == command) {
                        response.setPerformative(ACLMessage.UNKNOWN);
                    } else if (command.equals(ACT_WALK)) {
                        boolean result = walk();
                        if (result) {
                            response.setPerformative(ACLMessage.CONFIRM);
                            System.out.println("walk OK");
                        } else {
                            response.setPerformative(ACLMessage.DISCONFIRM);
                            System.out.println("walk FAIL");
                        }

                    } else if (command.equals(ACT_FEEL)) {
                        BitSet result = feel(); 
                        System.out.println("feel OK");
                        try {
                            response.setContentObject(String.valueOf(result.toLongArray()[0]));
                        } catch (Exception e) {
                        }

                    } else if (command.equals(ACT_TURN)) {
                        System.out.println("turn OK");
                        turn(); //always happens
                        response.setPerformative(ACLMessage.CONFIRM);

                    } else if (command.equals(ACT_SHOOT)) {
                        boolean result = shoot();
                        if (result) {
                            response.setPerformative(ACLMessage.CONFIRM);
                        } else {
                            response.setPerformative(ACLMessage.DISCONFIRM);
                        }

                    } else if (command.equals(ACT_AMMO)) {
                        int arrows = ammo();
                        response.setContent(Integer.toString(arrows));
                        response.setPerformative(ACLMessage.CONFIRM);

                    } else {
                        response.setPerformative(ACLMessage.UNKNOWN);
                    }
                    
                    send(response);
                    System.out.println("walker feels:" + feel());
                    print();
                } else {
                    block();
                }
            }
        });
    }

}
