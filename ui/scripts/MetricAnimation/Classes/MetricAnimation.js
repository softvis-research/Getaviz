/**
 * Base class for metric animations. Stores information about different metrics and their animation intensities
 * for an entity.
 */
class MetricAnimation{
    constructor(minFrequency, maxFrequency){
        this.minFrequency = minFrequency;
        this.maxFrequency = maxFrequency;
        this.metricIntensities = new Map();
    }

    /**
     * Check if this animation has any metric.
     */
    hasAnyMetric(){
        if (this.metricIntensities.size > 0){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check if this animation is defined for a specific metric.
     */
    hasMetric(metric){
        if (this.metricIntensities.has(metric)){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Add a metric with its intensity to this animation.
     */
    addMetric(metric, intensity){
        this.metricIntensities.set(metric, intensity);
    }

    /**
     * Remove a metric from this animation.
     */
    removeMetric(metric){
        this.metricIntensities.delete(metric);
    }

    /**
     * Get the average intensity for all metrics of this animation.
     */
    getAverageIntensity(){
        let avgIntensity = 0;
        let intensity;
        for (intensity of this.metricIntensities.values()){
            avgIntensity += intensity;
        }
        avgIntensity = avgIntensity / this.metricIntensities.size;
        return avgIntensity;
    }

    /**
     * Get the frequency value for this animation.
     * Dependent on the average intensity it scales between minFrequency and maxFrequency.
     */
    getAnimationFrequency(){
        let minMaxDiff = this.minFrequency - this.maxFrequency;
        let animationFrequency = minMaxDiff - (minMaxDiff * this.getAverageIntensity()) + this.maxFrequency;
        return animationFrequency;
    }
}

