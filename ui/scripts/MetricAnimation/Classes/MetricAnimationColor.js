class MetricAnimationColor extends MetricAnimation{
    constructor(minFrequency, maxFrequency){
        super(minFrequency, maxFrequency);

        this.metricColors = new Map();
        this.metricIntensities = new Map();
        this.colors = [];
    }

    hasAnyMetric(){
        if (this.metricColors.size > 0){
            return true;
        } else {
            return false;
        }
    }

    hasMetric(metric){
        if (this.metricColors.has(metric)){
            return true;
        } else {
            return false;
        }
    }

    addMetric(metric, colors, intensity){
        this.metricColors.set(metric, colors);
        this.metricIntensities.set(metric, intensity);
        this.resetColors();
    }

    removeMetric(metric){
        this.metricColors.delete(metric);
        this.metricIntensities.delete(metric);
        this.resetColors();
    }

    /**
     * Reset the color array and make sure every color appears only once.
     */
    resetColors(){
        let colorSet = new Set();
        for (let value of this.metricColors.values()) {
            for (let color of value){
                colorSet.add(color);
            }
        }
        this.colors = Array.from(colorSet.values());
        this.resetColorIndices();
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
        if (this.currentFromColorIndex >= this.colors.length){
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
