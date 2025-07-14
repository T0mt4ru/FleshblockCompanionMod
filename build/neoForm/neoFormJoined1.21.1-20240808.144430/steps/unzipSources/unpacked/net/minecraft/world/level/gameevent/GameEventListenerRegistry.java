package net.minecraft.world.level.gameevent;

import net.minecraft.core.Holder;
import net.minecraft.world.phys.Vec3;

public interface GameEventListenerRegistry {
    GameEventListenerRegistry NOOP = new GameEventListenerRegistry() {
        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public void register(GameEventListener p_251092_) {
        }

        @Override
        public void unregister(GameEventListener p_251937_) {
        }

        @Override
        public boolean visitInRangeListeners(
            Holder<GameEvent> p_316158_, Vec3 p_249086_, GameEvent.Context p_249012_, GameEventListenerRegistry.ListenerVisitor p_252106_
        ) {
            return false;
        }
    };

    boolean isEmpty();

    void register(GameEventListener listener);

    void unregister(GameEventListener listener);

    boolean visitInRangeListeners(Holder<GameEvent> gameEvent, Vec3 pos, GameEvent.Context context, GameEventListenerRegistry.ListenerVisitor visitor);

    @FunctionalInterface
    public interface ListenerVisitor {
        void visit(GameEventListener listener, Vec3 pos);
    }
}
