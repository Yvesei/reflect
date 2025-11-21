package core.model;

public class MetricInfo {

    private int wmc;
    private int atfd;
    private double tcc;

    public MetricInfo() {}

    public MetricInfo(int wmc, int atfd, double tcc) {
        this.wmc = wmc;
        this.atfd = atfd;
        this.tcc = tcc;
    }

    public int getWmc() {
        return wmc;
    }

    public int getAtfd() {
        return atfd;
    }

    public double getTcc() {
        return tcc;
    }

    public void setWmc(int wmc) {
        this.wmc = wmc;
    }

    public void setAtfd(int atfd) {
        this.atfd = atfd;
    }

    public void setTcc(double tcc) {
        this.tcc = tcc;
    }
}
