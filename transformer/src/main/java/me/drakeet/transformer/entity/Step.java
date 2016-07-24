package me.drakeet.transformer.entity;

/**
 * @author drakeet
 */
public enum Step {

    OnCreate {
        @Override public Step next() {
            return Step.OnStart;
        }
    },
    OnStart {
        @Override public Step next() {
            return Step.OnWorking;
        }
    },
    OnWorking {
        @Override public Step next() {
            return Step.OnDone;
        }
    },
    OnDone {
        @Override public Step next() {
            return null;
        }
    },
    OnStop {
        @Override public Step next() {
            return OnStart;
        }
    };


    public abstract Step next();
}
