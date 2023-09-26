package com.gempukku.lotro.effects;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.actions.SubAction;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.TribblePower;
import com.gempukku.lotro.game.TribblesGame;
import com.gempukku.lotro.results.ActivateTribblePowerResult;
import com.gempukku.lotro.rules.GameUtils;

public class ActivateTribblePowerEffect extends AbstractEffect<TribblesGame> {
    protected LotroPhysicalCard _source;
    protected String _activatingPlayer;
    protected TribblePower _tribblePower;
    protected ActivateTribblePowerResult _result;
    protected CostToEffectAction _action;
    public ActivateTribblePowerEffect(CostToEffectAction action, LotroPhysicalCard source) {
        _source = source;
        _activatingPlayer = source.getOwner();
        _tribblePower = source.getBlueprint().getTribblePower();
        _action = action;
        _result = new ActivateTribblePowerResult(_activatingPlayer, _tribblePower);
    }

    @Override
    public boolean isPlayableInFull(TribblesGame game) {
        return true;
    }

    @Override
    public String getText(TribblesGame game) {
        return "Activated " + GameUtils.getCardLink(_source);
    }

    public LotroPhysicalCard getSource() {
        return _source;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(TribblesGame game) {
        return new FullEffectResult(true);
    }

    protected FullEffectResult addActionAndReturnResult(TribblesGame game, SubAction subAction) {
        game.getActionsEnvironment().addActionToStack(subAction);
        game.getActionsEnvironment().emitEffectResult(_result);
        return new FullEffectResult(true);
    }
}


/*
FREEZE (WIP)
		effects: {
			type: trigger
			optional: true
			trigger: {
			    type: played
			    filter: self
			}
            effect: [
                {
                    type: ChooseTribblePower
                    memorize: powerChosen
                }
//                {
//                    type: addModifier
//                    modifier: {
//                        type: cantPlayCards
//                        filter: tribblePower(memory(powerChosen))
//                    }
//                    until: endOfPlayersNextTurn
// need to specify which player
//                }
//            }
            ]
		}

 */

/*
    GENEROSITY, LAUGHTER
         effects: {
 			type: trigger
 			optional: true
 			trigger: {
 			    type: played
 			    filter: self
 			}
 			effect: [
 			    {
                    type: ChooseOpponent
                    memorize: opp
                }
                {
                    type: ScorePoints
                    amount: 25000
                }
                {
                    type: ScorePoints
                    amount: 25000
                    player: FromMemory(opp)
                }
                {
                    type: drawCards
                }
            ]
 		}
         effects: {
 			type: trigger
 			optional: true
 			trigger: {
 			    type: played
 			    filter: self
 			}
 			effect: [
 			    {
                    type: ChoosePlayer
                    memorize: player1
                }
                {
                    type: discardFromHand
                    forced: true
                    hand: fromMemory(player1)
                    player: fromMemory(player1)
                }
                {
                    type: ChoosePlayerExcept
                    exclude: fromMemory(player1)
                    memorize: player2
                }
                {
                    type: putCardsFromHandOnBottomOfDeck
                    player: fromMemory(player2)
                }
                {
                    type: conditional
                    requires: [
                        {
                            type: playerIsNotSelf
                            memory: player1
                        }
                        {
                            type: playerIsNotSelf
                            memory: player2
                        }
                    ]
                    effect: {
                        type: scorePoints
                        amount: 25000
                    }
                }
            ]
 		}

 */