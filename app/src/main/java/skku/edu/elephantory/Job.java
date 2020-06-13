package skku.edu.elephantory;

public class Job {
    String submit_time;
    String start_time;
    String finish_time;
    String job_id;
    String name;
    String user;
    String queue;
    String state;
    String maps_total;
    String maps_completed;
    String reduces_total;
    String reduces_completed;
    String elapsed_time;

    // added by daegyu
    @Override
    public boolean equals(Object object) {
        boolean sameSame = false;
        if (object != null && object instanceof Job){
            sameSame = this.job_id.equals(((Job) object).job_id);
        }
        return sameSame;
    }
}