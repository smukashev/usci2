package kz.bsbnb.usci.reciver;

import kz.bsbnb.usci.model.Batch;
import kz.bsbnb.usci.model.reciver.LoadManagerConfig;

public interface ILoadManager {

    void init(LoadManagerConfig conf);

    void start();

    void stop();

    void calculatePriority(Batch batch);
}
