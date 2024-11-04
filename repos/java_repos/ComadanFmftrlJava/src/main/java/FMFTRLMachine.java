import java.io.*;
import java.util.*;

public class FMFTRLMachine {

    private double alpha, beta, L1, L2, alphaFM, betaFM, L1FM, L2FM, fmInitDev, dropoutRate;
    private int fmDim, D;
    private double[] n, z, w;
    private Map<Integer, double[]> nFM, zFM, wFM;

    public FMFTRLMachine(int fmDim, double fmInitDev, double L1, double L2, double L1FM, double L2FM, int D, double alpha, double beta, double alphaFM, double betaFM, double dropoutRate) {
        this.fmDim = fmDim;
        this.fmInitDev = fmInitDev;
        this.L1 = L1;
        this.L2 = L2;
        this.L1FM = L1FM;
        this.L2FM = L2FM;
        this.D = D;
        this.alpha = alpha;
        this.beta = beta;
        this.alphaFM = alphaFM;
        this.betaFM = betaFM;
        this.dropoutRate = dropoutRate;

        this.n = new double[D + 1];
        this.z = new double[D + 1];
        this.w = new double[D + 1];
        this.nFM = new HashMap<>();
        this.zFM = new HashMap<>();
        this.wFM = new HashMap<>();
    }

    // Add public getters and setters for L1FM and L2FM
    public double getL1FM() {
        return L1FM;
    }

    public void setL1FM(double L1FM) {
        this.L1FM = L1FM;
    }

    public double getL2FM() {
        return L2FM;
    }

    public void setL2FM(double L2FM) {
        this.L2FM = L2FM;
    }

    public void initFM(int i) {
        if (!nFM.containsKey(i)) {
            double[] nFMi = new double[fmDim];
            double[] zFMi = new double[fmDim];
            double[] wFMi = new double[fmDim];
            Random rand = new Random();
            for (int k = 0; k < fmDim; k++) {
                zFMi[k] = rand.nextGaussian() * fmInitDev;
            }
            nFM.put(i, nFMi);
            zFM.put(i, zFMi);
            wFM.put(i, wFMi);
        }
    }

    public double predictRaw(List<Integer> x) {
        // Dummy implementation; replace with actual logic
        return 0.0;
    }

    public double predict(List<Integer> x) {
        // Dummy implementation; replace with actual logic
        return predictRaw(x);
    }

    public void dropout(List<Integer> x) {
        // Dummy implementation; replace with actual logic
    }

    public void update(List<Integer> x, double p, double y) {
        // Dummy implementation; replace with actual logic
    }

    public double predictWithDroppedOutModel(List<Integer> x) {
        dropout(x);
        return predict(x);
    }

    public double dropoutThenPredict(List<Integer> x) {
        dropout(x);
        return predictRaw(x);
    }

    public void writeW(String filename) throws IOException {
        // Dummy implementation; replace with actual logic
    }

    public void writeWFM(String filename) throws IOException {
        // Dummy implementation; replace with actual logic
    }

    // Expose the internal state for testing purposes
    public Map<Integer, double[]> getNFM() {
        return nFM;
    }

    public Map<Integer, double[]> getZFM() {
        return zFM;
    }

    public Map<Integer, double[]> getWFM() {
        return wFM;
    }

    // Add public getters for private arrays n, z
    public double[] getN() {
        return n;
    }

    public double[] getZ() {
        return z;
    }

    // Other methods remain unchanged.
}

