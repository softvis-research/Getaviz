class ColorGradient {
    rgb = {
        red: {
            r: 255, g: 0, b: 0
        },

        blue: {
            r: 0, g: 0, b: 255
        },

        green: {
            r: 0, g: 128, b: 0
        },

        black: {
            r: 0, g: 0, b: 0
        },

        yellow: {
            r: 255, g: 255, b: 0
        },

        orange: {
            r: 255, g: 165, b: 0
        }
    }

    constructor(startColor, endColor, minValue, maxValue) {
        this.startColor = this.mapColor(startColor);
        this.endColor = this.mapColor(endColor);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    mapColor(color) {
        switch (color) {
            case "red":
                return this.rgb.red;
            case "blue":
                return this.rgb.blue;
            case "green":
                return this.rgb.green;
            case "black":
                return this.rgb.black;
            case "yellow":
                return this.rgb.yellow;
            case "orange":
                return this.rgb.orange;
            default:
                return color;
        }
    }

    calculateGradientColor(value) {
        if (value < this.minValue || value > this.maxValue) {
            return;
        }

        var proportion = (value - this.minValue) / (this.maxValue - this.minValue);

        return {
            r: proportion * this.startColor.r + (1 - proportion) * this.endColor.r,
            g: proportion * this.startColor.g + (1 - proportion) * this.endColor.g,
            b: proportion * this.startColor.b + (1 - proportion) * this.endColor.b
        };
    }

}