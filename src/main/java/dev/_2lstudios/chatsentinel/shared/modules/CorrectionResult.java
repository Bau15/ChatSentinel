package dev._2lstudios.chatsentinel.shared.modules;

public final class CorrectionResult {
    private final String message;
    private final int corrections;

    public CorrectionResult(final String message, final int corrections) {
        this.message = message == null ? "" : message;
        this.corrections = Math.max(0, corrections);
    }

    public String getMessage() {
        return message;
    }

    public int getCorrections() {
        return corrections;
    }

    public boolean isCorrected() {
        return corrections > 0;
    }
}