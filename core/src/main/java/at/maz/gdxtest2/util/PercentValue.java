package at.maz.gdxtest2.util;

public class PercentValue {
    private float value;

    public PercentValue(float value) {
        setValue(value);
    }

    private void setValue(float value) {
        this.value = Math.max(0, Math.min(value, 100));
    }

    public float getValue() {
        return value;
    }

    public void inc(float incV) {
        setValue(value + incV);
    }

}
