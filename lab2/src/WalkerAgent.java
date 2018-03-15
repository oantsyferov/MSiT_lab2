
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.EnumSet;

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

    final public String ACT_WALK = "walk";
    final public String ACT_FEEL = "feel";
    final public String ACT_TURN = "turn";
    final public String ACT_SHOOT = "shoot";
    final public String ACT_AMMO = "ammo";
    
    public enum Feels {
        GOLD_NEAR, PIT_NEAR, WAMPUS_NEAR,
        GOLD_FOUND, PIT_FOUND, 
        WAMPUS_FOUND_ALIVE, WAMPUS_FOUND_DEAD
    };

    final String[][] map = new String[][] {
        //0   1   2   3   4   5   6   7   8
        {" "," "," "," "," "," ","P"," "," "},//0
        {" "," ","G"," "," "," "," "," "," "},//1
        {" ","P"," "," ","P"," ","G"," "," "},//2
        {" "," ","G"," "," "," "," ","P"," "},//3
        {" "," "," "," ","M","P"," "," "," "},//4
        {"P"," "," ","G"," "," "," ","G"," "} //5
    };
    
    int x = 0;
    int y = 0;
    int direction = 0; //0-top, 1-right, 2-down, 3-left
    
    int ammo = 1;

    void turn(){
        direction += 1;
        if(4 == direction) {
            direction = 0;
        }
    }
    
    boolean walk(){
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
        if(0 == x && 0 == y) {
            ammo = 1;
        }
        
        return true;
    }
    
    EnumSet<Feels> feelRoom(int y, int x){
        EnumSet<Feels> result = EnumSet.noneOf(Feels.class);
        String content = map[y][x];
        if ("P" == content) {
            result.add(Feels.PIT_FOUND);
        } else if ("G" == content) {
            result.add(Feels.GOLD_FOUND);
        } else if ("M" == content) {
            result.add(Feels.WAMPUS_FOUND_ALIVE);
        }
        return result;
    }
    
    EnumSet<Feels> weakenFeels(EnumSet<Feels> feels) {
         EnumSet<Feels> result = EnumSet.noneOf(Feels.class);
         
         if(feels.contains(Feels.GOLD_FOUND)) {
             result.add(Feels.GOLD_NEAR);
         }
         
         if(feels.contains(Feels.PIT_FOUND)) {
             result.add(Feels.PIT_NEAR);
         }
         
         if(feels.contains(Feels.WAMPUS_FOUND_ALIVE)) {
             result.add(Feels.WAMPUS_NEAR);
         }
         
         return result;
    }
    
    EnumSet<Feels> feel() {
        EnumSet<Feels> result = EnumSet.noneOf(Feels.class);
        //check current room
        result.addAll(feelRoom(y,x));
       
        //check neighboring rooms
        if (x > 0) {
            result.addAll(weakenFeels(feelRoom(y, x - 1)));
        }
        if (y > 0) {
            result.addAll(weakenFeels(feelRoom(y - 1, x)));
        }
        if (x + 1 < map[0].length) {
            result.addAll(weakenFeels(feelRoom(y, x + 1)));
        }
        if (y + 1 < map.length) {
            result.addAll(weakenFeels(feelRoom(y + 1, x)));
        }

        
        return result;
    }
    
    void shoot() {
        
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
                    if(" " == map[row][column]) {
                        System.out.print("- ");
                    }
                    else
                    {
                        System.out.print(map[row][column] + " ");
                    }
                    
                }
            }
            System.out.println("");
        }
        System.out.println();
    }

    @Override
    protected void setup() {
        super.setup();

        registerService("walker", MSG_QUEUE_CLASS);
        
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

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage message = receive();
                if (null != message) {
                    String command = message.getContent();
                    if (ACT_WALK == command) {
                        walk();

                    } else if (ACT_FEEL == command) {
                        EnumSet<Feels> feels = feel();

                    } else if (ACT_TURN == command) {
                        turn();

                    } else if (ACT_SHOOT == command) {
                        shoot();

                    } else if (ACT_AMMO == command) {
                        int arrows = ammo();

                    }
                } else {
                    block();
                }
            }
        });
    }

}
