/**
 * Class for color animations. Stores information about different metrics and their animation intensities
 * for an entity.
 */
class MetricAnimationColor extends MetricAnimation{
    constructor(minFrequency, maxFrequency){
        super(minFrequency, maxFrequency);

        this.metricColors = new Map();
        this.colors = [];
    }

    /**
     * Adds a metric with an array of animationColors to this animation.
     */
    addMetric(metric, colors, intensity){
        this.metricColors.set(metric, colors);
        this.metricIntensities.set(metric, intensity);
        this.resetColors();
    }

    /**
     * Removes a metric from this animation.
     */
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

    /**
     * Reset the color iteration indices, so the next iteration over the colors can start again from the beginning.
     */
    resetColorIndices(){
        this.currentFromColorIndex = -1;
        this.currentToColorIndex = 0;
    }

    /**
     * Iterates to the next startColor of this animation and returns it.
     */
    getNextFromColor(){
        this.currentFromColorIndex++;
        if (this.currentFromColorIndex >= this.colors.length){
            this.currentFromColorIndex = 0;
        }
        return this.colors[this.currentFromColorIndex];
    }

    /**
     * Iterates to the next targetColor of this animation and returns it.
     */
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
