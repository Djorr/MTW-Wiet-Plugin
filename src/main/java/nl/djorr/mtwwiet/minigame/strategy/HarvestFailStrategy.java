package nl.djorr.mtwwiet.minigame.strategy;

import nl.djorr.mtwwiet.minigame.model.IHarvestMinigame;

public interface HarvestFailStrategy {
    boolean isFail(IHarvestMinigame minigame);
} 