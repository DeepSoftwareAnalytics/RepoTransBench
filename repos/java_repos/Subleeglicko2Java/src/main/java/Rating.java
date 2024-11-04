public class Rating {
    private double mu;
    private double phi;
    private double sigma;
    private double volatility;
    
    public Rating(double mu, double phi, double sigma) {
        this.mu = mu;
        this.phi = phi;
        this.sigma = sigma;
        this.volatility = 0.06; // Assuming a default value
    }

    public Rating(double mu, double phi) {
        this(mu, phi, 0.06);
    }

    public double getMu() {
        return mu;
    }

    public double getPhi() {
        return phi;
    }

    public double getSigma() {
        return sigma;
    }

    public double getVolatility() {
        return this.volatility;
    }

    @Override
    public String toString() {
        return String.format("Rating(mu=%.3f, phi=%.3f, sigma=%.3f, volatility=%.3f)", mu, phi, sigma, volatility);
    }
}
