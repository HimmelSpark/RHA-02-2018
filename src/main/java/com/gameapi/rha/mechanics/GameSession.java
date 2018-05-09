package com.gameapi.rha.mechanics;

import com.gameapi.rha.mechanics.game.GameUser;
import com.gameapi.rha.mechanics.game.TacticalMap;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class GameSession {
    private static final AtomicLong ID_GENERATOR = new AtomicLong(0);
    private boolean isFinished;

    private @NotNull Long  sessionId;
    private @NotNull List<GameUser> players;
    private @NotNull TacticalMap map;

    public GameSession(List<GameUser> players) {
        this.players = players;
        this.sessionId = ID_GENERATOR.getAndIncrement();

        this.map = new TacticalMap();
    }


    public void terminateSession() {
    }

    public List<GameUser> getPlayers() {
        return players;
    }

    public void setFinished() {
    }

    public Long getSessionId() {
        return sessionId;
    }

    public Object getEnemy(@org.jetbrains.annotations.NotNull String userN) {
        return userN;
    }

    public TacticalMap getMap() {
        return map;
    }

    public void setMap(TacticalMap map) {
        this.map = map;
    }

    public boolean tryFinishGame() {
        return false;
    }

    public GameUser getNext(String current) {
        Integer i=0;
        while (!(players.get(i).getUserNickname().equals(current)) && i<players.size())
        {
            i++;
        }
        if(i+1<players.size()) {
            return (players.get(i+1));
        }
        else {
            return (players.get(0));
        }
    }
}
