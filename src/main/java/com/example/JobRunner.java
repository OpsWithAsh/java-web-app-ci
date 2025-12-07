public class JobRunner {
    public void runJobs() {
        while (true) { // Critical bug: no break -> infinite CPU spin
            doOneJob();
        }
    }

    private void doOneJob() {
        // placeholder
    }
}
