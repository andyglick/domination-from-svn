package net.yura.domination.engine.ai;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.core.RiskGame;
import net.yura.util.Service;

public class AIManager {

    private static int wait=500;
    public static int getWait() {
            return wait;
    }
    public static void setWait(int w) {
            wait = w;
    }



    private final Map<Integer,AI> ais = new HashMap();

    public AIManager() {
        System.out.println("#######################################################");
        Iterator<AI> providers = (Iterator) Service.providers(AIManager.class);
        while (providers.hasNext()) {
            AI ai = providers.next();
            int type = ai.getType();
            if ( ais.get( type ) !=null ) {
                throw new RuntimeException("more then 1 ai with same type");
            }
            ais.put( type , ai );
        }
        System.out.println("AIs "+ais);
    }

    public void play(Risk risk) {
            RiskGame game = risk.getGame();
            String output = getOutput(game, game.getCurrentPlayer().getType() );
            try { Thread.sleep(wait); }
            catch(InterruptedException e) {}
            risk.parser(output);
    }

    public String getOutput(RiskGame game,int type) {

            AI usethisAI=ais.get(type);

            if (usethisAI==null) {
                throw new IllegalArgumentException("can not find ai for type "+type);
            }
            
            usethisAI.setGame(game);

            String output=null;

            switch ( game.getState() ) {
                    case RiskGame.STATE_TRADE_CARDS:	output = usethisAI.getTrade(); break;
                    case RiskGame.STATE_PLACE_ARMIES:	output = usethisAI.getPlaceArmies(); break;
                    case RiskGame.STATE_ATTACKING:	output = usethisAI.getAttack(); break;
                    case RiskGame.STATE_ROLLING:	output = usethisAI.getRoll(); break;
                    case RiskGame.STATE_BATTLE_WON:	output = usethisAI.getBattleWon(); break;
                    case RiskGame.STATE_FORTIFYING:	output = usethisAI.getTacMove(); break;
                    case RiskGame.STATE_SELECT_CAPITAL:	output = usethisAI.getCapital(); break;

                    case RiskGame.STATE_END_TURN:	output = "endgo"; break;
                    case RiskGame.STATE_GAME_OVER:	/* output="closegame"; */ break;
                    case RiskGame.STATE_DEFEND_YOURSELF:output = usethisAI.getAutoDefendString(); break;

                    default: throw new RuntimeException("AI error: unknown state "+ game.getState() );
            }

            if (output==null) { throw new NullPointerException("AI ERROR!"); }

            return output;
    }
}
