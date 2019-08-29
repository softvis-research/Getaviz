class MetricAnimation{
    constructor(minFrequency, maxFrequency){
        this.minFrequency = minFrequency;
        this.maxFrequency = maxFrequency;
    }

    getAnimationFrequency(intensity){
        // linear scale between min and max value
        let minMaxDiff = this.minFrequency - this.maxFrequency;
        let animationFrequency = minMaxDiff - (minMaxDiff * intensity) + this.maxFrequency;
        return animationFrequency;
    }
}

