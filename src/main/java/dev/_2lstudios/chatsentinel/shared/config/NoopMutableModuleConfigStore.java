package dev._2lstudios.chatsentinel.shared.config;

import java.io.IOException;

public final class NoopMutableModuleConfigStore implements MutableModuleConfigStore {
    @Override
    public void setBoolean(final String path, final boolean value) throws IOException {
    }
}
