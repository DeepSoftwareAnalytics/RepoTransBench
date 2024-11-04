import java.io.IOException;
import java.util.*;
import static java.lang.Math.*;

public class RunModelDropoutExample {
    public static void main(String[] args) throws IOException {
        Random rand = new Random(5);  // seed random variable for reproducibility
        System.out.println("Start Training:");

        int reportFrequency = 1000000;
        String trainingFile = "../data/train.csv";

        int fmDim = 4;
        double fmInitDev = 0.01;
        String hashSalt = "salty";
        
        double alpha = 0.1;
        double beta = 1.0;
        double alphaFM = 0.05;
        double betaFM = 1.0;

        int pD = 22;
        int D = (int) pow(2, pD);

        double L1 = 1.0;
        double L2 = 0.1;
        double L1FM = 2.0;
        double L2FM = 1.0;

        double dropoutRate = 0.8;
        int nEpochs = 5;

        long start = System.currentTimeMillis();

        FMFTRLMachine learner = new FMFTRLMachine(fmDim, fmInitDev, L1, L2, L1FM, L2FM, D, alpha, beta, alphaFM, betaFM, dropoutRate);

        for (int e = 0; e < nEpochs; e++) {
            if (e == 0) {
                learner.setL1FM(0.0);
                learner.setL2FM(0.0);
            } else {
                learner.setL1FM(L1FM);
                learner.setL2FM(L2FM);
            }

            double cvLoss = 0.0;
            double cvCount = 0.0;
            double progressiveLoss = 0.0;
            double progressiveCount = 0.0;

            for (DataGenerator.Data data : new DataGenerator(trainingFile, D, hashSalt)) {
                if (data.date == 30) {
                    double p = learner.predictWithDroppedOutModel(data.x);
                    double loss = LogLoss.logLoss(p, data.y);
                    cvLoss += loss;
                    cvCount += 1.0;
                } else {
                    double p = learner.dropoutThenPredict(data.x);
                    double loss = LogLoss.logLoss(p, data.y);
                    learner.update(data.x, p, data.y);
                    progressiveLoss += loss;
                    progressiveCount += 1.0;

                    if (data.t % reportFrequency == 0) {
                        System.out.println("Epoch " + e + "\tcount: " + data.t + "\tProgressive Loss: " + progressiveLoss / progressiveCount);
                    }
                }
            }

            System.out.println("Epoch " + e + " finished.\tvalidation loss: " + cvLoss / cvCount + "\telapsed time: " + (System.currentTimeMillis() - start) + " ms");
        }

        // save the weights
        String wOutfile = "param.w.txt";
        String wFmOutfile = "param.w_fm.txt";
        learner.writeW(wOutfile);
        learner.writeWFM(wFmOutfile);
    }
}

