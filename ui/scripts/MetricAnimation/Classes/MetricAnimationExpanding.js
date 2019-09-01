class MetricAnimationExpanding extends MetricAnimation{
    constructor(animationType, minFrequency, maxFrequency, maxScale, defaultScale){
        super(minFrequency, maxFrequency);

        this.animationType = animationType;
        this.defaultScale = defaultScale;
        this.maxScale = maxScale;
    }

    getAnimationFrequency(){
        let animationFrequency;
        switch (this.animationType) {
            case "frequency":
                animationFrequency = super.getAnimationFrequency();
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
                scale = 1 + ((this.maxScale -1) * this.getAverageIntensity());
                break;
        }
        return scale;
    }
}