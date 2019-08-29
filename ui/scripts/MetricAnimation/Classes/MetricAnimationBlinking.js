class MetricAnimationBlinking extends MetricAnimation{
    constructor(minFrequency, maxFrequency){
        super(minFrequency, maxFrequency);

        this.metricColors = new Map();
        this.metricIntensities = new Map();
        this.colors = [];
    }

    hasMetric(){
        if (this.metricColors.size > 0){
            return true;
        } else {
            return false;
        }
    }

    getMetricsSize(){
        return this.metricColors.size;
    }

    addMetric(metric, color, intensity){
        this.metricColors.set(metric, color);
        this.metricIntensities.set(metric, intensity);
        this.colors = Array.from(this.metricColors.values());
    }

    removeMetric(metric){
        this.metricColors.delete(metric);
        this.metricIntensities.delete(metric);
        this.colors = Array.from(this.metricColors.values());
    }

    getAnimationFrequency(){
        // 1. get average intensity
        let avgIntensity = 0;
        let intensity;
        for (intensity of this.metricIntensities.values()){
            avgIntensity += intensity;
        }
        avgIntensity = avgIntensity / this.metricIntensities.size;

        // 2. linear scale between min and max value
        return super.getAnimationFrequency(avgIntensity);
    }

    resetColorIndices(){
        this.currentFromColorIndex = -1;
        this.currentToColorIndex = 0;
    }

    getNextFromColor(){
        this.currentFromColorIndex++;
        if (this.currentFromColorIndex >= this.metricColors.size){
            this.currentFromColorIndex = 0;
        }
        return this.colors[this.currentFromColorIndex];
    }

    getNextToColor(){
        if (this.colors.length === 1){
            return this.colors[0];
        } else {
            this.currentToColorIndex++;
            if (this.currentToColorIndex >= this.colors.length){
                this.currentToColorIndex = 0;
            }
            return this.colors[this.currentToColorIndex];
        }
    }
}
