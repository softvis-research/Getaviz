class MetricAnimationExpanding extends MetricAnimation{
    constructor(animationType, minFrequency, maxFrequency, maxScale, defaultScale, intensity){
        super(minFrequency, maxFrequency);

        this.animationType = animationType;
        this.defaultScale = defaultScale;
        this.maxScale = maxScale;
        this.intensity = intensity;
    }

    getAnimationFrequency(){
        let animationFrequency;
        switch (this.animationType) {
            case "frequency":
                animationFrequency = super.getAnimationFrequency(this.intensity);
                break;
            case "size":
                animationFrequency = this.minFrequency;
                break;
        }
        return animationFrequency;
    }

    getScale(){
        let scale;
        switch (this.animationType) {
            case "frequency":
                scale = this.defaultScale;
                break;
            case "size":
                scale = 1 + ((this.maxScale -1) * this.intensity);
                break;
        }
        return scale;
    }
}