package com.gameapi.rha.mechanics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.time.Clock;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
public class MechanicsExecutor implements Runnable {
    @NotNull
    private static final Logger LOGGER = LoggerFactory.getLogger(MechanicsExecutor.class);
    private static final long STEP_TIME = 50;

    @NotNull
    private final GameMechanics gameMechanics;

    @NotNull
    private final Clock clock = Clock.systemDefaultZone();

    private final Executor tickExecutor = Executors.newSingleThreadExecutor();

    @Autowired
    public MechanicsExecutor(
            @NotNull GameMechanics gameMechanics
    ) {
        this.gameMechanics = gameMechanics;
    }

    @PostConstruct
    public void initAfterStartup() {
        tickExecutor.execute(this);
    }

    @Override
    public void run() {
        //        try {
        //            mainCycle();
        //        } finally {
        //            LOGGER.warn("Mechanic executor terminated");
        //        }
    }

}
