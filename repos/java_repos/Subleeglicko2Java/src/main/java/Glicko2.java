import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class Glicko2 {
    public static final double MU = 1500;
    public static final double PHI = 350;
    public static final double SIGMA = 0.06;
    public static final double TAU = 1.0;
    public static final double EPSILON = 0.000001;

    public double mu;
    public double phi;
    public double sigma;
    public double tau;
    public double epsilon;

    public Glicko2(double mu, double phi, double sigma, double tau, double epsilon) {
        this.mu = mu;
        this.phi = phi;
        this.sigma = sigma;
        this.tau = tau;
        this.epsilon = epsilon;
    }

    public Glicko2(double tau) {
        this(MU, PHI, SIGMA, tau, EPSILON);
    }

    public Rating createRating(double mu, double phi, double sigma) {
        return new Rating(mu, phi, sigma);
    }

    public Rating createRating(double mu, double phi) {
        return new Rating(mu, phi);
    }

    public Rating scaleDown(Rating rating, double ratio) {
        double mu = (rating.getMu() - this.mu) / ratio;
        double phi = rating.getPhi() / ratio;
        return createRating(mu, phi, rating.getSigma());
    }

    public Rating scaleUp(Rating rating, double ratio) {
        double mu = rating.getMu() * ratio + this.mu;
        double phi = rating.getPhi() * ratio;
        return createRating(mu, phi, rating.getSigma());
    }

    public double reduceImpact(Rating rating) {
        return 1.0 / Math.sqrt(1 + (3 * Math.pow(rating.getPhi(), 2)) / Math.pow(Math.PI, 2));
    }

    public double expectScore(Rating rating, Rating otherRating, double impact) {
        return 1.0 / (1 + Math.exp(-impact * (rating.getMu() - otherRating.getMu())));
    }

    public double determineSigma(Rating rating, double difference, double variance) {
        double phi = rating.getPhi();
        double differenceSquared = Math.pow(difference, 2);
        double alpha = Math.log(Math.pow(rating.getSigma(), 2));

        class Function {
            double f(double x) {
                double tmp = Math.pow(phi, 2) + variance + Math.exp(x);
                double a = Math.exp(x) * (differenceSquared - tmp) / (2 * Math.pow(tmp, 2));
                double b = (x - alpha) / Math.pow(tau, 2);
                return a - b;
            }
        }

        Function function = new Function();
        double a = alpha;
        double b;
        if (differenceSquared > Math.pow(phi, 2) + variance) {
            b = Math.log(differenceSquared - Math.pow(phi, 2) - variance);
        } else {
            int k = 1;
            while (function.f(alpha - k * Math.sqrt(Math.pow(tau, 2))) < 0) {
                k++;
            }
            b = alpha - k * Math.sqrt(Math.pow(tau, 2));
        }

        double fA = function.f(a);
        double fB = function.f(b);

        while (Math.abs(b - a) > epsilon) {
            double c = a + (a - b) * fA / (fB - fA);
            double fC = function.f(c);
            if (fC * fB < 0) {
                a = b;
                fA = fB;
            } else {
                fA /= 2;
            }
            b = c;
            fB = fC;
        }

        return Math.exp(a / 2);
    }

    public Rating rate(Rating rating, Object[] series) {
        rating = scaleDown(rating, 173.7178);
        double varianceInv = 0;
        double difference = 0;

        if (series.length == 0) {
            double phiStar = Math.sqrt(Math.pow(rating.getPhi(), 2) + Math.pow(rating.getSigma(), 2));
            return scaleUp(createRating(rating.getMu(), phiStar, rating.getSigma()), 173.7178);
        }

        for (int i = 0; i < series.length; i += 2) {
            Result result = (Result) series[i];
            Rating otherRating = (Rating) series[i + 1];
            otherRating = scaleDown(otherRating, 173.7178);
            double actualScore = convertResultToScore(result); // Using a helper method to convert Result to double
            double impact = reduceImpact(otherRating);
            double expectedScore = expectScore(rating, otherRating, impact);
            varianceInv += Math.pow(impact, 2) * expectedScore * (1 - expectedScore);
            difference += impact * (actualScore - expectedScore);
        }

        difference /= varianceInv;
        double variance = 1.0 / varianceInv;
        double sigma = determineSigma(rating, difference, variance);
        double phiStar = Math.sqrt(Math.pow(rating.getPhi(), 2) + Math.pow(sigma, 2));
        double phi = 1.0 / Math.sqrt(1.0 / Math.pow(phiStar, 2) + 1.0 / variance);
        double mu = rating.getMu() + Math.pow(phi, 2) * (difference / variance);

        return scaleUp(createRating(mu, phi, sigma), 173.7178);
    }

    private double convertResultToScore(Result result) {
        switch (result) {
            case WIN: return 1.0;
            case DRAW: return 0.5;
            case LOSS: return 0.0;
            default: throw new IllegalArgumentException("Unknown result: " + result);
        }
    }

    public Rating[] rate1vs1(Rating rating1, Rating rating2, boolean drawn) {
        return new Rating[]{
            rate(rating1, new Object[]{drawn ? Result.DRAW : Result.WIN, rating2}),
            rate(rating2, new Object[]{drawn ? Result.DRAW : Result.LOSS, rating1})
        };
    }

    public double quality1vs1(Rating rating1, Rating rating2) {
        double expectedScore1 = expectScore(rating1, rating2, reduceImpact(rating1));
        double expectedScore2 = expectScore(rating2, rating1, reduceImpact(rating2));
        double expectedScore = (expectedScore1 + expectedScore2) / 2;
        return 2 * (0.5 - Math.abs(0.5 - expectedScore));
    }
}
