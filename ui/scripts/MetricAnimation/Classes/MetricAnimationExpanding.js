/**
 * Class for expanding animations. Stores information about different metrics and their animation intensities
 * for an entity.
 */
class MetricAnimationExpanding extends MetricAnimation{

    constructor(animationType, minFrequency, maxFrequency, maxScale, defaultScale){
        super(minFrequency, maxFrequency);

        this.animationType = animationType;
        this.defaultScale = defaultScale;
        this.maxScale = maxScale;
    }

    /**
     * Get the frequency value for this animation, dependent of the animationType.
     * For "frequency" animations, return a value dependent on the average intensity and scaled
     * between minFrequency and maxFrequency.
     * For "size" animations, return always the minFrequency because the animationFrequency is always the same.
     */
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

    /**
     * Get the scale value for this animation, dependent of the animationType.
     * A "frequency" animations scales always to the default scale.
     * A "size" animations scales to a value dependent on the average intensity between 1 and the max scale.
     */
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