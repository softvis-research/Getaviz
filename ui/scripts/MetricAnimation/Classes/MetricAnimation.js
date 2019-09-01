class MetricAnimation{
    constructor(minFrequency, maxFrequency){
        this.minFrequency = minFrequency;
        this.maxFrequency = maxFrequency;
        this.metricIntensities = new Map();
    }

    hasAnyMetric(){
        if (this.metricIntensities.size > 0){
            return true;
        } else {
            return false;
        }
    }

    hasMetric(metric){
        if (this.metricIntensities.has(metric)){
            return true;
        } else {
            return false;
        }
    }

    addMetric(metric, intensity){
        this.metricIntensities.set(metric, intensity);
    }

    removeMetric(metric){
        this.metricIntensities.delete(metric);
    }

    getAverageIntensity(){
        let avgIntensity = 0;
        let intensity;
        for (intensity of this.metricIntensities.values()){
            avgIntensity += intensity;
        }
        avgIntensity = avgIntensity / this.metricIntensities.size;
        return avgIntensity;
    }

    getAnimationFrequency(){
        let minMaxDiff = this.minFrequency - this.maxFrequency;
        let animationFrequency = minMaxDiff - (minMaxDiff * this.getAverageIntensity()) + this.maxFrequency;
        return animationFrequency;
    }
}

