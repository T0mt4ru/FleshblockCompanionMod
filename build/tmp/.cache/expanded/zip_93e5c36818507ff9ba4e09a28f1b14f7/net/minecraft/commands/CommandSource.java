package net.minecraft.commands;

import net.minecraft.network.chat.Component;

public interface CommandSource {
    /**
     * A {@code CommandSource} that ignores all messages.
     */
    CommandSource NULL = new CommandSource() {
        @Override
        public void sendSystemMessage(Component p_230799_) {
        }

        @Override
        public boolean acceptsSuccess() {
            return false;
        }

        @Override
        public boolean acceptsFailure() {
            return false;
        }

        @Override
        public boolean shouldInformAdmins() {
            return false;
        }
    };

    void sendSystemMessage(Component component);

    boolean acceptsSuccess();

    boolean acceptsFailure();

    boolean shouldInformAdmins();

    default boolean alwaysAccepts() {
        return false;
    }
}
