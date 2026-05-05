package dev._2lstudios.chatsentinel.shared.filter;

import java.io.IOException;

public interface UserFilterWriter {
    void appendExpression(String moduleId, String expression) throws IOException;
}
