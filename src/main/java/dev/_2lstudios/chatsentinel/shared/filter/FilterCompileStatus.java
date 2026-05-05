package dev._2lstudios.chatsentinel.shared.filter;

public final class FilterCompileStatus {
    private static final int PROGRESS_FILE_INTERVAL = 10;
    private static final long PROGRESS_TIME_INTERVAL_MILLIS = 250L;
    private static final int BAR_WIDTH = 20;

    private final MessageSink messageSink;
    private int totalFiles;
    private int compiledFiles;
    private int lastProgressFiles;
    private long lastProgressMillis;

    public FilterCompileStatus(MessageSink messageSink) {
        this.messageSink = messageSink;
    }

    public void start(int totalFiles) {
        this.totalFiles = Math.max(0, totalFiles);
        this.compiledFiles = 0;
        this.lastProgressFiles = 0;
        this.lastProgressMillis = System.currentTimeMillis();
        send("Compiling filters: 0/" + this.totalFiles);
    }

    public void fileCompiled() {
        compiledFiles++;
        long now = System.currentTimeMillis();
        if (compiledFiles >= totalFiles
                || compiledFiles - lastProgressFiles >= PROGRESS_FILE_INTERVAL
                || now - lastProgressMillis >= PROGRESS_TIME_INTERVAL_MILLIS) {
            send(formatProgress());
            lastProgressFiles = compiledFiles;
            lastProgressMillis = now;
        }
    }

    public void done(FilterCompileReport report) {
        send(formatSummary(report));
    }

    public static String formatSummary(FilterCompileReport report) {
        return "Compiled " + report.getFilesCompiled() + " files, "
                + report.getExpressionsTotal() + " expressions, "
                + report.getErrors().size() + " errors";
    }

    public static String formatError(FilterCompileError error) {
        return "Filter compile error: " + error.getSource().getRelativePath()
                + " index " + error.getExpressionIndex()
                + ": " + error.getErrorMessage();
    }

    private String formatProgress() {
        int filled = totalFiles <= 0 ? BAR_WIDTH : Math.min(BAR_WIDTH, compiledFiles * BAR_WIDTH / totalFiles);
        StringBuilder builder = new StringBuilder("Compiling filters: [");
        for (int i = 0; i < BAR_WIDTH; i++) {
            builder.append(i < filled ? '#' : '-');
        }
        builder.append("] ").append(compiledFiles).append('/').append(totalFiles);
        return builder.toString();
    }

    private void send(String message) {
        if (messageSink != null) {
            messageSink.send(message);
        }
    }

    public interface MessageSink {
        void send(String message);
    }
}
