package dev._2lstudios.chatsentinel.shared.config;

import java.io.IOException;

public interface MutableModuleConfigStore {
    void setBoolean(String path, boolean value) throws IOException;
}
